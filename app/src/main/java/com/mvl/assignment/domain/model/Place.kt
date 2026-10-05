package com.mvl.assignment.domain.model

/**
 * A resolved map location.
 *
 * @param name formatted address (see [com.mvl.assignment.domain.AddressFormatter]).
 * @param aqi air quality index; `null` when the station reports no value ("-").
 * @param nickname user-provided label; `null` when not set.
 */
data class Place(
    val coordinate: Coordinate,
    val name: String,
    val aqi: Int?,
    val nickname: String? = null,
) {
    /** Label shown on the A/B boxes: nickname wins over the address. */
    val displayName: String get() = nickname ?: name
}
