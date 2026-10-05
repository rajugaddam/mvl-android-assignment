package com.mvl.assignment.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.usecase.GetMonthlyHistoryUseCase
import com.mvl.assignment.domain.usecase.RestoreBookingUseCase
import com.mvl.assignment.ui.common.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getMonthlyHistory: GetMonthlyHistoryUseCase,
    private val restoreBooking: RestoreBookingUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _events = Channel<HistoryEvent>(Channel.BUFFERED)
    val events: Flow<HistoryEvent> = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onRetry() = load()

    /** Re-use a past booking's A/B (AQI re-fetched) and go back to the map. */
    fun onBookClick(book: Book) {
        val loaded = _uiState.value as? HistoryUiState.Loaded ?: return
        if (loaded.isRestoring) return
        _uiState.value = loaded.copy(isRestoring = true, restoreError = null)

        viewModelScope.launch {
            restoreBooking(book)
                .onSuccess { _events.send(HistoryEvent.BackToMap) }
                .onFailure { error ->
                    updateLoaded { it.copy(isRestoring = false, restoreError = error.toUiText()) }
                }
        }
    }

    fun onRestoreErrorDismissed() {
        updateLoaded { it.copy(restoreError = null) }
    }

    /** `GET /books?year=&month=` for the current month; ignored while a request is in flight. */
    private fun load() {
        if (loadJob?.isActive == true) return
        _uiState.value = HistoryUiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = getMonthlyHistory().fold(
                onSuccess = { HistoryUiState.Loaded(it) },
                onFailure = { HistoryUiState.Error(it.toUiText()) },
            )
        }
    }

    private fun updateLoaded(transform: (HistoryUiState.Loaded) -> HistoryUiState.Loaded) {
        _uiState.update { state -> if (state is HistoryUiState.Loaded) transform(state) else state }
    }
}
