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


    override fun run(args: ApplicationArguments?) {
        val allSeats = seatRepository.findAll()
        val hashOps = redisTemplate.opsForHash<String, String>()

        allSeats.forEach { seat ->
            val key = "seat:${seat.seatNumber}"
            hashOps.putAll(key, mapOf(
                "amount" to seat.amount.toString(),
                "isReserved" to "false",
                "reservedBy" to "",
                "reservedAt" to ""
            ))
        }
    }
}
