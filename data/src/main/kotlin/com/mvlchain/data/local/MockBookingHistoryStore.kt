package com.mvlchain.data.local

import android.content.Context
import com.mvlchain.data.remote.dto.BookResponseDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
internal data class PersistedMockBooking(
    val year: Int,
    val month: Int,
    val response: BookResponseDto,
)

/**
 * Persists mocked booking rows to app storage so history survives process death.
 * Used only when [com.mvlchain.data.BuildConfig.MOCK_BOOKS_NETWORK] is true.
 */
@Singleton
class MockBookingHistoryStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    private val file = File(context.filesDir, "mock_booking_history.json")
    private val lock = Any()

    fun appendFront(year: Int, month: Int, response: BookResponseDto) {
        synchronized(lock) {
            val list = readAllInternal().toMutableList()
            list.add(0, PersistedMockBooking(year, month, response))
            writeAllInternal(list)
        }
    }

    fun findByMonth(year: Int, month: Int): List<BookResponseDto> {
        synchronized(lock) {
            return readAllInternal()
                .filter { it.year == year && it.month == month }
                .map { it.response }
        }
    }

    private fun readAllInternal(): List<PersistedMockBooking> {
        if (!file.exists()) return emptyList()
        val text = file.readText()
        if (text.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<PersistedMockBooking>>(text)
        }.getOrElse { emptyList() }
    }

    private fun writeAllInternal(items: List<PersistedMockBooking>) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(items))
    }
}
