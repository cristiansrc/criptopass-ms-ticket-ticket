package com.criptopass.ms.ticket.ticket.adapter.`in`.rest

import com.criptopass.ms.ticket.ticket.application.dto.mapper.toResponse
import com.criptopass.ms.ticket.ticket.application.dto.request.TicketScanRequest
import com.criptopass.ms.ticket.ticket.application.dto.response.TicketValidationResponse
import com.criptopass.ms.ticket.ticket.application.port.`in`.ValidateTicketUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/validation")
@PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
class ValidationController(
    private val validateTicketUseCase: ValidateTicketUseCase,
) {

    @PostMapping("/scan")
    fun scanTicket(
        authentication: Authentication,
        @Valid @RequestBody request: TicketScanRequest,
    ): ResponseEntity<TicketValidationResponse> {
        val validatorUserId = authentication.name.toLong()
        val result = validateTicketUseCase.scanTicket(
            qrCode = request.qrCode,
            validatorUserId = validatorUserId,
        )
        return ResponseEntity.ok(result.toResponse())
    }

    @PostMapping("/{ticketId}")
    fun validateTicket(
        authentication: Authentication,
        @PathVariable ticketId: Long,
    ): ResponseEntity<TicketValidationResponse> {
        val validatorUserId = authentication.name.toLong()
        val result = validateTicketUseCase.validateTicket(
            ticketId = ticketId,
            validatorUserId = validatorUserId,
        )
        return ResponseEntity.ok(result.toResponse())
    }
}
