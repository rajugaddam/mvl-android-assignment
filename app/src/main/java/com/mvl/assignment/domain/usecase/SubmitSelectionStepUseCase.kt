package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.model.SelectionStep
import com.mvl.assignment.domain.repository.SelectionRepository
import javax.inject.Inject

/** What the main (V) button achieved. */
sealed interface StepOutcome {
    /** Set A / Set B: the place at the marker was resolved and stored. */
    data class PlaceSet(val selection: Selection) : StepOutcome

    /** Book: `POST /books` succeeded. */
    data class Booked(val book: Book) : StepOutcome
}

/**
 * The V button's rule: while A or B is empty, resolve the marker position and store it in the next
 * slot (Set A → Set B); once both are set, book the trip.
 */
class SubmitSelectionStepUseCase @Inject constructor(
    private val selectionRepository: SelectionRepository,
    private val resolvePlace: ResolvePlaceUseCase,
    private val selectPlace: SelectPlaceUseCase,
    private val bookTrip: BookTripUseCase,
) {
    suspend operator fun invoke(markerPosition: Coordinate): Result<StepOutcome> =
        when (selectionRepository.selection.value.step) {
            SelectionStep.SET_A, SelectionStep.SET_B ->
                resolvePlace(markerPosition)
                    .mapCatching { place -> StepOutcome.PlaceSet(selectPlace(place).getOrThrow()) }

            SelectionStep.BOOK ->
                bookTrip().map { StepOutcome.Booked(it) }
        }
}
