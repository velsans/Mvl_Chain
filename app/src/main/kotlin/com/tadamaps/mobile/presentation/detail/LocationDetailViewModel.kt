package com.tadamaps.mobile.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvlchain.domain.model.LocationSlot
import com.mvlchain.domain.model.MapLocation
import com.mvlchain.domain.repository.LocationPreferenceRepository
import com.mvlchain.domain.usecase.SaveNicknameUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVI: location detail store — [uiState], [effects], [processIntent].
 */
class LocationDetailViewModel @Inject constructor(
    private val saveNickname: SaveNicknameUseCase,
    private val preferences: LocationPreferenceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LocationDetailUiState(slot = LocationSlot.A, location = null),
    )
    val uiState: StateFlow<LocationDetailUiState> = _uiState.asStateFlow()

    private val _effects = Channel<LocationDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun processIntent(intent: LocationDetailIntent) {
        when (intent) {
            is LocationDetailIntent.Hydrate -> hydrate(intent.slot, intent.location)
            is LocationDetailIntent.NicknameChanged -> onNicknameChanged(intent.value)
            LocationDetailIntent.SaveClicked -> save()
            LocationDetailIntent.ErrorDismissed -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun hydrate(slot: LocationSlot, location: MapLocation?) {
        viewModelScope.launch {
            val nick = preferences.getNickname(slot).orEmpty()
            _uiState.update {
                it.copy(
                    slot = slot,
                    location = location,
                    nickname = nick,
                    error = null,
                )
            }
        }
    }

    private fun onNicknameChanged(value: String) {
        _uiState.update {
            it.copy(nickname = value.take(LocationDetailUiState.MAX_NICKNAME_LENGTH), error = null)
        }
    }

    private fun save() {
        val current = _uiState.value
        if (current.isNicknameTooLong) {
            _uiState.update { it.copy(error = "Max ${LocationDetailUiState.MAX_NICKNAME_LENGTH} characters") }
            return
        }
        viewModelScope.launch {
            saveNickname(current.slot, current.nickname.trim().takeIf { it.isNotEmpty() })
            _effects.send(LocationDetailEffect.NavigateBack)
        }
    }
}
