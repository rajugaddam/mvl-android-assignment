package com.mvl.assignment.data.repository

import com.mvl.assignment.data.remote.api.BookApi
import com.mvl.assignment.di.NetworkModule
import com.mvl.assignment.domain.FixedMonthProvider
import com.mvl.assignment.domain.model.BookNotFoundException
import com.mvl.assignment.domain.model.BookingMonth
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.Place
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

class BookRepositoryImplTest {

    private val server = MockWebServer()
    private lateinit var repository: BookRepositoryImpl

    private val a = Place(Coordinate(37.4842, 127.0343), "A", aqi = 57)
    private val b = Place(Coordinate(37.4979, 127.0276), "B", aqi = null)

    private val bookJson =
        """{"id":"bk-1","a":{"latitude":37.4842,"longitude":127.0343,"name":"A"},
           "b":{"latitude":37.4979,"longitude":127.0276,"name":"B"},"price":5500}"""

    @Before
    fun setUp() {
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(NetworkModule.provideJson().asConverterFactory("application/json".toMediaType()))
            .build()
            .create<BookApi>()
        repository = BookRepositoryImpl(api, FixedMonthProvider(BookingMonth(2026, 9)), UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `a booking just made is served from the response without another request`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201).setBody(bookJson))
        repository.book(a, b)

        val book = repository.getBook("bk-1").getOrThrow()

        assertEquals(5500.0, book.price, 0.0)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `a booking not in memory is fetched from the current month's history`() = runTest {
        server.enqueue(MockResponse().setBody("[$bookJson]"))

        val book = repository.getBook("bk-1").getOrThrow()

        assertEquals("bk-1", book.id)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/books?year=2026&month=9", request.path)
    }

    @Test
    fun `an unknown booking is not found`() = runTest {
        server.enqueue(MockResponse().setBody("[]"))

        assertTrue(repository.getBook("missing").exceptionOrNull() is BookNotFoundException)
    }
}
