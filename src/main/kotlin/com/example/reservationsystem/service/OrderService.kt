package com.example.reservationsystem.service

import com.example.reservationsystem.domain.Order
import com.example.reservationsystem.domain.OrderStatus
import com.example.reservationsystem.repository.OrderRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.rmi.server.UID
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    @Transactional
    fun createPendingOrder(seatNumber: String, userId: String, price: Long): Order {
        val order = Order(
            userId = userId,
            orderUid = UUID.randomUUID().toString(),
            amount = price,
            status = OrderStatus.PENDING,
            seatNumber = seatNumber
        )
        return orderRepository.save(order)
    }

    @Transactional
    fun complete(orderUid: String, paymentKey: String): Order {
        val order = orderRepository.findByOrderUid(orderUid)
            ?: throw IllegalArgumentException("주문을 찾을 수 없습니다. orderUid: $orderUid")

        if (order.status == OrderStatus.PAID) {
            return order
        }

        order.status = OrderStatus.PAID
        order.paymentKey = paymentKey
        return order
    }

    @Transactional
    fun fail(orderUid: String): Order {
        val order = orderRepository.findByOrderUid(orderUid)
            ?: throw IllegalArgumentException("주문을 찾을 수 없습니다. orderUid: $orderUid")

        if (order.status == OrderStatus.FAILED || order.status == OrderStatus.PAID) {
            return order
        }

        order.status = OrderStatus.FAILED
        return order
    }

    @Transactional
    fun cancel(orderUid: String): Order {
        val order = orderRepository.findByOrderUid(orderUid)
            ?: throw IllegalArgumentException("주문을 찾을 수 없습니다. orderUid: $orderUid")

        if (order.status == OrderStatus.CANCELLED) {
            return order
        }

        order.status = OrderStatus.CANCELLED
        return order
    }
}
