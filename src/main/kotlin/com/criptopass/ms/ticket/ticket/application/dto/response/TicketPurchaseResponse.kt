package com.criptopass.ms.ticket.ticket.application.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class TicketPurchaseResponse(
    @JsonProperty("order_id")
    val orderId: String,
    @JsonProperty("ticket_type_id")
    val ticketTypeId: Long,
    val quantity: Int,
    @JsonProperty("total_amount")
    val totalAmount: Double,
    @JsonProperty("payment_preference_id")
    val paymentPreferenceId: String,
    val status: String,
    @JsonProperty("created_at")
    val createdAt: Instant,
)
