package com.mvl.assignment.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mvl.assignment.R
import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.model.MonthlyHistory
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.ui.common.ObserveAsEvents
import com.mvl.assignment.ui.common.formatPrice
import com.mvl.assignment.ui.common.labelRes
import com.mvl.assignment.ui.components.ErrorBanner
import com.mvl.assignment.ui.components.LoadingContent
import com.mvl.assignment.ui.components.LoadingOverlay
import com.mvl.assignment.ui.components.MessageContent
import com.mvl.assignment.ui.theme.Dimens

/**
 * Screen 4: this month's bookings with total count and total price. Tapping a booking re-uses
 * its A/B on the map; system back returns to the map.
 */
@Composable
fun HistoryScreen(
    onBackToMap: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            HistoryEvent.BackToMap -> onBackToMap()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        when (val current = state) {
            HistoryUiState.Loading -> LoadingContent()

            is HistoryUiState.Error -> MessageContent(
                message = current.message.asString(),
                onRetry = viewModel::onRetry,
            )

            is HistoryUiState.Loaded -> {
                HistoryContent(history = current.history, onBookClick = viewModel::onBookClick)

                current.restoreError?.let { error ->
                    ErrorBanner(
                        message = error.asString(),
                        onDismiss = viewModel::onRestoreErrorDismissed,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(Dimens.ScreenPadding),
                    )
                }
                if (current.isRestoring) LoadingOverlay()
            }
        }
    }
}

@Composable
private fun HistoryContent(
    history: MonthlyHistory,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TotalsHeader(totalCount = history.totalCount, totalPrice = history.totalPrice)
        HorizontalDivider(
            thickness = Dimens.SectionDividerThickness,
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        if (history.books.isEmpty()) {
            MessageContent(
                message = stringResource(R.string.history_empty),
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items = history.books, key = { it.id }) { book ->
                    HistoryItem(book = book, onClick = { onBookClick(book) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

/** "Total Count | Total Price" columns, label on top and the bold value below. */
@Composable
private fun TotalsHeader(totalCount: Int, totalPrice: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.SpacingLarge),
    ) {
        TotalColumn(
            label = stringResource(R.string.label_total_count),
            value = totalCount.toString(),
            modifier = Modifier.weight(1f),
        )
        TotalColumn(
            label = stringResource(R.string.label_total_price),
            value = formatPrice(totalPrice),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TotalColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

/** One booking: "A  location name" / "B  location name". */
@Composable
private fun HistoryItem(book: Book, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpacingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall),
    ) {
        HistoryPlaceRow(slot = Slot.A, name = book.a.name)
        HistoryPlaceRow(slot = Slot.B, name = book.b.name)
    }
}

@Composable
private fun HistoryPlaceRow(slot: Slot, name: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingLarge)) {
        Text(
            text = stringResource(slot.labelRes()),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}
