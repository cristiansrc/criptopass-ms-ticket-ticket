package com.criptopass.ms.ticket.ticket.application.service

import com.criptopass.ms.ticket.ticket.application.dto.request.PaymentConfirmationWebhookRequest
import com.criptopass.ms.ticket.ticket.application.port.out.PaymentOrderData
import com.criptopass.ms.ticket.ticket.application.port.out.PaymentOrderRepositoryPort
import com.criptopass.ms.ticket.ticket.application.port.out.TicketRepositoryPort
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class PaymentWebhookService(
    private val ticketRepository: TicketRepositoryPort,
    private val paymentOrderRepository: PaymentOrderRepositoryPort,
) {

    /**
     * Procesa la confirmación de pago del Payment Service.
     * Idempotente por payment_id.
     */
    fun processPaymentConfirmation(request: PaymentConfirmationWebhookRequest): PaymentWebhookResult {
        // Verificar idempotencia
        val existing = paymentOrderRepository.findByPaymentId(request.paymentId)
        if (existing != null) {
            return PaymentWebhookResult(
                accepted = false,
                orderId = request.orderId,
                ticketsActivated = 0,
                message = "Confirmación ya procesada para payment_id: ${request.paymentId}",
            )
        }

        val now = Instant.now()

        // Si el pago fue rechazado, marcar tickets como EXPIRED
        if (request.status == "REJECTED" || request.status == "REFUNDED") {
            val tickets = ticketRepository.findByOrderId(request.orderId)
            val updatedTickets = tickets.map { it.copy(status = TicketStatus.EXPIRED, updatedAt = now) }
            ticketRepository.saveAll(updatedTickets)

            paymentOrderRepository.save(
                PaymentOrderData(
                    orderId = request.orderId,
                    paymentId = request.paymentId,
                    status = request.status,
                    processedAt = now,
                )
            )

            return PaymentWebhookResult(
                accepted = true,
                orderId = request.orderId,
                ticketsActivated = 0,
                message = "Pago ${request.status.lowercase()}. Tickets marcados como EXPIRED.",
            )
        }

        // Buscar tickets por order_id
        val tickets = ticketRepository.findByOrderId(request.orderId)
        if (tickets.isEmpty()) {
            // Guardar el payment_id como procesado para idempotencia
            paymentOrderRepository.save(
                PaymentOrderData(
                    orderId = request.orderId,
                    paymentId = request.paymentId,
                    status = "PROCESSED",
                    processedAt = now,
                )
            )

            return PaymentWebhookResult(
                accepted = true,
                orderId = request.orderId,
                ticketsActivated = 0,
                message = "No se encontraron tickets pendientes para la orden: ${request.orderId}",
            )
        }

        // Transicionar tickets de PENDING_PAYMENT a ACTIVE
        val activatedTickets = tickets.map { ticket ->
            if (ticket.status == TicketStatus.PENDING_PAYMENT) {
                ticket.copy(
                    status = TicketStatus.ACTIVE,
                    qrCode = generateQrCodeData(ticket.id ?: 0L),
                    updatedAt = now,
                )
            } else {
                ticket
            }
        }

        ticketRepository.saveAll(activatedTickets)

        val activatedCount = activatedTickets.count { it.status == TicketStatus.ACTIVE }

        // Guardar payment_id para idempotencia
        paymentOrderRepository.save(
            PaymentOrderData(
                orderId = request.orderId,
                paymentId = request.paymentId,
                status = "PROCESSED",
                processedAt = now,
            )
        )

        return PaymentWebhookResult(
            accepted = true,
            orderId = request.orderId,
            ticketsActivated = activatedCount,
            message = "Procesados $activatedCount tickets activados para orden: ${request.orderId}",
        )
    }

    private fun generateQrCodeData(ticketId: Long): String {
        val now = System.currentTimeMillis()
        return "TICKET:$ticketId:$now"
    }
}

data class PaymentWebhookResult(
    val accepted: Boolean,
    val orderId: String,
    val ticketsActivated: Int,
    val message: String,
)
