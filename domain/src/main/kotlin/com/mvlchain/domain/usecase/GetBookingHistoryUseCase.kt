package com.mvlchain.domain.usecase

import com.mvlchain.domain.model.BookHistoryItem
import com.mvlchain.domain.repository.BookRepository

class GetBookingHistoryUseCase(
    private val bookRepository: BookRepository,
) {
    suspend operator fun invoke(year: Int, month: Int): Result<List<BookHistoryItem>> =
        bookRepository.getHistory(year, month)
}
