package com.mvl.assignment.data.mapper

import com.mvl.assignment.data.local.CachedPlaceEntity
import com.mvl.assignment.data.remote.dto.BookDto
import com.mvl.assignment.data.remote.dto.PlaceDto
import com.mvl.assignment.data.remote.dto.ReverseGeocodeResponse
import com.mvl.assignment.data.remote.dto.WaqiResponse
import com.mvl.assignment.domain.model.AdministrativeArea
import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.GeocodedAddress
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.model.RemoteServiceException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import java.util.Locale

private const val WAQI_STATUS_OK = "ok"

/** waqi has no reading for this spot (e.g. `{"status":"nope","data":"can not connect"}`). */
private const val WAQI_STATUS_NO_DATA = "nope"

/** `geo:<lat>;<lng>` path segment for the waqi geo feed. */
fun Coordinate.toWaqiGeo(): String =
    String.format(Locale.US, "geo:%.6f;%.6f", latitude, longitude)

/**
 * @return the AQI, or `null` when the station reports "-" / no value, or waqi has no data for
 *   the location (`status: "nope"`) — shown as "-" so A/B can still be set anywhere.
 * @throws RemoteServiceException when waqi answers with `status: "error"`.
 */
fun WaqiResponse.toAqi(): Int? {
    if (status == WAQI_STATUS_NO_DATA) return null
    if (status != WAQI_STATUS_OK) {
        val message = (data as? JsonPrimitive)?.contentOrNull ?: "Air quality request failed"
        throw RemoteServiceException(message)
    }
    val aqi = (data as? JsonObject)?.get("aqi") as? JsonPrimitive
    return aqi?.intOrNull
}

fun ReverseGeocodeResponse.toDomain(): GeocodedAddress = GeocodedAddress(
    administrative = localityInfo?.administrative.orEmpty().mapNotNull { dto ->
        val name = dto.name ?: return@mapNotNull null
        val order = dto.order ?: return@mapNotNull null
        AdministrativeArea(name = name, order = order)
    },
    city = city,
    locality = locality,
)

fun Place.toDto(): PlaceDto = PlaceDto(
    latitude = coordinate.latitude,
    longitude = coordinate.longitude,
    name = name,
    aqi = aqi,
    nickname = nickname,
)

fun PlaceDto.toDomain(): Place = Place(
    coordinate = Coordinate(latitude, longitude),
    name = name,
    aqi = aqi,
    nickname = nickname,
)

/** Nicknames belong to a trip, not to the location, so they are not cached. */
fun Place.toCachedEntity(cacheKey: String, updatedAtMillis: Long): CachedPlaceEntity = CachedPlaceEntity(
    cacheKey = cacheKey,
    latitude = coordinate.latitude,
    longitude = coordinate.longitude,
    name = name,
    aqi = aqi,
    updatedAtMillis = updatedAtMillis,
)

fun CachedPlaceEntity.toDomain(): Place = Place(
    coordinate = Coordinate(latitude, longitude),
    name = name,
    aqi = aqi,
)

fun BookDto.toDomain(): Book = Book(
    id = id,
    a = a.toDomain(),
    b = b.toDomain(),
    price = price,
)
