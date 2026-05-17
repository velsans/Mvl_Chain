package com.tadamaps.mobile.presentation.booking

import com.mvlchain.domain.model.BookingResult
import com.mvlchain.domain.model.MapLocation

/**
 * MVVM: UI state for the booking result screen.
 */
sealed interface BookingUiState {
    data object Idle : BookingUiState
    data class Loading(
        val locationA: MapLocation,
        val locationB: MapLocation,
    ) : BookingUiState
    data class Error(
        val message: String,
        val locationA: MapLocation?,
        val locationB: MapLocation?,
    ) : BookingUiState
    data class IdleAfterError(
        val locationA: MapLocation,
        val locationB: MapLocation,
    ) : BookingUiState
    data class Success(val result: BookingResult) : BookingUiState
}

/**
 * MVVM: user intents → ViewModel.
 */
sealed interface BookingUserEvent {
    /** Loads payload from [com.tadamaps.mobile.presentation.navigation.BookingNavigator] and submits. */
    data object StartBooking : BookingUserEvent
    data object ViewHistoryClicked : BookingUserEvent
    data object BackToMapClicked : BookingUserEvent
    data object ErrorAcknowledged : BookingUserEvent
}

/**
 * MVVM: one-shot effects → View (navigation).
 */
sealed interface BookingEffect {
    data object NavigateToHistory : BookingEffect
    data object NavigateBack : BookingEffect
}
