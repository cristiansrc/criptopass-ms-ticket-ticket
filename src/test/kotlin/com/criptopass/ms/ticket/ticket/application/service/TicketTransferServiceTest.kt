package com.criptopass.ms.ticket.ticket.application.service

import com.criptopass.ms.ticket.ticket.application.port.out.BlockchainPort
import com.criptopass.ms.ticket.ticket.application.port.out.TicketRepositoryPort
import com.criptopass.ms.ticket.ticket.application.port.out.UserPort
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotFoundException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotTransferableException
import com.criptopass.ms.ticket.ticket.domain.model.EventSummary
import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import com.criptopass.ms.ticket.ticket.domain.model.TicketType
import com.criptopass.ms.ticket.ticket.domain.service.TicketDomainService
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

class TicketTransferServiceTest {

    private val ticketRepository = mockk<TicketRepositoryPort>()
    private val userPort = mockk<UserPort>()
    private val blockchainPort = mockk<BlockchainPort>()
    private val ticketDomainService = TicketDomainService()

    private lateinit var transferService: TicketTransferService

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
        description = "General entry ticket",
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
        blockchainTokenId = 123L,
        purchasedAt = Instant.now(),
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    @BeforeEach
    fun setUp() {
        transferService = TicketTransferService(
            ticketRepository = ticketRepository,
            userPort = userPort,
            blockchainPort = blockchainPort,
            ticketDomainService = ticketDomainService,
        )
    }

    @Test
    fun `should transfer ticket successfully when all conditions are met`() {
        // Arrange
        val ticketId = 1L
        val recipientEmail = "recipient@test.com"
        val currentUserId = 100L
        val recipientId = 200L
        val txHash = "tx_hash_abc123"

        every { ticketRepository.findById(ticketId) } returns testTicket
        every { userPort.getUserIdByEmail(recipientEmail) } returns recipientId
        every { ticketRepository.save(any()) } answers { firstArg() }
        every { blockchainPort.transferToken(any(), any(), any()) } returns txHash

        // Act
        val result = transferService.transferTicket(ticketId, recipientEmail, currentUserId)

        // Assert
        assertNotNull(result)
        assertEquals(TicketStatus.ACTIVE, result.status)
        assertEquals(recipientId, result.ownerId)
        assertEquals(recipientEmail, result.ownerEmail)
        assertEquals(txHash, result.blockchainTxHash)

        verify { ticketRepository.save(withArg { ticket ->
            assertEquals(TicketStatus.TRANSFERRED, ticket.status)
            assertEquals(testTicket.ownerId, ticket.ownerId)
        }) }

        verify { blockchainPort.transferToken(testTicket.blockchainTokenId!!, currentUserId, recipientId) }
    }

    @Test
    fun `should throw TicketNotFoundException when ticket not found`() {
        // Arrange
        every { ticketRepository.findById(999L) } returns null

        // Act & Assert
        assertThrows<TicketNotFoundException> {
            transferService.transferTicket(999L, "recipient@test.com", 100L)
        }
    }

    @Test
    fun `should throw TicketNotTransferableException when ticket is not ACTIVE`() {
        // Arrange
        val pendingTicket = testTicket.copy(status = TicketStatus.PENDING_PAYMENT)
        every { ticketRepository.findById(1L) } returns pendingTicket

        // Act & Assert
        assertThrows<TicketNotTransferableException> {
            transferService.transferTicket(1L, "recipient@test.com", 100L)
        }
    }

    @Test
    fun `should throw TicketNotTransferableException when ticket is VALIDATED`() {
        // Arrange
        val validatedTicket = testTicket.copy(status = TicketStatus.VALIDATED)
        every { ticketRepository.findById(1L) } returns validatedTicket

        // Act & Assert
        assertThrows<TicketNotTransferableException> {
            transferService.transferTicket(1L, "recipient@test.com", 100L)
        }
    }

    @Test
    fun `should throw TicketNotTransferableException when ticket is REVOKED`() {
        // Arrange
        val revokedTicket = testTicket.copy(status = TicketStatus.REVOKED)
        every { ticketRepository.findById(1L) } returns revokedTicket

        // Act & Assert
        assertThrows<TicketNotTransferableException> {
            transferService.transferTicket(1L, "recipient@test.com", 100L)
        }
    }

    @Test
    fun `should throw TicketNotFoundException when recipient user not found`() {
        // Arrange
        every { ticketRepository.findById(1L) } returns testTicket
        every { userPort.getUserIdByEmail("unknown@test.com") } returns null

        // Act & Assert
        assertThrows<TicketNotFoundException> {
            transferService.transferTicket(1L, "unknown@test.com", 100L)
        }
    }

    @Test
    fun `should complete transfer when blockchain transfer fails`() {
        // Arrange
        val ticketId = 1L
        val recipientEmail = "recipient@test.com"
        val currentUserId = 100L
        val recipientId = 200L

        every { ticketRepository.findById(ticketId) } returns testTicket
        every { userPort.getUserIdByEmail(recipientEmail) } returns recipientId
        every { ticketRepository.save(any()) } answers { firstArg() }
        every { blockchainPort.transferToken(any(), any(), any()) } throws RuntimeException("Blockchain error")

        // Act
        val result = transferService.transferTicket(ticketId, recipientEmail, currentUserId)

        // Assert - Transfer should succeed despite blockchain failure
        assertNotNull(result)
        assertEquals(TicketStatus.ACTIVE, result.status)
        assertEquals(recipientId, result.ownerId)
    }

    @Test
    fun `should transfer ticket without blockchain token when token is null`() {
        // Arrange
        val ticketWithoutToken = testTicket.copy(blockchainTokenId = null)
        val ticketId = 1L
        val recipientEmail = "recipient@test.com"
        val currentUserId = 100L
        val recipientId = 200L

        every { ticketRepository.findById(ticketId) } returns ticketWithoutToken
        every { userPort.getUserIdByEmail(recipientEmail) } returns recipientId
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = transferService.transferTicket(ticketId, recipientEmail, currentUserId)

        // Assert
        assertNotNull(result)
        assertEquals(TicketStatus.ACTIVE, result.status)

        verify(exactly = 0) { blockchainPort.transferToken(any(), any(), any()) }
    }

    @Test
    fun `should create new ticket with same price as original`() {
        // Arrange
        val ticketId = 1L
        val recipientEmail = "recipient@test.com"
        val currentUserId = 100L
        val recipientId = 200L

        every { ticketRepository.findById(ticketId) } returns testTicket
        every { userPort.getUserIdByEmail(recipientEmail) } returns recipientId
        every { ticketRepository.save(any()) } answers { firstArg() }
        every { blockchainPort.transferToken(any(), any(), any()) } returns "tx_hash"

        // Act
        val result = transferService.transferTicket(ticketId, recipientEmail, currentUserId)

        // Assert
        assertEquals(testTicket.price, result.price)
    }

    @Test
    fun `should create new ticket with same event and ticket type as original`() {
        // Arrange
        val ticketId = 1L
        val recipientEmail = "recipient@test.com"
        val currentUserId = 100L
        val recipientId = 200L

        every { ticketRepository.findById(ticketId) } returns testTicket
        every { userPort.getUserIdByEmail(recipientEmail) } returns recipientId
        every { ticketRepository.save(any()) } answers { firstArg() }
        every { blockchainPort.transferToken(any(), any(), any()) } returns "tx_hash"

        // Act
        val result = transferService.transferTicket(ticketId, recipientEmail, currentUserId)

        // Assert
        assertEquals(testTicket.event, result.event)
        assertEquals(testTicket.ticketType, result.ticketType)
    }

    @Test
    fun `should throw when user is not ticket owner`() {
        // Arrange
        val nonOwnerId = 999L
        every { ticketRepository.findById(1L) } returns testTicket

        // Act & Assert
        assertThrows<com.criptopass.ms.ticket.ticket.domain.exception.TicketNotOwnedByUserException> {
            transferService.transferTicket(1L, "recipient@test.com", nonOwnerId)
        }
    }

    @Test
    fun `should throw when trying to transfer from TRANSFERRED status`() {
        // Arrange
        val transferredTicket = testTicket.copy(status = TicketStatus.TRANSFERRED)
        val ticketId = 1L
        val recipientEmail = "recipient@test.com"
        val currentUserId = 100L

        every { ticketRepository.findById(ticketId) } returns transferredTicket

        // Act & Assert
        assertThrows<TicketNotTransferableException> {
            transferService.transferTicket(ticketId, recipientEmail, currentUserId)
        }
    }
}
