package com.criptopass.ms.ticket.ticket.application.service

import com.criptopass.ms.ticket.ticket.application.port.`in`.GetTicketUseCase
import com.criptopass.ms.ticket.ticket.application.port.`in`.ListTicketsUseCase
import com.criptopass.ms.ticket.ticket.application.port.out.TicketPage
import com.criptopass.ms.ticket.ticket.application.port.out.TicketRepositoryPort
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotFoundException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotOwnedByUserException
import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TicketService(
    private val ticketRepository: TicketRepositoryPort,
) : ListTicketsUseCase, GetTicketUseCase {

    override fun listMyTickets(
        userId: Long,
        page: Int,
        size: Int,
        status: TicketStatus?,
        eventId: Long?,
    ): TicketPage {
        return ticketRepository.findByOwnerId(
            ownerId = userId,
            page = page,
            size = size,
            status = status,
            eventId = eventId,
        )
    }

    override fun listEventTickets(
        eventId: Long,
        page: Int,
        size: Int,
        status: TicketStatus?,
    ): TicketPage {
        return ticketRepository.findByEventId(
            eventId = eventId,
            page = page,
            size = size,
            status = status,
        )
    }

    override fun getTicketById(ticketId: Long, userId: Long): Ticket {
        val ticket = ticketRepository.findById(ticketId)
            ?: throw TicketNotFoundException(ticketId)

        if (!ticket.isOwnedBy(userId)) {
            throw TicketNotOwnedByUserException(ticketId, userId)
        }

        return ticket
    }
}
