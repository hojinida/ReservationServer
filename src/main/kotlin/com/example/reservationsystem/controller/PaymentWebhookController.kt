package com.example.reservationsystem.controller

import com.example.reservationsystem.dto.request.CancelWebhookRequest
import com.example.reservationsystem.dto.request.PaymentWebhookRequest
import com.example.reservationsystem.exception.BadRequestException
import com.example.reservationsystem.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/webhooks")
class PaymentWebhookController(
    private val paymentService: PaymentService
) {
    @PostMapping("/payment")
    fun handlePaymentWebhook(
        @RequestBody request: PaymentWebhookRequest,
        @RequestHeader("X-Signature") signature: String
    ): ResponseEntity<String> {
        val result = when (request.status.uppercase()) {
            "SUCCESS" -> paymentService.processPaymentSuccess(request, signature)
            "FAILURE" -> paymentService.processPaymentFailure(request, signature)
            else -> throw BadRequestException("알 수 없는 상태값입니다: ${request.status}")
        }
        return ResponseEntity.ok(result)
    }

    @PostMapping("/cancel")
    fun handleCancelWebhook(
        @RequestBody request: CancelWebhookRequest,
        @RequestHeader("X-Signature") signature: String
    ): ResponseEntity<String> {
        val result = when (request.status.uppercase()) {
            "SUCCESS" -> paymentService.successfulCancel(request, signature)
            "FAILURE" -> paymentService.failedCancel(request, signature)
            else -> throw BadRequestException("알 수 없는 상태값입니다: ${request.status}")
        }
        return ResponseEntity.ok(result)
    }
}
