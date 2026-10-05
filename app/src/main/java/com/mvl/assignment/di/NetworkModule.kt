package com.mvl.assignment.di

import com.mvl.assignment.BuildConfig
import com.mvl.assignment.data.remote.api.AirQualityApi
import com.mvl.assignment.data.remote.api.BookApi
import com.mvl.assignment.data.remote.api.GeocodingApi
import com.mvl.assignment.data.remote.interceptor.WaqiTokenInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 15L
    private const val FALLBACK_BASE_URL = "https://books.invalid/"
    private val JSON_MEDIA_TYPE = "application/json".toMediaType()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    /**
     * Shared base client. Per-API clients derive from it with `newBuilder()`, so interceptors they add
     * (token, mock backend) run after logging — the logged URL never contains the waqi token.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            }
        }
        .build()

    @Provides
    @WaqiToken
    fun provideWaqiToken(): String = BuildConfig.WAQI_TOKEN

    @Provides
    @Singleton
    @AqiApi
    fun provideAqiRetrofit(
        client: OkHttpClient,
        tokenInterceptor: WaqiTokenInterceptor,
        json: Json,
    ): Retrofit = retrofit(
        baseUrl = BuildConfig.WAQI_BASE_URL.toHttpUrl(),
        client = client.newBuilder().addInterceptor(tokenInterceptor).build(),
        json = json,
    )

    @Provides
    @Singleton
    @GeoApi
    fun provideGeoRetrofit(client: OkHttpClient, json: Json): Retrofit =
        retrofit(BuildConfig.GEOCODING_BASE_URL.toHttpUrl(), client, json)

    /** The [BooksApi] client comes from the flavor's BooksNetworkModule (mock or prod). */
    @Provides
    @Singleton
    @BooksApi
    fun provideBooksRetrofit(@BooksApi client: OkHttpClient, json: Json): Retrofit =
        retrofit(booksBaseUrl(BuildConfig.BOOKS_BASE_URL), client, json)

    @Provides
    @Singleton
    fun provideAirQualityApi(@AqiApi retrofit: Retrofit): AirQualityApi = retrofit.create()

    @Provides
    @Singleton
    fun provideGeocodingApi(@GeoApi retrofit: Retrofit): GeocodingApi = retrofit.create()

    @Provides
    @Singleton
    fun provideBookApi(@BooksApi retrofit: Retrofit): BookApi = retrofit.create()

    private fun retrofit(baseUrl: HttpUrl, client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE))
            .build()

    /** A missing/invalid prod URL must not crash DI; requests then fail and the UI shows an error. */
    private fun booksBaseUrl(raw: String): HttpUrl {
        val normalized = raw.trim().let { if (it.endsWith("/")) it else "$it/" }
        return normalized.toHttpUrlOrNull() ?: FALLBACK_BASE_URL.toHttpUrl()
    }
}
