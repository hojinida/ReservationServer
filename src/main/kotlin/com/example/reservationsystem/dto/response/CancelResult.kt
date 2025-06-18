package com.example.reservationsystem.dto.response

data class CancelResult (
    val orderUid: String,
    val amount: Long,
    val idempotencyKey: String
)
