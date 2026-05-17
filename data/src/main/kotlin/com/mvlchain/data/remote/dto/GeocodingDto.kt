package com.mvlchain.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReverseGeocodeResponse(
    val localityInfo: LocalityInfo? = null,
)

@Serializable
data class LocalityInfo(
    val administrative: List<AdministrativeEntry>? = null,
)

@Serializable
data class AdministrativeEntry(
    val name: String? = null,
    val order: Int? = null,
)
