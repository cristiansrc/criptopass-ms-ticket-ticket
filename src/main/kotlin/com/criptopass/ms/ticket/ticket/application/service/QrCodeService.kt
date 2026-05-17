package com.criptopass.ms.ticket.ticket.application.service

import com.criptopass.ms.ticket.ticket.application.dto.response.TicketQRResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class QrCodeService(
    @Value("\${app.qr-code.ttl-minutes}") private val ttlMinutes: Long,
    @Value("\${app.qr-code.secret}") private val qrSecret: String,
    @Value("\${app.gateway.base-path}") private val basePath: String,
) {

    /**
     * Genera un código QR firmado para una boleta.
     * El QR tiene un TTL configurable y contiene:
     * - ticketId
     * - Firma HMAC-SHA256
     * - expiresAt
     */
    fun generateQrCode(ticketId: Long): TicketQRResponse {
        val expiresAt = Instant.now().plusSeconds(ttlMinutes * 60)
        val expiresAtEpoch = expiresAt.toEpochMilli()

        val rawData = "$ticketId:$expiresAtEpoch"
        val signature = calculateHmac(rawData, qrSecret)
        val qrCodeData = "$ticketId:$signature:$expiresAtEpoch"
        val qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeData.toByteArray())

        return TicketQRResponse(
            ticketId = ticketId,
            qrCode = qrCodeBase64,
            qrImageUrl = "$basePath/tickets/$ticketId/qr/image",
            expiresAt = expiresAt,
        )
    }

    /**
     * Verifica la firma de un código QR y su expiración.
     */
    fun verifyQrCode(qrCodeBase64: String): VerifyResult {
        return try {
            val decoded = Base64.getDecoder().decode(qrCodeBase64)
            val parts = String(decoded).split(":")
            if (parts.size != 3) {
                return VerifyResult(false, "Formato de QR inválido")
            }

            val ticketId = parts[0]
            val signature = parts[1]
            val expiresAtEpoch = parts[2].toLong()

            // Verificar expiración
            if (Instant.now().toEpochMilli() > expiresAtEpoch) {
                return VerifyResult(false, "QR code expirado")
            }

            // Verificar firma
            val expectedSignature = calculateHmac("$ticketId:$expiresAtEpoch", qrSecret)
            if (signature != expectedSignature) {
                return VerifyResult(false, "Firma de QR inválida")
            }

            VerifyResult(true, "QR válido", ticketId = ticketId.toLongOrNull())
        } catch (e: Exception) {
            VerifyResult(false, "Error al verificar QR: ${e.message}")
        }
    }

    private fun calculateHmac(data: String, secret: String): String {
        val secretKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKey)
        val hmacBytes = mac.doFinal(data.toByteArray())
        return hmacBytes.joinToString("") { "%02x".format(it) }
    }
}

data class VerifyResult(
    val valid: Boolean,
    val message: String,
    val ticketId: Long? = null,
)
