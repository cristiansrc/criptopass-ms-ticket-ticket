package com.criptopass.ms.ticket.ticket.domain.exception

import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus

class InvalidTicketStatusTransitionException(
    ticketId: Long?,
    currentState: TicketStatus? = null,
    message: String? = null,
) : RuntimeException(
    message
        ?: "Transición de estado inválida para el ticket $ticketId desde estado ${currentState?.name ?: "desconocido"}"
) {

    constructor(ticketId: Long) : this(ticketId, null, null)
}
