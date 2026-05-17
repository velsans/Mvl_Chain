package com.mvlchain.data.remote.api

import com.mvlchain.data.remote.dto.AqiFeedResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * WAQI geolocalized feed (see https://aqicn.org/json-api/doc/).
 */
interface AqiApi {
    @GET("feed/geo:{coords}/")
    suspend fun geoFeed(
        @Path(value = "coords", encoded = true) coords: String,
        @Query("token") token: String,
    ): AqiFeedResponse
}
