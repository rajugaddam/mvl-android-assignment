package com.mvl.assignment.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.domain.usecase.LimitNicknameInputUseCase
import com.mvl.assignment.domain.usecase.ObservePlaceUseCase
import com.mvl.assignment.domain.usecase.UpdateNicknameUseCase
import com.mvl.assignment.ui.common.UiText
import com.mvl.assignment.ui.common.WhileUiSubscribed
import com.mvl.assignment.ui.common.toUiText
import com.mvl.assignment.ui.navigation.PlaceDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    observePlace: ObservePlaceUseCase,
    private val updateNickname: UpdateNicknameUseCase,
    private val limitNicknameInput: LimitNicknameInputUseCase,
) : ViewModel() {

    private val slot: Slot = savedStateHandle.toRoute<PlaceDetailRoute>().slot

    /**
     * Nickname field text. Held as Compose state (updated synchronously, so the cursor never jumps)
     * and mirrored into SavedStateHandle to survive rotation and process death.
     */
    var nicknameInput by mutableStateOf(savedStateHandle.get<String>(KEY_NICKNAME_INPUT).orEmpty())
        private set

    private val error = MutableStateFlow<UiText?>(null)

    private val _events = Channel<PlaceDetailEvent>(Channel.BUFFERED)
    val events: Flow<PlaceDetailEvent> = _events.receiveAsFlow()

    private val content: Flow<PlaceDetailContent> = observePlace(slot)
        .onEach { place ->
            // Seed the field once with the stored nickname; later edits are the user's.
            if (place != null && savedStateHandle.get<Boolean>(KEY_NICKNAME_SEEDED) != true) {
                savedStateHandle[KEY_NICKNAME_SEEDED] = true
                storeNicknameInput(place.nickname.orEmpty())
            }
        }
        .map { place -> place?.let(PlaceDetailContent::Loaded) ?: PlaceDetailContent.Missing }

    val uiState: StateFlow<PlaceDetailUiState> = combine(content, error) { content, error ->
        PlaceDetailUiState(slot = slot, content = content, error = error)
    }.stateIn(
        scope = viewModelScope,
        started = WhileUiSubscribed,
        initialValue = PlaceDetailUiState(slot = slot),
    )

    fun onNicknameChange(input: String) {
        storeNicknameInput(limitNicknameInput(input))
        error.value = null
    }

    /** V button: save the nickname (blank clears it) and go back to the map. */
    fun onConfirm() {
        updateNickname(slot, nicknameInput)
            .onSuccess { viewModelScope.launch { _events.send(PlaceDetailEvent.Done) } }
            .onFailure { error.value = it.toUiText() }
    }

    private fun storeNicknameInput(value: String) {
        nicknameInput = value
        savedStateHandle[KEY_NICKNAME_INPUT] = value
    }

    private companion object {
        const val KEY_NICKNAME_INPUT = "nickname_input"
        const val KEY_NICKNAME_SEEDED = "nickname_seeded"
    }
}
