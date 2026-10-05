package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.domain.repository.SelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveSelectionUseCase @Inject constructor(
    private val selectionRepository: SelectionRepository,
) {
    operator fun invoke(): StateFlow<Selection> = selectionRepository.selection
}

class ObservePlaceUseCase @Inject constructor(
    private val selectionRepository: SelectionRepository,
) {
    operator fun invoke(slot: Slot): Flow<Place?> =
        selectionRepository.selection.map { it[slot] }.distinctUntilChanged()
}
