package com.example.reservationsystem.repository

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class IdempotencyRepository(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {

    fun saveResult(idempotencyKey: String, result: Any, ttl: Duration = Duration.ofHours(6)) {
        val key = "idempotency:$idempotencyKey"
        val value = objectMapper.writeValueAsString(result)
        redisTemplate.opsForValue().set(key, value, ttl)
    }

    fun getResult(idempotencyKey: String): String? {
        val key = "idempotency:$idempotencyKey"
        return redisTemplate.opsForValue().get(key)
    }

}
