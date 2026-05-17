package com.criptopass.ms.ticket.ticket.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class PaymentConfirmationWebhookRequest(
    @field:NotBlank
    val orderId: String,

    @field:NotBlank
    val paymentId: String,

    @field:NotBlank
    val status: String,

    @field:NotNull
    val paidAt: Instant,

    val amount: Double? = null,

    val currency: String? = null,
)
