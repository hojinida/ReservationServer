package com.example.reservationsystem.service

import com.example.reservationsystem.config.PaymentProperties
import org.springframework.stereotype.Service
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class SignatureService(
    paymentProperties: PaymentProperties
) {
    private val algorithm = "HmacSHA256"
    private val secretKeySpec = SecretKeySpec(paymentProperties.secretKey.toByteArray(), algorithm)

    fun generate(data: String): String {
        val mac = Mac.getInstance(algorithm)
        mac.init(secretKeySpec)
        val signatureBytes = mac.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(signatureBytes)
    }

    fun verify(data: String, signature: String): Boolean {
        val expectedSignature = generate(data)
        return expectedSignature == signature
    }
}
