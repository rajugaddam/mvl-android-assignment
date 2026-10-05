package com.mvl.assignment.domain

import com.mvl.assignment.domain.model.Coordinate
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * Cache identity of a location: coordinates are "the same" when equal up to the 3rd decimal.
 * Digits are truncated (not rounded), so 37.5642 and 37.5645 share the key "37.564,…".
 */
class PlaceCacheKey @Inject constructor() {

    fun of(coordinate: Coordinate): String =
        "${truncate(coordinate.latitude)},${truncate(coordinate.longitude)}"

    private fun truncate(value: Double): String =
        BigDecimal.valueOf(value).setScale(DECIMALS, RoundingMode.DOWN).toPlainString()

    private companion object {
        const val DECIMALS = 3
    }
}
