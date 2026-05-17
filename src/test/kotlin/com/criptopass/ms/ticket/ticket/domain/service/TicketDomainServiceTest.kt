package com.criptopass.ms.ticket.ticket.domain.service

import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotFoundException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotOwnedByUserException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotTransferableException
import com.criptopass.ms.ticket.ticket.domain.model.EventSummary
import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import com.criptopass.ms.ticket.ticket.domain.model.TicketType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertTrue

class TicketDomainServiceTest {

    private lateinit var domainService: TicketDomainService
    private lateinit var activeTicket: Ticket
    private lateinit var pendingTicket: Ticket
    private lateinit var validatedTicket: Ticket

    @BeforeEach
    fun setUp() {
        domainService = TicketDomainService()

        val event = EventSummary(1L, "Event", Instant.now(), "Venue")
        val ticketType = TicketType(1L, 1L, "General", null, 50.0, 100, 100, 5, Instant.now())

        activeTicket = Ticket(
            id = 1L, event = event, ticketType = ticketType,
            ownerId = 100L, ownerEmail = "a@b.com", price = 50.0,
            status = TicketStatus.ACTIVE, createdAt = Instant.now(), updatedAt = Instant.now(),
        )

        pendingTicket = activeTicket.copy(id = 2L, status = TicketStatus.PENDING_PAYMENT)
        validatedTicket = activeTicket.copy(id = 3L, status = TicketStatus.VALIDATED)
    }

    @Test
    fun `canTransfer should succeed for ACTIVE ticket owned by user`() {
        domainService.canTransfer(activeTicket, 100L)
    }

    @Test
    fun `canTransfer should throw when user is not owner`() {
        assertThrows<TicketNotOwnedByUserException> {
            domainService.canTransfer(activeTicket, 999L)
        }
    }

    @Test
    fun `canTransfer should throw for non-ACTIVE ticket`() {
        assertThrows<TicketNotTransferableException> {
            domainService.canTransfer(pendingTicket, 100L)
        }
    }

    @Test
    fun `canTransfer should throw for VALIDATED ticket`() {
        assertThrows<TicketNotTransferableException> {
            domainService.canTransfer(validatedTicket, 100L)
        }
    }

    @Test
    fun `canValidate should return true for ACTIVE and TRANSFERRED tickets`() {
        assertTrue(domainService.canValidate(activeTicket))
        val transferred = activeTicket.copy(status = TicketStatus.TRANSFERRED)
        assertTrue(domainService.canValidate(transferred))
    }

    @Test
    fun `canValidate should return false for non-ACTIVE tickets`() {
        kotlin.test.assertFalse(domainService.canValidate(pendingTicket))
        kotlin.test.assertFalse(domainService.canValidate(validatedTicket))
    }

    @Test
    fun `canRevoke should return true for ACTIVE and TRANSFERRED tickets`() {
        assertTrue(domainService.canRevoke(activeTicket))
        val transferred = activeTicket.copy(status = TicketStatus.TRANSFERRED)
        assertTrue(domainService.canRevoke(transferred))
    }

    @Test
    fun `canRevoke should return false for terminal tickets`() {
        kotlin.test.assertFalse(domainService.canRevoke(validatedTicket))
        kotlin.test.assertFalse(domainService.canRevoke(activeTicket.copy(status = TicketStatus.REVOKED)))
    }

    @Test
    fun `checkOwnership should throw when user is not owner`() {
        assertThrows<TicketNotOwnedByUserException> {
            domainService.checkOwnership(activeTicket, 999L)
        }
    }

    @Test
    fun `checkOwnership should pass when user is owner`() {
        domainService.checkOwnership(activeTicket, 100L)
    }

    @Test
    fun `requireTicketFound should return ticket when not null`() {
        val result = domainService.requireTicketFound(activeTicket, 1L)
        assertTrue(result.id == 1L)
    }

    @Test
    fun `requireTicketFound should throw when ticket is null`() {
        assertThrows<TicketNotFoundException> {
            domainService.requireTicketFound(null, 999L)
        }
    }
}
