package com.criptopass.ms.ticket.ticket.adapter.`in`.rest

import com.criptopass.ms.ticket.ticket.application.dto.mapper.toPagedResponse
import com.criptopass.ms.ticket.ticket.application.dto.mapper.toResponse
import com.criptopass.ms.ticket.ticket.application.dto.request.TicketTransferRequest
import com.criptopass.ms.ticket.ticket.application.dto.response.PagedResponse
import com.criptopass.ms.ticket.ticket.application.dto.response.TicketQRResponse
import com.criptopass.ms.ticket.ticket.application.dto.response.TicketResponse
import com.criptopass.ms.ticket.ticket.application.port.`in`.GetTicketUseCase
import com.criptopass.ms.ticket.ticket.application.port.`in`.ListTicketsUseCase
import com.criptopass.ms.ticket.ticket.application.port.`in`.TransferTicketUseCase
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
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
import java.time.Instant

@RestController
@RequestMapping("/tickets")
class TicketController(
    private val listTicketsUseCase: ListTicketsUseCase,
    private val getTicketUseCase: GetTicketUseCase,
    private val transferTicketUseCase: TransferTicketUseCase,
) {

    @GetMapping
    fun listMyTickets(
        authentication: Authentication,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: TicketStatus?,
        @RequestParam(name = "event_id", required = false) eventId: Long?,
    ): ResponseEntity<PagedResponse<TicketResponse>> {
        val userId = authentication.name.toLong()
        val result = listTicketsUseCase.listMyTickets(
            userId = userId,
            page = page,
            size = size,
            status = status,
            eventId = eventId,
        )
        return ResponseEntity.ok(result.toPagedResponse())
    }

    @GetMapping("/{ticketId}")
    fun getTicketById(
        authentication: Authentication,
        @PathVariable ticketId: Long,
    ): ResponseEntity<TicketResponse> {
        val userId = authentication.name.toLong()
        val ticket = getTicketUseCase.getTicketById(ticketId, userId)
        return ResponseEntity.ok(ticket.toResponse())
    }

    @GetMapping("/{ticketId}/qr")
    fun getTicketQR(
        authentication: Authentication,
        @PathVariable ticketId: Long,
    ): ResponseEntity<TicketQRResponse> {
        val userId = authentication.name.toLong()
        val ticket = getTicketUseCase.getTicketById(ticketId, userId)

        val qrCode = "TICKET:${ticket.id}:${System.currentTimeMillis()}"
        val expiresAt = Instant.now().plusSeconds(300) // 5 min TTL

        return ResponseEntity.ok(
            TicketQRResponse(
                ticketId = ticket.id ?: 0L,
                qrCode = qrCode,
                qrImageUrl = "/ms-ticket-ticket/v1/tickets/${ticket.id}/qr/image",
                expiresAt = expiresAt,
            )
        )
    }

    @PostMapping("/{ticketId}/transfer")
    fun transferTicket(
        authentication: Authentication,
        @PathVariable ticketId: Long,
        @Valid @RequestBody request: TicketTransferRequest,
    ): ResponseEntity<TicketResponse> {
        val userId = authentication.name.toLong()
        val newTicket = transferTicketUseCase.transferTicket(
            ticketId = ticketId,
            recipientEmail = request.recipientEmail,
            currentUserId = userId,
        )
        return ResponseEntity.ok(newTicket.toResponse())
    }
}
