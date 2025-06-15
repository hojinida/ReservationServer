package com.example.reservationsystem.service

import com.example.reservationsystem.dto.request.CancelWebhookRequest
import com.example.reservationsystem.dto.request.PaymentWebhookRequest
import com.example.reservationsystem.repository.IdempotencyRepository
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val orderService: OrderService,
    private val seatReservationService: SeatReservationService,
    private val idempotencyRepository: IdempotencyRepository
) {
    fun processPaymentSuccess(request: PaymentWebhookRequest): String {
        val cachedResult = idempotencyRepository.getResult(request.idempotencyKey)
        if (cachedResult != null) return cachedResult

        val completedOrder = orderService.complete(request.orderUid, request.paymentKey)

        seatReservationService.completeReservation(completedOrder.seatNumber)

        val result = "결제 성공 처리 완료"
        idempotencyRepository.saveResult(request.idempotencyKey, result)
        return result
    }

    fun processPaymentFailure(request: PaymentWebhookRequest): String {
        val cachedResult = idempotencyRepository.getResult(request.idempotencyKey)
        if (cachedResult != null) return cachedResult

        val failedOrder = orderService.fail(request.orderUid)

        seatReservationService.cancelReservation(failedOrder.seatNumber, failedOrder.userId)

        val result = "결제 실패 처리 완료"
        idempotencyRepository.saveResult(request.idempotencyKey, result)
        return result
    }

    fun successfulCancel(request: CancelWebhookRequest): String {
        val cachedResult = idempotencyRepository.getResult(request.idempotencyKey)
        if (cachedResult != null) return cachedResult

        val order = orderService.cancel(request.orderUid)

        seatReservationService.cancelReservation(order.seatNumber, order.userId)

        // 이메일 발송 (가상)

        val result = "취소 성공 처리 완료"
        idempotencyRepository.saveResult(request.idempotencyKey, result)
        return result
    }

    fun failedCancel(request: CancelWebhookRequest): String {
        val cachedResult = idempotencyRepository.getResult(request.idempotencyKey)
        if (cachedResult != null) return cachedResult

        // 이메일 발송 (가상)

        val result = "취소 실패 처리 완료"
        idempotencyRepository.saveResult(request.idempotencyKey, result)
        return result
    }
}
