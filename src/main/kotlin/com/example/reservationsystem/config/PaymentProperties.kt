package com.example.reservationsystem.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "payment")
data class PaymentProperties(
    var secretKey: String = "", var paymentsServerUrl: String = ""
)
