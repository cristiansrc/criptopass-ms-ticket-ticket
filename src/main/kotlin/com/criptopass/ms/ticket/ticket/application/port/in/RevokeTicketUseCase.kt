package com.criptopass.ms.ticket.ticket.application.port.`in`

import com.criptopass.ms.ticket.ticket.domain.model.Ticket

interface RevokeTicketUseCase {

    fun revokeTicket(ticketId: Long, adminUserId: Long): Ticket
}
