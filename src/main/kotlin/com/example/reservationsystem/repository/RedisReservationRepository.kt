package com.example.reservationsystem.repository

import com.example.reservationsystem.dto.ReservationResult
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Repository
import java.time.Duration


@Repository
class RedisReservationRepository(
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val script: DefaultRedisScript<List<Any>> = DefaultRedisScript<List<Any>>().apply {
        setLocation(ClassPathResource("scripts/reserve-seat-atomically.lua"))
        resultType = List::class.java as Class<List<Any>>
    }

    fun reserveSeat(seatNumber: String, userId: String, lockDuration: Duration): ReservationResult {
        val reservedUsersKey = "reserved_users"
        val seatKey = "seat:$seatNumber"

        val result = redisTemplate.execute(
            script,
            listOf(reservedUsersKey, seatKey),
            userId,
            lockDuration.toMillis().toString()
        )

        return ReservationResult.from(result)
    }

    fun cancelReservation(seatNumber: String, userId: String) {
        val reservedUsersKey = "reserved_users"
        val seatKey = "seat:$seatNumber"

        redisTemplate.execute { connection ->
            connection.multi()
            connection.keyCommands().del(seatKey.toByteArray())
            connection.setCommands().sRem(reservedUsersKey.toByteArray(), userId.toByteArray())
            connection.exec()
        }
    }

    fun releaseReservation(seatNumber: String, userId: String) {
        val seatInfoKey = "seat:$seatNumber"

        redisTemplate.opsForHash<String, String>().putAll(seatInfoKey, mapOf(
            "isReserved" to "false",
            "reservedBy" to "",
            "reservedAt" to ""
        ))

        redisTemplate.persist(seatInfoKey)
    }

    fun persistReservation(seatNumber: String) {
        val reservationKey = "seat:$seatNumber"
        redisTemplate.persist(reservationKey)
    }
}
