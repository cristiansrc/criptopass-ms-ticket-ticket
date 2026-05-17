package com.criptopass.ms.ticket.ticket.application.service

import com.criptopass.ms.ticket.ticket.application.port.`in`.TransferTicketUseCase
import com.criptopass.ms.ticket.ticket.application.port.out.BlockchainPort
import com.criptopass.ms.ticket.ticket.application.port.out.TicketRepositoryPort
import com.criptopass.ms.ticket.ticket.application.port.out.UserPort
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotFoundException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotTransferableException
import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import com.criptopass.ms.ticket.ticket.domain.service.TicketDomainService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class TicketTransferService(
    private val ticketRepository: TicketRepositoryPort,
    private val userPort: UserPort,
    private val blockchainPort: BlockchainPort,
    private val ticketDomainService: TicketDomainService,
) : TransferTicketUseCase {

    override fun transferTicket(ticketId: Long, recipientEmail: String, currentUserId: Long): Ticket {
        val ticket = ticketRepository.findById(ticketId)
            ?: throw TicketNotFoundException(ticketId)

        // Verificar propiedad
        ticketDomainService.checkOwnership(ticket, currentUserId)

        // Verificar estado transferible
        if (ticket.status != TicketStatus.ACTIVE) {
            throw TicketNotTransferableException(
                ticketId = ticketId,
                currentState = ticket.status,
            )
        }

        // Buscar destinatario
        val recipientId = userPort.getUserIdByEmail(recipientEmail)
            ?: throw TicketNotFoundException(null, "Usuario no encontrado con email: $recipientEmail")

        val now = Instant.now()

        // Actualizar ticket original a TRANSFERRED
        val transferredTicket = ticket.transfer()
        ticketRepository.save(transferredTicket)

        // Crear nuevo ticket ACTIVE para el destinatario
        val newTicket = Ticket(
            event = ticket.event,
            ticketType = ticket.ticketType,
            ownerId = recipientId,
            ownerEmail = recipientEmail,
            price = ticket.price,
            status = TicketStatus.ACTIVE,
            qrCode = null,
            blockchainTokenId = ticket.blockchainTokenId,
            purchasedAt = ticket.purchasedAt,
            createdAt = now,
            updatedAt = now,
        )

        val savedNewTicket = ticketRepository.save(newTicket)

        // Transferir en blockchain si hay token
        if (ticket.blockchainTokenId != null) {
            try {
                val txHash = blockchainPort.transferToken(
                    tokenId = ticket.blockchainTokenId!!,
                    fromOwnerId = currentUserId,
                    toOwnerId = recipientId,
                )
                val updated = savedNewTicket.copy(blockchainTxHash = txHash)
                return ticketRepository.save(updated)
            } catch (e: Exception) {
                // Si blockchain falla, el ticket se transfiere igual
                // El token se puede transferir después
            }
        }

        return savedNewTicket
    }
}
