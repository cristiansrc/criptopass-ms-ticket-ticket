package com.criptopass.ms.ticket.ticket.application.service

import com.criptopass.ms.ticket.ticket.application.port.`in`.RevokeTicketUseCase
import com.criptopass.ms.ticket.ticket.application.port.out.TicketRepositoryPort
import com.criptopass.ms.ticket.ticket.domain.exception.InvalidTicketStatusTransitionException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotFoundException
import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TicketRevokeService(
    private val ticketRepository: TicketRepositoryPort,
) : RevokeTicketUseCase {

    override fun revokeTicket(ticketId: Long, adminUserId: Long): Ticket {
        val ticket = ticketRepository.findById(ticketId)
            ?: throw TicketNotFoundException(ticketId)

        if (ticket.status == TicketStatus.REVOKED || ticket.status == TicketStatus.VALIDATED) {
            throw InvalidTicketStatusTransitionException(
                ticketId = ticketId,
                currentState = ticket.status,
                message = "La boleta no puede ser revocada en estado ${ticket.status.name}",
            )
        }

        val revokedTicket = ticket.copy(
            status = TicketStatus.REVOKED,
            updatedAt = java.time.Instant.now(),
        )

        return ticketRepository.save(revokedTicket)
    }
}
