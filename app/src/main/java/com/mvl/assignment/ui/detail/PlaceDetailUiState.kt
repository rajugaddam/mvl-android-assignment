package com.mvl.assignment.ui.detail

import androidx.compose.runtime.Immutable
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.ui.common.UiText

@Immutable
data class PlaceDetailUiState(
    val slot: Slot,
    val content: PlaceDetailContent = PlaceDetailContent.Loading,
    val error: UiText? = null,
)

sealed interface PlaceDetailContent {
    data object Loading : PlaceDetailContent
    data class Loaded(val place: Place) : PlaceDetailContent

    /** The slot is empty (e.g. the selection was lost after process death). */
    data object Missing : PlaceDetailContent
}

sealed interface PlaceDetailEvent {
    data object Done : PlaceDetailEvent
}
