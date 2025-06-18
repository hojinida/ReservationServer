package com.example.reservationsystem.dto.response

import com.example.reservationsystem.domain.Order


sealed class PurchaseResult {
    data class Success(val order: Order, val idempotencyKey: String) : PurchaseResult()
}
