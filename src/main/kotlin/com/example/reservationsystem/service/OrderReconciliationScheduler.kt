package com.example.reservationsystem.service

import com.example.reservationsystem.domain.OrderStatus
import com.example.reservationsystem.repository.OrderRepository
import com.example.reservationsystem.repository.RedisReservationRepository
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class OrderReconciliationScheduler(
    private val orderRepository: OrderRepository,
    private val redisReservationRepository: RedisReservationRepository
) {

    @Scheduled(fixedDelay = 600000)
    @Transactional
    fun reconcilePendingOrders() {
        val cutoffTime = LocalDateTime.now().minusMinutes(15)

        val stuckOrders =
            orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING, cutoffTime)

        if (stuckOrders.isEmpty()) {
            return
        }

        stuckOrders.forEach { order ->
            order.status = OrderStatus.FAILED
            redisReservationRepository.cancelReservation(order.seatNumber, order.userId)
        }
    }
}
