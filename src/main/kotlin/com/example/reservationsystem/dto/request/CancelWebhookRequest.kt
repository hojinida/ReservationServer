package com.example.reservationsystem.dto.request

data class CancelWebhookRequest(
    val idempotencyKey: String,
    val orderUid: String,
    val paymentKey: String,
    val status: String,
)
