package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.AddressFormatter
import com.mvl.assignment.domain.PlaceCacheKey
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.repository.AirQualityRepository
import com.mvl.assignment.domain.repository.GeocodingRepository
import com.mvl.assignment.domain.repository.PlaceCacheRepository
import com.mvl.assignment.domain.util.runCatchingCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Builds a [Place] for [Coordinate]: served from the local cache when a place with the same
 * coordinates (to the 3rd decimal) was resolved before, otherwise the address and AQI are
 * fetched in parallel and the result is cached.
 */
class ResolvePlaceUseCase @Inject constructor(
    private val geocodingRepository: GeocodingRepository,
    private val airQualityRepository: AirQualityRepository,
    private val placeCacheRepository: PlaceCacheRepository,
    private val addressFormatter: AddressFormatter,
    private val placeCacheKey: PlaceCacheKey,
) {
    suspend operator fun invoke(coordinate: Coordinate): Result<Place> {
        val key = placeCacheKey.of(coordinate)
        // A broken cache must never block the network path.
        runCatchingCancellable { placeCacheRepository.get(key) }.getOrNull()?.let { return Result.success(it) }

        return runCatchingCancellable {
            coroutineScope {
                val address = async { geocodingRepository.reverseGeocode(coordinate).getOrThrow() }
                val aqi = async { airQualityRepository.getAirQuality(coordinate).getOrThrow() }
                Place(
                    coordinate = coordinate,
                    name = addressFormatter.format(address.await()) ?: coordinate.toDisplayString(),
                    aqi = aqi.await(),
                )
            }
        }.onSuccess { place ->
            runCatchingCancellable { placeCacheRepository.save(key, place) }
        }
    }
}
