package com.tadamaps.mobile.presentation.history

import com.mvlchain.domain.model.BookHistoryItem

/**
 * MVI: UI state for booking history.
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
 * MVI: user intents -> ViewModel (pure data).
 */
sealed interface HistoryIntent {
    data object Refresh : HistoryIntent
    data class ItemClicked(val item: BookHistoryItem) : HistoryIntent
    data object ErrorAcknowledged : HistoryIntent
}

/**
 * MVI: one-shot effects -> View (restore map + pop stack).
 */
sealed interface HistoryEffect {
    data class RestoreBookingOnMap(val item: BookHistoryItem) : HistoryEffect
}
