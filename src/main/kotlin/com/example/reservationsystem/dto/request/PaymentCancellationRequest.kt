package com.example.reservationsystem.dto.request

data class PaymentCancellationRequest(
    val paymentKey: String, val idempotencyKey: String
)
