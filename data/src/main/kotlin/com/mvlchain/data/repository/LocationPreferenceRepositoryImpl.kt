package com.mvlchain.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mvlchain.data.local.dto.CachedMapLocationDto
import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.model.LocationSlot
import com.mvlchain.domain.model.MapLocation
import com.mvlchain.domain.repository.LocationPreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.locationDataStore: DataStore<Preferences> by preferencesDataStore(name = "location_prefs")

/**
 * Persisted nicknames and coordinate cache (3-decimal keying via [GeoCoordinate.roundedKey]).
 */
@Singleton
class LocationPreferenceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) : LocationPreferenceRepository {

    private val store get() = context.locationDataStore

    override suspend fun saveNickname(slot: LocationSlot, nickname: String?) {
        store.edit { prefs ->
            prefs[keyForNickname(slot)] = nickname.orEmpty()
        }
    }

    override suspend fun getNickname(slot: LocationSlot): String? {
        val prefs = store.data.first()
        return prefs[keyForNickname(slot)]?.takeIf { it.isNotBlank() }
    }

    override fun nicknameFlow(slot: LocationSlot): Flow<String?> =
        store.data.map { prefs -> prefs[keyForNickname(slot)]?.takeIf { it.isNotBlank() } }

    override suspend fun cacheLocation(location: MapLocation) {
        val key = cacheKey(location.coordinate)
        val dto = CachedMapLocationDto(
            latitude = location.coordinate.latitude,
            longitude = location.coordinate.longitude,
            address = location.formattedAddress,
            aqi = location.airQualityIndex,
        )
        store.edit { prefs ->
            prefs[stringPreferencesKey(key)] = json.encodeToString(dto)
        }
    }

    override suspend fun getCachedLocation(coordinate: GeoCoordinate): MapLocation? {
        val prefs = store.data.first()
        val raw = prefs[stringPreferencesKey(cacheKey(coordinate))] ?: return null
        val dto = runCatching { json.decodeFromString<CachedMapLocationDto>(raw) }.getOrNull()
            ?: return null
        return MapLocation(
            coordinate = GeoCoordinate(dto.latitude, dto.longitude),
            formattedAddress = dto.address,
            airQualityIndex = dto.aqi,
            nickname = null,
        )
    }

    private fun keyForNickname(slot: LocationSlot) = when (slot) {
        LocationSlot.A -> stringPreferencesKey("nickname_a")
        LocationSlot.B -> stringPreferencesKey("nickname_b")
    }

    private fun cacheKey(coordinate: GeoCoordinate) = "cache_${coordinate.roundedKey()}"
}
