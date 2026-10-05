package com.mvl.assignment.domain

import com.mvl.assignment.domain.model.AdministrativeArea
import com.mvl.assignment.domain.model.GeocodedAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddressFormatterTest {

    private val formatter = AddressFormatter()

    @Test
    fun `spec sample - two most specific areas, broad to specific`() {
        val address = GeocodedAddress(
            administrative = listOf(
                AdministrativeArea("South Korea", order = 2),
                AdministrativeArea("Seoul", order = 4),
                AdministrativeArea("Seocho District", order = 6),
                AdministrativeArea("Yangjae 2(i)-dong", order = 8),
            ),
            city = "Seoul",
            locality = "Yangjae 2(i)-dong",
        )

        assertEquals("Seocho District, Yangjae 2(i)-dong", formatter.format(address))
    }

    @Test
    fun `input order does not matter`() {
        val address = GeocodedAddress(
            administrative = listOf(
                AdministrativeArea("Yangjae 2(i)-dong", order = 8),
                AdministrativeArea("South Korea", order = 2),
                AdministrativeArea("Seocho District", order = 6),
            ),
            city = null,
            locality = null,
        )

        assertEquals("Seocho District, Yangjae 2(i)-dong", formatter.format(address))
    }

    @Test
    fun `falls back to city and locality when fewer than two areas`() {
        val address = GeocodedAddress(
            administrative = listOf(AdministrativeArea("Seoul", order = 4)),
            city = "Seoul",
            locality = "Gangnam",
        )

        assertEquals("Seoul, Gangnam", formatter.format(address))
    }

    @Test
    fun `blank names are ignored`() {
        val address = GeocodedAddress(
            administrative = listOf(AdministrativeArea(" ", order = 9)),
            city = "",
            locality = "Gangnam",
        )

        assertEquals("Gangnam", formatter.format(address))
    }

    @Test
    fun `returns null when nothing is usable`() {
        assertNull(formatter.format(GeocodedAddress(emptyList(), city = null, locality = null)))
    }
}
