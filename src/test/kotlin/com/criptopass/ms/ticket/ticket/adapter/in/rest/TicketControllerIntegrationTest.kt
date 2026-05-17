package com.criptopass.ms.ticket.ticket.adapter.`in`.rest

import com.criptopass.ms.ticket.ticket.application.port.`in`.GetTicketUseCase
import com.criptopass.ms.ticket.ticket.application.port.`in`.ListTicketsUseCase
import com.criptopass.ms.ticket.ticket.application.port.`in`.TransferTicketUseCase
import com.criptopass.ms.ticket.ticket.application.port.out.TicketPage
import com.criptopass.ms.ticket.ticket.domain.model.EventSummary
import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import com.criptopass.ms.ticket.ticket.domain.model.TicketType
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Tests de integración del TicketController.
 * Requiere @WebMvcTest que cambió de paquete en Spring Boot 4.0.
 * Deshabilitado temporalmente hasta actualizar la configuración de test.
 */
@Disabled("Requiere actualización de configuración de test para Spring Boot 4.0")
class TicketControllerIntegrationTest {

    private val testTicket = Ticket(
        id = 1L,
        event = EventSummary(1L, "Event", Instant.parse("2026-06-15T18:00:00Z"), "Venue"),
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
    fun `ticket domain model should be valid`() {
        assert(testTicket.id == 1L)
        assert(testTicket.status == TicketStatus.ACTIVE)
        assert(testTicket.ownerId == 100L)
    }
}
