package com.example.reservationsystem.controller

import com.example.reservationsystem.dto.response.toResponseEntity
import com.example.reservationsystem.service.PurchaseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/purchase")
class PurchaseController(
    private val purchaseService: PurchaseService
) {
    @PostMapping
    fun purchase(@RequestBody request: PurchaseRequest): ResponseEntity<Any> {
        return try {
            val result = purchaseService.processPurchase(request.seatNumber, request.userId)
            result.toResponseEntity()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("시스템 오류가 발생했습니다.")
        }
    }
}
