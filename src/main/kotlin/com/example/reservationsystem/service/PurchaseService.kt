package com.example.reservationsystem.service

import com.example.reservationsystem.dto.response.PurchaseResult
import com.example.reservationsystem.enums.ReservationStatus
import com.example.reservationsystem.repository.IdempotencyRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class PurchaseService(
    private val seatReservationService: SeatReservationService,
    private val orderService: OrderService,
    private val idempotencyRepository: IdempotencyRepository,
    private val signatureService: SignatureService
) {

    fun processPurchase(seatNumber: String, userId: String): PurchaseResult {
        val reservationResult = seatReservationService.reserveSeat(seatNumber, userId)

        return when (reservationResult.status) {
            ReservationStatus.SUCCESS -> {
                try {
                    val idempotencyKey = UUID.randomUUID().toString()

                    val order = orderService.createPendingOrder(
                        seatNumber = seatNumber,
                        userId = userId,
                        price = reservationResult.price
                    )

                    val dataToSign = "${order.orderUid}:${order.amount}:${idempotencyKey}"
                    val signature = signatureService.generate(dataToSign)
                    idempotencyRepository.saveResult(idempotencyKey, "PAYMENT_PENDING", Duration.ofMinutes(15))
                    PurchaseResult.Success(order, idempotencyKey, signature)
                } catch (e: Exception) {
                    seatReservationService.cancelReservation(seatNumber, userId)
                    PurchaseResult.OrderCreationFailed(e.message ?: "주문 생성 실패")
                }
            }
            ReservationStatus.SEAT_NOT_EXISTS -> PurchaseResult.SeatNotExists
            ReservationStatus.USER_ALREADY_RESERVED -> PurchaseResult.UserAlreadyReserved
            ReservationStatus.SEAT_ALREADY_TAKEN -> PurchaseResult.SeatAlreadyTaken
        }
    }
}
