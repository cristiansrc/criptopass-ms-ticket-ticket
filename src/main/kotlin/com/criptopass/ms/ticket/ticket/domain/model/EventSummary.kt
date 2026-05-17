package com.criptopass.ms.ticket.ticket.domain.model

import java.time.Instant

/**
 * Resumen de un evento asociado a una boleta.
 * Value object inmutable del dominio.
 */
data class EventSummary(
    val id: Long,
    val name: String,
    val startDate: Instant,
    val venueName: String?,
)
