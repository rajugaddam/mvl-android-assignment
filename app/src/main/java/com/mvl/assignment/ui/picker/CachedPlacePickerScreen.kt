package com.mvl.assignment.ui.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mvl.assignment.R
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.ui.common.ObserveAsEvents
import com.mvl.assignment.ui.components.LoadingContent
import com.mvl.assignment.ui.components.MessageContent
import com.mvl.assignment.ui.theme.Dimens

/** Screen 5: list of previously resolved places; tapping one fills the empty slot. */
@Composable
fun CachedPlacePickerScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CachedPlacePickerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            CachedPlacePickerEvent.Done -> onDone()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        when (val current = state) {
            CachedPlacePickerUiState.Loading -> LoadingContent(modifier = Modifier.weight(1f))

            is CachedPlacePickerUiState.Error -> MessageContent(
                message = current.message.asString(),
                modifier = Modifier.weight(1f),
            )

            is CachedPlacePickerUiState.Loaded ->
                if (current.places.isEmpty()) {
                    MessageContent(
                        message = stringResource(R.string.picker_empty),
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        // Cached places are unique per coordinate; the key must be a Bundle-saveable type.
                        items(items = current.places, key = { it.coordinate.toString() }) { place ->
                            CachedPlaceRow(place = place, onClick = { viewModel.onPlaceClick(place) })
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
        }
    }
}

@Composable
private fun CachedPlaceRow(place: Place, onClick: () -> Unit) {
    Text(
        text = place.name,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpacingMedium),
    )
}
