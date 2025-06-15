package com.example.reservationsystem.dto.request

data class CancelWebhookRequest(
    override val idempotencyKey: String,
    override val orderUid: String,
    override val amount: Long,
    val paymentKey: String,
    val status: String,
    override val signature: String
) : WebhookRequest
