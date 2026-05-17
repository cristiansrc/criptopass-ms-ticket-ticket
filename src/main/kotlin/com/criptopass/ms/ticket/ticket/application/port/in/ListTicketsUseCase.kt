package com.criptopass.ms.ticket.ticket.application.port.`in`

import com.criptopass.ms.ticket.ticket.application.port.out.TicketPage
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus

interface ListTicketsUseCase {

    fun listMyTickets(
        userId: Long,
        page: Int = 0,
        size: Int = 20,
        status: TicketStatus? = null,
        eventId: Long? = null,
    ): TicketPage

    fun listEventTickets(
        eventId: Long,
        page: Int = 0,
        size: Int = 20,
        status: TicketStatus? = null,
    ): TicketPage
}
