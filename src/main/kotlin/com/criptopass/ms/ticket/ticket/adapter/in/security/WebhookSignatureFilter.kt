package com.criptopass.ms.ticket.ticket.adapter.`in`.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
@Order(1)
class WebhookSignatureFilter(
    @Value("\${app.webhook.signature-secret}") private val signatureSecret: String,
) : OncePerRequestFilter() {

    companion object {
        private const val HEADER_SIGNATURE = "X-Webhook-Signature"
        private const val HMAC_ALGORITHM = "HmacSHA256"
        private const val WEBHOOK_PATH_PREFIX = "/webhooks"
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return !(request.method == "POST" && request.requestURI.startsWith(WEBHOOK_PATH_PREFIX))
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val cachedRequest = ContentCachingRequestWrapper(request, 1048576)

        val signatureHeader = cachedRequest.getHeader(HEADER_SIGNATURE)
            ?: run {
                sendUnauthorized(response, cachedRequest, "WEBHOOK_SIGNATURE_MISSING", "Missing X-Webhook-Signature header")
                return
            }

        cachedRequest.inputStream.readBytes()
        val payload = cachedRequest.contentAsByteArray
        val expectedSignature = calculateHmacSha256(payload, signatureSecret)

        if (!MessageDigest.isEqual(signatureHeader.toByteArray(), expectedSignature.toByteArray())) {
            sendUnauthorized(response, cachedRequest, "WEBHOOK_SIGNATURE_INVALID", "Invalid webhook signature")
            return
        }

        filterChain.doFilter(cachedRequest, response)
    }

    private fun sendUnauthorized(
        response: HttpServletResponse,
        request: HttpServletRequest,
        code: String,
        message: String,
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.writer.write(
            """{"timestamp":"${java.time.Instant.now()}","status":401,"error":"UNAUTHORIZED","code":"$code","message":"$message","path":"${request.requestURI}","trace_id":"","details":[]}"""
        )
    }

    private fun calculateHmacSha256(data: ByteArray, secret: String): String {
        val secretKey = SecretKeySpec(secret.toByteArray(), HMAC_ALGORITHM)
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(secretKey)
        val hmacBytes = mac.doFinal(data)
        return hmacBytes.joinToString("") { "%02x".format(it) }
    }
}
