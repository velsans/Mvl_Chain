package com.mvlchain.data.remote.interceptor

import com.mvlchain.data.BuildConfig
import com.mvlchain.data.remote.dto.BookHistoryResponseDto
import com.mvlchain.data.remote.dto.BookRequestDto
import com.mvlchain.data.remote.dto.BookResponseDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.util.Calendar
import java.util.UUID

/**
 * Simulates booking APIs locally without affecting domain contracts.
 */
class MockBooksInterceptor(
    private val json: Json,
    private val mockDelayMs: Long = BuildConfig.MOCK_DELAY_MS,
    private val enabled: Boolean = BuildConfig.MOCK_BOOKS_NETWORK,
) : Interceptor {

    private data class RecordedBooking(val year: Int, val month: Int, val dto: BookResponseDto)

    private val recordedBookings = mutableListOf<RecordedBooking>()
    private val recordedLock = Any()
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!enabled || !chain.request().url.encodedPath.contains("books")) {
            return chain.proceed(chain.request())
        }

        Thread.sleep(mockDelayMs)

        val request = chain.request()
        val path = request.url.encodedPath

        return when {
            request.method == "POST" && path.contains("books") -> handlePost(request)
            request.method == "GET" && path.contains("books") -> handleGet(request)
            else -> chain.proceed(request)
        }
    }

    private fun handlePost(request: okhttp3.Request): Response {
        val bodyString = request.body?.let { body ->
            Buffer().apply { body.writeTo(this) }.readUtf8()
        }?.ifBlank { "{}" } ?: "{}"
        val parsed = runCatching { json.decodeFromString<BookRequestDto>(bodyString) }.getOrNull()
            ?: return errorResponse("invalid payload")

        // Spec: mocked price (e.g. 10_000); still echo A/B from the request body.
        val price = 10_000.0
        val responseDto = BookResponseDto(
            id = UUID.randomUUID().toString(),
            a = parsed.a,
            b = parsed.b,
            price = price,
        )
        val cal = Calendar.getInstance()
        synchronized(recordedLock) {
            recordedBookings.add(
                0,
                RecordedBooking(
                    year = cal.get(Calendar.YEAR),
                    month = cal.get(Calendar.MONTH) + 1,
                    dto = responseDto,
                ),
            )
        }
        return jsonResponse(json.encodeToString(responseDto))
    }

    private fun handleGet(request: okhttp3.Request): Response {
        val cal = Calendar.getInstance()
        val defaultYear = cal.get(Calendar.YEAR)
        val defaultMonth = cal.get(Calendar.MONTH) + 1
        val year = request.url.queryParameter("year")?.toIntOrNull() ?: defaultYear
        val month = request.url.queryParameter("month")?.toIntOrNull() ?: defaultMonth

        val items = synchronized(recordedLock) {
            recordedBookings
                .filter { it.year == year && it.month == month }
                .map { it.dto }
        }
        val dto = BookHistoryResponseDto(items = items)
        return jsonResponse(json.encodeToString(dto))
    }

    private fun jsonResponse(body: String): Response =
        Response.Builder()
            .request(okhttp3.Request.Builder().url("https://mock.local/books").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body.toResponseBody(JSON.toMediaType()))
            .build()

    private fun errorResponse(message: String): Response {
        val err = """{"error":"$message"}"""
        return Response.Builder()
            .request(okhttp3.Request.Builder().url("https://mock.local/books").build())
            .protocol(Protocol.HTTP_1_1)
            .code(400)
            .message("Bad Request")
            .body(err.toResponseBody(JSON.toMediaType()))
            .build()
    }

    companion object {
        private val JSON = "application/json"
    }
}
