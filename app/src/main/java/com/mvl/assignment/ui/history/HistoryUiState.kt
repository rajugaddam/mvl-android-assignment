package com.mvl.assignment.ui.history

import androidx.compose.runtime.Immutable
import com.mvl.assignment.domain.model.MonthlyHistory
import com.mvl.assignment.ui.common.UiText

sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    /**
     * Totals are computed by GetMonthlyHistoryUseCase from the full `GET /books` response.
     * [isRestoring] while a tapped booking's AQI is re-fetched; [restoreError] if that failed.
     */
    @Immutable
    data class Loaded(
        val history: MonthlyHistory,
        val isRestoring: Boolean = false,
        val restoreError: UiText? = null,
    ) : HistoryUiState

    data class Error(val message: UiText) : HistoryUiState
}

sealed interface HistoryEvent {
    /** A past booking was restored as A/B; return to the map (button shows "Book"). */
    data object BackToMap : HistoryEvent
}
