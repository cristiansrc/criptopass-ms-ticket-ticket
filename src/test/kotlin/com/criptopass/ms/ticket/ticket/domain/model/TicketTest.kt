package com.criptopass.ms.ticket.ticket.domain.model

import com.criptopass.ms.ticket.ticket.domain.exception.InvalidTicketStatusTransitionException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TicketTest {

    private val event = EventSummary(
        id = 1L,
        name = "Test Event",
        startDate = Instant.now(),
        venueName = "Test Venue",
    )

    private val ticketType = TicketType(
        id = 1L,
        eventId = 1L,
        name = "General",
        description = "General Admission",
        price = 50.0,
        quantity = 100,
        availableQuantity = 100,
        maxPerUser = 5,
        createdAt = Instant.now(),
    )

    private fun createTicket(status: TicketStatus = TicketStatus.PENDING_PAYMENT): Ticket = Ticket(
        id = 1L,
        event = event,
        ticketType = ticketType,
        ownerId = 100L,
        ownerEmail = "owner@test.com",
        price = 50.0,
        status = status,
        purchasedAt = Instant.now(),
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    // === PENDING_PAYMENT transitions ===

    @Test
    fun `should activate from PENDING_PAYMENT`() {
        val ticket = createTicket(TicketStatus.PENDING_PAYMENT)
        val activated = ticket.activate()

        assertEquals(TicketStatus.ACTIVE, activated.status)
        assertNotNull(activated.updatedAt)
    }

    @Test
    fun `should expire from PENDING_PAYMENT`() {
        val ticket = createTicket(TicketStatus.PENDING_PAYMENT)
        val expired = ticket.expire()

        assertEquals(TicketStatus.EXPIRED, expired.status)
        assertNotNull(expired.updatedAt)
    }

    @Test
    fun `should throw when activating from non PENDING_PAYMENT state`() {
        val ticket = createTicket(TicketStatus.ACTIVE)
        assertThrows<InvalidTicketStatusTransitionException> {
            ticket.activate()
        }
    }

    // === ACTIVE transitions ===

    @Test
    fun `should validate from ACTIVE`() {
        val ticket = createTicket(TicketStatus.ACTIVE)
        val validated = ticket.validate(validatorUserId = 200L)

        assertEquals(TicketStatus.VALIDATED, validated.status)
        assertNotNull(validated.validatedAt)
        assertEquals(200L, validated.validatedBy)
    }

    @Test
    fun `should transfer from ACTIVE`() {
        val ticket = createTicket(TicketStatus.ACTIVE)
        val transferred = ticket.transfer()

        assertEquals(TicketStatus.TRANSFERRED, transferred.status)
    }

    @Test
    fun `should revoke from ACTIVE`() {
        val ticket = createTicket(TicketStatus.ACTIVE)
        val revoked = ticket.revoke()

        assertEquals(TicketStatus.REVOKED, revoked.status)
    }

    @Test
    fun `should throw when validating from non ACTIVE state`() {
        val ticket = createTicket(TicketStatus.PENDING_PAYMENT)
        assertThrows<InvalidTicketStatusTransitionException> {
            ticket.validate(validatorUserId = 200L)
        }
    }

    @Test
    fun `should throw when transferring from non ACTIVE state`() {
        val ticket = createTicket(TicketStatus.PENDING_PAYMENT)
        assertThrows<InvalidTicketStatusTransitionException> {
            ticket.transfer()
        }
    }

    // === TRANSFERRED transitions ===

    @Test
    fun `should allow transfer from ACTIVE (valid transition)`() {
        val ticket = createTicket(TicketStatus.ACTIVE)
        val transferred = ticket.transfer()

        assertEquals(TicketStatus.TRANSFERRED, transferred.status)
    }

    // === REVOKED (terminal) ===

    @Test
    fun `should throw when trying to validate a revoked ticket`() {
        val ticket = createTicket(TicketStatus.REVOKED)
        assertThrows<InvalidTicketStatusTransitionException> {
            ticket.validate(validatorUserId = 200L)
        }
    }

    @Test
    fun `should throw when trying to transfer a revoked ticket`() {
        val ticket = createTicket(TicketStatus.REVOKED)
        assertThrows<InvalidTicketStatusTransitionException> {
            ticket.transfer()
        }
    }

    @Test
    fun `should throw when trying to revoke a revoked ticket`() {
        val ticket = createTicket(TicketStatus.REVOKED)
        assertThrows<InvalidTicketStatusTransitionException> {
            ticket.revoke()
        }
    }

    // === VALIDATED (terminal) ===

    @Test
    fun `should throw when trying to validate an already validated ticket`() {
        val ticket = createTicket(TicketStatus.VALIDATED)
        assertThrows<InvalidTicketStatusTransitionException> {
            ticket.validate(validatorUserId = 200L)
        }
    }

    // === isOwnedBy ===

    @Test
    fun `should return true when user is owner`() {
        val ticket = createTicket()
        assertTrue(ticket.isOwnedBy(100L))
    }

    @Test
    fun `should return false when user is not owner`() {
        val ticket = createTicket()
        kotlin.test.assertFalse(ticket.isOwnedBy(999L))
    }

    // === canGenerateQrCode ===

    @Test
    fun `should allow QR code generation for ACTIVE and TRANSFERRED tickets`() {
        val activeTicket = createTicket(TicketStatus.ACTIVE)
        val transferredTicket = createTicket(TicketStatus.TRANSFERRED)

        assertTrue(activeTicket.canGenerateQrCode())
        assertTrue(transferredTicket.canGenerateQrCode())
    }

    @Test
    fun `should not allow QR code generation for non-active tickets`() {
        val pendingTicket = createTicket(TicketStatus.PENDING_PAYMENT)
        val validatedTicket = createTicket(TicketStatus.VALIDATED)
        val revokedTicket = createTicket(TicketStatus.REVOKED)
        val expiredTicket = createTicket(TicketStatus.EXPIRED)

        kotlin.test.assertFalse(pendingTicket.canGenerateQrCode())
        kotlin.test.assertFalse(validatedTicket.canGenerateQrCode())
        kotlin.test.assertFalse(revokedTicket.canGenerateQrCode())
        kotlin.test.assertFalse(expiredTicket.canGenerateQrCode())
    }
}
