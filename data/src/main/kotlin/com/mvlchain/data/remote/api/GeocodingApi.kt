package com.mvlchain.data.remote.api

import com.mvlchain.data.remote.dto.ReverseGeocodeResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * BigDataCloud client reverse geocoding API.
 */
interface GeocodingApi {
    @GET("data/reverse-geocode-client")
    suspend fun reverseGeocode(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("localityLanguage") localityLanguage: String = "en",
        @Query("key") apiKey: String? = null,
    ): ReverseGeocodeResponse
}
