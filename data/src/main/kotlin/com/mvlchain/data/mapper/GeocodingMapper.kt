package com.mvlchain.data.mapper

import com.mvlchain.data.remote.dto.ReverseGeocodeResponse

/**
 * Formats BigDataCloud-style [localityInfo.administrative]:
 * take the **two entries with the highest [order]** values, sort those two by **order ascending**,
 * then concatenate their [name]s with ", " (e.g. `Seocho District, Yangjae 2(i)-dong`).
 */
object GeocodingMapper {
    fun formatAddress(response: ReverseGeocodeResponse): String {
        val entries = response.localityInfo?.administrative.orEmpty()
            .filter { !it.name.isNullOrBlank() && it.order != null }
        if (entries.isEmpty()) return "Unknown area"

        val topTwoByOrder = entries
            .sortedByDescending { it.order!! }
            .take(2)
            .sortedBy { it.order!! }

        return topTwoByOrder
            .mapNotNull { it.name }
            .joinToString(separator = ", ")
            .ifBlank { "Unknown area" }
    }
}
