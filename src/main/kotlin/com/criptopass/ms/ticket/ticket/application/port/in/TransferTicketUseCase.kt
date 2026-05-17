package com.criptopass.ms.ticket.ticket.application.port.`in`

import com.criptopass.ms.ticket.ticket.domain.model.Ticket

interface TransferTicketUseCase {

    /**
     * Transfiere una boleta a otro usuario.
     * El ticket original pasa a TRANSFERRED y se crea un nuevo ticket ACTIVE para el destinatario.
     * @return el NUEVO ticket del destinatario (ACTIVE)
     */
    fun transferTicket(ticketId: Long, recipientEmail: String, currentUserId: Long): Ticket
}
