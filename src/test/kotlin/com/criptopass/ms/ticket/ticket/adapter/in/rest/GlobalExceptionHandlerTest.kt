package com.criptopass.ms.ticket.ticket.adapter.`in`.rest

import com.criptopass.ms.ticket.ticket.domain.exception.InsufficientTicketsException
import com.criptopass.ms.ticket.ticket.domain.exception.InvalidTicketStatusTransitionException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotFoundException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotOwnedByUserException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotTransferableException
import com.criptopass.ms.ticket.ticket.domain.model.TicketStatus
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()
    private val request = object : HttpServletRequest by org.springframework.mock.web.MockHttpServletRequest() {
        override fun getRequestURI(): String = "/test/path"
        override fun getMethod(): String = "GET"
    }

    @Test
    fun `handleTicketNotFound should return 404`() {
        val exception = TicketNotFoundException(1L)
        val response = handler.handleTicketNotFound(exception, request)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("NOT_FOUND", response.body?.error)
        assertEquals("TICKET_NOT_FOUND", response.body?.code)
    }

    @Test
    fun `handleTicketNotOwned should return 403`() {
        val exception = TicketNotOwnedByUserException(1L, 100L)
        val response = handler.handleTicketNotOwned(exception, request)
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals("FORBIDDEN", response.body?.error)
    }

    @Test
    fun `handleInvalidTransition should return 409`() {
        val exception = InvalidTicketStatusTransitionException(1L, TicketStatus.PENDING_PAYMENT)
        val response = handler.handleInvalidTransition(exception, request)
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("CONFLICT", response.body?.error)
    }

    @Test
    fun `handleNotTransferable should return 409`() {
        val exception = TicketNotTransferableException(1L, TicketStatus.PENDING_PAYMENT)
        val response = handler.handleNotTransferable(exception, request)
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("TICKET_NOT_TRANSFERABLE", response.body?.code)
    }

    @Test
    fun `handleInsufficientTickets should return 409`() {
        val exception = InsufficientTicketsException(1L, 5, 2)
        val response = handler.handleInsufficientTickets(exception, request)
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("INSUFFICIENT_TICKETS", response.body?.code)
    }

    @Test
    fun `handleUnexpected should return 500`() {
        val exception = RuntimeException("Algo salió mal")
        val response = handler.handleUnexpected(exception, request)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("INTERNAL_ERROR", response.body?.code)
        assertEquals("Ha ocurrido un error inesperado", response.body?.message)
    }

    @Test
    fun `ApiErrorResponse should have trace_id field mapped correctly`() {
        val exception = TicketNotFoundException(1L)
        val response = handler.handleTicketNotFound(exception, request)
        assertEquals("", response.body?.traceId)
    }

    @Test
    fun `ApiErrorResponse should have all required fields`() {
        val exception = TicketNotFoundException(1L)
        val response = handler.handleTicketNotFound(exception, request)
        val body = response.body!!
        assertEquals("/test/path", body.path)
        assertEquals(404, body.status)
        assertNotNull(body.timestamp)
        assertEquals(emptyList<Any>(), body.details)
    }

    private fun assertNotNull(value: Any?) {
        kotlin.test.assertNotNull(value)
    }
}
