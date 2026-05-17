package com.criptopass.ms.ticket.ticket.adapter.`in`.rest

import com.criptopass.ms.ticket.ticket.application.dto.request.TicketPurchaseRequest
import com.criptopass.ms.ticket.ticket.application.dto.response.TicketPurchaseResponse
import com.criptopass.ms.ticket.ticket.application.dto.response.TicketTypeResponse
import com.criptopass.ms.ticket.ticket.application.port.`in`.PurchaseTicketUseCase
import com.criptopass.ms.ticket.ticket.application.port.out.EventPort
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/ticket-types")
class TicketTypeController(
    private val eventPort: EventPort,
    private val purchaseTicketUseCase: PurchaseTicketUseCase,
) {

    @GetMapping
    fun listTicketTypes(
        @RequestParam(name = "event_id") eventId: Long,
    ): ResponseEntity<List<TicketTypeResponse>> {
        val ticketTypes = eventPort.getTicketTypes(eventId)
        val responses = ticketTypes.map { ticketType ->
            TicketTypeResponse(
                id = ticketType.id,
                eventId = ticketType.eventId,
                name = ticketType.name,
                description = ticketType.description,
                price = ticketType.price,
                quantity = ticketType.quantity,
                availableQuantity = ticketType.availableQuantity,
                maxPerUser = ticketType.maxPerUser,
                createdAt = ticketType.createdAt,
            )
        }
        return ResponseEntity.ok(responses)
    }

    @PostMapping("/{ticketTypeId}/purchase")
    fun purchaseTicket(
        authentication: Authentication,
        @PathVariable ticketTypeId: Long,
        @Valid @RequestBody request: TicketPurchaseRequest,
    ): ResponseEntity<TicketPurchaseResponse> {
        val userId = authentication.name.toLong()
        val userEmail = authentication.name // simplified; in real app would extract from JWT claims

        val result = purchaseTicketUseCase.purchaseTicket(
            ticketTypeId = ticketTypeId,
            quantity = request.quantity,
            userId = userId,
            userEmail = userEmail,
        )

        val response = TicketPurchaseResponse(
            orderId = result.orderId,
            ticketTypeId = result.ticketTypeId,
            quantity = result.quantity,
            totalAmount = result.totalAmount,
            paymentPreferenceId = result.paymentPreferenceId,
            status = result.status,
            createdAt = java.time.Instant.now(),
        )

        return ResponseEntity
            .created(URI.create("/ms-ticket-ticket/v1/orders/${result.orderId}"))
            .body(response)
    }
}
