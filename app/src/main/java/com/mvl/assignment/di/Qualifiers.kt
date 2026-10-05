package com.mvl.assignment.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AqiApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeoApi

/** Books backend; its OkHttpClient is provided per flavor (mock interceptor vs real server). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BooksApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WaqiToken

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
