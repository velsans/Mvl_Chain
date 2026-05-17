package com.mvlchain.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AqiFeedResponse(
    val status: String? = null,
    val data: AqiData? = null,
)

@Serializable
data class AqiData(
    val aqi: Int? = null,
)
