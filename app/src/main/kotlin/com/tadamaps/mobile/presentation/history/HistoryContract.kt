package com.tadamaps.mobile.presentation.history

import com.mvlchain.domain.model.BookHistoryItem

/**
 * MVVM: UI state for booking history.
 */
sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Error(val message: String) : HistoryUiState
    data class Loaded(
        val items: List<BookHistoryItem>,
        val totalCount: Int,
        val totalPrice: Double,
    ) : HistoryUiState
}

/**
 * MVVM: user intents → ViewModel.
 */
sealed interface HistoryUserEvent {
    data object Refresh : HistoryUserEvent
    data class ItemClicked(val item: BookHistoryItem) : HistoryUserEvent
}

/**
 * MVVM: one-shot effects → View (restore map + pop stack).
 */
sealed interface HistoryEffect {
    data class RestoreBookingOnMap(val item: BookHistoryItem) : HistoryEffect
}
