package com.example.reservationsystem.service

import com.example.reservationsystem.dto.response.PurchaseResult
import com.example.reservationsystem.enums.ReservationStatus
import com.example.reservationsystem.exception.ConflictException
import com.example.reservationsystem.exception.NotFoundException
import com.example.reservationsystem.exception.OrderProcessingException
import com.example.reservationsystem.repository.RedisReservationRepository
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class PurchaseService(
    private val redisReservationRepository: RedisReservationRepository,
    private val orderService: OrderService,
) {

    fun processPurchase(seatNumber: String, userId: String): PurchaseResult.Success {
        val reservationResult = redisReservationRepository.reserveReservation(
            seatNumber, userId, Duration.ofMinutes(15)
        )
        when (reservationResult.status) {
            ReservationStatus.SUCCESS -> {
                try {
                    val idempotencyKey = UUID.randomUUID().toString()
                    val order = orderService.createPendingOrder(
                        seatNumber = seatNumber, userId = userId, price = reservationResult.price
                    )
                    return PurchaseResult.Success(order, idempotencyKey)
                } catch (e: Exception) {
                    redisReservationRepository.cancelReservation(seatNumber, userId)
                    throw OrderProcessingException("주문 생성 중 오류가 발생했습니다: ${e.message}")
                }
            }

            ReservationStatus.SEAT_NOT_EXISTS -> throw NotFoundException("존재하지 않는 좌석입니다.")
            ReservationStatus.USER_ALREADY_RESERVED -> throw ConflictException("이미 예매하신 사용자입니다.")
            ReservationStatus.SEAT_ALREADY_TAKEN -> throw ConflictException("이미 선점된 좌석입니다.")
        }
    }
}
