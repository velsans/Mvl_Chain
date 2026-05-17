package com.mvlchain.data.local.dto

import kotlinx.serialization.Serializable

@Serializable
data class CachedMapLocationDto(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val aqi: Int?,
    val nickname: String? = null,
)
