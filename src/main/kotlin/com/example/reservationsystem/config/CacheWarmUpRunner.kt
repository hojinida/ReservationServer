package com.example.reservationsystem.config

import com.example.reservationsystem.repository.SeatRepository
import org.redisson.api.RedissonClient
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import kotlin.concurrent.withLock

@Component
class CacheWarmUpRunner(
    private val seatRepository: SeatRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val redissonClient: RedissonClient
) : ApplicationRunner {
    private val lockKey = "cache-warm-up-lock"

    override fun run(args: ApplicationArguments?) {
        val lock = redissonClient.getLock(lockKey)

        try {
            val isLocked = lock.tryLock(0, 10, TimeUnit.MINUTES)

            if (!isLocked) {
                return
            }
            clearUserReservations()

            val allSeats = seatRepository.findAll()
            val hashOps = redisTemplate.opsForHash<String, String>()

            redisTemplate.executePipelined { connection ->
                allSeats.forEach { seat ->
                    val key = "seat:${seat.seatNumber}"
                    val map = mapOf(
                        "amount" to seat.amount.toString(),
                        "isReserved" to "false",
                        "reservedBy" to "",
                        "reservedAt" to ""
                    )
                    hashOps.putAll(key, map)
                }
                null
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }

    private fun clearUserReservations() {
        repeat(100) { shardNumber ->
            redisTemplate.delete("reserved_users:$shardNumber")
        }
    }
}
