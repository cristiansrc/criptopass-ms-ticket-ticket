package com.criptopass.ms.ticket.ticket.application.dto.response

import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class TicketResponse(
    val id: Long,
    val event: EventSummaryResponse,
    @JsonProperty("ticket_type")
    val ticketType: TicketTypeResponse,
    @JsonProperty("owner_id")
    val ownerId: Long,
    @JsonProperty("owner_email")
    val ownerEmail: String,
    val price: Double,
    val status: TicketStatus,
    @JsonProperty("qr_code")
    val qrCode: String? = null,
    @JsonProperty("blockchain_token_id")
    val blockchainTokenId: Long? = null,
    @JsonProperty("blockchain_tx_hash")
    val blockchainTxHash: String? = null,
    @JsonProperty("seat_number")
    val seatNumber: String? = null,
    @JsonProperty("purchased_at")
    val purchasedAt: Instant? = null,
    @JsonProperty("validated_at")
    val validatedAt: Instant? = null,
    @JsonProperty("created_at")
    val createdAt: Instant? = null,
    @JsonProperty("updated_at")
    val updatedAt: Instant? = null,
)

data class EventSummaryResponse(
    val id: Long,
    val name: String,
    @JsonProperty("start_date")
    val startDate: Instant,
    @JsonProperty("venue_name")
    val venueName: String? = null,
)

data class TicketTypeResponse(
    val id: Long,
    @JsonProperty("event_id")
    val eventId: Long,
    val name: String,
    val description: String? = null,
    val price: Double,
    val quantity: Int,
    @JsonProperty("available_quantity")
    val availableQuantity: Int,
    @JsonProperty("max_per_user")
    val maxPerUser: Int,
    @JsonProperty("created_at")
    val createdAt: Instant? = null,
)
