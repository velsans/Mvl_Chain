package com.mvlchain.domain.usecase

import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.repository.AirQualityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetAirQualityUseCaseTest {

    private class FakeRepo(private val value: Result<Int>) : AirQualityRepository {
        override suspend fun getAirQuality(coordinate: GeoCoordinate): Result<Int> = value
    }

    @Test
    fun `propagates success`() = runTest {
        val useCase = GetAirQualityUseCase(FakeRepo(Result.success(55)))
        val result = useCase(GeoCoordinate(0.0, 0.0))
        assertEquals(55, result.getOrNull())
    }

    @Test
    fun `propagates failure`() = runTest {
        val useCase = GetAirQualityUseCase(FakeRepo(Result.failure(IllegalStateException("boom"))))
        val result = useCase(GeoCoordinate(0.0, 0.0))
        assertTrue(result.isFailure)
    }
}
