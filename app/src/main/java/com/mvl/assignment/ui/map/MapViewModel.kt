package com.mvl.assignment.ui.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvl.assignment.R
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.usecase.GetAirQualityUseCase
import com.mvl.assignment.domain.usecase.GetCurrentLocationUseCase
import com.mvl.assignment.domain.usecase.ObserveSelectionUseCase
import com.mvl.assignment.domain.usecase.StepOutcome
import com.mvl.assignment.domain.usecase.SubmitSelectionStepUseCase
import com.mvl.assignment.ui.common.UiText
import com.mvl.assignment.ui.common.WhileUiSubscribed
import com.mvl.assignment.ui.common.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val config: MapConfig,
    observeSelection: ObserveSelectionUseCase,
    private val getAirQuality: GetAirQualityUseCase,
    private val submitSelectionStep: SubmitSelectionStepUseCase,
    private val getCurrentLocation: GetCurrentLocationUseCase,
) : ViewModel() {

    private val selection: StateFlow<Selection> = observeSelection()

    /** Latest map center to query; [AqiRequest.attempt] lets "retry" re-run the same coordinate. */
    private val aqiRequest = MutableStateFlow<AqiRequest?>(null)
    private val action = MutableStateFlow(ActionState())

    private val _events = Channel<MapEvent>(Channel.BUFFERED)
    val events: Flow<MapEvent> = _events.receiveAsFlow()

    /**
     * Camera idle → debounce → skip duplicates → transformLatest: a newer position cancels the
     * in-flight request, so a stale AQI can never overwrite a fresh one.
     */
    private val aqiState: StateFlow<AqiUiState> = aqiRequest
        .filterNotNull()
        .debounce(AQI_DEBOUNCE_MILLIS)
        .distinctUntilChanged()
        .transformLatest { request ->
            emit(AqiUiState.Loading)
            emit(
                getAirQuality(request.coordinate).fold(
                    onSuccess = { AqiUiState.Value(it) },
                    onFailure = { AqiUiState.Error },
                ),
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AqiUiState.Idle)

    val uiState: StateFlow<MapUiState> = combine(selection, aqiState, action, ::toUiState)
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = toUiState(selection.value, aqiState.value, action.value),
        )

    fun onCameraIdle(target: Coordinate) {
        aqiRequest.update { current -> if (current?.coordinate == target) current else AqiRequest(target) }
    }

    fun onRetryAqi() {
        aqiRequest.update { it?.copy(attempt = it.attempt + 1) }
    }

    /** Main (V) button; the step rule (Set A → Set B → Book) lives in SubmitSelectionStepUseCase. */
    fun onPrimaryAction(target: Coordinate) {
        if (action.value.isSubmitting) return
        action.value = ActionState(isSubmitting = true)

        viewModelScope.launch {
            val result = submitSelectionStep(target)
            (result.getOrNull() as? StepOutcome.Booked)?.let { _events.send(MapEvent.OpenBooking(it.book.id)) }
            action.value = ActionState(
                message = result.exceptionOrNull()?.let { MapMessage(it.toUiText(), retryable = true) },
            )
        }
    }

    fun onMessageDismissed() {
        action.update { it.copy(message = null) }
    }

    /** Centers the map on the device once per screen instance (not again after rotation). */
    fun onLocationPermissionResult(granted: Boolean) {
        if (savedStateHandle.get<Boolean>(KEY_LOCATION_HANDLED) == true) return
        savedStateHandle[KEY_LOCATION_HANDLED] = true

        if (!granted) {
            showMessage(UiText(R.string.error_location_unavailable))
            return
        }
        viewModelScope.launch {
            getCurrentLocation()
                .onSuccess { _events.send(MapEvent.MoveCamera(it)) }
                .onFailure { showMessage(it.toUiText()) }
        }
    }

    private fun showMessage(text: UiText) {
        action.update { it.copy(message = MapMessage(text, retryable = false)) }
    }

    private fun toUiState(selection: Selection, aqi: AqiUiState, action: ActionState) = MapUiState(
        aLabel = selection.a?.displayName,
        bLabel = selection.b?.displayName,
        step = selection.step,
        aqi = aqi,
        isSubmitting = action.isSubmitting,
        message = action.message,
        isMapsKeyMissing = !config.hasApiKey,
    )

    private data class AqiRequest(val coordinate: Coordinate, val attempt: Int = 0)

    private data class ActionState(
        val isSubmitting: Boolean = false,
        val message: MapMessage? = null,
    )

    private companion object {
        const val AQI_DEBOUNCE_MILLIS = 300L
        const val KEY_LOCATION_HANDLED = "location_handled"
    }
}
