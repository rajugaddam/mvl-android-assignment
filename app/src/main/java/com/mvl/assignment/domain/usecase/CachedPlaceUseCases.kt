package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.domain.repository.PlaceCacheRepository
import com.mvl.assignment.domain.repository.SelectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Places resolved before (Screen 5 list), most recent first. */
class GetCachedPlacesUseCase @Inject constructor(
    private val placeCacheRepository: PlaceCacheRepository,
) {
    operator fun invoke(): Flow<List<Place>> = placeCacheRepository.observeAll()
}

/** Puts [Place] into a specific slot (used when picking a cached place for an empty A or B). */
class SetSlotPlaceUseCase @Inject constructor(
    private val selectionRepository: SelectionRepository,
) {
    operator fun invoke(slot: Slot, place: Place): Selection {
        selectionRepository.update { it.with(slot, place) }
        return selectionRepository.selection.value
    }
}
