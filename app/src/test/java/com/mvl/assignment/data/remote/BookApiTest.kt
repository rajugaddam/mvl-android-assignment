package com.mvl.assignment.data.remote

import com.mvl.assignment.data.remote.api.BookApi
import com.mvl.assignment.data.remote.dto.BookRequest
import com.mvl.assignment.data.remote.dto.PlaceDto
import com.mvl.assignment.di.NetworkModule
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

/** Verifies the books API contract: method, path, query and JSON body. */
class BookApiTest {

    private val server = MockWebServer()
    private lateinit var api: BookApi

    private val a = PlaceDto(37.4842, 127.0343, "Seocho District, Yangjae 2(i)-dong", aqi = 57, nickname = "Home")
    private val b = PlaceDto(37.4979, 127.0276, "Gangnam District, Yeoksam 1(il)-dong", aqi = null)

    @Before
    fun setUp() {
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(NetworkModule.provideJson().asConverterFactory("application/json".toMediaType()))
            .build()
            .create()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `createBook POSTs A and B as JSON to books`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{"id":"bk-1","a":{"latitude":37.4842,"longitude":127.0343,"name":"A","aqi":57},
                   "b":{"latitude":37.4979,"longitude":127.0276,"name":"B"},"price":5500,"createdAt":1}""",
            ),
        )

        val response = api.createBook(BookRequest(a, b))

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/books", recorded.path)
        val body = Json.parseToJsonElement(recorded.body.readUtf8()).jsonObject
        val sentA = body.getValue("a").jsonObject
        val sentB = body.getValue("b").jsonObject
        assertEquals(37.4842, sentA.getValue("latitude").jsonPrimitive.double, 0.0)
        assertEquals(127.0343, sentA.getValue("longitude").jsonPrimitive.double, 0.0)
        assertEquals(57, sentA.getValue("aqi").jsonPrimitive.int)
        assertEquals("Home", sentA.getValue("nickname").jsonPrimitive.content)
        assertEquals(37.4979, sentB.getValue("latitude").jsonPrimitive.double, 0.0)

        assertEquals("bk-1", response.id)
        assertEquals(5500.0, response.price, 0.0)
    }

    @Test
    fun `getBooks GETs books with year and month query`() = runTest {
        server.enqueue(MockResponse().setBody("[]"))

        val books = api.getBooks(year = 2026, month = 9)

        val recorded = server.takeRequest()
        val url = requireNotNull(recorded.requestUrl)
        assertEquals("GET", recorded.method)
        assertEquals("/books", url.encodedPath)
        assertEquals("2026", url.queryParameter("year"))
        assertEquals("9", url.queryParameter("month"))
        assertEquals(emptyList<Any>(), books)
    }

    @Test
    fun `getBooks parses the assignment's response shape`() = runTest {
        // Doc example (+ the assumed id): integer prices, no createdAt.
        server.enqueue(
            MockResponse().setBody(
                """
                [
                  {"id":"1",
                   "a":{"latitude":36.564,"longitude":127.001,"aqi":30,"name":"Seoul A Location"},
                   "b":{"latitude":36.567,"longitude":127,"aqi":40,"name":"Seoul B Location"},
                   "price":10000},
                  {"id":"2",
                   "a":{"latitude":36.577,"longitude":127.033,"aqi":50,"name":"Seoul C Location"},
                   "b":{"latitude":36.567,"longitude":127,"aqi":60,"name":"Seoul D Location"},
                   "price":20000}
                ]
                """.trimIndent(),
            ),
        )

        val books = api.getBooks(year = 2020, month = 11)

        assertEquals(2, books.size)
        assertEquals(10000.0, books[0].price, 0.0)
        assertEquals(20000.0, books[1].price, 0.0)
        assertEquals("Seoul D Location", books[1].b.name)
        assertEquals(60, books[1].b.aqi)
    }

    @Test(expected = HttpException::class)
    fun `non-2xx response is an error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":"bad"}"""))

        api.getBooks(year = 2026, month = 13)
    }
}
