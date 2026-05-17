package com.mvlchain.data.repository

import com.mvlchain.data.BuildConfig
import com.mvlchain.data.mapper.GeocodingMapper
import com.mvlchain.data.remote.api.GeocodingApi
import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.repository.GeocodingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingRepositoryImpl @Inject constructor(
    private val api: GeocodingApi,
) : GeocodingRepository {
    override suspend fun reverseGeocode(coordinate: GeoCoordinate): Result<String> = runCatching {
        val key = BuildConfig.GEO_API_KEY.takeIf { it.isNotBlank() }
        val body = api.reverseGeocode(
            latitude = coordinate.latitude,
            longitude = coordinate.longitude,
            apiKey = key,
        )
        GeocodingMapper.formatAddress(body)
    }
}
