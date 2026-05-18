package com.mvlchain.data.repository

import com.mvlchain.data.local.db.CachedMapLocationDao
import com.mvlchain.data.local.db.CachedMapLocationEntity
import com.mvlchain.data.local.db.SlotNicknameDao
import com.mvlchain.data.local.db.SlotNicknameEntity
import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.model.LocationSlot
import com.mvlchain.domain.model.MapLocation
import com.mvlchain.domain.repository.LocationPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Slot nicknames + coordinate-keyed address cache backed by **Room** (3-decimal keying via
 * [GeoCoordinate.roundedKey]).
 */
@Singleton
class LocationPreferenceRepositoryImpl @Inject constructor(
    private val cachedMapLocationDao: CachedMapLocationDao,
    private val slotNicknameDao: SlotNicknameDao,
) : LocationPreferenceRepository {

    override suspend fun saveNickname(slot: LocationSlot, nickname: String?) {
        val key = slot.name
        if (nickname.isNullOrBlank()) {
            slotNicknameDao.deleteSlot(key)
        } else {
            slotNicknameDao.upsert(SlotNicknameEntity(slot = key, nickname = nickname.trim()))
        }
    }

    override suspend fun getNickname(slot: LocationSlot): String? =
        slotNicknameDao.getNicknameRaw(slot.name)?.takeIf { it.isNotBlank() }

    override fun nicknameFlow(slot: LocationSlot): Flow<String?> =
        slotNicknameDao.observeNicknameRows(slot.name).map { rows ->
            rows.firstOrNull()?.takeIf { it.isNotBlank() }
        }

    override suspend fun cacheLocation(location: MapLocation) {
        val key = cacheKey(location.coordinate)
        cachedMapLocationDao.upsert(
            CachedMapLocationEntity(
                cacheKey = key,
                latitude = location.coordinate.latitude,
                longitude = location.coordinate.longitude,
                address = location.formattedAddress,
                aqi = location.airQualityIndex,
                cachedNickname = location.nickname?.takeIf { it.isNotBlank() },
            ),
        )
    }

    override suspend fun getCachedLocation(coordinate: GeoCoordinate): MapLocation? {
        val row = cachedMapLocationDao.getByKey(cacheKey(coordinate)) ?: return null
        return MapLocation(
            coordinate = GeoCoordinate(row.latitude, row.longitude),
            formattedAddress = row.address,
            airQualityIndex = row.aqi,
            nickname = row.cachedNickname?.takeIf { it.isNotBlank() },
        )
    }

    override suspend fun stripCachedLocationNicknames() {
        cachedMapLocationDao.clearCachedNicknames()
    }

    private fun cacheKey(coordinate: GeoCoordinate) = "cache_${coordinate.roundedKey()}"
}
