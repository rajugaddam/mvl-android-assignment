package com.mvl.assignment.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlaceDto(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val aqi: Int? = null,
    val nickname: String? = null,
)

/** `POST /books` body. */
@Serializable
data class BookRequest(
    val a: PlaceDto,
    val b: PlaceDto,
)

/**
 * `POST /books` response and `GET /books` list item: the request's A/B plus `price`.
 * `id` is an assumed extra field (allowed by the assignment) used to open Screen 3 and key the history list.
 */
@Serializable
data class BookDto(
    val id: String,
    val a: PlaceDto,
    val b: PlaceDto,
    /** Double, as specified by the `POST /books` / `GET /books` contract. */
    val price: Double,
    /** Not part of the contract; the mock backend sets it to place bookings in a month. */
    val createdAt: Long? = null,
)
