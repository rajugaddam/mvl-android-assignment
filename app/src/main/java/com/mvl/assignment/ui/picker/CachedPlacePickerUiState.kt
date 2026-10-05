package com.mvl.assignment.ui.picker

import androidx.compose.runtime.Immutable
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.ui.common.UiText

sealed interface CachedPlacePickerUiState {
    data object Loading : CachedPlacePickerUiState

    @Immutable
    data class Loaded(val places: List<Place>) : CachedPlacePickerUiState

    data class Error(val message: UiText) : CachedPlacePickerUiState
}

sealed interface CachedPlacePickerEvent {
    data object Done : CachedPlacePickerEvent
}
