package com.example.reservationsystem.dto.request

data class PaymentWebhookRequest(
    override val idempotencyKey: String,
    override val orderUid: String,
    val paymentKey: String,
    val status: String,
    override val signature: String
) : WebhookRequest
