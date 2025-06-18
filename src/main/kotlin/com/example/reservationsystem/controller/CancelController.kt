package com.example.reservationsystem.controller

import com.example.reservationsystem.client.PaymentApiClient
import com.example.reservationsystem.dto.request.CancelRequest
import com.example.reservationsystem.dto.request.PaymentCancellationRequest
import com.example.reservationsystem.service.CancelService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cancel")
class CancelController(
    private val cancelService: CancelService, private val paymentApiClient: PaymentApiClient
) {
    @PostMapping
    fun requestCancel(@RequestBody request: CancelRequest): ResponseEntity<String> {
        val result =
            cancelService.validateAndGetOrderForCancellation(request.orderUId, request.userId)
        val requestPayload = PaymentCancellationRequest(
            orderUid = result.orderUid,
            amount = result.amount,
            idempotencyKey = result.idempotencyKey
        )
        paymentApiClient.requestPaymentCancel(requestPayload)
        return ResponseEntity.ok("취소 요청이 접수되었습니다.")
    }
}
