package com.mvl.assignment.data.remote.dto

import kotlinx.serialization.Serializable

/** BigDataCloud `GET /data/reverse-geocode-client` response (only the fields we use). */
@Serializable
data class ReverseGeocodeResponse(
    val city: String? = null,
    val locality: String? = null,
    val localityInfo: LocalityInfoDto? = null,
)

@Serializable
data class LocalityInfoDto(
    val administrative: List<AdministrativeDto> = emptyList(),
)

@Serializable
data class AdministrativeDto(
    val name: String? = null,
    val order: Int? = null,
)
