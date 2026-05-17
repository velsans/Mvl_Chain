package com.tadamaps.mobile.presentation.detail

import com.mvlchain.domain.model.LocationSlot
import com.mvlchain.domain.model.MapLocation

/**
 * MVVM: UI state for location nickname / detail.
 */
data class LocationDetailUiState(
    val slot: LocationSlot,
    val location: MapLocation?,
    val nickname: String = "",
    val error: String? = null,
) {
    val characterCount: Int get() = nickname.length
    val isNicknameTooLong: Boolean get() = nickname.length > MAX_NICKNAME_LENGTH

    companion object {
        const val MAX_NICKNAME_LENGTH = 20
    }
}

/**
 * MVVM: user intents → ViewModel.
 */
sealed interface LocationDetailUserEvent {
    data class Hydrate(val slot: LocationSlot, val location: MapLocation?) : LocationDetailUserEvent
    data class NicknameChanged(val value: String) : LocationDetailUserEvent
    data object SaveClicked : LocationDetailUserEvent
    data object ErrorDismissed : LocationDetailUserEvent
}

/**
 * MVVM: one-shot effects → View (e.g. navigate back after save).
 */
sealed interface LocationDetailEffect {
    data object NavigateBack : LocationDetailEffect
}
