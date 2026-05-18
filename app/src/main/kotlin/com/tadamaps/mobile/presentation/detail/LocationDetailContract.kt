package com.tadamaps.mobile.presentation.detail

import com.mvlchain.domain.model.LocationSlot
import com.mvlchain.domain.model.MapLocation

/**
 * MVI: UI state for location nickname / detail.
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
 * MVI: user intents -> ViewModel (pure data).
 */
sealed interface LocationDetailIntent {
    data class Hydrate(val slot: LocationSlot, val location: MapLocation?) : LocationDetailIntent
    data class NicknameChanged(val value: String) : LocationDetailIntent
    data object SaveClicked : LocationDetailIntent
    data object ErrorDismissed : LocationDetailIntent
}

/**
 * MVI: one-shot effects -> View (e.g. navigate back after save).
 */
sealed interface LocationDetailEffect {
    data object NavigateBack : LocationDetailEffect
}
