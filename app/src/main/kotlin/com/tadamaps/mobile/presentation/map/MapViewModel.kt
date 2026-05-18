package com.tadamaps.mobile.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.tadamaps.mobile.core.dispatcher.IoDispatcher
import com.mvlchain.domain.model.BookingRequest
import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.model.LocationSlot
import com.mvlchain.domain.repository.LocationPreferenceRepository
import com.mvlchain.domain.usecase.BuildMapLocationUseCase
import com.mvlchain.domain.usecase.GetAirQualityUseCase
import com.mvlchain.domain.usecase.ObserveNicknameUseCase
import com.tadamaps.mobile.presentation.navigation.BookingNavigator
import com.tadamaps.mobile.presentation.navigation.MapRestorationStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Map feature **MVI** store — [uiState], [effects], [processIntent].
 *
 * [cameraRelocationEpoch] increments on programmatic camera moves (GPS, reset, history restore)
 * so the map picks up the target after leaving composition (e.g. Booking / Detail).
 */
class MapViewModel @Inject constructor(
    private val getAirQualityUseCase: GetAirQualityUseCase,
    private val buildMapLocation: BuildMapLocationUseCase,
    private val observeNickname: ObserveNicknameUseCase,
    private val bookingNavigator: BookingNavigator,
    private val restorationStore: MapRestorationStore,
    private val locationPreferences: LocationPreferenceRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val lastCameraCenter = MutableStateFlow(MapDefaults.INITIAL_CENTER)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _cameraRelocationEpoch = MutableStateFlow(0)
    val cameraRelocationEpoch: StateFlow<Int> = _cameraRelocationEpoch.asStateFlow()

    private val _effects = Channel<MapEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val centerQueries = MutableStateFlow(
        GeoCoordinate(MapDefaults.INITIAL_CENTER.latitude, MapDefaults.INITIAL_CENTER.longitude),
    )

    init {
        observeNicknames()
        observeCenterAqi()
        restoreFromHistory()
    }

    /**
     * Clears chosen A/B map pins and booking step immediately, then drops saved nicknames so the
     * sheet shows the empty slot placeholder. Use when the user dismisses booking, history, or
     * location detail without restoring a history row.
     */
    fun clearMapAfterLeavingFlow() {
        reset()
        viewModelScope.launch(ioDispatcher) {
            locationPreferences.saveNickname(LocationSlot.A, null)
            locationPreferences.saveNickname(LocationSlot.B, null)
        }
    }

    fun peekCameraCenter(): LatLng = lastCameraCenter.value

    private fun bumpCameraRelocation() {
        _cameraRelocationEpoch.update { it + 1 }
    }

    fun processIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.CameraIdle -> onCameraIdle(intent.center)
            MapIntent.PrimaryActionClicked -> onPrimaryCta()
            MapIntent.ErrorConsumed -> consumeError()
            is MapIntent.MoveCameraTo -> moveTo(intent.latLng)
        }
    }

    private fun onCameraIdle(center: LatLng) {
        lastCameraCenter.value = center
        centerQueries.value = GeoCoordinate(center.latitude, center.longitude)
    }

    private fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun onPrimaryCta() {
        when (_uiState.value.step) {
            MapBookingStep.PickA -> capture(LocationSlot.A)
            MapBookingStep.PickB -> capture(LocationSlot.B)
            MapBookingStep.ReadyToBook -> submitBooking()
        }
    }

    private fun submitBooking() {
        val a = _uiState.value.locationA ?: return
        val b = _uiState.value.locationB ?: return
        bookingNavigator.enqueue(BookingRequest(a, b))
        viewModelScope.launch {
            _effects.send(MapEffect.NavigateToBooking)
        }
    }

    private fun moveTo(latLng: LatLng) {
        viewModelScope.launch {
            lastCameraCenter.value = latLng
            centerQueries.value = GeoCoordinate(latLng.latitude, latLng.longitude)
            bumpCameraRelocation()
        }
    }

    private fun capture(slot: LocationSlot) {
        val snapshot = lastCameraCenter.value
        val coordinate = GeoCoordinate(snapshot.latitude, snapshot.longitude)
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, errorMessage = null) }
            val result = withContext(ioDispatcher) {
                buildMapLocation(coordinate, slot)
            }
            result.fold(
                onSuccess = { location ->
                    val nextStep = when (slot) {
                        LocationSlot.A -> MapBookingStep.PickB
                        LocationSlot.B -> MapBookingStep.ReadyToBook
                    }
                    _uiState.update { state ->
                        when (slot) {
                            LocationSlot.A -> state.copy(
                                locationA = location,
                                step = nextStep,
                                busy = false,
                            )
                            LocationSlot.B -> state.copy(
                                locationB = location,
                                step = nextStep,
                                busy = false,
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            busy = false,
                            errorMessage = error.message ?: "Unable to save location",
                        )
                    }
                },
            )
        }
    }

    private fun observeNicknames() {
        merge(
            observeNickname(LocationSlot.A).mapLatest { LocationSlot.A to it },
            observeNickname(LocationSlot.B).mapLatest { LocationSlot.B to it },
        ).onEach { (slot, nickname) ->
            _uiState.update { state ->
                when (slot) {
                    LocationSlot.A -> state.copy(
                        locationA = state.locationA?.copy(
                            nickname = nickname?.takeIf { it.isNotBlank() },
                        ),
                    )
                    LocationSlot.B -> state.copy(
                        locationB = state.locationB?.copy(
                            nickname = nickname?.takeIf { it.isNotBlank() },
                        ),
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun observeCenterAqi() {
        centerQueries
            .debounce(320)
            .distinctUntilChanged { old, new -> old.roundedKey() == new.roundedKey() }
            .onEach { coordinate ->
                _uiState.update { it.copy(loadingAqi = true) }
                val result = withContext(ioDispatcher) { getAirQualityUseCase(coordinate) }
                result.fold(
                    onSuccess = { aqi ->
                        _uiState.update { it.copy(centerAqi = aqi, loadingAqi = false) }
                    },
                    onFailure = {
                        _uiState.update { it.copy(centerAqi = null, loadingAqi = false) }
                    },
                )
            }
            .launchIn(viewModelScope)
    }

    fun reset() {
        _uiState.value = MapUiState()
        val default = MapDefaults.INITIAL_CENTER
        lastCameraCenter.value = default
        centerQueries.value = GeoCoordinate(default.latitude, default.longitude)
        bumpCameraRelocation()
    }

    suspend fun applyRestoration(item: com.mvlchain.domain.model.BookHistoryItem) {
        val (refreshedA, refreshedB, target) = withContext(ioDispatcher) {
            val aCoord = item.locationA.coordinate
            val bCoord = item.locationB.coordinate
            val aAqi = getAirQualityUseCase(aCoord).getOrNull()
            val bAqi = getAirQualityUseCase(bCoord).getOrNull()
            val aBase = item.locationA.copy(airQualityIndex = aAqi ?: item.locationA.airQualityIndex)
            val bBase = item.locationB.copy(airQualityIndex = bAqi ?: item.locationB.airQualityIndex)
            val nickA = locationPreferences.getNickname(LocationSlot.A)
            val nickB = locationPreferences.getNickname(LocationSlot.B)
            val a = aBase.copy(nickname = nickA?.takeIf { it.isNotBlank() })
            val b = bBase.copy(nickname = nickB?.takeIf { it.isNotBlank() })
            val t = LatLng(a.coordinate.latitude, a.coordinate.longitude)
            Triple(a, b, t)
        }
        withContext(kotlinx.coroutines.Dispatchers.Main.immediate) {
            _uiState.update {
                MapUiState(
                    step = MapBookingStep.ReadyToBook,
                    locationA = refreshedA,
                    locationB = refreshedB,
                )
            }
            lastCameraCenter.value = target
            bumpCameraRelocation()
        }
        centerQueries.value = refreshedA.coordinate
    }

    private fun restoreFromHistory() {
        val pending = restorationStore.consume() ?: return
        viewModelScope.launch {
            applyRestoration(pending)
        }
    }
}
