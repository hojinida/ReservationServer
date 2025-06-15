package com.example.reservationsystem.dto.response

import com.example.reservationsystem.enums.ReservationStatus

data class ReservationResult(
    val status: ReservationStatus,
    val price: Long = 0L
) {
    companion object {
        fun from(result: List<Any>?): ReservationResult {
            if (result == null || result.size < 2) {
                throw IllegalArgumentException("잘못된 Lua 스크립트 결과")
            }

            val statusCode = (result[0] as Number).toLong()
            val price = (result[1] as Number).toLong()

            val status = when (statusCode) {
                1L -> ReservationStatus.SUCCESS
                0L -> ReservationStatus.SEAT_ALREADY_TAKEN
                -1L -> ReservationStatus.USER_ALREADY_RESERVED
                -2L -> ReservationStatus.SEAT_NOT_EXISTS
                else -> throw IllegalArgumentException("알 수 없는 결과 코드: $statusCode")
            }

            return ReservationResult(status, price)
        }
    }
}
