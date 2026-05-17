package com.criptopass.ms.ticket.ticket.application.service

import com.criptopass.ms.ticket.ticket.application.port.`in`.TicketValidationResult
import com.criptopass.ms.ticket.ticket.application.port.`in`.ValidateTicketUseCase
import com.criptopass.ms.ticket.ticket.application.port.out.TicketRepositoryPort
import com.criptopass.ms.ticket.ticket.domain.exception.InvalidTicketStatusTransitionException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotFoundException
import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class TicketValidationService(
    private val ticketRepository: TicketRepositoryPort,
) : ValidateTicketUseCase {

    override fun scanTicket(qrCode: String, validatorUserId: Long): TicketValidationResult {
        val ticket = ticketRepository.findByQrCode(qrCode)
            ?: throw TicketNotFoundException(null, "No se encontró boleta con el QR proporcionado")

        return validateTicketInternal(ticket, validatorUserId)
    }

    override fun validateTicket(ticketId: Long, validatorUserId: Long): TicketValidationResult {
        val ticket = ticketRepository.findById(ticketId)
            ?: throw TicketNotFoundException(ticketId)

        return validateTicketInternal(ticket, validatorUserId)
    }

    private fun validateTicketInternal(ticket: Ticket, validatorUserId: Long): TicketValidationResult {
        if (ticket.status != TicketStatus.ACTIVE && ticket.status != TicketStatus.TRANSFERRED) {
            throw InvalidTicketStatusTransitionException(
                ticketId = ticket.id,
                currentState = ticket.status,
                message = "La boleta no puede ser validada en estado ${ticket.status.name}. Debe estar ACTIVE.",
            )
        }

        val now = Instant.now()
        val validatedTicket = ticket.copy(
            status = TicketStatus.VALIDATED,
            validatedAt = now,
            validatedBy = validatorUserId,
            updatedAt = now,
        )

        val saved = ticketRepository.save(validatedTicket)

        return TicketValidationResult(
            valid = true,
            ticket = saved,
            message = "Boleta validada exitosamente",
            validatedAt = now,
            validatedBy = validatorUserId,
        )
    }
}
