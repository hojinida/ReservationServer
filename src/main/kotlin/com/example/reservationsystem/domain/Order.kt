package com.example.reservationsystem.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener::class)
data class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,

    @Column(unique = true) val orderUid: String,

    @Column(nullable = false) val userId: String,

    @Column(nullable = false) val seatNumber: String,

    @Column(nullable = false) val amount: Long,

    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: OrderStatus,
    @CreatedDate @Column(nullable = false, updatable = false) var createdAt: LocalDateTime? = null,
)

