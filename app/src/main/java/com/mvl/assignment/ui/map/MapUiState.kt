package com.mvl.assignment.ui.map

import androidx.compose.runtime.Immutable
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.SelectionStep
import com.mvl.assignment.ui.common.UiText

@Immutable
data class MapUiState(
    /** Label of slot A (nickname or address); `null` while empty. */
    val aLabel: String? = null,
    val bLabel: String? = null,
    /** Drives the main button text: Set A → Set B → Book. */
    val step: SelectionStep = SelectionStep.SET_A,
    val aqi: AqiUiState = AqiUiState.Idle,
    /** A place is being resolved or a booking is being sent; the main button ignores taps. */
    val isSubmitting: Boolean = false,
    val message: MapMessage? = null,
    /** No Maps key configured: the map renders blank, so explain why. */
    val isMapsKeyMissing: Boolean = false,
)

sealed interface AqiUiState {
    data object Idle : AqiUiState
    data object Loading : AqiUiState
    data class Value(val aqi: Int?) : AqiUiState
    data object Error : AqiUiState
}

/** Error shown over the map; [retryable] errors re-run the main action. */
@Immutable
data class MapMessage(
    val text: UiText,
    val retryable: Boolean,
)

sealed interface MapEvent {
    data class MoveCamera(val coordinate: Coordinate) : MapEvent
    data class OpenBooking(val bookId: String) : MapEvent
}
