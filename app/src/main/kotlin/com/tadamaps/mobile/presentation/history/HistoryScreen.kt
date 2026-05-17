package com.tadamaps.mobile.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.tadamaps.mobile.R
import com.mvlchain.domain.model.BookHistoryItem
import com.mvlchain.domain.model.GeoCoordinate
import com.mvlchain.domain.model.MapLocation
import com.tadamaps.mobile.presentation.map.MapViewModel
import com.tadamaps.mobile.presentation.navigation.Routes
import com.tadamaps.mobile.presentation.theme.MvlTheme
import java.text.NumberFormat
import java.util.Locale

private fun formatHistoryTotalPrice(price: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.getDefault())
    nf.maximumFractionDigits = 0
    nf.minimumFractionDigits = 0
    return nf.format(price)
}

/**
 * Stateless history UI for previews and [HistoryScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryScreenContent(
    uiState: HistoryUiState,
    onItemClick: (BookHistoryItem) -> Unit,
) {
    val padH = dimensionResource(R.dimen.mvl_screen_padding_horizontal)
    val listSpacing = dimensionResource(R.dimen.mvl_list_spacing)
    val headerPadV = dimensionResource(R.dimen.mvl_history_header_padding_vertical)
    val dividerPadV = dimensionResource(R.dimen.mvl_history_divider_padding_vertical)
    val dividerThickness = dimensionResource(R.dimen.mvl_divider_thickness)
    val listNoSpace = dimensionResource(R.dimen.mvl_spacing_none)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = padH),
            verticalArrangement = Arrangement.spacedBy(listSpacing),
        ) {
            when (val current = uiState) {
                HistoryUiState.Loading -> CircularProgressIndicator()

                is HistoryUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = current.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                is HistoryUiState.Loaded -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = headerPadV, bottom = headerPadV),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.history_total_count_label),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${current.totalCount}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.history_total_price_label),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = formatHistoryTotalPrice(current.totalPrice),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(listNoSpace),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(
                            items = current.items,
                            key = { _, item -> item.id },
                        ) { index, item ->
                            HistoryBookingGroup(
                                item = item,
                                onClick = { onItemClick(item) },
                            )
                            if (index < current.items.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = dividerPadV),
                                    thickness = dividerThickness,
                                    color = MaterialTheme.colorScheme.outlineVariant,
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
 * MVVM: History **View** — [HistoryViewModel], events/effects.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavHostController) {
    val viewModel: HistoryViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mapViewModel: MapViewModel = hiltViewModel(
        navController.getBackStackEntry(Routes.Map),
    )

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onUserEvent(HistoryUserEvent.Refresh)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HistoryEffect.RestoreBookingOnMap -> {
                    navController.getBackStackEntry(Routes.Map).savedStateHandle[MapViewModel.RESET_KEY] = false
                    mapViewModel.applyRestoration(effect.item)
                    navController.popBackStack(Routes.Map, false)
                }
            }
        }
    }

    HistoryScreenContent(
        uiState = uiState,
        onItemClick = { viewModel.onUserEvent(HistoryUserEvent.ItemClicked(it)) },
    )
}

@Composable
private fun HistoryBookingGroup(
    item: BookHistoryItem,
    onClick: () -> Unit,
) {
    val rowPadV = dimensionResource(R.dimen.mvl_history_row_padding_vertical)
    val rowSpacing = dimensionResource(R.dimen.mvl_row_spacing)
    val labelColumnWidth = dimensionResource(R.dimen.mvl_history_label_column_width)
    val labelPadEnd = dimensionResource(R.dimen.mvl_spacing_xs)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = rowPadV),
        verticalArrangement = Arrangement.spacedBy(rowSpacing),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Text(
                text = stringResource(R.string.map_slot_letter_a),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .width(labelColumnWidth)
                    .padding(end = labelPadEnd)
                    .alignByBaseline(),
            )
            Text(
                text = item.locationA.formattedAddress,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.alignByBaseline(),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Text(
                text = stringResource(R.string.map_slot_letter_b),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .width(labelColumnWidth)
                    .padding(end = labelPadEnd)
                    .alignByBaseline(),
            )
            Text(
                text = item.locationB.formattedAddress,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private fun previewHistoryItems(): List<BookHistoryItem> {
    val a1 = MapLocation(GeoCoordinate(0.0, 0.0), "Bengaluru South City Corporation", 80, null)
    val b1 = MapLocation(GeoCoordinate(0.0, 0.0), "Maddikera (East) mandal", 72, null)
    val a2 = MapLocation(GeoCoordinate(0.0, 0.0), "Maddikera (East) mandal", 65, null)
    val b2 = MapLocation(GeoCoordinate(0.0, 0.0), "Bengaluru South Taluk", 70, null)
    val a3 = MapLocation(GeoCoordinate(0.0, 0.0), "Ongal", 60, null)
    val b3 = MapLocation(GeoCoordinate(0.0, 0.0), "Poonamalle taluk", 62, null)
    return listOf(
        BookHistoryItem("1", a1, b1, 15_000.0, 2026, 5),
        BookHistoryItem("2", a2, b2, 15_000.0, 2026, 5),
        BookHistoryItem("3", a3, b3, 15_000.0, 2026, 5),
    )
}

@Preview(name = "History · loaded", showBackground = true, showSystemUi = true)
@Composable
private fun HistoryScreenPreviewLoaded() {
    val items = previewHistoryItems()
    MvlTheme {
        HistoryScreenContent(
            uiState = HistoryUiState.Loaded(
                items = items,
                totalCount = items.size,
                totalPrice = items.sumOf { it.price },
            ),
            onItemClick = {},
        )
    }
}

@Preview(name = "History · loading", showBackground = true, showSystemUi = true)
@Composable
private fun HistoryScreenPreviewLoading() {
    MvlTheme {
        HistoryScreenContent(
            uiState = HistoryUiState.Loading,
            onItemClick = {},
        )
    }
}

@Preview(name = "History · error", showBackground = true, showSystemUi = true)
@Composable
private fun HistoryScreenPreviewError() {
    MvlTheme {
        HistoryScreenContent(
            uiState = HistoryUiState.Error("Could not load history"),
            onItemClick = {},
        )
    }
}
