package com.example.reservationsystem.dto.request

interface WebhookRequest {
    val orderUid: String
    val amount: Long
}
