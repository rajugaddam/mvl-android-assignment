package com.mvl.assignment.data.remote

import com.mvl.assignment.data.mapper.toWaqiGeo
import com.mvl.assignment.data.remote.api.AirQualityApi
import com.mvl.assignment.data.remote.interceptor.WaqiTokenInterceptor
import com.mvl.assignment.di.NetworkModule
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.MissingApiKeyException
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

class AirQualityApiTest {

    private val server = MockWebServer()

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.shutdown()

    private fun api(token: String): AirQualityApi = Retrofit.Builder()
        .baseUrl(server.url("/"))
        .client(OkHttpClient.Builder().addInterceptor(WaqiTokenInterceptor(token)).build())
        .addConverterFactory(NetworkModule.provideJson().asConverterFactory("application/json".toMediaType()))
        .build()
        .create()

    @Test
    fun `requests geo feed with token query`() = runTest {
        server.enqueue(MockResponse().setBody("""{"status":"ok","data":{"aqi":57}}"""))

        api("test-token").getFeed(Coordinate(37.5642, 127.0).toWaqiGeo())

        val url = requireNotNull(server.takeRequest().requestUrl)
        assertEquals("/feed/geo:37.564200;127.000000/", url.encodedPath)
        assertEquals("test-token", url.queryParameter("token"))
    }

    @Test
    fun `missing token fails without a request`() = runTest {
        val result = runCatching { api("").getFeed("geo:1;1") }

        assertTrue(result.exceptionOrNull() is MissingApiKeyException)
        assertEquals(0, server.requestCount)
    }
}
