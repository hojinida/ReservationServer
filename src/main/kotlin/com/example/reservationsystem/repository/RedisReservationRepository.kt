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
    private val reserveSeatScript: DefaultRedisScript<List<Any>> =
        DefaultRedisScript<List<Any>>().apply {
            setLocation(ClassPathResource("scripts/reserve-seat-atomically.lua"))
            resultType = List::class.java as Class<List<Any>>
        }

    private val completePaymentScript = DefaultRedisScript<String>().apply {
        setLocation(ClassPathResource("scripts/complete-payment.lua"))
        resultType = String::class.java
    }

    private val cancelPaymentScript = DefaultRedisScript<String>().apply {
        setLocation(ClassPathResource("scripts/cancel-payment.lua"))
        resultType = String::class.java
    }

    private val saveIdempotencyScript = DefaultRedisScript<String>().apply {
        setLocation(ClassPathResource("scripts/save-idempotency.lua"))
        resultType = String::class.java
    }

    private val userReservationShards = 100

    fun reserveSeat(seatNumber: String, userId: String, lockDuration: Duration): ReservationResult {
        val reservedUsersKey = getShardedUserKey(userId)
        val seatKey = "seat:$seatNumber"

        val result = redisTemplate.execute(
            reserveSeatScript,
            listOf(reservedUsersKey, seatKey),
            userId,
            lockDuration.toMillis().toString()
        )

        return ReservationResult.from(result)
    }

    // 결제 완료 처리 (원자적)
    fun completePayment(idempotencyKey: String, seatNumber: String): String {
        return redisTemplate.execute(
            completePaymentScript,
            listOf("idempotency:$idempotencyKey", "seat:$seatNumber"),
            "결제 성공 처리 완료",
            "21600"
        ) ?: throw RuntimeException("레디스 연산 실패")
    }

    // 결제 취소/실패 처리 (원자적)
    fun cancelPayment(
        idempotencyKey: String, seatNumber: String, userId: String, message: String
    ): String {
        val reservedUsersKey = getShardedUserKey(userId)

        return redisTemplate.execute(
            cancelPaymentScript,
            listOf("idempotency:$idempotencyKey", "seat:$seatNumber", reservedUsersKey),
            userId,
            message,
            "21600"
        ) ?: throw RuntimeException("레디스 연산 실패")
    }

    fun saveIdempotencyIfNotExists(idempotencyKey: String, result: String): String {
        return redisTemplate.execute(
            saveIdempotencyScript, listOf("idempotency:$idempotencyKey"), result, "21600"
        ) ?: throw RuntimeException("레디스 연산 실패")
    }

    fun getIdempotency(idempotencyKey: String): String? {
        return redisTemplate.opsForValue().get(idempotencyKey)
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
