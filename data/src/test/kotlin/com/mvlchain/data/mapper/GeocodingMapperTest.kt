package com.mvlchain.data.mapper

import com.mvlchain.data.remote.dto.AdministrativeEntry
import com.mvlchain.data.remote.dto.LocalityInfo
import com.mvlchain.data.remote.dto.ReverseGeocodeResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class GeocodingMapperTest {

    @Test
    fun `uses two highest order administrative names ascending order then comma join`() {
        val response = ReverseGeocodeResponse(
            localityInfo = LocalityInfo(
                administrative = listOf(
                    AdministrativeEntry(name = "South Korea", order = 2),
                    AdministrativeEntry(name = "Seoul", order = 3),
                    AdministrativeEntry(name = "Seocho District", order = 4),
                    AdministrativeEntry(name = "Yangjae 2(i)-dong", order = 5),
                ),
            ),
        )
        val formatted = GeocodingMapper.formatAddress(response)
        assertEquals("Seocho District, Yangjae 2(i)-dong", formatted)
    }

    @Test
    fun `mixed orders pick top two by value then ascending join`() {
        val response = ReverseGeocodeResponse(
            localityInfo = LocalityInfo(
                administrative = listOf(
                    AdministrativeEntry(name = "Child", order = 3),
                    AdministrativeEntry(name = "Parent", order = 8),
                    AdministrativeEntry(name = "Ignore", order = 1),
                ),
            ),
        )
        val formatted = GeocodingMapper.formatAddress(response)
        assertEquals("Child, Parent", formatted)
    }
}
