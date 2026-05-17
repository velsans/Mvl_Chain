package com.tadamaps.mobile.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.tadamaps.mobile.R
import com.mvlchain.domain.model.MapLocation

/**
 * @param showAddressAndNickname When true (e.g. booking), shows the full formatted address on the
 * main line and a separate nickname line when set. When false, uses [MapLocation.displayLabel] only.
 */
@Composable
fun LocationSummaryBlock(
    label: String,
    location: MapLocation,
    modifier: Modifier = Modifier,
    showAddressAndNickname: Boolean = false,
) {
    val rowSpacing = dimensionResource(R.dimen.mvl_row_spacing)
    val nick = location.nickname?.takeIf { it.isNotBlank() }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(rowSpacing),
    ) {
        if (showAddressAndNickname) {
            Text(
                "$label ${location.formattedAddress}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            nick?.let { n ->
                Text(
                    n,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Text(
                "$label ${location.displayLabel}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            "aqi ${location.airQualityIndex ?: "0"}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
