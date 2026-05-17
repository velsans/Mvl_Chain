package com.mvlchain.domain.repository

import com.mvlchain.domain.model.GeoCoordinate

/**
 * Reverse-geocoding using BigDataCloud rules (administrative top-2 by order).
 */
fun interface GeocodingRepository {
    suspend fun reverseGeocode(coordinate: GeoCoordinate): Result<String>
}
