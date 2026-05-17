package com.criptopass.ms.ticket.ticket.application.service

import com.criptopass.ms.ticket.ticket.application.dto.request.PaymentConfirmationWebhookRequest
import com.criptopass.ms.ticket.ticket.application.port.out.PaymentOrderData
import com.criptopass.ms.ticket.ticket.application.port.out.PaymentOrderRepositoryPort
import com.criptopass.ms.ticket.ticket.application.port.out.TicketRepositoryPort
import com.criptopass.ms.ticket.ticket.domain.model.EventSummary
import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import com.criptopass.ms.ticket.ticket.domain.model.TicketType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PaymentWebhookServiceTest {

    private val ticketRepository = mockk<TicketRepositoryPort>()
    private val paymentOrderRepository = mockk<PaymentOrderRepositoryPort>()
    private lateinit var webhookService: PaymentWebhookService

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
        status = TicketStatus.PENDING_PAYMENT,
        purchasedAt = Instant.now(),
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    @BeforeEach
    fun setUp() {
        webhookService = PaymentWebhookService(
            ticketRepository = ticketRepository,
            paymentOrderRepository = paymentOrderRepository,
        )
    }

    @Test
    fun `should activate tickets when payment is APPROVED`() {
        // Arrange
        val request = PaymentConfirmationWebhookRequest(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "APPROVED",
            paidAt = Instant.now(),
        )

        every { paymentOrderRepository.findByPaymentId("pref_abc123") } returns null
        every { ticketRepository.findByOrderId("ORD-123") } returns listOf(testTicket)
        every { ticketRepository.saveAll(any()) } answers { firstArg() }
        every { paymentOrderRepository.save(any()) } returns PaymentOrderData(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "PROCESSED",
            processedAt = Instant.now(),
        )

        // Act
        val result = webhookService.processPaymentConfirmation(request)

        // Assert
        assertNotNull(result)
        assertTrue(result.accepted)
        assertEquals(1, result.ticketsActivated)
        assertEquals("ORD-123", result.orderId)

        verify { ticketRepository.saveAll(withArg { tickets ->
            assertTrue(tickets.all { it.status == TicketStatus.ACTIVE })
            assertTrue(tickets.all { it.qrCode != null })
        }) }

        verify { paymentOrderRepository.save(withArg { order ->
            assertEquals("pref_abc123", order.paymentId)
            assertEquals("PROCESSED", order.status)
        }) }
    }

    @Test
    fun `should mark tickets as EXPIRED when payment is REJECTED`() {
        // Arrange
        val request = PaymentConfirmationWebhookRequest(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "REJECTED",
            paidAt = Instant.now(),
        )

        every { paymentOrderRepository.findByPaymentId("pref_abc123") } returns null
        every { ticketRepository.findByOrderId("ORD-123") } returns listOf(testTicket)
        every { ticketRepository.saveAll(any()) } answers { firstArg() }
        every { paymentOrderRepository.save(any()) } returns PaymentOrderData(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "REJECTED",
            processedAt = Instant.now(),
        )

        // Act
        val result = webhookService.processPaymentConfirmation(request)

        // Assert
        assertNotNull(result)
        assertTrue(result.accepted)
        assertEquals(0, result.ticketsActivated)

        verify { ticketRepository.saveAll(withArg { tickets ->
            assertTrue(tickets.all { it.status == TicketStatus.EXPIRED })
        }) }

        verify { paymentOrderRepository.save(withArg { order ->
            assertEquals("REJECTED", order.status)
        }) }
    }

    @Test
    fun `should return idempotent result when payment_id was already processed`() {
        // Arrange
        val request = PaymentConfirmationWebhookRequest(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "APPROVED",
            paidAt = Instant.now(),
        )

        val existingOrder = PaymentOrderData(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "PROCESSED",
            processedAt = Instant.now(),
        )

        every { paymentOrderRepository.findByPaymentId("pref_abc123") } returns existingOrder

        // Act
        val result = webhookService.processPaymentConfirmation(request)

        // Assert
        assertNotNull(result)
        assertFalse(result.accepted)
        assertEquals(0, result.ticketsActivated)
        assertTrue(result.message!!.contains("ya procesada"))

        verify(exactly = 0) { ticketRepository.findByOrderId(any()) }
        verify(exactly = 0) { ticketRepository.saveAll(any()) }
        verify(exactly = 0) { paymentOrderRepository.save(any()) }
    }

    @Test
    fun `should handle empty ticket list gracefully`() {
        // Arrange
        val request = PaymentConfirmationWebhookRequest(
            orderId = "ORD-999",
            paymentId = "pref_abc123",
            status = "APPROVED",
            paidAt = Instant.now(),
        )

        every { paymentOrderRepository.findByPaymentId("pref_abc123") } returns null
        every { ticketRepository.findByOrderId("ORD-999") } returns emptyList()
        every { paymentOrderRepository.save(any()) } returns PaymentOrderData(
            orderId = "ORD-999",
            paymentId = "pref_abc123",
            status = "PROCESSED",
            processedAt = Instant.now(),
        )

        // Act
        val result = webhookService.processPaymentConfirmation(request)

        // Assert
        assertNotNull(result)
        assertTrue(result.accepted)
        assertEquals(0, result.ticketsActivated)
        assertTrue(result.message!!.contains("No se encontraron tickets"))
    }

    @Test
    fun `should mark tickets as EXPIRED when payment is REFUNDED`() {
        // Arrange
        val request = PaymentConfirmationWebhookRequest(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "REFUNDED",
            paidAt = Instant.now(),
        )

        every { paymentOrderRepository.findByPaymentId("pref_abc123") } returns null
        every { ticketRepository.findByOrderId("ORD-123") } returns listOf(testTicket)
        every { ticketRepository.saveAll(any()) } answers { firstArg() }
        every { paymentOrderRepository.save(any()) } returns PaymentOrderData(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "REFUNDED",
            processedAt = Instant.now(),
        )

        // Act
        val result = webhookService.processPaymentConfirmation(request)

        // Assert
        assertNotNull(result)
        assertTrue(result.accepted)
        assertEquals(0, result.ticketsActivated)
        assertTrue(result.message!!.lowercase().contains("refunded"))

        verify { ticketRepository.saveAll(withArg { tickets ->
            assertTrue(tickets.all { it.status == TicketStatus.EXPIRED })
        }) }
    }

    @Test
    fun `should only activate PENDING_PAYMENT tickets`() {
        // Arrange
        val activeTicket = testTicket.copy(id = 2L, status = TicketStatus.ACTIVE)
        val pendingTicket = testTicket.copy(id = 1L, status = TicketStatus.PENDING_PAYMENT)

        val request = PaymentConfirmationWebhookRequest(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "APPROVED",
            paidAt = Instant.now(),
        )

        every { paymentOrderRepository.findByPaymentId("pref_abc123") } returns null
        every { ticketRepository.findByOrderId("ORD-123") } returns listOf(pendingTicket, activeTicket)
        every { ticketRepository.saveAll(any()) } answers { firstArg() }
        every { paymentOrderRepository.save(any()) } returns PaymentOrderData(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "PROCESSED",
            processedAt = Instant.now(),
        )

        // Act
        val result = webhookService.processPaymentConfirmation(request)

        // Assert - Service counts all ACTIVE tickets after processing (including pre-existing ones)
        assertEquals(2, result.ticketsActivated)

        verify { ticketRepository.saveAll(withArg { tickets ->
            val pendingActivated = tickets.find { it.id == 1L }
            val alreadyActive = tickets.find { it.id == 2L }
            assertEquals(TicketStatus.ACTIVE, pendingActivated?.status)
            assertEquals(TicketStatus.ACTIVE, alreadyActive?.status)
        }) }
    }

    @Test
    fun `should generate QR code for activated tickets`() {
        // Arrange
        val request = PaymentConfirmationWebhookRequest(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "APPROVED",
            paidAt = Instant.now(),
        )

        every { paymentOrderRepository.findByPaymentId("pref_abc123") } returns null
        every { ticketRepository.findByOrderId("ORD-123") } returns listOf(testTicket)
        every { ticketRepository.saveAll(any()) } answers { firstArg() }
        every { paymentOrderRepository.save(any()) } returns PaymentOrderData(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "PROCESSED",
            processedAt = Instant.now(),
        )

        // Act
        val result = webhookService.processPaymentConfirmation(request)

        // Assert
        verify { ticketRepository.saveAll(withArg { tickets ->
            assertTrue(tickets.all { it.qrCode != null })
            assertTrue(tickets.all { it.qrCode!!.startsWith("TICKET:") })
        }) }
    }

    @Test
    fun `should save payment order for idempotency tracking`() {
        // Arrange
        val request = PaymentConfirmationWebhookRequest(
            orderId = "ORD-123",
            paymentId = "pref_unique123",
            status = "APPROVED",
            paidAt = Instant.now(),
        )

        every { paymentOrderRepository.findByPaymentId("pref_unique123") } returns null
        every { ticketRepository.findByOrderId("ORD-123") } returns listOf(testTicket)
        every { ticketRepository.saveAll(any()) } answers { firstArg() }
        every { paymentOrderRepository.save(any()) } returns PaymentOrderData(
            orderId = "ORD-123",
            paymentId = "pref_unique123",
            status = "PROCESSED",
            processedAt = Instant.now(),
        )

        // Act
        webhookService.processPaymentConfirmation(request)

        // Assert
        verify { paymentOrderRepository.save(withArg { order ->
            assertEquals("ORD-123", order.orderId)
            assertEquals("pref_unique123", order.paymentId)
            assertEquals("PROCESSED", order.status)
            assertNotNull(order.processedAt)
        }) }
    }

    @Test
    fun `should handle multiple tickets in single order`() {
        // Arrange
        val tickets = (1..5).map { i ->
            testTicket.copy(id = i.toLong(), status = TicketStatus.PENDING_PAYMENT)
        }

        val request = PaymentConfirmationWebhookRequest(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "APPROVED",
            paidAt = Instant.now(),
        )

        every { paymentOrderRepository.findByPaymentId("pref_abc123") } returns null
        every { ticketRepository.findByOrderId("ORD-123") } returns tickets
        every { ticketRepository.saveAll(any()) } answers { firstArg() }
        every { paymentOrderRepository.save(any()) } returns PaymentOrderData(
            orderId = "ORD-123",
            paymentId = "pref_abc123",
            status = "PROCESSED",
            processedAt = Instant.now(),
        )

        // Act
        val result = webhookService.processPaymentConfirmation(request)

        // Assert
        assertEquals(5, result.ticketsActivated)

        verify { ticketRepository.saveAll(withArg { tickets ->
            assertEquals(5, tickets.size)
            assertTrue(tickets.all { it.status == TicketStatus.ACTIVE })
        }) }
    }
}
