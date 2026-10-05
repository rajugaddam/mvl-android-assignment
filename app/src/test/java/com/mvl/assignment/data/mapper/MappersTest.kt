package com.mvl.assignment.data.mapper

import com.mvl.assignment.data.remote.dto.ReverseGeocodeResponse
import com.mvl.assignment.data.remote.dto.WaqiResponse
import com.mvl.assignment.di.NetworkModule
import com.mvl.assignment.domain.AddressFormatter
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.model.RemoteServiceException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    private val json = NetworkModule.provideJson()

    private fun waqi(body: String) = json.decodeFromString(WaqiResponse.serializer(), body)

    @Test
    fun `numeric aqi is parsed`() {
        assertEquals(57, waqi("""{"status":"ok","data":{"aqi":57,"idx":1}}""").toAqi())
    }

    @Test
    fun `dash aqi becomes null`() {
        assertNull(waqi("""{"status":"ok","data":{"aqi":"-"}}""").toAqi())
    }

    @Test
    fun `missing aqi becomes null`() {
        assertNull(waqi("""{"status":"ok","data":{}}""").toAqi())
    }

    @Test
    fun `no data status becomes null`() {
        assertNull(waqi("""{"status":"nope","data":"can not connect"}""").toAqi())
    }

    @Test
    fun `error status throws with the server message`() {
        val error = runCatching { waqi("""{"status":"error","data":"Invalid key"}""").toAqi() }.exceptionOrNull()

        assertTrue(error is RemoteServiceException)
        assertEquals("Invalid key", error?.message)
    }

    @Test
    fun `reverse geocode response maps to the spec address`() {
        val response = json.decodeFromString(
            ReverseGeocodeResponse.serializer(),
            """
            {
              "city": "Seoul",
              "locality": "Yangjae 2(i)-dong",
              "localityInfo": {
                "administrative": [
                  {"name": "South Korea", "order": 2, "adminLevel": 2},
                  {"name": "Seoul", "order": 5, "adminLevel": 4},
                  {"name": "Seocho District", "order": 7, "adminLevel": 6},
                  {"name": "Yangjae 2(i)-dong", "order": 9, "adminLevel": 8},
                  {"description": "entry without name", "order": 10}
                ],
                "informative": [{"name": "Asia", "order": 1}]
              }
            }
            """.trimIndent(),
        )

        assertEquals("Seocho District, Yangjae 2(i)-dong", AddressFormatter().format(response.toDomain()))
    }

    @Test
    fun `assignment localityInfo example gives the documented address`() {
        // Verbatim localityInfo from the assignment's "Address name formatting reference".
        val response = json.decodeFromString(
            ReverseGeocodeResponse.serializer(),
            """
            {
              "localityInfo": {
                "administrative": [
                  {"order": 2, "adminLevel": 2, "name": "South Korea", "description": "republic in East Asia",
                   "isoName": "Korea (the Republic of)", "isoCode": "KR", "wikidataId": "Q884"},
                  {"order": 3, "adminLevel": 1, "name": "Seoul", "description": "capital city of South Korea",
                   "isoName": "Seoul-teukbyeolsi", "isoCode": "KR-11", "wikidataId": "Q8684"},
                  {"order": 4, "adminLevel": 6, "name": "Seocho District",
                   "description": "one of the districts (gu) of Seoul, South Korea", "wikidataId": "Q20395"},
                  {"order": 5, "adminLevel": 8, "name": "Yangjae 2(i)-dong"}
                ],
                "informative": [
                  {"order": 0, "name": "Asia", "description": "continent, mainly on the Earth's northeastern quadrant",
                   "isoCode": "AS", "wikidataId": "Q48"},
                  {"order": 1, "name": "Korean Peninsula", "description": "peninsula in East Asia", "wikidataId": "Q483134"}
                ]
              }
            }
            """.trimIndent(),
        )

        assertEquals("Seocho District, Yangjae 2(i)-dong", AddressFormatter().format(response.toDomain()))
    }

    @Test
    fun `place round-trips through the dto`() {
        val place = Place(Coordinate(37.5, 127.0), "Seocho District", aqi = null, nickname = "Home")

        assertEquals(place, place.toDto().toDomain())
    }
}
