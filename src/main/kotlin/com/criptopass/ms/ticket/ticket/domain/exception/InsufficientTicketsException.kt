package com.criptopass.ms.ticket.ticket.domain.exception

class InsufficientTicketsException(
    ticketTypeId: Long?,
    requested: Int? = null,
    available: Int? = null,
    message: String? = null,
) : RuntimeException(
    message
        ?: "Boletas insuficientes para el tipo $ticketTypeId. Solicitadas: $requested, disponibles: $available"
) {

    constructor(ticketTypeId: Long) : this(ticketTypeId, null, null, null)
}
