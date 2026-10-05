package com.mvl.assignment.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * waqi `GET /feed/geo:lat;lng/` response.
 * `data` is an object on success but a plain string (error message) when `status` is "error",
 * and `data.aqi` may be a number or "-", so both are kept as [JsonElement] and parsed leniently.
 */
@Serializable
data class WaqiResponse(
    val status: String,
    val data: JsonElement? = null,
)
