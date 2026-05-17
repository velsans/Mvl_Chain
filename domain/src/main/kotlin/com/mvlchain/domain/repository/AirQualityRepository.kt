package com.mvlchain.domain.repository

import com.mvlchain.domain.model.GeoCoordinate

/**
 * Provides air quality index for a coordinate (waqi.info).
 */
fun interface AirQualityRepository {
    suspend fun getAirQuality(coordinate: GeoCoordinate): Result<Int>
}
