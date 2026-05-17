package com.mvlchain.data.repository

import com.mvlchain.data.remote.api.BookApi
import com.mvlchain.data.remote.dto.BookRequestDto
import com.mvlchain.data.remote.dto.toDomain
import com.mvlchain.data.remote.dto.toDto
import com.mvlchain.domain.model.BookHistoryItem
import com.mvlchain.domain.model.BookingRequest
import com.mvlchain.domain.model.BookingResult
import com.mvlchain.domain.repository.BookRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val api: BookApi,
) : BookRepository {
    override suspend fun createBooking(request: BookingRequest): Result<BookingResult> = runCatching {
        val dto = api.createBook(
            BookRequestDto(
                a = request.locationA.toDto(),
                b = request.locationB.toDto(),
            ),
        )
        BookingResult(
            id = dto.id,
            locationA = dto.a.toDomain(),
            locationB = dto.b.toDomain(),
            price = dto.price,
        )
    }

    override suspend fun getHistory(year: Int, month: Int): Result<List<BookHistoryItem>> = runCatching {
        val response = api.listBooks(year, month)
        response.items.map { dto ->
            BookHistoryItem(
                id = dto.id ?: dto.hashCode().toString(),
                locationA = dto.a.toDomain(),
                locationB = dto.b.toDomain(),
                price = dto.price,
                year = year,
                month = month,
            )
        }
    }
}
