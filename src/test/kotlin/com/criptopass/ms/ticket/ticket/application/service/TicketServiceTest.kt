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
    fun `listMyTickets should return paged results`() {
        every {
            ticketRepository.findByOwnerId(100L, 0, 20, null, null)
        } returns TicketPage(listOf(testTicket), 0, 20, 1L, 1)

        val result = ticketService.listMyTickets(100L, 0, 20)
        assertEquals(1, result.totalElements)
    }
}
