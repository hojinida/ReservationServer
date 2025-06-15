package com.example.reservationsystem.dto.request

interface WebhookRequest {
    val idempotencyKey: String
    val orderUid: String
    val signature: String
}
