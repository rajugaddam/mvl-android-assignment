package com.mvl.assignment.di

import com.mvl.assignment.BuildConfig
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.ui.map.MapConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    /** Seoul City Hall: shown until the current location arrives. */
    private val FALLBACK_TARGET = Coordinate(latitude = 37.5665, longitude = 126.9780)
    private const val MAP_ZOOM = 16f

    @Provides
    fun provideMapConfig(): MapConfig = MapConfig(
        hasApiKey = BuildConfig.HAS_MAPS_API_KEY,
        fallbackTarget = FALLBACK_TARGET,
        zoom = MAP_ZOOM,
    )
}
