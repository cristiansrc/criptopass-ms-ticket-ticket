package com.criptopass.ms.ticket.ticket.domain.model

import com.criptopass.ms.ticket.ticket.domain.exception.InvalidTicketStatusTransitionException
import java.time.Instant

/**
 * Entidad central del dominio que representa una boleta digital.
 *
 * Las transiciones de estado están encapsuladas en métodos de dominio
 * que validan el estado actual antes de permitir el cambio.
 */
data class Ticket(
    val id: Long? = null,
    val event: EventSummary,
    val ticketType: TicketType,
    val ownerId: Long,
    val ownerEmail: String,
    val price: Double,
    val status: TicketStatus,
    val qrCode: String? = null,
    val blockchainTokenId: Long? = null,
    val blockchainTxHash: String? = null,
    val seatNumber: String? = null,
    val purchasedAt: Instant? = null,
    val validatedAt: Instant? = null,
    val validatedBy: Long? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {

    /**
     * Transiciona la boleta de PENDING_PAYMENT a ACTIVE
     * cuando el pago es confirmado.
     */
    fun activate(): Ticket {
        requireCurrentStatus(TicketStatus.PENDING_PAYMENT, TicketStatus.ACTIVE)
        return copy(
            status = TicketStatus.ACTIVE,
            updatedAt = Instant.now(),
        )
    }

    /**
     * Transiciona la boleta a VALIDATED cuando es escaneada en la entrada del evento.
     */
    fun validate(validatorUserId: Long): Ticket {
        requireCurrentStatus(
            TicketStatus.ACTIVE,
            TicketStatus.VALIDATED,
            description = "Solo boletas ACTIVE pueden ser validadas",
        )
        return copy(
            status = TicketStatus.VALIDATED,
            validatedAt = Instant.now(),
            validatedBy = validatorUserId,
            updatedAt = Instant.now(),
        )
    }

    /**
     * Transiciona la boleta a TRANSFERRED cuando el propietario la transfiere.
     * El nuevo ticket (ACTIVE) para el destinatario se crea en el servicio de aplicación.
     */
    fun transfer(): Ticket {
        requireCurrentStatus(
            TicketStatus.ACTIVE,
            TicketStatus.TRANSFERRED,
            description = "Solo boletas ACTIVE pueden ser transferidas",
        )
        return copy(
            status = TicketStatus.TRANSFERRED,
            updatedAt = Instant.now(),
        )
    }

    /**
     * Transiciona la boleta a REVOKED (revocación administrativa).
     */
    fun revoke(): Ticket {
        requireCurrentStatus(
            TicketStatus.ACTIVE,
            TicketStatus.REVOKED,
            description = "Solo boletas ACTIVE pueden ser revocadas",
        )
        return copy(
            status = TicketStatus.REVOKED,
            updatedAt = Instant.now(),
        )
    }

    /**
     * Transiciona la boleta de PENDING_PAYMENT a EXPIRED
     * cuando el timeout de pago se cumple.
     */
    fun expire(): Ticket {
        requireCurrentStatus(TicketStatus.PENDING_PAYMENT, TicketStatus.EXPIRED)
        return copy(
            status = TicketStatus.EXPIRED,
            updatedAt = Instant.now(),
        )
    }

    /**
     * Verifica si el usuario es el propietario de esta boleta.
     */
    fun isOwnedBy(userId: Long): Boolean = this.ownerId == userId

    /**
     * Verifica si la boleta está en un estado que permite generar QR code.
     */
    fun canGenerateQrCode(): Boolean = status == TicketStatus.ACTIVE || status == TicketStatus.TRANSFERRED

    private fun requireCurrentStatus(
        vararg validTransitions: Pair<TicketStatus, TicketStatus>,
        description: String? = null,
    ) {
        val isValid = validTransitions.any { (from, _) -> this.status == from }
        if (!isValid) {
            throw InvalidTicketStatusTransitionException(
                ticketId = id,
                currentState = status,
                message = description
                        ?: "No se puede transicionar desde el estado ${status.name}",
            )
        }
    }

    private fun requireCurrentStatus(
        expectedStatus: TicketStatus,
        targetStatus: TicketStatus,
        description: String? = null,
    ) {
        if (this.status != expectedStatus) {
            throw InvalidTicketStatusTransitionException(
                ticketId = id,
                currentState = status,
                message = description
                        ?: "No se puede transicionar a $targetStatus desde ${status.name}. Se requiere $expectedStatus",
            )
        }
    }
}
