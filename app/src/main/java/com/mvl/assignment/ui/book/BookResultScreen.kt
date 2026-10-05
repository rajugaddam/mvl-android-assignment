package com.mvl.assignment.ui.book

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mvl.assignment.R
import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.ui.common.ObserveAsEvents
import com.mvl.assignment.ui.common.formatPrice
import com.mvl.assignment.ui.components.BottomPrimaryButton
import com.mvl.assignment.ui.components.InfoRow
import com.mvl.assignment.ui.components.LoadingContent
import com.mvl.assignment.ui.components.MessageContent
import com.mvl.assignment.ui.components.PlaceHeader
import com.mvl.assignment.ui.components.aqiText
import com.mvl.assignment.ui.theme.Dimens

/** Screen 3: booked A/B with the server-computed price. */
@Composable
fun BookResultScreen(
    onNavigateBack: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookResultViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // System back resets the selection before leaving (spec: map returns to "Set A").
    BackHandler(onBack = viewModel::onBack)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            BookResultEvent.NavigateBack -> onNavigateBack()
            BookResultEvent.OpenHistory -> onOpenHistory()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        when (val current = state) {
            BookResultUiState.Loading -> LoadingContent(modifier = Modifier.weight(1f))

            is BookResultUiState.Error -> MessageContent(
                message = current.message.asString(),
                onRetry = viewModel::onRetry,
                modifier = Modifier.weight(1f),
            )

            is BookResultUiState.Loaded -> BookDetails(
                book = current.book,
                onConfirm = viewModel::onConfirm,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BookDetails(
    book: Book,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            PlaceSection(slot = Slot.A, place = book.a)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            PlaceSection(slot = Slot.B, place = book.b)
        }

        Column(
            modifier = Modifier.padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLarge),
        ) {
            PriceRow(price = book.price)
            BottomPrimaryButton(text = stringResource(R.string.action_confirm), onClick = onConfirm)
        }
    }
}

/** "A  location name" with aqi and nickname rows underneath. */
@Composable
private fun PlaceSection(slot: Slot, place: Place) {
    Column(modifier = Modifier.padding(Dimens.ScreenPadding)) {
        PlaceHeader(slot = slot, name = place.name)
        InfoRow(
            label = stringResource(R.string.label_aqi),
            value = aqiText(place.aqi),
            modifier = Modifier.padding(top = Dimens.SpacingSmall),
        )
        InfoRow(
            label = stringResource(R.string.label_nickname),
            value = place.nickname ?: stringResource(R.string.value_none),
        )
    }
}

/** "price" in grey on the left, the amount in bold on the right. */
@Composable
private fun PriceRow(price: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.label_price),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = formatPrice(price), style = MaterialTheme.typography.titleMedium)
    }
}
