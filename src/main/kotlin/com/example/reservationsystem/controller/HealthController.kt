package com.example.reservationsystem.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress
import java.time.LocalDateTime

@RestController
class HealthController {

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "server" to getServerInfo(),
            "timestamp" to LocalDateTime.now().toString()
        )
    }

    private fun getServerInfo(): String {
        return try {
            InetAddress.getLocalHost().hostAddress
        } catch (e: Exception) {
            "unknown"
        }
    }
}
