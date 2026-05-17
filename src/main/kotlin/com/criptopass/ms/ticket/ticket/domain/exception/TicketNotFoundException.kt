package com.criptopass.ms.ticket.ticket.domain.exception

class TicketNotFoundException(ticketId: Long?, message: String? = null)
    : RuntimeException(message ?: "Ticket no encontrado: $ticketId") {

    constructor(ticketId: Long) : this(ticketId, null)
}
