package com.example.reservationsystem.controller

import com.example.reservationsystem.dto.request.CancelWebhookRequest
import com.example.reservationsystem.dto.request.PaymentWebhookRequest
import com.example.reservationsystem.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/webhooks")
class PaymentWebhookController(
    private val paymentService: PaymentService
) {
    @PostMapping("/payment")
    fun handlePaymentWebhook(@RequestBody request: PaymentWebhookRequest): ResponseEntity<String> {
        try {
            val result = when (request.status.uppercase()) {
                "SUCCESS" -> paymentService.processPaymentSuccess(request)
                "FAILURE" -> paymentService.processPaymentFailure(request)
                else -> return ResponseEntity.badRequest().body("알 수 없는 상태값입니다: ${request.status}")
            }
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body("웹훅 처리 중 오류 발생: ${e.message}")
        }
    }

    @PostMapping("/cancel")
    fun handleCancelWebhook(@RequestBody request: CancelWebhookRequest): ResponseEntity<String> {
        try {
            val result = when (request.status.uppercase()) {
                "SUCCESS" -> paymentService.successfulCancel(request)
                "FAILURE" -> paymentService.failedCancel(request)
                else -> return ResponseEntity.badRequest().body("알 수 없는 상태값입니다: ${request.status}")
            }
            return ResponseEntity.ok(result)
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body("웹훅 처리 중 오류 발생: ${e.message}")
        }
    }
}
