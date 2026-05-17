package com.criptopass.ms.ticket.ticket.domain.exception

import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus

class TicketNotTransferableException(
    ticketId: Long?,
    currentState: TicketStatus? = null,
    message: String? = null,
) : RuntimeException(
    message
        ?: "El ticket $ticketId no es transferible en estado ${currentState?.name ?: "desconocido"}"
) {

    constructor(ticketId: Long) : this(ticketId, null, null)
}
