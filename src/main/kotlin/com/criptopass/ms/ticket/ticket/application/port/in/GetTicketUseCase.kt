package com.criptopass.ms.ticket.ticket.application.port.`in`

import com.criptopass.ms.ticket.ticket.domain.model.Ticket

interface GetTicketUseCase {

    fun getTicketById(ticketId: Long, userId: Long): Ticket
}
