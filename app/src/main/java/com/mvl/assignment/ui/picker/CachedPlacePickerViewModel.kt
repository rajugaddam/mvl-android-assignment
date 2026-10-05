package com.mvl.assignment.ui.picker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.domain.usecase.GetCachedPlacesUseCase
import com.mvl.assignment.domain.usecase.SetSlotPlaceUseCase
import com.mvl.assignment.ui.common.WhileUiSubscribed
import com.mvl.assignment.ui.common.toUiText
import com.mvl.assignment.ui.navigation.CachedPickerRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CachedPlacePickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCachedPlaces: GetCachedPlacesUseCase,
    private val setSlotPlace: SetSlotPlaceUseCase,
) : ViewModel() {

    private val slot: Slot = savedStateHandle.toRoute<CachedPickerRoute>().slot
    private var picked = false

    val uiState: StateFlow<CachedPlacePickerUiState> = getCachedPlaces()
        .map<List<Place>, CachedPlacePickerUiState> { CachedPlacePickerUiState.Loaded(it) }
        .catch { emit(CachedPlacePickerUiState.Error(it.toUiText())) }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = CachedPlacePickerUiState.Loading,
        )

    private val _events = Channel<CachedPlacePickerEvent>(Channel.BUFFERED)
    val events: Flow<CachedPlacePickerEvent> = _events.receiveAsFlow()

    /** Fills the slot with the chosen place and returns to the map (single pick per screen). */
    fun onPlaceClick(place: Place) {
        if (picked) return
        picked = true
        setSlotPlace(slot, place)
        viewModelScope.launch { _events.send(CachedPlacePickerEvent.Done) }
    }
}
