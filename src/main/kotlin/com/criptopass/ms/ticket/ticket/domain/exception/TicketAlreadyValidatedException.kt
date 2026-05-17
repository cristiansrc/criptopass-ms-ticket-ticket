package com.criptopass.ms.ticket.ticket.domain.exception

class TicketAlreadyValidatedException(ticketId: Long?, message: String? = null)
    : RuntimeException(message ?: "El ticket $ticketId ya ha sido validado") {

    constructor(ticketId: Long) : this(ticketId, null)
}
