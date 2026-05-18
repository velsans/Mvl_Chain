package com.mvlchain.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_map_locations")
data class CachedMapLocationEntity(
    @PrimaryKey @ColumnInfo(name = "cache_key") val cacheKey: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "address") val address: String,
    @ColumnInfo(name = "aqi") val aqi: Int?,
    /** Nickname stored with cache row (reverse-geocode cache metadata). */
    @ColumnInfo(name = "cached_nickname") val cachedNickname: String?,
)
