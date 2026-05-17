package com.criptopass.ms.ticket.ticket.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TicketStatusTest {

    @Test
    fun `should have 6 status values`() {
        val values = TicketStatus.values()
        assertTrue(values.size == 6, "Debe haber exactamente 6 estados")
    }

    @Test
    fun `should contain all expected status values`() {
        val values = TicketStatus.values().toSet()
        assertAll(
            { assertTrue(values.contains(TicketStatus.PENDING_PAYMENT)) },
            { assertTrue(values.contains(TicketStatus.ACTIVE)) },
            { assertTrue(values.contains(TicketStatus.TRANSFERRED)) },
            { assertTrue(values.contains(TicketStatus.VALIDATED)) },
            { assertTrue(values.contains(TicketStatus.REVOKED)) },
            { assertTrue(values.contains(TicketStatus.EXPIRED)) },
        )
    }

    @Test
    fun `VALIDATED REVOKED and EXPIRED should be terminal`() {
        assertAll(
            { assertTrue(TicketStatus.VALIDATED.isTerminal()) },
            { assertTrue(TicketStatus.REVOKED.isTerminal()) },
            { assertTrue(TicketStatus.EXPIRED.isTerminal()) },
        )
    }

    @Test
    fun `PENDING_PAYMENT ACTIVE and TRANSFERRED should not be terminal`() {
        assertAll(
            { assertFalse(TicketStatus.PENDING_PAYMENT.isTerminal()) },
            { assertFalse(TicketStatus.ACTIVE.isTerminal()) },
            { assertFalse(TicketStatus.TRANSFERRED.isTerminal()) },
        )
    }
}
