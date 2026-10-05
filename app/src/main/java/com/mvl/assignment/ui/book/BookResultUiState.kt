package com.mvl.assignment.ui.book

import androidx.compose.runtime.Immutable
import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.ui.common.UiText

sealed interface BookResultUiState {
    data object Loading : BookResultUiState

    /** [book] is the server response of `POST /books` (price included), not the request. */
    @Immutable
    data class Loaded(val book: Book) : BookResultUiState

    data class Error(val message: UiText) : BookResultUiState
}

sealed interface BookResultEvent {
    /** Selection was reset; return to an empty map ("Set A"). */
    data object NavigateBack : BookResultEvent

    data object OpenHistory : BookResultEvent
}
