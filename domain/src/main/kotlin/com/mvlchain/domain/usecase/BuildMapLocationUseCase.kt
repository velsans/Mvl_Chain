package com.mvlchain.domain.usecase

import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.model.LocationSlot
import com.mvlchain.domain.model.MapLocation
import com.mvlchain.domain.repository.LocationPreferenceRepository

/**
 * Builds a [MapLocation] using network sources and applies nickname override from preferences.
 */
class BuildMapLocationUseCase(
    private val getAirQuality: GetAirQualityUseCase,
    private val reverseGeocode: ReverseGeocodeUseCase,
    private val preferences: LocationPreferenceRepository,
) {
    suspend operator fun invoke(
        coordinate: GeoCoordinate,
        slot: LocationSlot,
        useCache: Boolean = true,
    ): Result<MapLocation> {
        if (useCache) {
            val cached = preferences.getCachedLocation(coordinate)
            if (cached != null) {
                val nick = preferences.getNickname(slot)
                return Result.success(
                    cached.copy(nickname = nick?.takeIf { it.isNotBlank() }),
                )
            }
        }

        val address = reverseGeocode(coordinate).getOrElse { return Result.failure(it) }
        val aqi = getAirQuality(coordinate).getOrElse { return Result.failure(it) }
        val nick = preferences.getNickname(slot)
        val location = MapLocation(
            coordinate = coordinate,
            formattedAddress = address,
            airQualityIndex = aqi,
            nickname = nick?.takeIf { it.isNotBlank() },
        )
        preferences.cacheLocation(location)
        return Result.success(location)
    }
}
