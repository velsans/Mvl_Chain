package com.tadamaps.mobile.presentation.booking

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.tadamaps.mobile.R
import com.mvlchain.domain.model.BookingResult
import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.model.MapLocation
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
        topBar = { TopAppBar(title = { Text("Booking") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = padH),
            verticalArrangement = Arrangement.spacedBy(blockSpacing),
        ) {
            when (val current = uiState) {
                BookingUiState.Idle,
                BookingUiState.Loading,
                -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    if (current is BookingUiState.Loading) {
                        Text("Placing booking…", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                is BookingUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(blockSpacing),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = current.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Button(onClick = onBackToMap) {
                                Text("Back")
                            }
                        }
                    }
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
                            LocationSummaryBlock(
                                label = "A",
                                location = result.locationA,
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            LocationSummaryBlock(
                                label = "B",
                                location = result.locationB,
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

    LaunchedEffect(Unit) {
        viewModel.onUserEvent(BookingUserEvent.StartBooking)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                BookingEffect.NavigateToHistory -> navController.navigate(Routes.History)
                BookingEffect.NavigateBack -> {
                    val mapEntry = navController.getBackStackEntry(Routes.Map)
                    mapEntry.savedStateHandle[MapViewModel.RESET_KEY] = true
                    navController.popBackStack()
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
}

@Composable
private fun LocationSummaryBlock(
    label: String,
    location: MapLocation,
) {
    val rowSpacing = dimensionResource(R.dimen.mvl_row_spacing)
    Column(verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
        Text(
            "$label ${location.formattedAddress}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
        Text(
            "aqi ${location.airQualityIndex ?: "0"}",
            style = MaterialTheme.typography.bodyMedium,
        )
        location.nickname?.takeIf { it.isNotBlank() }?.let { nick ->
            Text(
                nick,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun previewBookingSuccess(): BookingUiState.Success {
    val a = MapLocation(GeoCoordinate(0.0, 0.0), "Balam Rd", 80, "home")
    val b = MapLocation(GeoCoordinate(0.0, 0.0), "Circuit Rd", 72, "office")
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
            uiState = BookingUiState.Loading,
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
        BookingScreenContent(
            uiState = BookingUiState.Error("Network error"),
            onBackToMap = {},
            onViewHistory = {},
        )
    }
}
