package com.criptopass.ms.ticket.ticket.application.service

import com.criptopass.ms.ticket.ticket.application.port.out.TicketPage
import com.criptopass.ms.ticket.ticket.application.port.out.TicketRepositoryPort
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotFoundException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotOwnedByUserException
import com.criptopass.ms.ticket.ticket.domain.model.EventSummary
import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import com.criptopass.ms.ticket.ticket.domain.model.TicketType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TicketServiceTest {

    private val ticketRepository = mockk<TicketRepositoryPort>()
    private val ticketService = TicketService(ticketRepository)

    private val testTicket = Ticket(
        id = 1L,
        event = EventSummary(1L, "Event", Instant.now(), "Venue"),
        ticketType = TicketType(1L, 1L, "General", null, 50.0, 100, 50, 5, Instant.now()),
        ownerId = 100L,
        ownerEmail = "user@test.com",
        price = 50.0,
        status = TicketStatus.ACTIVE,
        purchasedAt = Instant.now(),
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )

    @Test
    fun `getTicketById should return ticket when owned by user`() {
        every { ticketRepository.findById(1L) } returns testTicket
        val result = ticketService.getTicketById(1L, 100L)
        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals(100L, result.ownerId)
        assertEquals(TicketStatus.ACTIVE, result.status)
    }

    @Test
    fun `getTicketById should throw when ticket not found`() {
        every { ticketRepository.findById(999L) } returns null
        assertThrows<TicketNotFoundException> {
            ticketService.getTicketById(999L, 100L)
        }
    }

    @Test
    fun `getTicketById should throw when user is not owner`() {
        every { ticketRepository.findById(1L) } returns testTicket
        assertThrows<TicketNotOwnedByUserException> {
            ticketService.getTicketById(1L, 999L)
        }
    }

    @Test
    fun `listMyTickets should return paged results with default parameters`() {
        every {
            ticketRepository.findByOwnerId(100L, 0, 20, null, null)
        } returns TicketPage(listOf(testTicket), 0, 20, 1L, 1)

        val result = ticketService.listMyTickets(100L, 0, 20)
        assertEquals(1, result.totalElements)
        assertEquals(1, result.content.size)
    }

    @Test
    fun `listMyTickets should filter by status when provided`() {
        every {
            ticketRepository.findByOwnerId(100L, 0, 10, TicketStatus.ACTIVE, null)
        } returns TicketPage(listOf(testTicket), 0, 10, 1L, 1)

        val result = ticketService.listMyTickets(100L, 0, 10, TicketStatus.ACTIVE)
        assertEquals(1, result.totalElements)
        verify { ticketRepository.findByOwnerId(100L, 0, 10, TicketStatus.ACTIVE, null) }
    }

    @Test
    fun `listMyTickets should filter by eventId when provided`() {
        every {
            ticketRepository.findByOwnerId(100L, 1, 15, null, 5L)
        } returns TicketPage(emptyList(), 1, 15, 0L, 0)

        val result = ticketService.listMyTickets(100L, 1, 15, eventId = 5L)
        assertEquals(0, result.totalElements)
        verify { ticketRepository.findByOwnerId(100L, 1, 15, null, 5L) }
    }

    @Test
    fun `listMyTickets should return empty page when no tickets exist`() {
        every {
            ticketRepository.findByOwnerId(100L, 0, 20, null, null)
        } returns TicketPage(emptyList(), 0, 20, 0L, 0)

        val result = ticketService.listMyTickets(100L, 0, 20)
        assertEquals(0, result.totalElements)
        assertEquals(0, result.content.size)
    }

    @Test
    fun `listEventTickets should return paged results for event`() {
        every {
            ticketRepository.findByEventId(1L, 0, 20, null)
        } returns TicketPage(listOf(testTicket), 0, 20, 1L, 1)

        val result = ticketService.listEventTickets(1L, 0, 20)
        assertEquals(1, result.totalElements)
        assertEquals(1, result.content.size)
    }

    @Test
    fun `listEventTickets should filter by status when provided`() {
        every {
            ticketRepository.findByEventId(1L, 0, 10, TicketStatus.ACTIVE)
        } returns TicketPage(listOf(testTicket), 0, 10, 1L, 1)

        val result = ticketService.listEventTickets(1L, 0, 10, TicketStatus.ACTIVE)
        assertEquals(1, result.totalElements)
        verify { ticketRepository.findByEventId(1L, 0, 10, TicketStatus.ACTIVE) }
    }

    @Test
    fun `listEventTickets should return empty page when event has no tickets`() {
        every {
            ticketRepository.findByEventId(999L, 0, 20, null)
        } returns TicketPage(emptyList(), 0, 20, 0L, 0)

        val result = ticketService.listEventTickets(999L, 0, 20)
        assertEquals(0, result.totalElements)
        assertEquals(0, result.content.size)
    }

    @Test
    fun `listEventTickets should handle pagination correctly`() {
        every {
            ticketRepository.findByEventId(1L, 2, 10, null)
        } returns TicketPage(listOf(testTicket), 2, 10, 50L, 5)

        val result = ticketService.listEventTickets(1L, 2, 10)
        assertEquals(50, result.totalElements)
        assertEquals(5, result.totalPages)
        assertEquals(2, result.page)
    }
}
