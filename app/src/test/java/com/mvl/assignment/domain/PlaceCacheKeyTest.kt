package com.mvl.assignment.domain

import com.mvl.assignment.domain.model.Coordinate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PlaceCacheKeyTest {

    private val cacheKey = PlaceCacheKey()

    @Test
    fun `spec example - 37_5642 and 37_5645 are the same place`() {
        assertEquals(
            cacheKey.of(Coordinate(37.5642, 127.0385)),
            cacheKey.of(Coordinate(37.5645, 127.0381)),
        )
    }

    @Test
    fun `assignment example - 37_5642,127_0016 equals 37_5645,127_0018`() {
        assertEquals(
            cacheKey.of(Coordinate(37.5642, 127.0016)),
            cacheKey.of(Coordinate(37.5645, 127.0018)),
        )
    }

    @Test
    fun `digits are truncated, not rounded`() {
        assertEquals("37.564,127.038", cacheKey.of(Coordinate(37.5649, 127.0389)))
    }

    @Test
    fun `different 3rd decimal is a different place`() {
        assertNotEquals(
            cacheKey.of(Coordinate(37.5642, 127.0385)),
            cacheKey.of(Coordinate(37.5652, 127.0385)),
        )
    }

    @Test
    fun `negative coordinates truncate toward zero`() {
        assertEquals("-33.868,-151.209", cacheKey.of(Coordinate(-33.8688, -151.2093)))
    }

    @Test
    fun `short values are padded to 3 decimals`() {
        assertEquals("37.500,127.000", cacheKey.of(Coordinate(37.5, 127.0)))
    }
}
