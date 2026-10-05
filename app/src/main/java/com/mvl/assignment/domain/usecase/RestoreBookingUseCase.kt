package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.repository.AirQualityRepository
import com.mvl.assignment.domain.repository.SelectionRepository
import com.mvl.assignment.domain.util.runCatchingCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Re-uses a past booking's A and B (names and nicknames) as the current selection.
 * AQI is time-sensitive, so it is fetched again for both places; the map then shows "Book".
 */
class RestoreBookingUseCase @Inject constructor(
    private val airQualityRepository: AirQualityRepository,
    private val selectionRepository: SelectionRepository,
) {
    suspend operator fun invoke(book: Book): Result<Selection> = runCatchingCancellable {
        coroutineScope {
            val aqiA = async { airQualityRepository.getAirQuality(book.a.coordinate).getOrThrow() }
            val aqiB = async { airQualityRepository.getAirQuality(book.b.coordinate).getOrThrow() }
            Selection(
                a = book.a.copy(aqi = aqiA.await()),
                b = book.b.copy(aqi = aqiB.await()),
            )
        }
    }.onSuccess { restored ->
        selectionRepository.update { restored }
    }
}
