package com.example.reservationsystem.controller

import com.example.reservationsystem.dto.request.CancelRequest
import com.example.reservationsystem.service.CancelService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cancel")
class CancelController(
    private val cancelService: CancelService
) {
    @PostMapping
    fun requestCancel(@RequestBody request: CancelRequest): ResponseEntity<String> {
        cancelService.requestCancellation(request.orderUId, request.userId)
        return ResponseEntity.ok("취소 요청이 접수되었습니다. 처리 결과는 이메일로 발송됩니다.")
    }
}
