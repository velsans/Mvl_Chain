package com.tadamaps.mobile.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.tadamaps.mobile.presentation.booking.BookingViewModel
import com.tadamaps.mobile.presentation.detail.LocationDetailViewModel
import com.tadamaps.mobile.presentation.history.HistoryViewModel
import com.tadamaps.mobile.presentation.map.MapViewModel
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Central [ViewModelProvider.Factory] wired by **Dagger** (no Hilt).
 */
@Singleton
class MviViewModelFactory @Inject constructor(
    private val mapViewModelProvider: Provider<MapViewModel>,
    private val bookingViewModelProvider: Provider<BookingViewModel>,
    private val historyViewModelProvider: Provider<HistoryViewModel>,
    private val locationDetailViewModelProvider: Provider<LocationDetailViewModel>,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(MapViewModel::class.java) ->
                mapViewModelProvider.get() as T
            modelClass.isAssignableFrom(BookingViewModel::class.java) ->
                bookingViewModelProvider.get() as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                historyViewModelProvider.get() as T
            modelClass.isAssignableFrom(LocationDetailViewModel::class.java) ->
                locationDetailViewModelProvider.get() as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
