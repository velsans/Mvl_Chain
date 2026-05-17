package com.mvlchain.data.remote.dto

import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.model.MapLocation
import kotlinx.serialization.Serializable

@Serializable
data class MapLocationDto(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val aqi: Int? = null,
    val nickname: String? = null,
)

@Serializable
data class BookRequestDto(
    val a: MapLocationDto,
    val b: MapLocationDto,
)

@Serializable
data class BookResponseDto(
    val id: String? = null,
    val a: MapLocationDto,
    val b: MapLocationDto,
    val price: Double,
)

@Serializable
data class BookHistoryResponseDto(
    val items: List<BookResponseDto> = emptyList(),
)

fun MapLocation.toDto() = MapLocationDto(
    latitude = coordinate.latitude,
    longitude = coordinate.longitude,
    address = formattedAddress,
    aqi = airQualityIndex,
    nickname = nickname,
)

fun MapLocationDto.toDomain() = MapLocation(
    coordinate = GeoCoordinate(latitude, longitude),
    formattedAddress = address,
    airQualityIndex = aqi,
    nickname = nickname,
)
