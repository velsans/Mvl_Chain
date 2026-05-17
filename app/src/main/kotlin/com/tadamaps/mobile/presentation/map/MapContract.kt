package com.tadamaps.mobile.presentation.map

import com.google.android.gms.maps.model.LatLng
import com.mvlchain.domain.model.MapLocation

/**
 * MVVM: immutable UI state for the map feature.
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
 * MVVM: user intents from the View (Compose) → ViewModel.
 */
sealed interface MapUserEvent {
    data class CameraIdle(val center: LatLng) : MapUserEvent
    data object PrimaryActionClicked : MapUserEvent
    data object ErrorConsumed : MapUserEvent
    data class MoveCameraTo(val latLng: LatLng) : MapUserEvent
}

/**
 * MVVM: one-shot side effects from ViewModel → View (navigation, dialogs).
 */
sealed interface MapEffect {
    data object NavigateToBooking : MapEffect
}
