package com.mvlchain.domain.model

/**
 * A map selection enriched with reverse-geocoding and air quality.
 */
data class MapLocation(
    val coordinate: GeoCoordinate,
    val formattedAddress: String,
    val airQualityIndex: Int?,
    val nickname: String? = null,
) {
    val displayLabel: String get() = nickname?.takeIf { it.isNotBlank() } ?: formattedAddress
}
