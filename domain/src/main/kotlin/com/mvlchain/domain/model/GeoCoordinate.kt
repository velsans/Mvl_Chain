package com.mvlchain.domain.model

/**
 * Geographic coordinate with helpers for cache key rounding (3 decimal places).
 */
data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double,
) {
    fun roundedKey(): String =
        "${"%.3f".format(latitude)},${"%.3f".format(longitude)}"

    companion object {
        fun fromRounded(latitude: Double, longitude: Double) = GeoCoordinate(latitude, longitude)
    }
}
