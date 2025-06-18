package com.example.reservationsystem.controller

import com.example.reservationsystem.client.PaymentApiClient
import com.example.reservationsystem.dto.request.PaymentConfirmRequest
import com.example.reservationsystem.dto.request.PurchaseRequest
import com.example.reservationsystem.service.PurchaseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/purchase")
class PurchaseController(
    private val purchaseService: PurchaseService,
    private val paymentApiClient: PaymentApiClient,
) {
    @PostMapping
    fun purchase(@RequestBody request: PurchaseRequest): ResponseEntity<Any> {
        val result = purchaseService.processPurchase(request.seatNumber, request.userId)
        val paymentRequest = PaymentConfirmRequest(
            orderUid = result.order.orderUid,
            amount = result.order.amount,
            idempotencyKey = result.idempotencyKey
        )
        paymentApiClient.requestPaymentConfirm(paymentRequest)
        val response = mapOf(
            "message" to "결제 요청이 성공적으로 접수되었습니다.",
            "orderUid" to result.order.orderUid
        )
        return ResponseEntity.ok(response)
    }
}
