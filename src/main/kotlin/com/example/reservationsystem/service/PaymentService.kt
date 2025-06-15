package com.example.reservationsystem.service

import com.example.reservationsystem.dto.request.CancelWebhookRequest
import com.example.reservationsystem.dto.request.PaymentWebhookRequest
import com.example.reservationsystem.dto.request.WebhookRequest
import com.example.reservationsystem.repository.IdempotencyRepository
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val orderService: OrderService,
    private val seatReservationService: SeatReservationService,
    private val idempotencyRepository: IdempotencyRepository,
    private val signatureService: SignatureService
) {
    fun processPaymentSuccess(request: PaymentWebhookRequest): String {
        return executeWebhookLogic(request) {
            val completedOrder = orderService.complete(request.orderUid, request.paymentKey)
            seatReservationService.completeReservation(completedOrder.seatNumber)
            "결제 성공 처리 완료"
        }
    }

    fun processPaymentFailure(request: PaymentWebhookRequest): String {
        return executeWebhookLogic(request) {
            val failedOrder = orderService.fail(request.orderUid)
            seatReservationService.cancelReservation(failedOrder.seatNumber, failedOrder.userId)
            "결제 실패 처리 완료"
        }
    }

    fun successfulCancel(request: CancelWebhookRequest): String {
        return executeWebhookLogic(request) {
            val order = orderService.cancel(request.orderUid)
            seatReservationService.cancelReservation(order.seatNumber, order.userId)
            "취소 성공 처리 완료"
        }
    }

    fun failedCancel(request: CancelWebhookRequest): String {
        return executeWebhookLogic(request) {
            //이메일 로직
            "취소 실패 처리 완료"
        }
    }


    private fun executeWebhookLogic(request: WebhookRequest, businessLogic: () -> String): String {
        val dataToVerify = "${request.orderUid}:${request.idempotencyKey}"
        if (!signatureService.verify(dataToVerify, request.signature)) {
            throw SecurityException("서명 검증 실패")
        }

        val cachedResult = idempotencyRepository.getResult(request.idempotencyKey)
        if (cachedResult != null) return cachedResult

        val result = businessLogic()

        idempotencyRepository.saveResult(request.idempotencyKey, result)
        return result
    }
}
