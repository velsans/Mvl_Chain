package com.mvlchain.domain.repository

import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.usecase.ReverseGeocodeUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GeocodingRepositoryMockTest {

    @Test
    fun `reverse geocode use case delegates to repository`() = runTest {
        val repository = mockk<GeocodingRepository> {
            coEvery { reverseGeocode(GeoCoordinate(1.0, 2.0)) } returns Result.success("Road")
        }
        val useCase = ReverseGeocodeUseCase(repository)
        val result = useCase(GeoCoordinate(1.0, 2.0)).getOrNull()
        assertEquals("Road", result)
    }
}
