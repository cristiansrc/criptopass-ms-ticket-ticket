package com.criptopass.ms.ticket.ticket.domain.service

import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotTransferableException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotOwnedByUserException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotFoundException
import com.criptopass.ms.ticket.ticket.domain.exception.InvalidTicketStatusTransitionException

/**
 * Servicio de dominio puro para reglas de negocio relacionadas con boletas.
 * No tiene dependencias de infraestructura.
 */
class TicketDomainService {

    /**
     * Verifica si un ticket puede ser transferido.
     * Solo tickets ACTIVE pueden ser transferidos.
     */
    fun canTransfer(ticket: Ticket, userId: Long) {
        if (ticket.id == null) {
            throw TicketNotFoundException(null, "Ticket sin ID no puede ser transferido")
        }
        if (!ticket.isOwnedBy(userId)) {
            throw TicketNotOwnedByUserException(ticket.id, userId)
        }
        if (ticket.status != TicketStatus.ACTIVE) {
            throw TicketNotTransferableException(
                ticketId = ticket.id,
                currentState = ticket.status,
            )
        }
    }

    /**
     * Verifica si un ticket puede ser validado (escaneado en entrada).
     */
    fun canValidate(ticket: Ticket): Boolean {
        return ticket.status == TicketStatus.ACTIVE || ticket.status == TicketStatus.TRANSFERRED
    }

    /**
     * Verifica si un ticket puede ser revocado por un administrador.
     */
    fun canRevoke(ticket: Ticket): Boolean {
        return ticket.status == TicketStatus.ACTIVE || ticket.status == TicketStatus.TRANSFERRED
    }

    /**
     * Verifica si un usuario es propietario del ticket o tiene rol ADMIN.
     */
    fun checkOwnership(ticket: Ticket, userId: Long) {
        if (!ticket.isOwnedBy(userId)) {
            throw TicketNotOwnedByUserException(ticket.id, userId)
        }
    }

    /**
     * Verifica si un ticket existe (no es null).
     */
    fun requireTicketFound(ticket: Ticket?, ticketId: Long): Ticket {
        return ticket ?: throw TicketNotFoundException(ticketId)
    }
}
