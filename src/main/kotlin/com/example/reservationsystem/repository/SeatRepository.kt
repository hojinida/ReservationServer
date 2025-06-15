package com.example.reservationsystem.repository

import com.example.reservationsystem.domain.Seat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SeatRepository : JpaRepository<Seat, Long> {
    fun findBySeatNumber(seatNumber: String): Seat?
}
