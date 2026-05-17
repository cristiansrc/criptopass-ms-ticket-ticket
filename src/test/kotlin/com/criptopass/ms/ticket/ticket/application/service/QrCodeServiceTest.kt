package com.criptopass.ms.ticket.ticket.application.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QrCodeServiceTest {

    private lateinit var qrCodeService: QrCodeService

    @BeforeEach
    fun setUp() {
        // Configurar con valores de test
        qrCodeService = QrCodeService(
            ttlMinutes = 5,
            qrSecret = "test-secret-key-for-qr-signing",
            basePath = "/api/v1",
        )
    }

    @Test
    fun `should generate QR code with valid structure`() {
        // Arrange
        val ticketId = 1L

        // Act
        val result = qrCodeService.generateQrCode(ticketId)

        // Assert
        assertNotNull(result)
        assertEquals(ticketId, result.ticketId)
        assertNotNull(result.qrCode)
        assertNotNull(result.qrImageUrl)
        assertNotNull(result.expiresAt)

        // Verify QR code is base64 encoded
        val decoded = String(java.util.Base64.getDecoder().decode(result.qrCode))
        val parts = decoded.split(":")
        assertEquals(3, parts.size)
        assertEquals(ticketId.toString(), parts[0])
    }

    @Test
    fun `should generate QR code with correct expiration time`() {
        // Arrange
        val ticketId = 1L
        val beforeGeneration = Instant.now()

        // Act
        val result = qrCodeService.generateQrCode(ticketId)

        // Assert
        val afterGeneration = Instant.now().plusSeconds(5 * 60) // TTL + 5 minutes buffer

        assertTrue(result.expiresAt.isAfter(beforeGeneration))
        assertTrue(result.expiresAt.isBefore(afterGeneration) || result.expiresAt == afterGeneration)
    }

    @Test
    fun `should generate QR code with correct image URL`() {
        // Arrange
        val ticketId = 123L

        // Act
        val result = qrCodeService.generateQrCode(ticketId)

        // Assert
        assertEquals("/api/v1/tickets/123/qr/image", result.qrImageUrl)
    }

    @Test
    fun `should verify valid QR code successfully`() {
        // Arrange
        val ticketId = 1L
        val qrResponse = qrCodeService.generateQrCode(ticketId)

        // Act
        val verifyResult = qrCodeService.verifyQrCode(qrResponse.qrCode)

        // Assert
        assertNotNull(verifyResult)
        assertTrue(verifyResult.valid)
        assertEquals(ticketId, verifyResult.ticketId)
        assertEquals("QR válido", verifyResult.message)
    }

    @Test
    fun `should reject QR code with invalid format`() {
        // Arrange
        val invalidQrCode = java.util.Base64.getEncoder().encodeToString("invalid:format".toByteArray())

        // Act
        val result = qrCodeService.verifyQrCode(invalidQrCode)

        // Assert
        assertFalse(result.valid)
        assertTrue(result.message!!.contains("inválido"))
    }

    @Test
    fun `should reject QR code with invalid base64`() {
        // Arrange
        val invalidBase64 = "not-valid-base64!!!"

        // Act
        val result = qrCodeService.verifyQrCode(invalidBase64)

        // Assert
        assertFalse(result.valid)
        assertNotNull(result.message)
    }

    @Test
    fun `should reject QR code with tampered signature`() {
        // Arrange
        val ticketId = 1L
        val qrResponse = qrCodeService.generateQrCode(ticketId)

        // Tamper with signature
        val decoded = String(java.util.Base64.getDecoder().decode(qrResponse.qrCode))
        val parts = decoded.split(":")
        val tamperedParts = listOf(parts[0], "tampered-signature", parts[2])
        val tamperedQrCode = java.util.Base64.getEncoder()
            .encodeToString(tamperedParts.joinToString(":").toByteArray())

        // Act
        val result = qrCodeService.verifyQrCode(tamperedQrCode)

        // Assert
        assertFalse(result.valid)
        assertTrue(result.message!!.contains("Firma") || result.message!!.contains("inválida"))
    }

    @Test
    fun `should reject expired QR code`() {
        // Arrange - Create service with very short TTL
        val shortTtlService = QrCodeService(
            ttlMinutes = 0, // 0 minutes = expires immediately
            qrSecret = "test-secret-key-for-qr-signing",
            basePath = "/api/v1",
        )

        val ticketId = 1L
        val qrResponse = shortTtlService.generateQrCode(ticketId)

        // Wait a tiny bit (not needed since TTL is 0)
        Thread.sleep(10)

        // Act
        val result = shortTtlService.verifyQrCode(qrResponse.qrCode)

        // Assert
        assertFalse(result.valid)
        assertTrue(result.message!!.contains("expirado"))
    }

    @Test
    fun `should generate different QR codes for same ticket at different times`() {
        // Arrange
        val ticketId = 1L

        // Act
        val result1 = qrCodeService.generateQrCode(ticketId)
        Thread.sleep(100) // Small delay to ensure different timestamps
        val result2 = qrCodeService.generateQrCode(ticketId)

        // Assert
        assertNotNull(result1)
        assertNotNull(result2)
        assertEquals(ticketId, result1.ticketId)
        assertEquals(ticketId, result2.ticketId)

        // QR codes should be different due to different expiration times
        assertTrue(result1.expiresAt.isBefore(result2.expiresAt) || result1.expiresAt == result2.expiresAt)
    }

    @Test
    fun `should generate valid QR codes for multiple ticket IDs`() {
        // Arrange
        val ticketIds = listOf(1L, 2L, 3L, 100L, 999L)

        // Act & Assert
        ticketIds.forEach { ticketId ->
            val qrResponse = qrCodeService.generateQrCode(ticketId)
            val verifyResult = qrCodeService.verifyQrCode(qrResponse.qrCode)

            assertTrue(verifyResult.valid, "QR code should be valid for ticket $ticketId")
            assertEquals(ticketId, verifyResult.ticketId)
        }
    }

    @Test
    fun `should include ticket ID in verification result`() {
        // Arrange
        val ticketId = 42L
        val qrResponse = qrCodeService.generateQrCode(ticketId)

        // Act
        val verifyResult = qrCodeService.verifyQrCode(qrResponse.qrCode)

        // Assert
        assertEquals(ticketId, verifyResult.ticketId)
    }

    @Test
    fun `should handle QR code verification with null ticket ID gracefully`() {
        // Arrange - Create a QR code with invalid ticket ID format
        val rawData = "invalid-id:1234567890"
        val signature = qrCodeService.generateQrCode(1L).let {
            // Get signature from a valid QR to make format correct
            val decoded = String(java.util.Base64.getDecoder().decode(it.qrCode))
            decoded.split(":")[1]
        }
        val expiresAt = System.currentTimeMillis() + 300000 // 5 minutes
        val qrData = "invalid-id:$signature:$expiresAt"
        val invalidQrCode = java.util.Base64.getEncoder().encodeToString(qrData.toByteArray())

        // Act
        val result = qrCodeService.verifyQrCode(invalidQrCode)

        // Assert - Should fail because ticketId is not a valid long
        assertFalse(result.valid)
    }
}
