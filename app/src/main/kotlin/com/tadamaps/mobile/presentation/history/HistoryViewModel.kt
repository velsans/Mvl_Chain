package com.tadamaps.mobile.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvlchain.domain.usecase.GetBookingHistoryUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * MVI: history store — [uiState], [effects], [processIntent].
 */
class HistoryViewModel @Inject constructor(
    private val historyUseCase: GetBookingHistoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _effects = Channel<HistoryEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun processIntent(intent: HistoryIntent) {
        when (intent) {
            HistoryIntent.Refresh -> refresh()
            HistoryIntent.ErrorAcknowledged -> {
                _uiState.value = HistoryUiState.Loaded(
                    items = emptyList(),
                    totalCount = 0,
                    totalPrice = 0.0,
                )
            }
            is HistoryIntent.ItemClicked -> {
                viewModelScope.launch {
                    _effects.send(HistoryEffect.RestoreBookingOnMap(intent.item))
                }
            }
        }
    }

    private fun refresh() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            historyUseCase(year, month).fold(
                onSuccess = { items ->
                    val total = items.sumOf { it.price }
                    _uiState.update {
                        HistoryUiState.Loaded(
                            items = items,
                            totalCount = items.size,
                            totalPrice = total,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        HistoryUiState.Error(error.message ?: "Unable to load history")
                    }
                },
            )
        }
    }
}
