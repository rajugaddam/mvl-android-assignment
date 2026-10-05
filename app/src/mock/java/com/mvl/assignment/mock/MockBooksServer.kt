package com.mvl.assignment.mock

import com.mvl.assignment.data.remote.dto.BookDto
import com.mvl.assignment.data.remote.dto.BookRequest
import com.mvl.assignment.data.remote.dto.PlaceDto
import com.mvl.assignment.data.time.TimeProvider
import com.mvl.assignment.data.time.bookingMonthOf
import com.mvl.assignment.domain.model.BookingMonth
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

data class MockHttpRequest(
    val method: String,
    val pathSegments: List<String>,
    val query: Map<String, String?>,
    val body: String?,
)

data class MockHttpResponse(
    val code: Int,
    val message: String,
    val body: String,
)

/**
 * Fake books backend behind [MockBooksInterceptor]. Responses are computed from the request, never fixed:
 * - `POST /books`: validates the body, assigns an id and timestamp, prices the trip by distance and
 *   saves it in [store] (persistent, so history survives app restarts like a real server's).
 * - `GET /books?year=&month=`: stored bookings of that month plus the month's sample bookings
 *   (only those not in the future).
 *
 * Samples come from [MockSeedConfig]: pairs of real places, generated from a year/month seed
 * (same month → same samples) the first time a month is requested.
 */
@Singleton
class MockBooksServer @Inject constructor(
    private val json: Json,
    private val timeProvider: TimeProvider,
    private val seedConfig: MockSeedConfig,
    private val store: MockBookingStore,
) {
    private val seededMonths = mutableMapOf<BookingMonth, List<BookDto>>()

    fun handle(request: MockHttpRequest): MockHttpResponse {
        if (request.pathSegments != listOf(BOOKS_PATH)) return errorResponse(404, "Not Found", "Unknown path")
        return when (request.method) {
            "POST" -> createBook(request.body)
            "GET" -> listBooks(request.query)
            else -> errorResponse(405, "Method Not Allowed", "Unsupported method ${request.method}")
        }
    }

    private fun createBook(body: String?): MockHttpResponse {
        val request = body
            ?.let { runCatching { json.decodeFromString(BookRequest.serializer(), it) }.getOrNull() }
            ?: return badRequest("Body must be {\"a\": place, \"b\": place}")
        if (!request.a.hasValidCoordinate() || !request.b.hasValidCoordinate()) {
            return badRequest("Invalid coordinates")
        }

        val book = BookDto(
            // Unique across app restarts (the store outlives any in-memory counter).
            id = "$BOOKING_ID_PREFIX${UUID.randomUUID()}",
            a = request.a,
            b = request.b,
            price = FareCalculator.price(request.a, request.b),
            createdAt = timeProvider.nowMillis(),
        )
        runCatching { store.add(book) }
            .onFailure { return errorResponse(500, "Internal Server Error", "Could not save the booking") }
        return MockHttpResponse(201, "Created", json.encodeToString(BookDto.serializer(), book))
    }

    private fun listBooks(query: Map<String, String?>): MockHttpResponse {
        val year = query[YEAR_PARAM]?.toIntOrNull()
        val month = query[MONTH_PARAM]?.toIntOrNull()
        if (year == null || month == null || year !in MIN_YEAR..MAX_YEAR || month !in 1..12) {
            return badRequest("Query must contain valid year and month")
        }
        val requested = BookingMonth(year, month)

        val now = timeProvider.nowMillis()
        val received = store.all().filter { bookingMonthOf(it.timestamp) == requested }
        val samples = seededSamples(requested).filter { it.timestamp <= now }
        val all = (received + samples).sortedByDescending { it.timestamp }
        return MockHttpResponse(200, "OK", json.encodeToString(ListSerializer(BookDto.serializer()), all))
    }

    /** The month's samples, generated on first request and then kept like any other booking. */
    private fun seededSamples(month: BookingMonth): List<BookDto> {
        if (!seedConfig.samplesEnabled) return emptyList()
        return synchronized(seededMonths) { seededMonths.getOrPut(month) { generateSamples(month) } }
    }

    /** Same year/month → same samples; different month → different count, trips and prices. */
    private fun generateSamples(month: BookingMonth): List<BookDto> {
        val random = Random(month.year * 100 + month.month)
        val start = Calendar.getInstance().apply {
            clear()
            set(month.year, month.month - 1, 1)
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
        val monthLength = end.timeInMillis - start.timeInMillis
        val count = random.nextInt(seedConfig.samplesPerMonth.first, seedConfig.samplesPerMonth.last + 1)

        return List(count) { index ->
            val (a, b) = randomTrip(random)
            BookDto(
                id = "$SAMPLE_ID_PREFIX${month.year}-${month.month}-$index",
                a = a,
                b = b,
                price = FareCalculator.price(a, b),
                createdAt = start.timeInMillis + random.nextLong(monthLength),
            )
        }
    }

    /** Two different catalogue places, so a trip is never from a place to itself. */
    private fun randomTrip(random: Random): Pair<PlaceDto, PlaceDto> {
        val places = seedConfig.places
        val from = random.nextInt(places.size)
        val to = (from + 1 + random.nextInt(places.size - 1)) % places.size
        return places[from].toDto(random) to places[to].toDto(random)
    }

    private fun SeedPlace.toDto(random: Random) = PlaceDto(
        latitude = latitude,
        longitude = longitude,
        name = name,
        aqi = random.nextInt(seedConfig.sampleAqi.first, seedConfig.sampleAqi.last + 1),
    )

    /** Every booking this server creates has `createdAt`; it is optional only in the shared DTO. */
    private val BookDto.timestamp: Long get() = checkNotNull(createdAt)

    private fun PlaceDto.hasValidCoordinate(): Boolean =
        latitude in -90.0..90.0 && longitude in -180.0..180.0

    private fun badRequest(reason: String) = errorResponse(400, "Bad Request", reason)

    private fun errorResponse(code: Int, message: String, reason: String) = MockHttpResponse(
        code = code,
        message = message,
        body = buildJsonObject { put("error", reason) }.toString(),
    )

    private companion object {
        const val BOOKS_PATH = "books"
        const val YEAR_PARAM = "year"
        const val MONTH_PARAM = "month"
        const val MIN_YEAR = 1970
        const val MAX_YEAR = 9999
        const val BOOKING_ID_PREFIX = "bk-"
        const val SAMPLE_ID_PREFIX = "sample-"
    }
}
