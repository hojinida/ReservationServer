package com.example.reservationsystem.dto.response

import com.example.reservationsystem.domain.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

sealed class PurchaseResult {
    data class Success(val order: Order, val idempotencyKey: String, val signature: String) :
        PurchaseResult()

    object SeatNotExists : PurchaseResult()
    object UserAlreadyReserved : PurchaseResult()
    object SeatAlreadyTaken : PurchaseResult()
    data class OrderCreationFailed(val message: String) : PurchaseResult()
}

fun PurchaseResult.toResponseEntity(): ResponseEntity<Any> {
    return when (this) {
        is PurchaseResult.Success -> {
            val redirectUrl =
                "http://localhost:8080/mock-payment-page?orderUid=${this.order.orderUid}&amount=${this.order.amount}&idempotencyKey=${this.idempotencyKey}&signature=${this.signature}"
            val headers = HttpHeaders()
            headers.add("Location", redirectUrl)
            ResponseEntity(headers, HttpStatus.FOUND)
        }

        is PurchaseResult.SeatNotExists -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("존재하지 않는 좌석입니다.")

        is PurchaseResult.UserAlreadyReserved -> ResponseEntity.status(HttpStatus.CONFLICT)
            .body("이미 예매하신 사용자입니다.")

        is PurchaseResult.SeatAlreadyTaken -> ResponseEntity.status(HttpStatus.CONFLICT)
            .body("이미 선점된 좌석입니다.")

        is PurchaseResult.OrderCreationFailed -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("주문 생성에 실패했습니다: ${message}")
    }
}
