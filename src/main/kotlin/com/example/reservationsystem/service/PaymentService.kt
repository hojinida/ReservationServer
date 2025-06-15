package com.example.reservationsystem.service

import com.example.reservationsystem.dto.request.CancelWebhookRequest
import com.example.reservationsystem.dto.request.PaymentWebhookRequest
import com.example.reservationsystem.dto.request.WebhookRequest
import com.example.reservationsystem.repository.RedisReservationRepository
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val orderService: OrderService,
    private val signatureService: SignatureService,
    private val redisReservationRepository: RedisReservationRepository
) {
    fun processPaymentSuccess(request: PaymentWebhookRequest, signature: String): String {
        verifySignature(request, signature)

        val order = orderService.findByOrderUidOrThrow(request.orderUid)

        val result = try {
            redisReservationRepository.completePayment(
                idempotencyKey = request.idempotencyKey,
                seatNumber = order.seatNumber
            )
        } catch (e: Exception) {
            throw RuntimeException("결제 처리 중 오류가 발생했습니다")
        }

        if (result == "결제 성공 처리 완료") {
            try {
                orderService.complete(request.orderUid)
            } catch (e: Exception) {
                throw e
            }
        }

        return result
    }

    fun processPaymentFailure(request: PaymentWebhookRequest, signature: String): String {
        verifySignature(request, signature)

        val order = orderService.findByOrderUidOrThrow(request.orderUid)

        val result = try {
            redisReservationRepository.cancelPayment(
                idempotencyKey = request.idempotencyKey,
                seatNumber = order.seatNumber,
                userId = order.userId,
                message = "결제 실패 처리 완료"
            )
        } catch (e: Exception) {
            throw RuntimeException("결제 실패 처리 중 오류가 발생했습니다")
        }

        if (result == "결제 실패 처리 완료") {
            try {
                orderService.fail(request.orderUid)
            } catch (e: Exception) {
                throw e
            }
        }

        return result
    }

    fun successfulCancel(request: CancelWebhookRequest, signature: String): String {
        verifySignature(request, signature)

        val order = orderService.findByOrderUidOrThrow(request.orderUid)

        val result = try {
            redisReservationRepository.cancelPayment(
                idempotencyKey = request.idempotencyKey,
                seatNumber = order.seatNumber,
                userId = order.userId,
                message = "취소 성공 처리 완료"
            )
        } catch (e: Exception) {
            throw RuntimeException("취소 처리 중 오류가 발생했습니다")
        }

        if (result == "취소 성공 처리 완료") {
            try {
                orderService.cancel(request.orderUid)
            } catch (e: Exception) {
                throw e
            }
        }

        return result
    }

    fun failedCancel(request: CancelWebhookRequest, signature: String): String {
        verifySignature(request, signature)

        val result = try {
            redisReservationRepository.saveIdempotencyIfNotExists(
                idempotencyKey = request.idempotencyKey,
                result = "취소 실패 처리 완료"
            )
        } catch (e: Exception) {
            throw RuntimeException("취소 실패 처리 중 오류가 발생했습니다")
        }

        return result
    }

    private fun verifySignature(request: WebhookRequest, signature: String) {
        val dataToVerify = "${request.orderUid}:${request.amount}:${request.idempotencyKey}"
        if (!signatureService.verify(dataToVerify, signature)) {
            throw SecurityException("서명 검증 실패")
        }
    }
}
