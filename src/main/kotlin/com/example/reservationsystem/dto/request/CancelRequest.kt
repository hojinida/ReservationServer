package com.example.reservationsystem.dto.request

data class CancelRequest(
    val userId: String,
    val orderId: Long
)
