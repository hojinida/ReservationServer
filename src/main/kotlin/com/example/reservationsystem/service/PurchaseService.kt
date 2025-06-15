package com.example.reservationsystem.service

import com.example.reservationsystem.dto.PurchaseResult
import com.example.reservationsystem.enums.ReservationStatus
import org.springframework.stereotype.Service

@Service
class PurchaseService(
    private val seatReservationService: SeatReservationService,
    private val orderService: OrderService
) {

    fun processPurchase(seatNumber: String, userId: String): PurchaseResult {
        val reservationResult = seatReservationService.reserveSeat(seatNumber, userId)

        return when (reservationResult.status) {
            ReservationStatus.SUCCESS -> {
                try {
                    val order = orderService.createPendingOrder(
                        seatNumber = seatNumber,
                        userId = userId,
                        price = reservationResult.price
                    )
                    PurchaseResult.Success(order)
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
