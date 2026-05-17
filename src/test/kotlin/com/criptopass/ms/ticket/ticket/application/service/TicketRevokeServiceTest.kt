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

class TicketRevokeServiceTest {

    private val ticketRepository = mockk<TicketRepositoryPort>()
    private lateinit var revokeService: TicketRevokeService

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
        purchasedAt = Instant.now(),
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    @BeforeEach
    fun setUp() {
        revokeService = TicketRevokeService(ticketRepository)
    }

    @Test
    fun `should revoke ticket successfully when status is ACTIVE`() {
        // Arrange
        val ticketId = 1L
        val adminUserId = 200L

        every { ticketRepository.findById(ticketId) } returns testTicket
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = revokeService.revokeTicket(ticketId, adminUserId)

        // Assert
        assertNotNull(result)
        assertEquals(TicketStatus.REVOKED, result.status)
        assertEquals(ticketId, result.id)

        verify { ticketRepository.save(withArg { ticket ->
            assertEquals(TicketStatus.REVOKED, ticket.status)
        }) }
    }

    @Test
    fun `should revoke ticket successfully when status is TRANSFERRED`() {
        // Arrange
        val transferredTicket = testTicket.copy(status = TicketStatus.TRANSFERRED)
        val ticketId = 1L
        val adminUserId = 200L

        every { ticketRepository.findById(ticketId) } returns transferredTicket
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = revokeService.revokeTicket(ticketId, adminUserId)

        // Assert
        assertNotNull(result)
        assertEquals(TicketStatus.REVOKED, result.status)
    }

    @Test
    fun `should revoke ticket successfully when status is PENDING_PAYMENT`() {
        // Arrange
        val pendingTicket = testTicket.copy(status = TicketStatus.PENDING_PAYMENT)
        val ticketId = 1L
        val adminUserId = 200L

        every { ticketRepository.findById(ticketId) } returns pendingTicket
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = revokeService.revokeTicket(ticketId, adminUserId)

        // Assert
        assertNotNull(result)
        assertEquals(TicketStatus.REVOKED, result.status)
    }

    @Test
    fun `should throw TicketNotFoundException when ticket not found`() {
        // Arrange
        every { ticketRepository.findById(999L) } returns null

        // Act & Assert
        assertThrows<TicketNotFoundException> {
            revokeService.revokeTicket(999L, 200L)
        }
    }

    @Test
    fun `should throw InvalidTicketStatusTransitionException when ticket is already REVOKED`() {
        // Arrange
        val revokedTicket = testTicket.copy(status = TicketStatus.REVOKED)
        every { ticketRepository.findById(1L) } returns revokedTicket

        // Act & Assert
        assertThrows<InvalidTicketStatusTransitionException> {
            revokeService.revokeTicket(1L, 200L)
        }
    }

    @Test
    fun `should throw InvalidTicketStatusTransitionException when ticket is VALIDATED`() {
        // Arrange
        val validatedTicket = testTicket.copy(status = TicketStatus.VALIDATED)
        every { ticketRepository.findById(1L) } returns validatedTicket

        // Act & Assert
        assertThrows<InvalidTicketStatusTransitionException> {
            revokeService.revokeTicket(1L, 200L)
        }
    }

    @Test
    fun `should update updatedAt timestamp when revoking ticket`() {
        // Arrange
        val ticketId = 1L
        val adminUserId = 200L

        every { ticketRepository.findById(ticketId) } returns testTicket
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = revokeService.revokeTicket(ticketId, adminUserId)

        // Assert
        assertNotNull(result.updatedAt)
        assertTrue(result.updatedAt.isAfter(testTicket.createdAt))
    }

    @Test
    fun `should preserve ticket data when revoking`() {
        // Arrange
        val ticketId = 1L
        val adminUserId = 200L

        every { ticketRepository.findById(ticketId) } returns testTicket
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = revokeService.revokeTicket(ticketId, adminUserId)

        // Assert
        assertEquals(testTicket.id, result.id)
        assertEquals(testTicket.event, result.event)
        assertEquals(testTicket.ticketType, result.ticketType)
        assertEquals(testTicket.ownerId, result.ownerId)
        assertEquals(testTicket.ownerEmail, result.ownerEmail)
        assertEquals(testTicket.price, result.price)
    }

    @Test
    fun `should handle revocation when ticket status is EXPIRED`() {
        // Arrange
        val expiredTicket = testTicket.copy(status = TicketStatus.EXPIRED)
        val ticketId = 1L
        val adminUserId = 200L

        every { ticketRepository.findById(ticketId) } returns expiredTicket
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act - EXPIRED tickets can be revoked based on current implementation
        val result = revokeService.revokeTicket(ticketId, adminUserId)

        // Assert
        assertEquals(TicketStatus.REVOKED, result.status)
    }
}
