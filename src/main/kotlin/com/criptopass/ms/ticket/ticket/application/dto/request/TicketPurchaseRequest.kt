package com.criptopass.ms.ticket.ticket.application.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class TicketPurchaseRequest(
    @field:Min(1)
    @field:Max(10)
    val quantity: Int,
)
