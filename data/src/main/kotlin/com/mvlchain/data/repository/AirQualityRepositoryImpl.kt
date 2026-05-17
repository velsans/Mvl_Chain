package com.mvlchain.data.repository

import com.mvlchain.data.BuildConfig
import com.mvlchain.data.remote.api.AqiApi
import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.repository.AirQualityRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AirQualityRepositoryImpl @Inject constructor(
    private val api: AqiApi,
) : AirQualityRepository {
    override suspend fun getAirQuality(coordinate: GeoCoordinate): Result<Int> = runCatching {
        val token = BuildConfig.AQI_TOKEN.ifBlank {
            error("AQI token missing — configure AQI_API_TOKEN in local.properties")
        }
        val response = api.geoFeed(
            coords = "${coordinate.latitude};${coordinate.longitude}",
            token = token,
        )
        val aqi = response.data?.aqi ?: error("AQI unavailable")
        aqi
    }
}
