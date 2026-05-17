package com.criptopass.ms.ticket.ticket.application.dto.request

import jakarta.validation.constraints.NotBlank

data class TicketScanRequest(
    @field:NotBlank
    val qrCode: String,
)
