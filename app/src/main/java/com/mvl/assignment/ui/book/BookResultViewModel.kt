package com.mvl.assignment.ui.book

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.mvl.assignment.domain.usecase.GetBookUseCase
import com.mvl.assignment.domain.usecase.ResetSelectionUseCase
import com.mvl.assignment.ui.common.toUiText
import com.mvl.assignment.ui.navigation.BookResultRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getBook: GetBookUseCase,
    private val resetSelection: ResetSelectionUseCase,
) : ViewModel() {

    private val bookId: String = savedStateHandle.toRoute<BookResultRoute>().bookId

    private val _uiState = MutableStateFlow<BookResultUiState>(BookResultUiState.Loading)
    val uiState: StateFlow<BookResultUiState> = _uiState.asStateFlow()

    private val _events = Channel<BookResultEvent>(Channel.BUFFERED)
    val events: Flow<BookResultEvent> = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onRetry() = load()

    /** V button: the trip is booked, so clear A/B and show this month's history. */
    fun onConfirm() = finish(BookResultEvent.OpenHistory)

    /** System back: return to the map in its initial state. */
    fun onBack() = finish(BookResultEvent.NavigateBack)

    private fun load() {
        if (loadJob?.isActive == true) return
        _uiState.value = BookResultUiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = getBook(bookId).fold(
                onSuccess = { BookResultUiState.Loaded(it) },
                onFailure = { BookResultUiState.Error(it.toUiText()) },
            )
        }
    }

    private fun finish(event: BookResultEvent) {
        resetSelection()
        viewModelScope.launch { _events.send(event) }
    }
}
