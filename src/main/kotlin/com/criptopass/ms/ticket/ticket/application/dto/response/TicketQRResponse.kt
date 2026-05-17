package com.criptopass.ms.ticket.ticket.application.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class TicketQRResponse(
    @JsonProperty("ticket_id")
    val ticketId: Long,
    @JsonProperty("qr_code")
    val qrCode: String,
    @JsonProperty("qr_image_url")
    val qrImageUrl: String,
    @JsonProperty("expires_at")
    val expiresAt: Instant,
)
