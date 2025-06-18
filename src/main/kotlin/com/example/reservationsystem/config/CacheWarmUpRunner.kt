package com.example.reservationsystem.config

import com.example.reservationsystem.repository.SeatRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class CacheWarmUpRunner(
    private val seatRepository: SeatRepository,
    private val redisTemplate: RedisTemplate<String, String>
) : ApplicationRunner {

    private val cacheInitKey = "cache:initialized"

    override fun run(args: ApplicationArguments?) {
        val canInitialize = redisTemplate.opsForValue().setIfAbsent(cacheInitKey, "true")

        when (canInitialize) {
            true -> {
                try {
                    initializeCache()
                } catch (e: Exception) {
                    redisTemplate.delete(cacheInitKey)
                    throw e
                }
            }

            false -> {
                return
            }

            null -> {
                throw RuntimeException("캐시 초기화 상태를 확인할 수 없습니다.")
            }
        }
    }

    private fun initializeCache() {
        clearUserReservations()
        warmUpSeatCache()
    }

    private fun clearUserReservations() {
        repeat(100) { shardNumber ->
            redisTemplate.delete("reserved_users:$shardNumber")
        }
    }

    private fun warmUpSeatCache() {
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
    }
}
