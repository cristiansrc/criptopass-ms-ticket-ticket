package com.criptopass.ms.ticket.ticket.adapter.`in`.rest

import com.criptopass.ms.ticket.ticket.application.dto.request.PaymentConfirmationWebhookRequest
import com.criptopass.ms.ticket.ticket.application.dto.response.PaymentConfirmationResponse
import com.criptopass.ms.ticket.ticket.application.service.PaymentWebhookService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/webhooks")
class WebhookController(
    private val paymentWebhookService: PaymentWebhookService,
) {

    @PostMapping("/payment/confirmation")
    fun paymentConfirmation(
        @Valid @RequestBody request: PaymentConfirmationWebhookRequest,
    ): ResponseEntity<PaymentConfirmationResponse> {
        val result = paymentWebhookService.processPaymentConfirmation(request)

        return ResponseEntity.ok(
            PaymentConfirmationResponse(
                accepted = result.accepted,
                orderId = result.orderId,
                ticketsActivated = result.ticketsActivated,
                message = result.message,
            )
        )
    }
}
