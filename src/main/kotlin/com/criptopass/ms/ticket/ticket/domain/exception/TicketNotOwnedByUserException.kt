package com.criptopass.ms.ticket.ticket.domain.exception

class TicketNotOwnedByUserException(ticketId: Long?, userId: Long?, message: String? = null)
    : RuntimeException(message ?: "El ticket $ticketId no pertenece al usuario $userId") {

    constructor(ticketId: Long, userId: Long) : this(ticketId, userId, null)
}
