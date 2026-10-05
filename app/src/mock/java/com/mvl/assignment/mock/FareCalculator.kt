package com.mvl.assignment.mock

import com.mvl.assignment.data.remote.dto.PlaceDto
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

/** Mock server pricing: base fare + per-km rate over the great-circle distance, rounded to 100. */
internal object FareCalculator {
    private const val BASE_FARE = 3_800.0
    private const val FARE_PER_KM = 1_000.0
    private const val ROUNDING_UNIT = 100.0
    private const val EARTH_RADIUS_KM = 6_371.0

    fun price(a: PlaceDto, b: PlaceDto): Double {
        val raw = BASE_FARE + FARE_PER_KM * distanceKm(a, b)
        return (raw / ROUNDING_UNIT).roundToLong() * ROUNDING_UNIT
    }

    private fun distanceKm(a: PlaceDto, b: PlaceDto): Double {
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val dLat = lat2 - lat1
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(h))
    }
}
