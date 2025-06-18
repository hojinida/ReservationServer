package com.example.reservationsystem.dto.request

data class PaymentConfirmRequest(
    val orderUid: String, val amount: Long, val idempotencyKey: String
)
