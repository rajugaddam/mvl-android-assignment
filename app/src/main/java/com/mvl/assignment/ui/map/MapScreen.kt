package com.mvl.assignment.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.mvl.assignment.R
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.ui.common.ObserveAsEvents
import com.mvl.assignment.ui.common.actionRes
import com.mvl.assignment.ui.components.ErrorBanner
import com.mvl.assignment.ui.components.PrimaryButton
import com.mvl.assignment.ui.components.SlotLabel
import com.mvl.assignment.ui.components.aqiText
import com.mvl.assignment.ui.theme.Dimens
import kotlinx.coroutines.flow.filter

/** Screen 1: map with a fixed center pin, live AQI and the A/B selection panel. */
@Composable
fun MapScreen(
    onOpenPlace: (Slot) -> Unit,
    onPickCachedPlace: (Slot) -> Unit,
    onOpenBooking: (bookId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val config = viewModel.config
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(config.fallbackTarget.toLatLng(), config.zoom)
    }

    val hasLocationPermission = rememberLocationPermission(onResult = viewModel::onLocationPermissionResult)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is MapEvent.MoveCamera -> cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(event.coordinate.toLatLng(), config.zoom),
            )
            is MapEvent.OpenBooking -> onOpenBooking(event.bookId)
        }
    }

    // Report the pin position whenever the camera stops moving (initial position included).
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .filter { moving -> !moving }
            .collect { viewModel.onCameraIdle(cameraPositionState.centerCoordinate()) }
    }

    MapContent(
        state = state,
        cameraPositionState = cameraPositionState,
        hasLocationPermission = hasLocationPermission,
        onRetryAqi = viewModel::onRetryAqi,
        onFilledSlotClick = onOpenPlace,
        onEmptySlotClick = onPickCachedPlace,
        onPrimaryAction = { viewModel.onPrimaryAction(cameraPositionState.centerCoordinate()) },
        onDismissMessage = viewModel::onMessageDismissed,
        modifier = modifier,
    )
}

@Composable
private fun MapContent(
    state: MapUiState,
    cameraPositionState: CameraPositionState,
    hasLocationPermission: Boolean,
    onRetryAqi: () -> Unit,
    onFilledSlotClick: (Slot) -> Unit,
    onEmptySlotClick: (Slot) -> Unit,
    onPrimaryAction: () -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AqiBar(aqi = state.aqi, onRetry = onRetryAqi)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                // Enabling the location layer without permission throws, so it follows the grant.
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = hasLocationPermission,
                ),
            )

            CenterPin(modifier = Modifier.align(Alignment.Center))

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(Dimens.SpacingSmall),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall),
            ) {
                if (state.isMapsKeyMissing) {
                    ErrorBanner(message = stringResource(R.string.error_missing_key, MAPS_KEY_NAME))
                }
                state.message?.let { message ->
                    ErrorBanner(
                        message = message.text.asString(),
                        onRetry = if (message.retryable) onPrimaryAction else null,
                        onDismiss = onDismissMessage,
                    )
                }
            }
        }

        SelectionPanel(
            state = state,
            onFilledSlotClick = onFilledSlotClick,
            onEmptySlotClick = onEmptySlotClick,
            onPrimaryAction = onPrimaryAction,
        )
    }
}

/** White strip above the map with "aqi  80" on the right. */
@Composable
private fun AqiBar(aqi: AqiUiState, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .height(Dimens.TopBarHeight)
            .padding(horizontal = Dimens.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.label_aqi),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        when (aqi) {
            AqiUiState.Loading -> CircularProgressIndicator(
                modifier = Modifier.size(Dimens.ProgressSmall),
                color = MaterialTheme.colorScheme.secondary,
                strokeWidth = Dimens.ProgressStroke,
            )
            AqiUiState.Error -> TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.action_retry), color = MaterialTheme.colorScheme.error)
            }
            AqiUiState.Idle -> AqiValue(text = stringResource(R.string.aqi_unknown))
            is AqiUiState.Value -> AqiValue(text = aqiText(aqi.aqi))
        }
    }
}

@Composable
private fun AqiValue(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

/** Pin whose tip marks the exact map center (the coordinate used for Set A / Set B). */
@Composable
private fun CenterPin(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_map_pin),
        contentDescription = stringResource(R.string.cd_map_pin),
        tint = MaterialTheme.colorScheme.secondary,
        modifier = modifier
            .size(Dimens.MapPinSize)
            .offset(y = -Dimens.MapPinSize / 2),
    )
}

/**
 * Bottom panel: A and B boxes on the left, the tall yellow action button on the right.
 * A filled box opens its detail (nickname); an empty box opens the saved places list.
 */
@Composable
private fun SelectionPanel(
    state: MapUiState,
    onFilledSlotClick: (Slot) -> Unit,
    onEmptySlotClick: (Slot) -> Unit,
    onPrimaryAction: () -> Unit,
) {
    // Boxes are not tappable while a place is being resolved / a booking is sent.
    fun slotClick(slot: Slot, label: String?): (() -> Unit)? = when {
        state.isSubmitting -> null
        label != null -> { { onFilledSlotClick(slot) } }
        else -> { { onEmptySlotClick(slot) } }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .padding(Dimens.ScreenPadding)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall),
        ) {
            SlotLabel(slot = Slot.A, text = state.aLabel, onClick = slotClick(Slot.A, state.aLabel))
            SlotLabel(slot = Slot.B, text = state.bLabel, onClick = slotClick(Slot.B, state.bLabel))
        }
        PrimaryButton(
            text = stringResource(state.step.actionRes()),
            onClick = onPrimaryAction,
            loading = state.isSubmitting,
            modifier = Modifier
                .width(Dimens.MapActionWidth)
                .fillMaxHeight(),
        )
    }
}

/**
 * Asks for location once per screen (not again after rotation), reports the outcome and
 * returns whether permission is currently granted (as state, so the map updates on grant).
 */
@Composable
private fun rememberLocationPermission(onResult: (granted: Boolean) -> Unit): Boolean {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.hasLocationPermission()) }
    var requested by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        granted = grants.values.any { it }
        onResult(granted)
    }

    LaunchedEffect(Unit) {
        when {
            granted -> onResult(true)
            !requested -> {
                requested = true
                launcher.launch(LOCATION_PERMISSIONS)
            }
        }
    }
    return granted
}

private fun Context.hasLocationPermission(): Boolean = LOCATION_PERMISSIONS.any {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}

private fun CameraPositionState.centerCoordinate(): Coordinate =
    position.target.let { Coordinate(it.latitude, it.longitude) }

private fun Coordinate.toLatLng() = LatLng(latitude, longitude)

private const val MAPS_KEY_NAME = "MAPS_API_KEY"

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)
