package com.criptopass.ms.ticket.ticket.domain.model

/**
 * Estados del ciclo de vida de una boleta digital en el ecosistema CriptoPass.
 *
 * Diagrama de transiciones:
 *   [*] --> PENDING_PAYMENT
 *   PENDING_PAYMENT --> ACTIVE, EXPIRED
 *   ACTIVE --> VALIDATED, TRANSFERRED, REVOKED
 *   TRANSFERRED --> ACTIVE, VALIDATED, REVOKED
 *   VALIDATED --> [*] (terminal)
 *   REVOKED --> [*] (terminal)
 *   EXPIRED --> [*] (terminal)
 */
enum class TicketStatus {
    PENDING_PAYMENT,
    ACTIVE,
    TRANSFERRED,
    VALIDATED,
    REVOKED,
    EXPIRED;

    /**
     * Indica si este estado es terminal (la boleta no puede cambiar a otro estado).
     */
    fun isTerminal(): Boolean = this in TERMINAL_STATUSES

    companion object {
        private val TERMINAL_STATUSES = setOf(VALIDATED, REVOKED, EXPIRED)
    }
}
