package com.example.reservationsystem.service

import com.example.reservationsystem.dto.response.ReservationResult
import com.example.reservationsystem.repository.RedisReservationRepository
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class SeatReservationService(
    private val redisReservationRepository: RedisReservationRepository
) {
    fun reserveSeat(seatNumber: String, userId: String): ReservationResult {
        return redisReservationRepository.reserveSeat(
            seatNumber = seatNumber,
            userId = userId,
            lockDuration = Duration.ofMinutes(10)
        )
    }

    fun cancelReservation(seatNumber: String, userId: String) {
        redisReservationRepository.cancelReservation(seatNumber, userId)
    }

    fun completeReservation(seatNumber: String) {
        redisReservationRepository.persistReservation(seatNumber)
    }
}
