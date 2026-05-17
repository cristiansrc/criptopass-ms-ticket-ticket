package com.criptopass.ms.ticket.ticket.domain.model

import java.time.Instant

/**
 * Modelo de dominio de un tipo de boleta (categoría dentro de un evento).
 * Value object inmutable.
 */
data class TicketType(
    val id: Long,
    val eventId: Long,
    val name: String,
    val description: String?,
    val price: Double,
    val quantity: Int,
    val availableQuantity: Int,
    val maxPerUser: Int,
    val createdAt: Instant,
)
