package com.example.reservationsystem.controller

import com.example.reservationsystem.dto.response.toResponseEntity
import com.example.reservationsystem.service.PurchaseService
import org.springframework.http.HttpHeaders
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
        val result = purchaseService.processPurchase(request.seatNumber, request.userId)

        val redirectUrl =
            "http://localhost:8080/mock-payment-page?orderUid=${result.order.orderUid}&amount=${result.order.amount}&idempotencyKey=${result.idempotencyKey}&signature=${result.signature}"
        val headers = HttpHeaders()
        headers.add("Location", redirectUrl)
        return ResponseEntity(headers, HttpStatus.FOUND)
    }
}
