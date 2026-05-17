package com.tadamaps.mobile.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.tadamaps.mobile.R
import com.mvlchain.domain.model.LocationSlot
import com.tadamaps.mobile.presentation.common.CommonErrorDialog
import com.tadamaps.mobile.presentation.map.MapViewModel
import com.tadamaps.mobile.presentation.navigation.Routes
import com.tadamaps.mobile.presentation.theme.MvlTheme

/**
 * Stateless location detail UI (previews + [LocationDetailScreen]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LocationDetailScreenContent(
    slotTitle: String,
    addressText: String?,
    aqiText: String,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    error: String?,
    onSave: () -> Unit,
    saveEnabled: Boolean,
    onBack: () -> Unit,
) {
    val padH = dimensionResource(R.dimen.mvl_screen_padding_horizontal)
    val padTop = dimensionResource(R.dimen.mvl_content_padding_top)
    val sectionSpacing = dimensionResource(R.dimen.mvl_section_spacing)
    val padBottom = dimensionResource(R.dimen.mvl_content_padding_bottom)
    val blockSpacing = dimensionResource(R.dimen.mvl_block_spacing)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.padding(top = padTop),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing),
            ) {
                Text(
                    text = slotTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                when {
                    !addressText.isNullOrBlank() -> {
                        Text(
                            text = addressText,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    !saveEnabled -> {
                        Text(
                            text = stringResource(R.string.detail_no_location),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("aqi", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = aqiText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(bottom = padBottom),
                verticalArrangement = Arrangement.spacedBy(blockSpacing),
            ) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = onNicknameChange,
                    placeholder = {
                        Text(stringResource(R.string.detail_nickname_placeholder))
                    },
                    supportingText = {
                        Text("${nickname.length}/${LocationDetailUiState.MAX_NICKNAME_LENGTH}")
                    },
                    singleLine = true,
                    isError = error != null,
                    enabled = saveEnabled,
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let { err ->
                    Text(err, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = onSave,
                    enabled = saveEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.detail_save),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

/**
 * MVVM: Location detail **View** — [LocationDetailViewModel], events/effects.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(
    navController: NavHostController,
    slotArg: String,
    viewModel: LocationDetailViewModel = hiltViewModel(),
) {
    val slot = when (slotArg.uppercase()) {
        "B" -> LocationSlot.B
        else -> LocationSlot.A
    }

    val mapViewModel: MapViewModel = hiltViewModel(
        navController.getBackStackEntry(Routes.Map),
    )
    val mapUiState by mapViewModel.uiState.collectAsStateWithLifecycle()

    val location = when (slot) {
        LocationSlot.A -> mapUiState.locationA
        LocationSlot.B -> mapUiState.locationB
    }

    LaunchedEffect(slot, location?.coordinate?.roundedKey()) {
        viewModel.onUserEvent(LocationDetailUserEvent.Hydrate(slot, location))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LocationDetailEffect.NavigateBack -> navController.popBackStack()
            }
        }
    }

    val leaveDetailWithoutSave: () -> Unit = {
        mapViewModel.clearMapAfterLeavingFlow()
        navController.popBackStack()
    }

    BackHandler(onBack = leaveDetailWithoutSave)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val slotTitle = when (slot) {
        LocationSlot.A -> stringResource(R.string.detail_location_a)
        LocationSlot.B -> stringResource(R.string.detail_location_b)
    }

    LocationDetailScreenContent(
        slotTitle = slotTitle,
        addressText = location?.formattedAddress?.takeIf { it.isNotBlank() },
        aqiText = location?.airQualityIndex?.toString() ?: "—",
        nickname = uiState.nickname,
        onNicknameChange = { viewModel.onUserEvent(LocationDetailUserEvent.NicknameChanged(it)) },
        error = uiState.error,
        onSave = { viewModel.onUserEvent(LocationDetailUserEvent.SaveClicked) },
        saveEnabled = location != null,
        onBack = leaveDetailWithoutSave,
    )

    CommonErrorDialog(
        message = uiState.error,
        onDismiss = { viewModel.onUserEvent(LocationDetailUserEvent.ErrorDismissed) },
    )
}

@Preview(name = "Location detail", showBackground = true, showSystemUi = true)
@Composable
private fun LocationDetailScreenPreview() {
    MvlTheme {
        LocationDetailScreenContent(
            slotTitle = "Location A",
            addressText = "123 Balam Rd, Singapore",
            aqiText = "80",
            nickname = "home",
            onNicknameChange = {},
            error = null,
            onSave = {},
            saveEnabled = true,
            onBack = {},
        )
    }
}

@Preview(name = "Location detail · no location", showBackground = true, showSystemUi = true)
@Composable
private fun LocationDetailScreenPreviewEmpty() {
    MvlTheme {
        LocationDetailScreenContent(
            slotTitle = "Location A",
            addressText = null,
            aqiText = "—",
            nickname = "",
            onNicknameChange = {},
            error = null,
            onSave = {},
            saveEnabled = false,
            onBack = {},
        )
    }
}
