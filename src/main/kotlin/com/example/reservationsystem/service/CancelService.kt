package com.example.reservationsystem.service

import com.example.reservationsystem.domain.OrderStatus
import com.example.reservationsystem.repository.OrderRepository
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class CancelService(
    private val orderRepository: OrderRepository,
) {
    fun requestCancellation(orderId: Long, userId: String) {
        val order = orderRepository.findById(orderId).orElse(null)
            ?: throw IllegalArgumentException("주문을 찾을 수 없습니다.")

        if (order.userId != userId) {
            throw IllegalArgumentException("본인의 예매만 취소할 수 있습니다.")
        }

        if (order.status != OrderStatus.PAID) {
            throw IllegalArgumentException("결제 완료된 주문만 취소할 수 있습니다.")
        }

        CompletableFuture.runAsync {
            // 비동기 결제서버 취소요청
        }
        
    }
}
