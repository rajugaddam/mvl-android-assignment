package com.mvl.assignment.domain

import com.mvl.assignment.domain.model.GeocodedAddress
import javax.inject.Inject

/**
 * Builds the location name from reverse geocoding: the two most specific administrative
 * areas, broad → specific, e.g. "Seocho District, Yangjae 2(i)-dong".
 * Falls back to city / locality when fewer than two areas are available.
 */
class AddressFormatter @Inject constructor() {

    fun format(address: GeocodedAddress): String? {
        val names = address.administrative
            .filter { it.name.isNotBlank() }
            .sortedByDescending { it.order }
            .take(PART_COUNT)
            .sortedBy { it.order }
            .map { it.name.trim() }
            .toMutableList()

        if (names.size < PART_COUNT) {
            listOfNotNull(address.city, address.locality)
                .map(String::trim)
                .filter { it.isNotEmpty() && it !in names }
                .forEach { if (names.size < PART_COUNT) names += it }
        }

        return names.distinct().takeIf { it.isNotEmpty() }?.joinToString(SEPARATOR)
    }

    private companion object {
        const val PART_COUNT = 2
        const val SEPARATOR = ", "
    }
}
