package com.example.reservationsystem.service

import com.example.reservationsystem.domain.OrderStatus
import com.example.reservationsystem.dto.response.CancelResult
import com.example.reservationsystem.exception.BadRequestException
import com.example.reservationsystem.exception.ForbiddenException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CancelService(
    private val orderService: OrderService,
) {
    fun validateAndGetOrderForCancellation(orderUid: String, userId: String): CancelResult {
        val order = orderService.findByOrderUidOrThrow(orderUid)
        if (order.userId != userId) {
            throw ForbiddenException("본인의 예매만 취소할 수 있습니다.")
        }
        if (order.status != OrderStatus.PAID) {
            throw BadRequestException("결제 완료된 주문만 취소할 수 있습니다.")
        }

        return CancelResult(
            orderUid = order.orderUid,
            amount = order.amount,
            idempotencyKey = UUID.randomUUID().toString()
        )
    }
}
