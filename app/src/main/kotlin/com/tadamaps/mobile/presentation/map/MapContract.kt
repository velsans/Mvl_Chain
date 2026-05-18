package com.tadamaps.mobile.presentation.map

import com.google.android.gms.maps.model.LatLng
import com.mvlchain.domain.model.MapLocation

/**
 * MVI: immutable UI state.
 */
enum class MapBookingStep {
    PickA,
    PickB,
    ReadyToBook,
}

data class MapUiState(
    val step: MapBookingStep = MapBookingStep.PickA,
    val centerAqi: Int? = null,
    val loadingAqi: Boolean = false,
    val busy: Boolean = false,
    val locationA: MapLocation? = null,
    val locationB: MapLocation? = null,
    val errorMessage: String? = null,
)

/**
 * MVI: user **intents** from the View (Compose) -> ViewModel. Pure data only (no callbacks).
 */
sealed interface MapIntent {
    data class CameraIdle(val center: LatLng) : MapIntent
    data object PrimaryActionClicked : MapIntent
    data object ErrorConsumed : MapIntent
    data class MoveCameraTo(val latLng: LatLng) : MapIntent
}

/**
 * MVI: one-shot **effects** from ViewModel -> View (navigation, etc.).
 */
sealed interface MapEffect {
    data object NavigateToBooking : MapEffect
}
