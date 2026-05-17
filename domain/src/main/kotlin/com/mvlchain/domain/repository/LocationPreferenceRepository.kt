package com.mvlchain.domain.repository

import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.model.LocationSlot
import com.mvlchain.domain.model.MapLocation
import kotlinx.coroutines.flow.Flow

/**
 * Local nicknames and optional coordinate-keyed cache for map locations.
 */
interface LocationPreferenceRepository {
    suspend fun saveNickname(slot: LocationSlot, nickname: String?)

    suspend fun getNickname(slot: LocationSlot): String?

    fun nicknameFlow(slot: LocationSlot): Flow<String?>

    suspend fun cacheLocation(location: MapLocation)

    suspend fun getCachedLocation(coordinate: GeoCoordinate): MapLocation?
}
