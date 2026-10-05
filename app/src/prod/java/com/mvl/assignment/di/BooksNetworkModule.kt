package com.mvl.assignment.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/** prod flavor: books requests go to the real server at BuildConfig.BOOKS_BASE_URL. */
@Module
@InstallIn(SingletonComponent::class)
object BooksNetworkModule {

    @Provides
    @Singleton
    @BooksApi
    fun provideBooksOkHttpClient(client: OkHttpClient): OkHttpClient = client
}
