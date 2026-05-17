package com.mvlchain.data.remote.api

import com.mvlchain.data.remote.dto.BookHistoryResponseDto
import com.mvlchain.data.remote.dto.BookRequestDto
import com.mvlchain.data.remote.dto.BookResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Booking REST facade — trafficked through a mocked OkHttp stack in non-production backends.
 */
interface BookApi {
    @POST("v1/books")
    suspend fun createBook(@Body body: BookRequestDto): BookResponseDto

    @GET("v1/books")
    suspend fun listBooks(
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): BookHistoryResponseDto
}
