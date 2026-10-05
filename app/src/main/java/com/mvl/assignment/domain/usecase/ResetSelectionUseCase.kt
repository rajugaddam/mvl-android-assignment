package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.repository.SelectionRepository
import javax.inject.Inject

/** Returns the map screen to its initial state ("Set A", empty labels). */
class ResetSelectionUseCase @Inject constructor(
    private val selectionRepository: SelectionRepository,
) {
    operator fun invoke() {
        selectionRepository.update { Selection() }
    }
}
