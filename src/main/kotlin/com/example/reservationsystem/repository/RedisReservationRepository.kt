package com.example.reservationsystem.repository

import com.example.reservationsystem.dto.response.ReservationResult
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Repository
import java.time.Duration
import kotlin.math.abs


@Repository
class RedisReservationRepository(
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val script: DefaultRedisScript<List<Any>> = DefaultRedisScript<List<Any>>().apply {
        setLocation(ClassPathResource("scripts/reserve-seat-atomically.lua"))
        resultType = List::class.java as Class<List<Any>>
    }
    private val userReservationShards = 100

    fun reserveSeat(seatNumber: String, userId: String, lockDuration: Duration): ReservationResult {
        val reservedUsersKey = getShardedUserKey(userId)
        val seatKey = "seat:$seatNumber"

        val result = redisTemplate.execute(
            script, listOf(reservedUsersKey, seatKey), userId, lockDuration.toMillis().toString()
        )

        return ReservationResult.from(result)
    }

    fun cancelReservation(seatNumber: String, userId: String) {
        releaseReservation(seatNumber, userId)
        redisTemplate.opsForSet().remove(getShardedUserKey(userId), userId)
    }

    fun releaseReservation(seatNumber: String, userId: String) {
        val seatInfoKey = "seat:$seatNumber"

        redisTemplate.opsForHash<String, String>().putAll(
            seatInfoKey, mapOf(
                "isReserved" to "false", "reservedBy" to "", "reservedAt" to ""
            )
        )

        redisTemplate.persist(seatInfoKey)
    }

    fun persistReservation(seatNumber: String) {
        val reservationKey = "seat:$seatNumber"
        redisTemplate.persist(reservationKey)
    }

    private fun getShardedUserKey(userId: String): String {
        val shardNumber = abs(userId.hashCode()) % userReservationShards
        return "reserved_users:$shardNumber"
    }
}
