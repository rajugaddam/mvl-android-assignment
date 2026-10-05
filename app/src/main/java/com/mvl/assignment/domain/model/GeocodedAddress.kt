package com.mvl.assignment.domain.model

/** Raw reverse-geocoding result; turned into a display name by the AddressFormatter. */
data class GeocodedAddress(
    val administrative: List<AdministrativeArea>,
    val city: String?,
    val locality: String?,
)

/** @param order higher = more specific (country < province < district < neighbourhood). */
data class AdministrativeArea(
    val name: String,
    val order: Int,
)
