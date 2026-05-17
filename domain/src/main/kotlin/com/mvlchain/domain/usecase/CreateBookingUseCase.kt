package com.mvlchain.domain.usecase

import com.mvlchain.domain.model.BookingRequest
import com.mvlchain.domain.model.BookingResult
import com.mvlchain.domain.repository.BookRepository

class CreateBookingUseCase(
    private val bookRepository: BookRepository,
) {
    suspend operator fun invoke(request: BookingRequest): Result<BookingResult> =
        bookRepository.createBooking(request)
}
