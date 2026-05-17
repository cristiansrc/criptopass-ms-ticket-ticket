package com.criptopass.ms.ticket.ticket.adapter.`in`.rest

import com.criptopass.ms.ticket.ticket.domain.exception.InsufficientTicketsException
import com.criptopass.ms.ticket.ticket.domain.exception.InvalidTicketStatusTransitionException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketAlreadyValidatedException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotFoundException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotOwnedByUserException
import com.criptopass.ms.ticket.ticket.domain.exception.TicketNotTransferableException
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // === 404 - Not Found ===

    @ExceptionHandler(TicketNotFoundException::class)
    fun handleTicketNotFound(
        exception: TicketNotFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.NOT_FOUND,
            error = "NOT_FOUND",
            code = "TICKET_NOT_FOUND",
            message = exception.message ?: "Boleta no encontrada",
            request = request,
        )
    }

    // === 403 - Forbidden ===

    @ExceptionHandler(TicketNotOwnedByUserException::class)
    fun handleTicketNotOwned(
        exception: TicketNotOwnedByUserException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.FORBIDDEN,
            error = "FORBIDDEN",
            code = "TICKET_NOT_OWNED",
            message = exception.message ?: "No tienes permisos sobre esta boleta",
            request = request,
        )
    }

    // === 409 - Conflict ===

    @ExceptionHandler(InvalidTicketStatusTransitionException::class)
    fun handleInvalidTransition(
        exception: InvalidTicketStatusTransitionException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.CONFLICT,
            error = "CONFLICT",
            code = "INVALID_STATUS_TRANSITION",
            message = exception.message ?: "Transición de estado inválida",
            request = request,
        )
    }

    @ExceptionHandler(TicketAlreadyValidatedException::class)
    fun handleAlreadyValidated(
        exception: TicketAlreadyValidatedException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.CONFLICT,
            error = "CONFLICT",
            code = "TICKET_ALREADY_VALIDATED",
            message = exception.message ?: "La boleta ya fue validada",
            request = request,
        )
    }

    @ExceptionHandler(TicketNotTransferableException::class)
    fun handleNotTransferable(
        exception: TicketNotTransferableException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.CONFLICT,
            error = "CONFLICT",
            code = "TICKET_NOT_TRANSFERABLE",
            message = exception.message ?: "La boleta no es transferible",
            request = request,
        )
    }

    @ExceptionHandler(InsufficientTicketsException::class)
    fun handleInsufficientTickets(
        exception: InsufficientTicketsException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.CONFLICT,
            error = "CONFLICT",
            code = "INSUFFICIENT_TICKETS",
            message = exception.message ?: "Boletas insuficientes",
            request = request,
        )
    }

    // === 400 - Bad Request ===

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        val details = exception.bindingResult.fieldErrors.map { error ->
            ApiErrorDetail(
                field = error.field,
                code = "FIELD_INVALID",
                message = error.defaultMessage ?: "Campo inválido",
                rejectedValue = error.rejectedValue?.toString(),
            )
        }

        return buildError(
            status = HttpStatus.BAD_REQUEST,
            error = "BAD_REQUEST",
            code = "VALIDATION_ERROR",
            message = "Error de validación en los campos de la solicitud",
            request = request,
            details = details,
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        exception: ConstraintViolationException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        val details = exception.constraintViolations.map { violation ->
            ApiErrorDetail(
                field = violation.propertyPath.toString(),
                code = "CONSTRAINT_VIOLATION",
                message = violation.message,
                rejectedValue = violation.invalidValue?.toString(),
            )
        }

        return buildError(
            status = HttpStatus.BAD_REQUEST,
            error = "BAD_REQUEST",
            code = "VALIDATION_ERROR",
            message = "Error de validación",
            request = request,
            details = details,
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMalformedJson(
        exception: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.BAD_REQUEST,
            error = "BAD_REQUEST",
            code = "INVALID_REQUEST_BODY",
            message = "El cuerpo de la solicitud contiene JSON inválido",
            request = request,
        )
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(
        exception: MissingServletRequestParameterException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.BAD_REQUEST,
            error = "BAD_REQUEST",
            code = "MISSING_PARAMETER",
            message = "Parámetro requerido faltante: ${exception.parameterName}",
            request = request,
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        exception: MethodArgumentTypeMismatchException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.BAD_REQUEST,
            error = "BAD_REQUEST",
            code = "INVALID_PARAMETER",
            message = "Valor inválido para el parámetro: ${exception.name}",
            request = request,
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        exception: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.BAD_REQUEST,
            error = "BAD_REQUEST",
            code = "INVALID_ARGUMENT",
            message = exception.message ?: "Argumento inválido",
            request = request,
        )
    }

    // === 401 - Unauthorized ===

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthentication(
        exception: AuthenticationException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.UNAUTHORIZED,
            error = "UNAUTHORIZED",
            code = "AUTHENTICATION_REQUIRED",
            message = "Autenticación requerida",
            request = request,
        )
    }

    // === 403 - Forbidden (Spring Security) ===

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        exception: AccessDeniedException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.FORBIDDEN,
            error = "FORBIDDEN",
            code = "ACCESS_DENIED",
            message = "No tienes permisos para acceder a este recurso",
            request = request,
        )
    }

    // === 405 - Method Not Allowed ===

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(
        exception: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.METHOD_NOT_ALLOWED,
            error = "METHOD_NOT_ALLOWED",
            code = "METHOD_NOT_ALLOWED",
            message = "Método HTTP no soportado: ${request.method}",
            request = request,
        )
    }

    // === 415 - Unsupported Media Type ===

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleUnsupportedMediaType(
        exception: HttpMediaTypeNotSupportedException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        return buildError(
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            error = "UNSUPPORTED_MEDIA_TYPE",
            code = "UNSUPPORTED_MEDIA_TYPE",
            message = "Tipo de contenido no soportado",
            request = request,
        )
    }

    // === 500 - Internal Server Error (Fallback) ===

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(
        exception: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        logger.error("Error no controlado. trace_id={} path={}", MDC.get("trace_id"), request.requestURI, exception)

        return buildError(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            error = "INTERNAL_ERROR",
            code = "INTERNAL_ERROR",
            message = "Ha ocurrido un error inesperado",
            request = request,
        )
    }

    private fun buildError(
        status: HttpStatus,
        error: String,
        code: String,
        message: String,
        request: HttpServletRequest,
        details: List<ApiErrorDetail> = emptyList(),
    ): ResponseEntity<ApiErrorResponse> {
        val response = ApiErrorResponse(
            timestamp = Instant.now(),
            status = status.value(),
            error = error,
            code = code,
            message = message,
            path = request.requestURI,
            traceId = MDC.get("trace_id") ?: "",
            details = details,
        )
        return ResponseEntity.status(status).body(response)
    }
}

data class ApiErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val code: String,
    val message: String,
    val path: String,
    @JsonProperty("trace_id")
    val traceId: String,
    val details: List<ApiErrorDetail> = emptyList(),
)

data class ApiErrorDetail(
    val field: String? = null,
    val code: String,
    val message: String,
    @JsonProperty("rejected_value")
    val rejectedValue: Any? = null,
)
