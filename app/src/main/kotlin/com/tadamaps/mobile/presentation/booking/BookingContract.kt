package com.tadamaps.mobile.presentation.booking

import com.mvlchain.domain.model.BookingResult
import com.mvlchain.domain.model.MapLocation

/**
 * MVI: UI state for the booking result screen.
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
 * MVI: user intents -> ViewModel (pure data).
 */
sealed interface BookingIntent {
    /** Loads payload from [com.tadamaps.mobile.presentation.navigation.BookingNavigator] and submits. */
    data object StartBooking : BookingIntent
    data object ViewHistoryClicked : BookingIntent
    data object BackToMapClicked : BookingIntent
    data object ErrorAcknowledged : BookingIntent
}

/**
 * MVI: one-shot effects -> View (navigation).
 */
sealed interface BookingEffect {
    data object NavigateToHistory : BookingEffect
    data object NavigateBack : BookingEffect
}
