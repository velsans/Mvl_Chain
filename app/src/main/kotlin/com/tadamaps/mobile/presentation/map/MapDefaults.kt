package com.tadamaps.mobile.presentation.map

import com.google.android.gms.maps.model.LatLng

/**
 * Shared map camera defaults (initial frame + ViewModel fallbacks) so the app stays region-consistent.
 */
object MapDefaults {
    val INITIAL_CENTER: LatLng = LatLng(20.5937, 78.9629)
    const val INITIAL_ZOOM: Float = 5f
}
