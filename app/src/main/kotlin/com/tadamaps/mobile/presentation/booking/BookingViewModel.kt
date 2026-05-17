package com.tadamaps.mobile.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvlchain.domain.model.BookingRequest
import com.mvlchain.domain.usecase.CreateBookingUseCase
import com.tadamaps.mobile.presentation.navigation.BookingNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVVM: booking ViewModel — [uiState], [effects], [onUserEvent].
 */
@HiltViewModel
class BookingViewModel @Inject constructor(
    private val navigator: BookingNavigator,
    private val createBooking: CreateBookingUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Idle)
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _effects = Channel<BookingEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onUserEvent(event: BookingUserEvent) {
        when (event) {
            BookingUserEvent.ErrorAcknowledged -> {
                when (val current = _uiState.value) {
                    is BookingUiState.Error -> {
                        val a = current.locationA
                        val b = current.locationB
                        if (a != null && b != null) {
                            _uiState.value = BookingUiState.IdleAfterError(a, b)
                        } else {
                            _uiState.value = BookingUiState.Idle
                        }
                    }
                    else -> Unit
                }
            }
            BookingUserEvent.StartBooking -> startBooking()
            BookingUserEvent.ViewHistoryClicked -> {
                viewModelScope.launch { _effects.send(BookingEffect.NavigateToHistory) }
            }
            BookingUserEvent.BackToMapClicked -> {
                viewModelScope.launch { _effects.send(BookingEffect.NavigateBack) }
            }
        }
    }

    private fun startBooking() {
        val request = navigator.consume()
        if (request == null) {
            _uiState.value = BookingUiState.Error("Missing booking payload", null, null)
            return
        }
        submit(request)
    }

    private fun submit(request: BookingRequest) {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading(request.locationA, request.locationB)
            createBooking(request).fold(
                onSuccess = { result ->
                    _uiState.update { BookingUiState.Success(result) }
                },
                onFailure = { error ->
                    _uiState.update {
                        BookingUiState.Error(
                            error.message ?: "Unable to complete booking",
                            request.locationA,
                            request.locationB,
                        )
                    }
                },
            )
        }
    }
}
