package com.example.reservationsystem.service

import com.example.reservationsystem.config.PaymentProperties
import com.example.reservationsystem.domain.OrderStatus
import com.example.reservationsystem.dto.request.PaymentCancellationRequest
import com.example.reservationsystem.exception.BadRequestException
import com.example.reservationsystem.exception.ForbiddenException
import com.example.reservationsystem.repository.OrderRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@Service
class CancelService(
    private val orderRepository: OrderRepository,
    private val signatureService: SignatureService,
    private val paymentProperties: PaymentProperties,
    private val objectMapper: ObjectMapper,
    private val orderService: OrderService,
    webClientBuilder: WebClient.Builder,
    @Qualifier("paymentCancelExecutor") private val paymentCancelExecutor: Executor
) {
    private val webClient = webClientBuilder.baseUrl(paymentProperties.paymentsServerUrl).build()

    fun requestCancellation(orderUid: String, userId: String) {
        val order = orderService.findByOrderUidOrThrow(orderUid)

        if (order.userId != userId) {
            throw ForbiddenException("본인의 예매만 취소할 수 있습니다.") as Throwable
        }

        if (order.status != OrderStatus.PAID) {
            throw BadRequestException("결제 완료된 주문만 취소할 수 있습니다.") as Throwable
        }

        CompletableFuture.runAsync({
            try {
                val idempotencyKeyForCancel = UUID.randomUUID().toString()

                val requestPayload = PaymentCancellationRequest(
                    paymentKey = order.paymentKey
                        ?: throw IllegalStateException("PaymentKey가 존재하지 않습니다."),
                    idempotencyKey = idempotencyKeyForCancel
                )

                val payloadJson = objectMapper.writeValueAsString(requestPayload)
                val signature = signatureService.generate(payloadJson)

                webClient.post().uri("/api/v1/pay/cancel").header("X-Signature", signature)
                    .header("Content-Type", "application/json").bodyValue(payloadJson).retrieve()
                    .bodyToMono(Void::class.java).subscribe()

            } catch (e: Exception) {
                throw RuntimeException("결제 취소 요청 중 오류 발생: ${e.message}", e)
            }
        }, paymentCancelExecutor)
    }
}
