package com.example.reservationsystem.dto.request

data class CancelRequest(
    val userId: String, val orderUId: String
)
