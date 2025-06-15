package com.example.reservationsystem.client

import com.example.reservationsystem.config.PaymentProperties
import com.example.reservationsystem.dto.request.PaymentCancellationRequest
import com.example.reservationsystem.dto.request.PaymentConfirmRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient


@Component
class PaymentApiClient(
    paymentProperties: PaymentProperties,
    webClientBuilder: WebClient.Builder,
    private val objectMapper: ObjectMapper
) {
    private val webClient = webClientBuilder.baseUrl(paymentProperties.paymentsServerUrl).build()

    fun requestPaymentConfirm(request: PaymentConfirmRequest, signature: String) {
        val payloadJson = objectMapper.writeValueAsString(request)
        sendPostRequest("/api/v1/pay/confirm", payloadJson, signature)
    }

    fun requestPaymentCancel(request: PaymentCancellationRequest, signature: String) {
        val payloadJson = objectMapper.writeValueAsString(request)
        sendPostRequest("/api/v1/pay/cancel", payloadJson, signature)
    }

    private fun sendPostRequest(uri: String, payloadJson: String, signature: String) {
        webClient.post()
            .uri(uri)
            .header("X-Signature", signature)
            .header("Content-Type", "application/json")
            .bodyValue(payloadJson)
            .retrieve()
            .toBodilessEntity()
            .subscribe()
    }
}
