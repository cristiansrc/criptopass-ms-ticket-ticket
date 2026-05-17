package com.criptopass.ms.ticket.ticket.adapter.`in`.rest

import com.criptopass.ms.ticket.ticket.application.dto.mapper.toPagedResponse
import com.criptopass.ms.ticket.ticket.application.dto.mapper.toResponse
import com.criptopass.ms.ticket.ticket.application.dto.response.PagedResponse
import com.criptopass.ms.ticket.ticket.application.dto.response.TicketResponse
import com.criptopass.ms.ticket.ticket.application.port.`in`.ListTicketsUseCase
import com.criptopass.ms.ticket.ticket.application.port.`in`.RevokeTicketUseCase
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tickets")
@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
class AdminTicketController(
    private val listTicketsUseCase: ListTicketsUseCase,
    private val revokeTicketUseCase: RevokeTicketUseCase,
) {

    @PostMapping("/{ticketId}/revoke")
    fun revokeTicket(
        authentication: Authentication,
        @PathVariable ticketId: Long,
    ): ResponseEntity<TicketResponse> {
        val adminUserId = authentication.name.toLong()
        val ticket = revokeTicketUseCase.revokeTicket(ticketId, adminUserId)
        return ResponseEntity.ok(ticket.toResponse())
    }

    @GetMapping("/events/{eventId}")
    fun listEventTickets(
        @PathVariable eventId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: TicketStatus?,
    ): ResponseEntity<PagedResponse<TicketResponse>> {
        val result = listTicketsUseCase.listEventTickets(
            eventId = eventId,
            page = page,
            size = size,
            status = status,
        )
        return ResponseEntity.ok(result.toPagedResponse())
    }
}
