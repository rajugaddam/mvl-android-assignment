package com.mvl.assignment.domain.repository

import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.model.BookingMonth
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.GeocodedAddress
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.model.Selection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AirQualityRepository {
    /** @return AQI at [coordinate], or `null` inside the result when the station has no value. */
    suspend fun getAirQuality(coordinate: Coordinate): Result<Int?>
}

interface GeocodingRepository {
    suspend fun reverseGeocode(coordinate: Coordinate): Result<GeocodedAddress>
}

interface BookRepository {
    /** `POST /books` with A and B; the returned [Book] is the server response. */
    suspend fun book(a: Place, b: Place): Result<Book>

    /** A booking previously returned by [book]. */
    suspend fun getBook(id: String): Result<Book>

    /** `GET /books?year=&month=`. */
    suspend fun getMonthlyBooks(month: BookingMonth): Result<List<Book>>
}

/** Locally cached resolved places, keyed by [com.mvl.assignment.domain.PlaceCacheKey]. */
interface PlaceCacheRepository {
    suspend fun get(key: String): Place?

    suspend fun save(key: String, place: Place)

    /** Most recently resolved first. */
    fun observeAll(): Flow<List<Place>>
}

interface LocationRepository {
    /** Device location; fails with LocationUnavailableException when there is no permission or fix. */
    suspend fun getCurrentLocation(): Result<Coordinate>
}

/** Holds the A/B selection shared by screens. State transitions live in the use cases. */
interface SelectionRepository {
    val selection: StateFlow<Selection>

    fun update(transform: (Selection) -> Selection)
}

/** Source of "now", injected so history queries are testable. */
interface CurrentMonthProvider {
    fun currentMonth(): BookingMonth
}
