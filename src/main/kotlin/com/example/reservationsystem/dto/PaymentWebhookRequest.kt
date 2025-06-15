package com.example.reservationsystem.dto

data class PaymentWebhookRequest(
    val idempotencyKey: String,
    val orderUid: String,
    val paymentKey: String,
    val status: String
)
