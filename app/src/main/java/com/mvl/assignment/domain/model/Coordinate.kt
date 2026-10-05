package com.mvl.assignment.domain.model

import java.util.Locale

data class Coordinate(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in -90.0..90.0) { "latitude out of range: $latitude" }
        require(longitude in -180.0..180.0) { "longitude out of range: $longitude" }
    }

    /** Fallback label used when reverse geocoding yields no usable name. */
    fun toDisplayString(): String =
        String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
}
