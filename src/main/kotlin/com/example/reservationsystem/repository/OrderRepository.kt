package com.example.reservationsystem.repository

import com.example.reservationsystem.domain.Order
import com.example.reservationsystem.domain.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByOrderUid(orderUid: String): Order?
    fun findByStatusAndCreatedAtBefore(status: OrderStatus, cutoffTime: LocalDateTime): List<Order>
}
