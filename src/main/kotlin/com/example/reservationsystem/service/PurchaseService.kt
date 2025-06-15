package com.example.reservationsystem.service

import com.example.reservationsystem.dto.response.PurchaseResult
import com.example.reservationsystem.enums.ReservationStatus
import com.example.reservationsystem.exception.ConflictException
import com.example.reservationsystem.exception.NotFoundException
import com.example.reservationsystem.exception.OrderProcessingException
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

    fun processPurchase(seatNumber: String, userId: String): PurchaseResult.Success {
        val reservationResult = seatReservationService.reserveSeat(seatNumber, userId)

        when (reservationResult.status) {
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
                    return PurchaseResult.Success(order, idempotencyKey, signature)
                } catch (e: Exception) {
                    seatReservationService.cancelReservation(seatNumber, userId)
                    throw OrderProcessingException("주문 생성 중 오류가 발생했습니다: ${e.message}") as Throwable
                }
            }
            ReservationStatus.SEAT_NOT_EXISTS -> throw NotFoundException("존재하지 않는 좌석입니다.") as Throwable
            ReservationStatus.USER_ALREADY_RESERVED -> throw ConflictException("이미 예매하신 사용자입니다.") as Throwable
            ReservationStatus.SEAT_ALREADY_TAKEN -> throw ConflictException("이미 선점된 좌석입니다.") as Throwable
        }
    }
}
