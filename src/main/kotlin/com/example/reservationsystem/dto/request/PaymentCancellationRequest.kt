package com.example.reservationsystem.dto.request

data class PaymentCancellationRequest(
    val orderUid: String, val amount: Long, val idempotencyKey: String
)
