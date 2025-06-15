package com.example.reservationsystem.controller

import com.example.reservationsystem.client.PaymentApiClient
import com.example.reservationsystem.dto.request.CancelRequest
import com.example.reservationsystem.dto.request.PaymentCancellationRequest
import com.example.reservationsystem.service.CancelService
import com.example.reservationsystem.service.SignatureService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cancel")
class CancelController(
    private val cancelService: CancelService,
    private val paymentApiClient: PaymentApiClient,
    private val signatureService: SignatureService,
    private val objectMapper: ObjectMapper
) {
    @PostMapping
    fun requestCancel(@RequestBody request: CancelRequest): ResponseEntity<String> {
        val order = cancelService.validateAndGetOrderForCancellation(request.orderUId, request.userId)
        val idempotencyKeyForCancel = "cancel-${order.orderUid}"
        val requestPayload = PaymentCancellationRequest(
            orderUid = order.orderUid,
            amount = order.amount,
            idempotencyKey = idempotencyKeyForCancel
        )
        val payloadJson = objectMapper.writeValueAsString(requestPayload)
        val signature = signatureService.generate(payloadJson)
        paymentApiClient.requestPaymentCancel(requestPayload, signature)
        return ResponseEntity.ok("취소 요청이 접수되었습니다.")
    }
}
