package com.example.reservationsystem.repository

import com.example.reservationsystem.domain.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByOrderUid(orderUid: String): Order?
}
