package com.criptopass.ms.ticket.ticket.application.port.`in`

import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import java.time.Instant

interface ValidateTicketUseCase {

    /**
     * Escanea y valida una boleta por su código QR.
     */
    fun scanTicket(qrCode: String, validatorUserId: Long): TicketValidationResult

    /**
     * Valida una boleta directamente por su ID.
     */
    fun validateTicket(ticketId: Long, validatorUserId: Long): TicketValidationResult
}

data class TicketValidationResult(
    val valid: Boolean,
    val ticket: Ticket,
    val message: String,
    val validatedAt: Instant,
    val validatedBy: Long,
)
