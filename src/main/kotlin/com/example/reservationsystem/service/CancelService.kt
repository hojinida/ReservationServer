package com.example.reservationsystem.service

import com.example.reservationsystem.config.PaymentProperties
import com.example.reservationsystem.domain.OrderStatus
import com.example.reservationsystem.dto.request.PaymentCancellationRequest
import com.example.reservationsystem.repository.OrderRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.util.Base64
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Service
class CancelService(
    private val orderRepository: OrderRepository,
    private val signatureService: SignatureService,
    private val paymentProperties: PaymentProperties,
    private val objectMapper: ObjectMapper,
    webClientBuilder: WebClient.Builder
) {
    private val webClient = webClientBuilder.baseUrl(paymentProperties.paymentsServerUrl).build()

    fun requestCancellation(orderUid: String, userId: String) {
        val order = orderRepository.findByOrderUid(orderUid)
            ?: throw IllegalArgumentException("주문을 찾을 수 없습니다.")

        if (order.userId != userId) {
            throw IllegalArgumentException("본인의 예매만 취소할 수 있습니다.")
        }

        if (order.status != OrderStatus.PAID) {
            throw IllegalArgumentException("결제 완료된 주문만 취소할 수 있습니다.")
        }

        CompletableFuture.runAsync {
            try {
                val idempotencyKeyForCancel = UUID.randomUUID().toString()

                val requestPayload = PaymentCancellationRequest(
                    paymentKey = order.paymentKey
                        ?: throw IllegalStateException("PaymentKey가 존재하지 않습니다."),
                    idempotencyKey = idempotencyKeyForCancel
                )

                val payloadJson = objectMapper.writeValueAsString(requestPayload)

                val signature = signatureService.generate(payloadJson)

                webClient.post()
                    .uri("/api/v1/pay/cancel")
                    .header("X-Signature", signature)
                    .header("Content-Type", "application/json")
                    .bodyValue(payloadJson)
                    .retrieve()
                    .bodyToMono(Void::class.java)
                    .subscribe()

            } catch (e: Exception) {
                // 예외 처리 로직 (예: 로그 기록, 알림 등)
                throw RuntimeException("결제 취소 요청 중 오류 발생: ${e.message}", e)
            }
        }
    }
}
