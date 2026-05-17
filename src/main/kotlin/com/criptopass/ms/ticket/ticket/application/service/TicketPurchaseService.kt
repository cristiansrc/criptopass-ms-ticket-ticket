package com.criptopass.ms.ticket.ticket.application.service

import com.criptopass.ms.ticket.ticket.application.port.`in`.PurchaseTicketUseCase
import com.criptopass.ms.ticket.ticket.application.port.`in`.TicketPurchaseResult
import com.criptopass.ms.ticket.ticket.application.port.out.BlockchainPort
import com.criptopass.ms.ticket.ticket.application.port.out.EventPort
import com.criptopass.ms.ticket.ticket.application.port.out.PaymentPort
import com.criptopass.ms.ticket.ticket.application.port.out.TicketRepositoryPort
import com.criptopass.ms.ticket.ticket.application.port.out.TicketTypeRepositoryPort
import com.criptopass.ms.ticket.ticket.domain.exception.InsufficientTicketsException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotFoundException
import com.criptopass.ms.ticket.ticket.domain.model.EventSummary
import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import com.criptopass.ms.ticket.ticket.domain.model.TicketType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class TicketPurchaseService(
    private val ticketRepository: TicketRepositoryPort,
    private val ticketTypeRepository: TicketTypeRepositoryPort,
    private val eventPort: EventPort,
    private val paymentPort: PaymentPort,
    private val blockchainPort: BlockchainPort,
) : PurchaseTicketUseCase {

    override fun purchaseTicket(
        ticketTypeId: Long,
        quantity: Int,
        userId: Long,
        userEmail: String,
    ): TicketPurchaseResult {
        val ticketType = ticketTypeRepository.findById(ticketTypeId)
            ?: throw TicketNotFoundException(ticketTypeId, "Tipo de boleta no encontrado: $ticketTypeId")

        if (ticketType.availableQuantity < quantity) {
            throw InsufficientTicketsException(
                ticketTypeId = ticketTypeId,
                requested = quantity,
                available = ticketType.availableQuantity,
            )
        }

        val event = eventPort.getEventById(ticketType.eventId)
            ?: throw TicketNotFoundException(ticketType.eventId, "Evento no encontrado: ${ticketType.eventId}")

        val orderId = "ORD-${Instant.now().toEpochMilli()}-${UUID.randomUUID().toString().take(8)}"
        val totalAmount = ticketType.price * quantity

        // Crear orden de pago
        val paymentPreferenceId = paymentPort.createOrder(
            orderId = orderId,
            ticketTypeId = ticketTypeId,
            quantity = quantity,
            totalAmount = totalAmount,
            userId = userId,
            userEmail = userEmail,
        )

        val now = Instant.now()

        // Crear tickets en estado PENDING_PAYMENT
        val tickets = (1..quantity).map { index ->
            Ticket(
                event = event,
                ticketType = ticketType,
                ownerId = userId,
                ownerEmail = userEmail,
                price = ticketType.price,
                status = TicketStatus.PENDING_PAYMENT,
                purchasedAt = now,
                createdAt = now,
                updatedAt = now,
            )
        }

        val savedTickets = ticketRepository.saveAll(tickets)

        // Pre-registrar tokens en blockchain
        savedTickets.forEach { ticket ->
            try {
                val tokenId = blockchainPort.registerToken(
                    ticketId = ticket.id ?: throw IllegalStateException("Ticket sin ID después de guardar"),
                    ownerId = userId,
                )
                val updatedTicket = ticket.copy(blockchainTokenId = tokenId)
                ticketRepository.save(updatedTicket)
            } catch (e: Exception) {
                // Si blockchain falla, no bloquear la compra
                // El token se puede registrar después
            }
        }

        return TicketPurchaseResult(
            orderId = orderId,
            ticketTypeId = ticketTypeId,
            quantity = quantity,
            totalAmount = totalAmount,
            paymentPreferenceId = paymentPreferenceId,
            status = "PENDING_PAYMENT",
        )
    }
}
