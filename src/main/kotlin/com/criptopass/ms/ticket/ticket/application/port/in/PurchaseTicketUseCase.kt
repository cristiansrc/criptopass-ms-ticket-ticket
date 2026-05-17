package com.criptopass.ms.ticket.ticket.application.port.`in`

interface PurchaseTicketUseCase {

    fun purchaseTicket(
        ticketTypeId: Long,
        quantity: Int,
        userId: Long,
        userEmail: String,
    ): TicketPurchaseResult
}

data class TicketPurchaseResult(
    val orderId: String,
    val ticketTypeId: Long,
    val quantity: Int,
    val totalAmount: Double,
    val paymentPreferenceId: String,
    val status: String,
)
