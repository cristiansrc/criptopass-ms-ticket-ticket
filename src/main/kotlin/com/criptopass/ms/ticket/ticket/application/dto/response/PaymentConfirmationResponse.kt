package com.criptopass.ms.ticket.ticket.application.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class PaymentConfirmationResponse(
    val accepted: Boolean,
    @JsonProperty("order_id")
    val orderId: String,
    @JsonProperty("tickets_activated")
    val ticketsActivated: Int,
    val message: String,
)
