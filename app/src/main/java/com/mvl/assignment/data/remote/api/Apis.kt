package com.mvl.assignment.data.remote.api

import com.mvl.assignment.data.remote.dto.BookDto
import com.mvl.assignment.data.remote.dto.BookRequest
import com.mvl.assignment.data.remote.dto.ReverseGeocodeResponse
import com.mvl.assignment.data.remote.dto.WaqiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** https://aqicn.org/json-api/doc/ — the token is added by WaqiTokenInterceptor. */
interface AirQualityApi {
    /** @param geo `geo:<lat>;<lng>` — passed pre-encoded because ':' and ';' are part of the path. */
    @GET("feed/{geo}/")
    suspend fun getFeed(@Path("geo", encoded = true) geo: String): WaqiResponse
}

/** https://www.bigdatacloud.com/free-api/free-reverse-geocode-to-city-api — no key required. */
interface GeocodingApi {
    @GET("data/reverse-geocode-client")
    suspend fun reverseGeocode(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("localityLanguage") language: String,
    ): ReverseGeocodeResponse
}

interface BookApi {
    @POST("books")
    suspend fun createBook(@Body request: BookRequest): BookDto

    @GET("books")
    suspend fun getBooks(
        @Query("year") year: Int,
        @Query("month") month: Int,
    ): List<BookDto>
}
