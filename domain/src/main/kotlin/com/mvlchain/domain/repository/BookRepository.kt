package com.mvlchain.domain.repository

import com.mvlchain.domain.model.BookHistoryItem
import com.mvlchain.domain.model.BookingRequest
import com.mvlchain.domain.model.BookingResult

/**
 * Booking API abstraction; mocked at data layer without leaking into domain.
 */
interface BookRepository {
    suspend fun createBooking(request: BookingRequest): Result<BookingResult>

    suspend fun getHistory(year: Int, month: Int): Result<List<BookHistoryItem>>
}
