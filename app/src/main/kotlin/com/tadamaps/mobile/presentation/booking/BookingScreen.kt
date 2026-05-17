package com.tadamaps.mobile.presentation.booking

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.tadamaps.mobile.R
import com.mvlchain.domain.model.BookingResult
import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.model.MapLocation
import com.tadamaps.mobile.presentation.common.CommonErrorDialog
import com.tadamaps.mobile.presentation.common.LocationSummaryBlock
import com.tadamaps.mobile.presentation.map.MapViewModel
import com.tadamaps.mobile.presentation.navigation.Routes
import com.tadamaps.mobile.presentation.theme.MvlTheme

/**
 * Stateless booking UI for previews and [BookingScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookingScreenContent(
    uiState: BookingUiState,
    onBackToMap: () -> Unit,
    onViewHistory: () -> Unit,
) {
    val padH = dimensionResource(R.dimen.mvl_screen_padding_horizontal)
    val blockSpacing = dimensionResource(R.dimen.mvl_block_spacing)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking") },
                navigationIcon = {
                    IconButton(onClick = onBackToMap) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigation_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = padH),
            verticalArrangement = Arrangement.spacedBy(blockSpacing),
        ) {
            when (val current = uiState) {
                BookingUiState.Idle -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                is BookingUiState.Loading -> {
                    SelectedLocationsList(
                        locationA = current.locationA,
                        locationB = current.locationB,
                    )
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text("Placing booking…", style = MaterialTheme.typography.bodyLarge)
                }

                is BookingUiState.IdleAfterError -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        SelectedLocationsList(
                            locationA = current.locationA,
                            locationB = current.locationB,
                        )
                    }
                }

                is BookingUiState.Error -> {
                    val a = current.locationA
                    val b = current.locationB
                    if (a != null && b != null) {
                        SelectedLocationsList(locationA = a, locationB = b)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }

                is BookingUiState.Success -> {
                    val result = current.result
                    val padBottom = dimensionResource(R.dimen.mvl_content_padding_bottom)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = padBottom),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(blockSpacing),
                        ) {
                            SelectedLocationsList(
                                locationA = result.locationA,
                                locationB = result.locationB,
                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(blockSpacing),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("price", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "%.0f".format(result.price),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            Button(
                                onClick = onViewHistory,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            ) {
                                Text(
                                    "V",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
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
 * MVVM: Booking **View** — observes [BookingViewModel.uiState], sends [BookingUserEvent], collects [BookingEffect].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(navController: NavHostController) {
    val viewModel: BookingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mapViewModel: MapViewModel = hiltViewModel(
        navController.getBackStackEntry(Routes.Map),
    )

    LaunchedEffect(Unit) {
        viewModel.onUserEvent(BookingUserEvent.StartBooking)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                BookingEffect.NavigateToHistory -> navController.navigate(Routes.History)
                BookingEffect.NavigateBack -> {
                    mapViewModel.clearMapAfterLeavingFlow()
                    navController.popBackStack(Routes.Map, false)
                }
            }
        }
    }

    BackHandler {
        viewModel.onUserEvent(BookingUserEvent.BackToMapClicked)
    }

    BookingScreenContent(
        uiState = uiState,
        onBackToMap = { viewModel.onUserEvent(BookingUserEvent.BackToMapClicked) },
        onViewHistory = { viewModel.onUserEvent(BookingUserEvent.ViewHistoryClicked) },
    )

    val bookingError = (uiState as? BookingUiState.Error)?.message
    CommonErrorDialog(
        message = bookingError,
        onDismiss = { viewModel.onUserEvent(BookingUserEvent.ErrorAcknowledged) },
    )
}

@Composable
private fun SelectedLocationsList(
    locationA: MapLocation,
    locationB: MapLocation,
) {
    val blockSpacing = dimensionResource(R.dimen.mvl_block_spacing)
    Column(verticalArrangement = Arrangement.spacedBy(blockSpacing)) {
        LocationSummaryBlock(label = "A", location = locationA, showAddressAndNickname = true)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        LocationSummaryBlock(label = "B", location = locationB, showAddressAndNickname = true)
    }
}

private fun previewBookingLocations(): Pair<MapLocation, MapLocation> {
    val a = MapLocation(GeoCoordinate(0.0, 0.0), "123 Balam Rd, Singapore", 80, "home")
    val b = MapLocation(GeoCoordinate(0.0, 0.0), "Circuit Rd", 72, "office")
    return a to b
}

private fun previewBookingSuccess(): BookingUiState.Success {
    val (a, b) = previewBookingLocations()
    return BookingUiState.Success(
        BookingResult(
            id = "preview",
            locationA = a,
            locationB = b,
            price = 24.0,
        ),
    )
}

@Preview(
    name = "Booking · idle",
    showBackground = true,
    showSystemUi = true,
    backgroundColor = 0xFFF0F0F0,
)
@Composable
private fun BookingScreenPreviewIdle() {
    MvlTheme {
        BookingScreenContent(
            uiState = BookingUiState.Idle,
            onBackToMap = {},
            onViewHistory = {},
        )
    }
}

@Preview(
    name = "Booking · loading",
    showBackground = true,
    showSystemUi = true,
    backgroundColor = 0xFFF0F0F0,
)
@Composable
private fun BookingScreenPreviewLoading() {
    MvlTheme {
        BookingScreenContent(
            uiState = BookingUiState.Loading(
                locationA = previewBookingLocations().first,
                locationB = previewBookingLocations().second,
            ),
            onBackToMap = {},
            onViewHistory = {},
        )
    }
}

@Preview(
    name = "Booking · success",
    showBackground = true,
    showSystemUi = true,
    backgroundColor = 0xFFF0F0F0,
)
@Composable
private fun BookingScreenPreviewSuccess() {
    MvlTheme {
        BookingScreenContent(
            uiState = previewBookingSuccess(),
            onBackToMap = {},
            onViewHistory = {},
        )
    }
}

@Preview(
    name = "Booking · error",
    showBackground = true,
    showSystemUi = true,
    backgroundColor = 0xFFF0F0F0,
)
@Composable
private fun BookingScreenPreviewError() {
    MvlTheme {
        Box {
            BookingScreenContent(
                uiState = run {
                    val (a, b) = previewBookingLocations()
                    BookingUiState.Error("Network error", a, b)
                },
                onBackToMap = {},
                onViewHistory = {},
            )
            CommonErrorDialog(message = "Network error", onDismiss = {})
        }
    }
}
