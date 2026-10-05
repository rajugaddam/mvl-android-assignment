package com.mvl.assignment.mock

import com.mvl.assignment.data.remote.dto.BookDto
import com.mvl.assignment.data.remote.dto.BookRequest
import com.mvl.assignment.data.remote.dto.PlaceDto
import com.mvl.assignment.data.time.TimeProvider
import com.mvl.assignment.di.NetworkModule
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Calendar

class MockBooksServerTest {

    private val json = NetworkModule.provideJson()

    /** 2026-09-15 12:00 in the device time zone. */
    private val now = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.SEPTEMBER, 15, 12, 0)
    }.timeInMillis

    private val clock = object : TimeProvider {
        override fun nowMillis(): Long = now
    }

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val storeFile by lazy { File(tempFolder.root, "mock-backend/books.json") }

    private fun serverWith(
        seedConfig: MockSeedConfig,
        store: MockBookingStore = FileMockBookingStore(storeFile, json),
    ) = MockBooksServer(json, clock, seedConfig, store)

    private val server by lazy { serverWith(MockSeedConfig()) }

    private val yangjae = PlaceDto(37.4842, 127.0343, "Yangjae")
    private val gangnam = PlaceDto(37.4979, 127.0276, "Gangnam")
    private val incheon = PlaceDto(37.4563, 126.7052, "Incheon")

    private fun post(a: PlaceDto, b: PlaceDto, server: MockBooksServer = this.server) = server.handle(
        MockHttpRequest("POST", listOf("books"), emptyMap(), json.encodeToString(BookRequest.serializer(), BookRequest(a, b))),
    )

    private fun get(year: String?, month: String?, server: MockBooksServer = this.server) = server.handle(
        MockHttpRequest("GET", listOf("books"), mapOf("year" to year, "month" to month), body = null),
    )

    private fun book(response: MockHttpResponse) = json.decodeFromString(BookDto.serializer(), response.body)

    private fun books(response: MockHttpResponse) =
        json.decodeFromString(ListSerializer(BookDto.serializer()), response.body)

    @Test
    fun `post echoes A and B with a generated id`() {
        val response = post(yangjae, gangnam)

        assertEquals(201, response.code)
        val created = book(response)
        assertEquals(yangjae, created.a)
        assertEquals(gangnam, created.b)
        assertEquals(now, created.createdAt)
        assertNotEquals(created.id, book(post(yangjae, gangnam)).id)
    }

    @Test
    fun `price depends on the distance between A and B`() {
        val short = book(post(yangjae, gangnam)).price
        val long = book(post(yangjae, incheon)).price

        assertTrue("long trip ($long) should cost more than short trip ($short)", long > short)
        assertEquals(0.0, short % 100, 0.0)
    }

    @Test
    fun `posted booking appears in its month only`() {
        val created = book(post(yangjae, gangnam))

        assertTrue(books(get("2026", "9")).any { it.id == created.id })
        assertTrue(books(get("2026", "8")).none { it.id == created.id })
    }

    @Test
    fun `history is deterministic per month and differs between months`() {
        val august = books(get("2026", "8"))

        assertEquals(august, books(get("2026", "8")))
        assertNotEquals(august, books(get("2026", "7")))
        assertTrue(august.isNotEmpty())
    }

    @Test
    fun `future months have no bookings`() {
        assertEquals(emptyList<BookDto>(), books(get("2027", "1")))
    }

    @Test
    fun `current month only contains past samples`() {
        assertTrue(books(get("2026", "9")).all { requireNotNull(it.createdAt) <= now })
    }

    @Test
    fun `bookings survive a restart of the mock backend`() {
        val created = book(post(yangjae, gangnam))

        // A new server + store on the same file, as after the app process is restarted.
        val restarted = serverWith(MockSeedConfig(samplesEnabled = false))

        assertEquals(listOf(created), books(get("2026", "9", restarted)))
    }

    @Test
    fun `an unreadable store file starts empty instead of failing`() {
        storeFile.parentFile?.mkdirs()
        storeFile.writeText("not json")
        val server = serverWith(MockSeedConfig(samplesEnabled = false))

        assertEquals(emptyList<BookDto>(), books(get("2026", "9", server)))
        val created = book(post(yangjae, gangnam, server))
        assertEquals(listOf(created), books(get("2026", "9", server)))
    }

    @Test
    fun `a booking that cannot be saved is a server error`() {
        val failingStore = object : MockBookingStore {
            override fun all() = emptyList<BookDto>()
            override fun add(book: BookDto) = error("disk full")
        }
        val server = serverWith(MockSeedConfig(samplesEnabled = false), failingStore)

        assertEquals(500, post(yangjae, gangnam, server).code)
    }

    @Test
    fun `without samples history contains exactly the posted bookings`() {
        val server = serverWith(MockSeedConfig(samplesEnabled = false))
        val first = book(post(yangjae, gangnam, server))
        val second = book(post(gangnam, incheon, server))

        assertEquals(listOf(first, second).sortedByDescending { it.createdAt }, books(get("2026", "9", server)))
        assertEquals(emptyList<BookDto>(), books(get("2026", "8", server)))
    }

    @Test
    fun `sample names always match their coordinates`() {
        val catalogue = MockSeedConfig.SEOUL_PLACES.associateBy { it.name }
        val samples = (1..8).flatMap { month -> books(get("2026", "$month")) }

        assertTrue(samples.isNotEmpty())
        samples.flatMap { listOf(it.a, it.b) }.forEach { place ->
            val seed = requireNotNull(catalogue[place.name]) { "Unknown sample place ${place.name}" }
            assertEquals(seed.latitude, place.latitude, 0.0)
            assertEquals(seed.longitude, place.longitude, 0.0)
        }
    }

    @Test
    fun `sample trips go between two different places and are priced by distance`() {
        val samples = books(get("2026", "8"))

        samples.forEach { sample ->
            assertNotEquals(sample.a.name, sample.b.name)
            assertEquals(FareCalculator.price(sample.a, sample.b), sample.price, 0.0)
        }
    }

    @Test
    fun `sample count follows the seed config`() {
        val server = serverWith(MockSeedConfig(samplesPerMonth = 3..3))

        assertEquals(3, books(get("2026", "8", server)).size)
    }

    @Test
    fun `seed config rejects a catalogue too small for a trip`() {
        val error = runCatching { MockSeedConfig(places = MockSeedConfig.SEOUL_PLACES.take(1)) }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun `invalid query is a bad request`() {
        assertEquals(400, get(null, "9").code)
        assertEquals(400, get("2026", "13").code)
        assertEquals(400, get("abc", "1").code)
    }

    @Test
    fun `invalid body is a bad request`() {
        val response = server.handle(MockHttpRequest("POST", listOf("books"), emptyMap(), """{"a":1}"""))
        assertEquals(400, response.code)
    }

    @Test
    fun `unknown path and method are rejected`() {
        assertEquals(404, server.handle(MockHttpRequest("GET", listOf("users"), emptyMap(), null)).code)
        assertEquals(405, server.handle(MockHttpRequest("DELETE", listOf("books"), emptyMap(), null)).code)
    }
}
