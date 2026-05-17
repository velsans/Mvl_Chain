package com.mvlchain.domain.usecase

import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.repository.AirQualityRepository

/**
 * Resolves AQI for the current map center.
 */
class GetAirQualityUseCase(
    private val repository: AirQualityRepository,
) {
    suspend operator fun invoke(coordinate: GeoCoordinate): Result<Int> = repository.getAirQuality(coordinate)
}
