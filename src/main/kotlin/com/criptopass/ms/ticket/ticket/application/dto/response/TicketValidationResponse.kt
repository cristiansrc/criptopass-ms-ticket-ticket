package com.criptopass.ms.ticket.ticket.application.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class TicketValidationResponse(
    val valid: Boolean,
    val ticket: TicketResponse,
    val message: String,
    @JsonProperty("validated_at")
    val validatedAt: Instant,
    @JsonProperty("validated_by")
    val validatedBy: Long,
)
