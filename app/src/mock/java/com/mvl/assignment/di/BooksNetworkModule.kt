package com.mvl.assignment.di

import android.content.Context
import com.mvl.assignment.mock.FileMockBookingStore
import com.mvl.assignment.mock.MockBookingStore
import com.mvl.assignment.mock.MockBooksInterceptor
import com.mvl.assignment.mock.MockBooksServer
import com.mvl.assignment.mock.MockSeedConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Singleton

/**
 * mock flavor: books requests are answered by [MockBooksServer] behind an OkHttp interceptor,
 * which stores bookings persistently in [MockBookingStore].
 * Repositories, use cases and UI are identical in both flavors — only this binding differs.
 */
@Module
@InstallIn(SingletonComponent::class)
object BooksNetworkModule {

    private const val MOCK_BACKEND_DIR = "mock-backend"
    private const val MOCK_BOOKINGS_FILE = "books.json"

    /** Sample bookings shown in History next to the user's own (real Seoul places). */
    @Provides
    @Singleton
    fun provideMockSeedConfig(): MockSeedConfig = MockSeedConfig()

    /** The mock backend's own storage: a JSON file in app-private storage, separate from the app's Room cache. */
    @Provides
    @Singleton
    fun provideMockBookingStore(@ApplicationContext context: Context, json: Json): MockBookingStore =
        FileMockBookingStore(File(File(context.filesDir, MOCK_BACKEND_DIR), MOCK_BOOKINGS_FILE), json)

    @Provides
    @Singleton
    @BooksApi
    fun provideBooksOkHttpClient(client: OkHttpClient, server: MockBooksServer): OkHttpClient =
        client.newBuilder()
            .addInterceptor(MockBooksInterceptor(server))
            .build()
}
