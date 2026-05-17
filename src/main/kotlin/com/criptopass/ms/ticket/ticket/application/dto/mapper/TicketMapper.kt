package com.criptopass.ms.ticket.ticket.application.dto.mapper

import com.criptopass.ms.ticket.ticket.application.dto.response.EventSummaryResponse
import com.criptopass.ms.ticket.ticket.application.dto.response.PagedResponse
import com.criptopass.ms.ticket.ticket.application.dto.response.TicketPurchaseResponse
import com.criptopass.ms.ticket.ticket.application.dto.response.TicketResponse
import com.criptopass.ms.ticket.ticket.application.dto.response.TicketTypeResponse
import com.criptopass.ms.ticket.ticket.application.dto.response.TicketValidationResponse
import com.criptopass.ms.ticket.ticket.application.port.`in`.TicketPurchaseResult
import com.criptopass.ms.ticket.ticket.application.port.`in`.TicketValidationResult
import com.criptopass.ms.ticket.ticket.application.port.out.TicketPage
import com.criptopass.ms.ticket.ticket.domain.model.Ticket
import java.time.Instant

fun Ticket.toResponse(): TicketResponse = TicketResponse(
    id = id ?: throw IllegalStateException("Ticket sin ID persistido"),
    event = EventSummaryResponse(
        id = event.id,
        name = event.name,
        startDate = event.startDate,
        venueName = event.venueName,
    ),
    ticketType = TicketTypeResponse(
        id = ticketType.id,
        eventId = ticketType.eventId,
        name = ticketType.name,
        description = ticketType.description,
        price = ticketType.price,
        quantity = ticketType.quantity,
        availableQuantity = ticketType.availableQuantity,
        maxPerUser = ticketType.maxPerUser,
        createdAt = ticketType.createdAt,
    ),
    ownerId = ownerId,
    ownerEmail = ownerEmail,
    price = price,
    status = status,
    qrCode = qrCode,
    blockchainTokenId = blockchainTokenId,
    blockchainTxHash = blockchainTxHash,
    seatNumber = seatNumber,
    purchasedAt = purchasedAt,
    validatedAt = validatedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun TicketPage.toPagedResponse(): PagedResponse<TicketResponse> = PagedResponse(
    content = content.map { it.toResponse() },
    page = page,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages,
)

fun TicketPurchaseResult.toResponse(): TicketPurchaseResponse = TicketPurchaseResponse(
    orderId = orderId,
    ticketTypeId = ticketTypeId,
    quantity = quantity,
    totalAmount = totalAmount,
    paymentPreferenceId = paymentPreferenceId,
    status = status,
    createdAt = java.time.Instant.now(),
)

fun TicketValidationResult.toResponse(): TicketValidationResponse = TicketValidationResponse(
    valid = valid,
    ticket = ticket.toResponse(),
    message = message,
    validatedAt = validatedAt,
    validatedBy = validatedBy,
)
