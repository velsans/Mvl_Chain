package com.mvlchain.domain.usecase

import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.repository.GeocodingRepository

/**
 * Wraps reverse-geocode resolution for UI layers.
 */
class ReverseGeocodeUseCase(
    private val repository: GeocodingRepository,
) {
    suspend operator fun invoke(coordinate: GeoCoordinate): Result<String> =
        repository.reverseGeocode(coordinate)
}
