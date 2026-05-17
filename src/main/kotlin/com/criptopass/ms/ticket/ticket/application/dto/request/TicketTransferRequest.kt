package com.criptopass.ms.ticket.ticket.application.dto.request

import jakarta.validation.constraints.Email

data class TicketTransferRequest(
    @field:Email
    val recipientEmail: String,
)
