package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.model.SelectionCompleteException
import com.mvl.assignment.domain.repository.SelectionRepository
import javax.inject.Inject

/** Stores [Place] in the next empty slot: Set A → Set B → Book. */
class SelectPlaceUseCase @Inject constructor(
    private val selectionRepository: SelectionRepository,
) {
    operator fun invoke(place: Place): Result<Selection> {
        var result: Result<Selection>? = null
        // The transform may be retried on concurrent updates, so result is set on every attempt.
        selectionRepository.update { current ->
            val slot = current.nextSlot
            if (slot == null) {
                result = Result.failure(SelectionCompleteException())
                current
            } else {
                current.with(slot, place).also { result = Result.success(it) }
            }
        }
        return checkNotNull(result)
    }
}
