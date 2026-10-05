package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.AddressFormatter
import com.mvl.assignment.domain.FakeAirQualityRepository
import com.mvl.assignment.domain.FakeGeocodingRepository
import com.mvl.assignment.domain.FakePlaceCacheRepository
import com.mvl.assignment.domain.PlaceCacheKey
import com.mvl.assignment.domain.SEOUL
import com.mvl.assignment.domain.model.AdministrativeArea
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.GeocodedAddress
import com.mvl.assignment.domain.model.Place
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ResolvePlaceUseCaseTest {

    private val address = GeocodedAddress(
        administrative = listOf(
            AdministrativeArea("Seocho District", order = 6),
            AdministrativeArea("Yangjae 2(i)-dong", order = 8),
        ),
        city = "Seoul",
        locality = null,
    )
    private val geocoding = FakeGeocodingRepository(Result.success(address))
    private val airQuality = FakeAirQualityRepository(Result.success(57))
    private val cache = FakePlaceCacheRepository()
    private val resolvePlace = ResolvePlaceUseCase(geocoding, airQuality, cache, AddressFormatter(), PlaceCacheKey())

    @Test
    fun `combines formatted address and aqi`() = runTest {
        val place = resolvePlace(SEOUL).getOrThrow()

        assertEquals(Place(SEOUL, "Seocho District, Yangjae 2(i)-dong", aqi = 57), place)
    }

    @Test
    fun `missing aqi is kept as null`() = runTest {
        airQuality.result = Result.success(null)

        assertEquals(null, resolvePlace(SEOUL).getOrThrow().aqi)
    }

    @Test
    fun `falls back to coordinates when address is empty`() = runTest {
        geocoding.result = Result.success(GeocodedAddress(emptyList(), null, null))

        assertEquals(SEOUL.toDisplayString(), resolvePlace(SEOUL).getOrThrow().name)
    }

    // Coroutines may re-create exceptions crossing async boundaries (stack-trace recovery),
    // so failures are compared by type and message, not identity.
    @Test
    fun `geocoding failure is propagated and nothing is cached`() = runTest {
        geocoding.result = Result.failure(IOException("offline"))

        val error = resolvePlace(SEOUL).exceptionOrNull()

        assertTrue(error is IOException)
        assertEquals("offline", error?.message)
        assertTrue(cache.places.isEmpty())
    }

    @Test
    fun `aqi failure is propagated`() = runTest {
        airQuality.result = Result.failure(IOException("offline"))

        val error = resolvePlace(SEOUL).exceptionOrNull()

        assertTrue(error is IOException)
        assertEquals("offline", error?.message)
    }

    @Test
    fun `same coordinates to the 3rd decimal are served from cache`() = runTest {
        val first = resolvePlace(Coordinate(37.5642, 127.0301)).getOrThrow()
        geocoding.result = Result.failure(IOException("must not be called"))

        val second = resolvePlace(Coordinate(37.5645, 127.0309)).getOrThrow()

        assertEquals(first, second)
        assertEquals(1, airQuality.requests.size)
    }

    @Test
    fun `different 3rd decimal goes to the network`() = runTest {
        resolvePlace(Coordinate(37.5642, 127.0301))
        resolvePlace(Coordinate(37.5652, 127.0301))

        assertEquals(2, airQuality.requests.size)
        assertEquals(2, cache.places.size)
    }
}
