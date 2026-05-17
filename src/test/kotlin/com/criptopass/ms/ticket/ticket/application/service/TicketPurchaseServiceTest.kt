package com.criptopass.ms.ticket.ticket.application.service

import com.criptopass.ms.ticket.ticket.application.port.out.BlockchainPort
import com.criptopass.ms.ticket.ticket.application.port.out.EventPort
import com.criptopass.ms.ticket.ticket.application.port.out.PaymentPort
import com.criptopass.ms.ticket.ticket.application.port.out.TicketRepositoryPort
import com.criptopass.ms.ticket.ticket.application.port.out.TicketTypeRepositoryPort
import com.criptopass.ms.ticket.ticket.domain.exception.InsufficientTicketsException
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

class TicketPurchaseServiceTest {

    private val ticketRepository = mockk<TicketRepositoryPort>()
    private val ticketTypeRepository = mockk<TicketTypeRepositoryPort>()
    private val eventPort = mockk<EventPort>()
    private val paymentPort = mockk<PaymentPort>()
    private val blockchainPort = mockk<BlockchainPort>()

    private lateinit var purchaseService: TicketPurchaseService

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

    private val testEvent = EventSummary(
        id = 1L,
        name = "Test Concert",
        startDate = Instant.now().plusSeconds(86400),
        venueName = "Test Stadium",
    )

    @BeforeEach
    fun setUp() {
        purchaseService = TicketPurchaseService(
            ticketRepository = ticketRepository,
            ticketTypeRepository = ticketTypeRepository,
            eventPort = eventPort,
            paymentPort = paymentPort,
            blockchainPort = blockchainPort,
        )
    }

    @Test
    fun `should purchase tickets successfully when stock is available`() {
        // Arrange
        val quantity = 2
        val userId = 100L
        val userEmail = "user@test.com"
        val paymentPreferenceId = "pref_123456"

        every { ticketTypeRepository.findById(1L) } returns testTicketType
        every { eventPort.getEventById(1L) } returns testEvent
        every { paymentPort.createOrder(any(), any(), any(), any(), any(), any()) } returns "pref_123456"
        every { ticketRepository.saveAll(any()) } answers {
            val tickets = firstArg<List<Ticket>>()
            tickets.map { it.copy(id = System.nanoTime()) }
        }
        every { blockchainPort.registerToken(any(), any()) } returns 123L
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = purchaseService.purchaseTicket(1L, quantity, userId, userEmail)

        // Assert
        assertNotNull(result)
        assertEquals(quantity, result.quantity)
        assertEquals(paymentPreferenceId, result.paymentPreferenceId)
        assertEquals("PENDING_PAYMENT", result.status)
        assertEquals(testTicketType.price * quantity, result.totalAmount)

        verify(exactly = 1) { ticketRepository.saveAll(any()) }
        verify(exactly = quantity) { blockchainPort.registerToken(any(), any()) }
    }

    @Test
    fun `should throw InsufficientTicketsException when requested quantity exceeds available`() {
        // Arrange
        val quantity = 51 // More than availableQuantity (50)
        val userId = 100L
        val userEmail = "user@test.com"

        every { ticketTypeRepository.findById(1L) } returns testTicketType

        // Act & Assert
        assertThrows<InsufficientTicketsException> {
            purchaseService.purchaseTicket(1L, quantity, userId, userEmail)
        }

        verify(exactly = 0) { paymentPort.createOrder(any(), any(), any(), any(), any(), any()) }
        verify(exactly = 0) { ticketRepository.saveAll(any()) }
    }

    @Test
    fun `should throw TicketNotFoundException when ticket type not found`() {
        // Arrange
        every { ticketTypeRepository.findById(999L) } returns null

        // Act & Assert
        assertThrows<TicketNotFoundException> {
            purchaseService.purchaseTicket(999L, 1, 100L, "user@test.com")
        }
    }

    @Test
    fun `should throw TicketNotFoundException when event not found`() {
        // Arrange
        every { ticketTypeRepository.findById(1L) } returns testTicketType
        every { eventPort.getEventById(1L) } returns null

        // Act & Assert
        assertThrows<TicketNotFoundException> {
            purchaseService.purchaseTicket(1L, 1, 100L, "user@test.com")
        }

        verify(exactly = 0) { paymentPort.createOrder(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `should continue purchase when blockchain registration fails`() {
        // Arrange
        val quantity = 1
        val userId = 100L
        val userEmail = "user@test.com"
        val paymentPreferenceId = "pref_123456"
        val savedTicket = Ticket(
            id = 1L,
            event = testEvent,
            ticketType = testTicketType,
            ownerId = userId,
            ownerEmail = userEmail,
            price = testTicketType.price,
            status = TicketStatus.PENDING_PAYMENT,
            purchasedAt = Instant.now(),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

        every { ticketTypeRepository.findById(1L) } returns testTicketType
        every { eventPort.getEventById(1L) } returns testEvent
        every { paymentPort.createOrder(any(), any(), any(), any(), any(), any()) } returns "pref_123456"
        every { ticketRepository.saveAll(any()) } returns listOf(savedTicket)
        every { blockchainPort.registerToken(any(), any()) } throws RuntimeException("Blockchain unavailable")
        every { ticketRepository.save(any()) } returns savedTicket

        // Act
        val result = purchaseService.purchaseTicket(1L, quantity, userId, userEmail)

        // Assert - Purchase should succeed despite blockchain failure
        assertNotNull(result)
        assertEquals("PENDING_PAYMENT", result.status)
    }

    @Test
    fun `should create tickets with PENDING_PAYMENT status`() {
        // Arrange
        val quantity = 1
        val userId = 100L
        val userEmail = "user@test.com"
        val paymentPreferenceId = "pref_123456"

        every { ticketTypeRepository.findById(1L) } returns testTicketType
        every { eventPort.getEventById(1L) } returns testEvent
        every { paymentPort.createOrder(any(), any(), any(), any(), any(), any()) } returns "pref_123456"
        every { ticketRepository.saveAll(any()) } answers {
            val tickets = firstArg<List<Ticket>>()
            tickets.map { it.copy(id = System.nanoTime()) }
        }
        every { blockchainPort.registerToken(any(), any()) } returns 123L
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        purchaseService.purchaseTicket(1L, quantity, userId, userEmail)

        // Assert - Verify tickets are created with correct status
        verify {
            ticketRepository.saveAll(withArg { tickets ->
                assertTrue(tickets.all { it.status == TicketStatus.PENDING_PAYMENT })
                assertTrue(tickets.all { it.ownerId == userId })
                assertTrue(tickets.all { it.ownerEmail == userEmail })
            })
        }
    }

    @Test
    fun `should calculate total amount correctly`() {
        // Arrange
        val quantity = 3
        val userId = 100L
        val userEmail = "user@test.com"
        val paymentPreferenceId = "pref_123456"
        val expectedTotal = testTicketType.price * quantity

        every { ticketTypeRepository.findById(1L) } returns testTicketType
        every { eventPort.getEventById(1L) } returns testEvent
        every { paymentPort.createOrder(any(), any(), any(), any(), any(), any()) } returns "pref_123456"
        every { ticketRepository.saveAll(any()) } answers {
            val tickets = firstArg<List<Ticket>>()
            tickets.map { it.copy(id = System.nanoTime()) }
        }
        every { blockchainPort.registerToken(any(), any()) } returns 123L
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result = purchaseService.purchaseTicket(1L, quantity, userId, userEmail)

        // Assert
        assertEquals(expectedTotal, result.totalAmount)
    }

    @Test
    fun `should generate unique order ID for each purchase`() {
        // Arrange
        val userId = 100L
        val userEmail = "user@test.com"
        val paymentPreferenceId = "pref_123456"

        every { ticketTypeRepository.findById(1L) } returns testTicketType
        every { eventPort.getEventById(1L) } returns testEvent
        every { paymentPort.createOrder(any(), any(), any(), any(), any(), any()) } returns "pref_123456"
        every { ticketRepository.saveAll(any()) } answers {
            val tickets = firstArg<List<Ticket>>()
            tickets.map { it.copy(id = System.nanoTime()) }
        }
        every { blockchainPort.registerToken(any(), any()) } returns 123L
        every { ticketRepository.save(any()) } answers { firstArg() }

        // Act
        val result1 = purchaseService.purchaseTicket(1L, 1, userId, userEmail)
        Thread.sleep(10) // Small delay to ensure different timestamps
        val result2 = purchaseService.purchaseTicket(1L, 1, userId, userEmail)

        // Assert
        assertNotNull(result1.orderId)
        assertNotNull(result2.orderId)
        // Order IDs should be different because they contain timestamp
        assertTrue(!result1.orderId.equals(result2.orderId), "Order IDs should be different")
    }
}
