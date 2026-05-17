package com.tadamaps.mobile.presentation.booking

import app.cash.turbine.test
import com.mvlchain.domain.model.BookingRequest
import com.mvlchain.domain.model.BookingResult
import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.model.MapLocation
import com.mvlchain.domain.usecase.CreateBookingUseCase
import com.tadamaps.mobile.presentation.navigation.BookingNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `emits success when booking resolves`() = runTest {
        val request = sampleRequest()
        val navigator = BookingNavigator().apply { enqueue(request) }
        val useCase = CreateBookingUseCase(
            object : com.mvlchain.domain.repository.BookRepository {
                override suspend fun createBooking(req: BookingRequest): Result<BookingResult> {
                    return Result.success(
                        BookingResult(
                            id = "id",
                            locationA = req.locationA,
                            locationB = req.locationB,
                            price = 12.3,
                        ),
                    )
                }

                override suspend fun getHistory(
                    year: Int,
                    month: Int,
                ): Result<List<com.mvlchain.domain.model.BookHistoryItem>> {
                    error("not used")
                }
            },
        )

        val viewModel = BookingViewModel(navigator, useCase)

        viewModel.uiState.test {
            assertTrue(awaitItem() is BookingUiState.Idle)
            viewModel.onUserEvent(BookingUserEvent.StartBooking)
            assertTrue(awaitItem() is BookingUiState.Loading)
            assertTrue(awaitItem() is BookingUiState.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun sampleRequest() = BookingRequest(
        locationA = MapLocation(
            coordinate = GeoCoordinate(1.0, 2.0),
            formattedAddress = "A",
            airQualityIndex = 1,
        ),
        locationB = MapLocation(
            coordinate = GeoCoordinate(3.0, 4.0),
            formattedAddress = "B",
            airQualityIndex = 2,
        ),
    )
}
