package com.criptopass.ms.ticket.ticket.application.service

import com.criptopass.ms.ticket.ticket.application.port.out.TicketRepositoryPort
import com.criptopass.ms.ticket.ticket.domain.exception.InvalidTicketStatusTransitionException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotFoundException
import com.criptopass.ms.ticket.ticket.domain.model.EventSummary
import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import com.criptopass.ms.ticket.ticket.domain.model.TicketType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TicketValidationServiceTest {

    private val ticketRepository = mockk<TicketRepositoryPort>()
    private lateinit var validationService: TicketValidationService

    private val testEvent = EventSummary(
        id = 1L,
        name = "Test Concert",
        startDate = Instant.now().plusSeconds(86400),
        venueName = "Test Stadium",
    )

    private val testTicketType = TicketType(
        id = 1L,
        eventId = 1L,
        name = "General Admission",
        description = null,
        price = 50.0,
        quantity = 100,
        availableQuantity = 50,
        maxPerUser = 5,
        createdAt = Instant.now(),
    )

    private val testTicket = Ticket(
        id = 1L,
        event = testEvent,
        ticketType = testTicketType,
        ownerId = 100L,
        ownerEmail = "owner@test.com",
        price = 50.0,
        status = TicketStatus.ACTIVE,
        qrCode = "QR_ABC123",
        purchasedAt = Instant.now(),
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    @BeforeEach
    fun setUp() {
        validationService = TicketValidationService(ticketRepository)
    }

    @Test
    fun `should validate ticket successfully by QR code when status is ACTIVE`() {
        // Arrange
        val qrCode = "QR_ABC123"
        val validatorUserId = 200L

        every { ticketRepository.findByQrCode(qrCode) } returns testTicket
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = validationService.scanTicket(qrCode, validatorUserId)

        // Assert
        assertNotNull(result)
        assertTrue(result.valid)
        assertEquals(TicketStatus.VALIDATED, result.ticket.status)
        assertNotNull(result.ticket.validatedAt)
        assertEquals(validatorUserId, result.ticket.validatedBy)
        assertEquals(validatorUserId, result.validatedBy)

        verify { ticketRepository.save(withArg { ticket ->
            assertEquals(TicketStatus.VALIDATED, ticket.status)
            assertEquals(validatorUserId, ticket.validatedBy)
        }) }
    }

    @Test
    fun `should validate ticket successfully by ID when status is ACTIVE`() {
        // Arrange
        val ticketId = 1L
        val validatorUserId = 200L

        every { ticketRepository.findById(ticketId) } returns testTicket
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = validationService.validateTicket(ticketId, validatorUserId)

        // Assert
        assertNotNull(result)
        assertTrue(result.valid)
        assertEquals(TicketStatus.VALIDATED, result.ticket.status)

        verify { ticketRepository.save(withArg { ticket ->
            assertEquals(TicketStatus.VALIDATED, ticket.status)
        }) }
    }

    @Test
    fun `should validate ticket when status is TRANSFERRED`() {
        // Arrange
        val transferredTicket = testTicket.copy(status = TicketStatus.TRANSFERRED)
        val ticketId = 1L
        val validatorUserId = 200L

        every { ticketRepository.findById(ticketId) } returns transferredTicket
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = validationService.validateTicket(ticketId, validatorUserId)

        // Assert
        assertNotNull(result)
        assertTrue(result.valid)
        assertEquals(TicketStatus.VALIDATED, result.ticket.status)
    }

    @Test
    fun `should throw TicketNotFoundException when ticket not found by QR`() {
        // Arrange
        every { ticketRepository.findByQrCode("INVALID_QR") } returns null

        // Act & Assert
        assertThrows<TicketNotFoundException> {
            validationService.scanTicket("INVALID_QR", 200L)
        }
    }

    @Test
    fun `should throw TicketNotFoundException when ticket not found by ID`() {
        // Arrange
        every { ticketRepository.findById(999L) } returns null

        // Act & Assert
        assertThrows<TicketNotFoundException> {
            validationService.validateTicket(999L, 200L)
        }
    }

    @Test
    fun `should throw InvalidTicketStatusTransitionException when ticket is PENDING_PAYMENT`() {
        // Arrange
        val pendingTicket = testTicket.copy(status = TicketStatus.PENDING_PAYMENT)
        every { ticketRepository.findById(1L) } returns pendingTicket

        // Act & Assert
        assertThrows<InvalidTicketStatusTransitionException> {
            validationService.validateTicket(1L, 200L)
        }
    }

    @Test
    fun `should throw InvalidTicketStatusTransitionException when ticket is already VALIDATED`() {
        // Arrange
        val validatedTicket = testTicket.copy(status = TicketStatus.VALIDATED)
        every { ticketRepository.findById(1L) } returns validatedTicket

        // Act & Assert
        assertThrows<InvalidTicketStatusTransitionException> {
            validationService.validateTicket(1L, 200L)
        }
    }

    @Test
    fun `should throw InvalidTicketStatusTransitionException when ticket is REVOKED`() {
        // Arrange
        val revokedTicket = testTicket.copy(status = TicketStatus.REVOKED)
        every { ticketRepository.findById(1L) } returns revokedTicket

        // Act & Assert
        assertThrows<InvalidTicketStatusTransitionException> {
            validationService.validateTicket(1L, 200L)
        }
    }

    @Test
    fun `should throw InvalidTicketStatusTransitionException when ticket is EXPIRED`() {
        // Arrange
        val expiredTicket = testTicket.copy(status = TicketStatus.EXPIRED)
        every { ticketRepository.findById(1L) } returns expiredTicket

        // Act & Assert
        assertThrows<InvalidTicketStatusTransitionException> {
            validationService.validateTicket(1L, 200L)
        }
    }

    @Test
    fun `should update validatedAt timestamp when validating ticket`() {
        // Arrange
        val ticketId = 1L
        val validatorUserId = 200L

        every { ticketRepository.findById(ticketId) } returns testTicket
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = validationService.validateTicket(ticketId, validatorUserId)

        // Assert
        assertNotNull(result.ticket.validatedAt)
        assertTrue(result.ticket.validatedAt.isAfter(testTicket.purchasedAt))
    }

    @Test
    fun `should update updatedAt timestamp when validating ticket`() {
        // Arrange
        val ticketId = 1L
        val validatorUserId = 200L

        every { ticketRepository.findById(ticketId) } returns testTicket
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = validationService.validateTicket(ticketId, validatorUserId)

        // Assert
        assertNotNull(result.ticket.updatedAt)
        assertTrue(result.ticket.updatedAt.isAfter(testTicket.createdAt))
    }

    @Test
    fun `should return success message when validation succeeds`() {
        // Arrange
        every { ticketRepository.findById(1L) } returns testTicket
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = validationService.validateTicket(1L, 200L)

        // Assert
        assertEquals("Boleta validada exitosamente", result.message)
    }
}
