package com.mvlchain.domain.repository

import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.model.LocationSlot
import com.mvlchain.domain.model.MapLocation
import kotlinx.coroutines.flow.Flow

/**
 * Local slot nicknames and coordinate-keyed cache for map locations.
 */
interface LocationPreferenceRepository {
    suspend fun saveNickname(slot: LocationSlot, nickname: String?)

    suspend fun getNickname(slot: LocationSlot): String?

    fun nicknameFlow(slot: LocationSlot): Flow<String?>

    suspend fun cacheLocation(location: MapLocation)

    suspend fun getCachedLocation(coordinate: GeoCoordinate): MapLocation?

    /** Clears nickname fields from all coordinate cache entries (e.g. after map reset). */
    suspend fun stripCachedLocationNicknames()
}
