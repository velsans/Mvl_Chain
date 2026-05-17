package com.tadamaps.mobile.presentation.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.tadamaps.mobile.R
import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.model.MapLocation
import com.tadamaps.mobile.presentation.common.CommonErrorDialog
import com.tadamaps.mobile.presentation.navigation.Routes
import com.tadamaps.mobile.presentation.theme.MvlTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Best-effort device location for centering the map: high-accuracy `getCurrentLocation`,
 * cached `lastLocation`, then a one-shot `requestLocationUpdates` (helps emulators / cold GPS).
 */
private suspend fun fetchUserLatLng(context: android.content.Context): LatLng? {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    if (!fine && !coarse) return null

    val client = LocationServices.getFusedLocationProviderClient(context)

    suspend fun awaitCurrent(priority: Int): android.location.Location? = try {
        withTimeout(18_000L) {
            client.getCurrentLocation(
                priority,
                CancellationTokenSource().token,
            ).await()
        }
    } catch (_: Exception) {
        null
    }

    awaitCurrent(Priority.PRIORITY_HIGH_ACCURACY)?.let {
        return LatLng(it.latitude, it.longitude)
    }
    awaitCurrent(Priority.PRIORITY_BALANCED_POWER_ACCURACY)?.let {
        return LatLng(it.latitude, it.longitude)
    }

    try {
        client.lastLocation.await()?.let {
            return LatLng(it.latitude, it.longitude)
        }
    } catch (_: Exception) {
    }

    return try {
        withTimeout(18_000L) {
            suspendCancellableCoroutine { cont ->
                val callback = object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        try {
                            client.removeLocationUpdates(this)
                        } catch (_: Exception) {
                        }
                        val loc = result.lastLocation
                        if (!cont.isActive) return
                        if (loc != null) {
                            cont.resume(LatLng(loc.latitude, loc.longitude))
                        } else {
                            cont.resume(null)
                        }
                    }
                }
                try {
                    val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                        .setMaxUpdates(1)
                        .setWaitForAccurateLocation(false)
                        .build()
                    client.requestLocationUpdates(request, callback, Looper.getMainLooper())
                } catch (e: Exception) {
                    if (cont.isActive) cont.resume(null)
                    return@suspendCancellableCoroutine
                }
                cont.invokeOnCancellation {
                    try {
                        client.removeLocationUpdates(callback)
                    } catch (_: Exception) {
                    }
                }
            }
        }
    } catch (_: Exception) {
        null
    }
}

/**
 * Moves the map camera so the center pin sits on the user's coordinates (when the map is ready).
 * Updates the ViewModel via [MapUserEvent.CameraIdle] so AQI/capture use that center.
 */
private suspend fun centerMapOnUser(
    context: android.content.Context,
    mapLoaded: Boolean,
    cameraPositionState: CameraPositionState,
    viewModel: MapViewModel,
) {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    if (!fine && !coarse) return

    val gpsFix = fetchUserLatLng(context)
    val latLng = gpsFix ?: MapDefaults.INITIAL_CENTER
    val zoom = if (gpsFix != null) {
        cameraPositionState.position.zoom.coerceAtLeast(15f)
    } else {
        MapDefaults.INITIAL_ZOOM
    }

    if (mapLoaded) {
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        viewModel.onUserEvent(MapUserEvent.CameraIdle(cameraPositionState.position.target))
    } else {
        viewModel.onUserEvent(MapUserEvent.CameraIdle(latLng))
    }
}

@Composable
private fun aqiCategoryLabel(aqi: Int): String {
    val resId = when {
        aqi <= 50 -> R.string.map_aqi_cat_good
        aqi <= 100 -> R.string.map_aqi_cat_moderate
        aqi <= 150 -> R.string.map_aqi_cat_sensitive
        aqi <= 200 -> R.string.map_aqi_cat_unhealthy
        aqi <= 300 -> R.string.map_aqi_cat_very_unhealthy
        else -> R.string.map_aqi_cat_hazardous
    }
    return stringResource(resId)
}

/**
 * Center pin + A/B sheet + AQI chip (no GoogleMap). Used by [MapScreen] and previews.
 */
@Composable
internal fun MapScreenChrome(
    uiState: MapUiState,
    onLocationAClick: () -> Unit,
    onLocationBClick: () -> Unit,
    onPrimaryFabClick: () -> Unit,
    onOpenGoogleMapsNavigation: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val mapPinSize = dimensionResource(R.dimen.mvl_map_pin_size)
    val mapPinOffsetY = dimensionResource(R.dimen.mvl_map_pin_offset_y)
    val mapAqiMargin = dimensionResource(R.dimen.mvl_map_aqi_margin)
    val mapAqiCorner = dimensionResource(R.dimen.mvl_map_aqi_corner_radius)
    val mapAqiProgress = dimensionResource(R.dimen.mvl_map_aqi_progress_size)
    val mapAqiStroke = dimensionResource(R.dimen.mvl_map_aqi_progress_stroke)
    val mapChipElevation = dimensionResource(R.dimen.mvl_map_chip_elevation)
    val mapSheetTopCorner = dimensionResource(R.dimen.mvl_map_sheet_corner_top)
    val mapSheetElevation = dimensionResource(R.dimen.mvl_map_sheet_elevation)
    val mapSheetPadH = dimensionResource(R.dimen.mvl_map_sheet_padding_horizontal)
    val mapSheetPadV = dimensionResource(R.dimen.mvl_map_sheet_padding_vertical)
    val mapSlotGapBeforeV = dimensionResource(R.dimen.mvl_map_slot_gap_before_v)
    val mapSlotRowHeight = dimensionResource(R.dimen.mvl_map_slot_row_height)
    val mapSlotRowGap = dimensionResource(R.dimen.mvl_map_slot_row_gap)
    val mapSlotTileCorner = dimensionResource(R.dimen.mvl_map_slot_tile_corner)
    val mapSlotTilePadH = dimensionResource(R.dimen.mvl_map_slot_tile_padding_horizontal)
    val mapSlotLabelPadStart = dimensionResource(R.dimen.mvl_map_slot_label_padding_start)
    val mapVCorner = dimensionResource(R.dimen.mvl_map_v_button_corner)
    val mapVBusySize = dimensionResource(R.dimen.mvl_map_v_busy_size)
    val mapVBusyStroke = dimensionResource(R.dimen.mvl_map_v_busy_stroke)
    val vButtonHeight = mapSlotRowHeight + mapSlotRowGap + mapSlotRowHeight
    val primaryActionLabel = when (uiState.step) {
        MapBookingStep.PickA -> stringResource(R.string.map_cta_set_a)
        MapBookingStep.PickB -> stringResource(R.string.map_cta_set_b)
        MapBookingStep.ReadyToBook -> stringResource(R.string.map_cta_book)
    }
    val slotPlaceholder = stringResource(R.string.map_slot_no_address)
    val locationA = uiState.locationA
    val locationB = uiState.locationB
    val hasCapturedA = locationA?.formattedAddress?.isNotBlank() == true
    val hasCapturedB = locationB?.formattedAddress?.isNotBlank() == true
    val lineA = locationA?.takeIf { hasCapturedA }?.formattedAddress
    val lineB = locationB?.takeIf { hasCapturedB }?.formattedAddress
    val nicknameA = locationA?.takeIf { hasCapturedA }?.nickname?.takeIf { it.isNotBlank() }
    val nicknameB = locationB?.takeIf { hasCapturedB }?.nickname?.takeIf { it.isNotBlank() }

    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.ic_map_center_pin),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -mapPinOffsetY)
                .size(mapPinSize),
            contentScale = ContentScale.Fit,
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(mapAqiMargin),
            shape = RoundedCornerShape(mapAqiCorner),
            color = colorResource(R.color.mvl_aqi_card_background),
            shadowElevation = mapChipElevation,
        ) {
            val muted = colorResource(R.color.mvl_aqi_muted)
            val valueColor = colorResource(R.color.mvl_aqi_value)
            val padH = dimensionResource(R.dimen.mvl_map_aqi_card_padding_h)
            val padV = dimensionResource(R.dimen.mvl_map_aqi_card_padding_v)
            val lineGap = dimensionResource(R.dimen.mvl_map_aqi_line_spacing)

            Column(
                modifier = Modifier.padding(horizontal = padH, vertical = padV),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(lineGap),
            ) {
                when {
                    uiState.loadingAqi -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(mapAqiProgress),
                            strokeWidth = mapAqiStroke,
                            color = valueColor,
                        )
                    }
                    uiState.centerAqi != null -> {
                        val aqi = uiState.centerAqi
                        Text(
                            text = stringResource(R.string.map_aqi_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = muted,
                        )
                        Text(
                            text = "${aqi.coerceAtLeast(0)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = valueColor,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = aqiCategoryLabel(aqi.coerceAtLeast(0)),
                            style = MaterialTheme.typography.bodySmall,
                            color = muted,
                            textAlign = TextAlign.Center,
                        )
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.map_aqi_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = muted,
                        )
                        Text(
                            text = stringResource(R.string.map_aqi_no_reading),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = valueColor,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = stringResource(R.string.map_aqi_no_reading),
                            style = MaterialTheme.typography.bodySmall,
                            color = muted,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = mapSheetElevation,
            shape = RoundedCornerShape(
                topStart = mapSheetTopCorner,
                topEnd = mapSheetTopCorner,
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (onOpenGoogleMapsNavigation != null) {
                    TextButton(
                        onClick = onOpenGoogleMapsNavigation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = mapSheetPadH),
                    ) {
                        Text(
                            text = stringResource(R.string.map_open_google_navigation),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = mapSheetPadH, vertical = mapSheetPadV),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val slotTileColor = MaterialTheme.colorScheme.surfaceVariant
                val slotShape = RoundedCornerShape(mapSlotTileCorner)
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .padding(end = mapSlotGapBeforeV),
                    verticalArrangement = Arrangement.spacedBy(mapSlotRowGap),
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(mapSlotRowHeight)
                            .clickable(
                                enabled = hasCapturedA,
                                onClick = onLocationAClick,
                            ),
                        shape = slotShape,
                        color = slotTileColor,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = mapSlotTilePadH),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.map_slot_letter_a),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = mapSlotLabelPadStart),
                                ) {
                                    Text(
                                        text = lineA ?: slotPlaceholder,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    nicknameA?.let { nick ->
                                        Text(
                                            text = nick,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(mapSlotRowHeight)
                            .clickable(
                                enabled = hasCapturedB,
                                onClick = onLocationBClick,
                            ),
                        shape = slotShape,
                        color = slotTileColor,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = mapSlotTilePadH),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(R.string.map_slot_letter_b),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = mapSlotLabelPadStart),
                                ) {
                                    Text(
                                        text = lineB ?: slotPlaceholder,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    nicknameB?.let { nick ->
                                        Text(
                                            text = nick,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(vButtonHeight)
                        .clickable(onClick = onPrimaryFabClick),
                    shape = RoundedCornerShape(mapVCorner),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.busy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(mapVBusySize),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = mapVBusyStroke,
                            )
                        } else {
                            Text(
                                text = primaryActionLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.mvl_spacing_xs)),
                            )
                        }
                    }
                }
            }
            }
        }
    }
}

/**
 * MVVM: Map **View** — observes [MapViewModel.uiState], sends [MapUserEvent], collects [MapEffect].
 */
@Composable
fun MapScreen(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
    viewModel: MapViewModel = hiltViewModel(backStackEntry),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraRelocationEpoch by viewModel.cameraRelocationEpoch.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mapLoaded by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    BackHandler {
        showExitConfirm = true
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(MapDefaults.INITIAL_CENTER, MapDefaults.INITIAL_ZOOM)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            scope.launch { centerMapOnUser(context, mapLoaded, cameraPositionState, viewModel) }
        }
    }

    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    LaunchedEffect(mapLoaded) {
        if (!mapLoaded) return@LaunchedEffect
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            centerMapOnUser(context, mapLoaded = true, cameraPositionState, viewModel)
        } else {
            viewModel.onUserEvent(MapUserEvent.CameraIdle(cameraPositionState.position.target))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                MapEffect.NavigateToBooking -> navController.navigate(Routes.Booking)
            }
        }
    }

    LaunchedEffect(mapLoaded, cameraRelocationEpoch) {
        if (!mapLoaded || cameraRelocationEpoch == 0) return@LaunchedEffect
        val c = viewModel.peekCameraCenter()
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(
                c,
                cameraPositionState.position.zoom.coerceAtLeast(13f),
            ),
        )
    }

    LaunchedEffect(cameraPositionState) {
        var wasMoving = false
        snapshotFlow { cameraPositionState.isMoving }
            .distinctUntilChanged()
            .collect { moving ->
                if (!moving && wasMoving) {
                    viewModel.onUserEvent(MapUserEvent.CameraIdle(cameraPositionState.position.target))
                }
                wasMoving = moving
            }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text(stringResource(R.string.map_exit_title)) },
            text = { Text(stringResource(R.string.map_exit_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirm = false
                        context.findActivity()?.finish()
                    },
                ) {
                    Text(stringResource(R.string.map_exit_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text(stringResource(R.string.map_exit_cancel))
                }
            },
        )
    }

    CommonErrorDialog(
        message = uiState.errorMessage,
        onDismiss = { viewModel.onUserEvent(MapUserEvent.ErrorConsumed) },
    )

    Scaffold(
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.NORMAL,
                    isMyLocationEnabled = false,
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    compassEnabled = true,
                    myLocationButtonEnabled = true,
                ),
                onMapLoaded = {
                    mapLoaded = true
                },
            )
            MapScreenChrome(
                uiState = uiState,
                onLocationAClick = {
                    if (uiState.locationA?.formattedAddress?.isNotBlank() == true) {
                        navController.navigate(Routes.locationDetail("A"))
                    }
                },
                onLocationBClick = {
                    if (uiState.locationB?.formattedAddress?.isNotBlank() == true) {
                        navController.navigate(Routes.locationDetail("B"))
                    }
                },
                onPrimaryFabClick = { viewModel.onUserEvent(MapUserEvent.PrimaryActionClicked) },
                onOpenGoogleMapsNavigation = run {
                    if (uiState.step != MapBookingStep.ReadyToBook) return@run null
                    val locA = uiState.locationA ?: return@run null
                    val locB = uiState.locationB ?: return@run null
                    {
                        context.openGoogleMapsDrivingDirections(
                            LatLng(locA.coordinate.latitude, locA.coordinate.longitude),
                            LatLng(locB.coordinate.latitude, locB.coordinate.longitude),
                        )
                    }
                },
            )
        }
    }
}

@Preview(name = "Map", showBackground = true, showSystemUi = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun MapScreenPreview() {
    MvlTheme {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colorResource(R.color.mvl_preview_map_placeholder)),
            )
            MapScreenChrome(
                uiState = MapUiState(
                    step = MapBookingStep.ReadyToBook,
                    centerAqi = 80,
                    loadingAqi = false,
                    locationA = MapLocation(
                        GeoCoordinate(0.0, 0.0),
                        "Balam Rd",
                        80,
                        "home",
                    ),
                    locationB = MapLocation(
                        GeoCoordinate(0.0, 0.0),
                        "Circuit Rd",
                        72,
                        "office",
                    ),
                ),
                onLocationAClick = {},
                onLocationBClick = {},
                onPrimaryFabClick = {},
                onOpenGoogleMapsNavigation = null,
            )
        }
    }
}

@Preview(name = "Map · AQI loading", showBackground = true, showSystemUi = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun MapScreenPreviewAqiLoading() {
    MvlTheme {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colorResource(R.color.mvl_preview_map_placeholder)),
            )
            MapScreenChrome(
                uiState = MapUiState(loadingAqi = true),
                onLocationAClick = {},
                onLocationBClick = {},
                onPrimaryFabClick = {},
                onOpenGoogleMapsNavigation = null,
            )
        }
    }
}
