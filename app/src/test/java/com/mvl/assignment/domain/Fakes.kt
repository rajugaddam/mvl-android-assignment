package com.mvl.assignment.domain

import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.model.BookNotFoundException
import com.mvl.assignment.domain.model.BookingMonth
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.GeocodedAddress
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.repository.AirQualityRepository
import com.mvl.assignment.domain.repository.BookRepository
import com.mvl.assignment.domain.repository.CurrentMonthProvider
import com.mvl.assignment.domain.repository.GeocodingRepository
import com.mvl.assignment.domain.repository.LocationRepository
import com.mvl.assignment.domain.repository.PlaceCacheRepository
import com.mvl.assignment.domain.repository.SelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

val SEOUL = Coordinate(37.4842, 127.0343)
val GANGNAM = Coordinate(37.4979, 127.0276)

fun place(name: String, coordinate: Coordinate = SEOUL, aqi: Int? = 42, nickname: String? = null) =
    Place(coordinate = coordinate, name = name, aqi = aqi, nickname = nickname)

class FakeSelectionRepository(initial: Selection = Selection()) : SelectionRepository {
    private val state = MutableStateFlow(initial)
    override val selection: StateFlow<Selection> = state
    override fun update(transform: (Selection) -> Selection) = state.update(transform)
}

class FakeAirQualityRepository(var result: Result<Int?> = Result.success(42)) : AirQualityRepository {
    val requests = mutableListOf<Coordinate>()
    override suspend fun getAirQuality(coordinate: Coordinate): Result<Int?> {
        requests += coordinate
        return result
    }
}

class FakeGeocodingRepository(var result: Result<GeocodedAddress>) : GeocodingRepository {
    override suspend fun reverseGeocode(coordinate: Coordinate): Result<GeocodedAddress> = result
}

class FakeBookRepository : BookRepository {
    var bookResult: ((Place, Place) -> Result<Book>) = { a, b ->
        Result.success(Book(id = "1", a = a, b = b, price = 1000.0))
    }
    var monthlyResult: Result<List<Book>> = Result.success(emptyList())
    val bookRequests = mutableListOf<Pair<Place, Place>>()
    val monthlyRequests = mutableListOf<BookingMonth>()
    private val books = mutableMapOf<String, Book>()

    override suspend fun book(a: Place, b: Place): Result<Book> {
        bookRequests += a to b
        return bookResult(a, b).onSuccess { books[it.id] = it }
    }

    override suspend fun getBook(id: String): Result<Book> =
        books[id]?.let { Result.success(it) } ?: Result.failure(BookNotFoundException(id))

    override suspend fun getMonthlyBooks(month: BookingMonth): Result<List<Book>> {
        monthlyRequests += month
        return monthlyResult
    }
}

class FakePlaceCacheRepository : PlaceCacheRepository {
    val places = linkedMapOf<String, Place>()
    private val state = MutableStateFlow<List<Place>>(emptyList())

    override suspend fun get(key: String): Place? = places[key]

    override suspend fun save(key: String, place: Place) {
        places[key] = place
        state.value = places.values.reversed()
    }

    override fun observeAll(): Flow<List<Place>> = state
}

class FakeLocationRepository(var result: Result<Coordinate> = Result.success(SEOUL)) : LocationRepository {
    override suspend fun getCurrentLocation(): Result<Coordinate> = result
}

class FixedMonthProvider(private val month: BookingMonth) : CurrentMonthProvider {
    override fun currentMonth(): BookingMonth = month
}
