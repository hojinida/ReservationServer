package com.example.reservationsystem

import com.example.reservationsystem.config.PaymentProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties(PaymentProperties::class)
class ReservationSystemApplication

fun main(args: Array<String>) {
    runApplication<ReservationSystemApplication>(*args)
}
