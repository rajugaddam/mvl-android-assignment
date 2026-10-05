package com.mvl.assignment.di

import com.mvl.assignment.data.repository.AirQualityRepositoryImpl
import com.mvl.assignment.data.repository.BookRepositoryImpl
import com.mvl.assignment.data.repository.GeocodingRepositoryImpl
import com.mvl.assignment.data.repository.LocationRepositoryImpl
import com.mvl.assignment.data.repository.PlaceCacheRepositoryImpl
import com.mvl.assignment.data.repository.SelectionRepositoryImpl
import com.mvl.assignment.data.time.CurrentMonthProviderImpl
import com.mvl.assignment.data.time.SystemTimeProvider
import com.mvl.assignment.data.time.TimeProvider
import com.mvl.assignment.domain.repository.AirQualityRepository
import com.mvl.assignment.domain.repository.BookRepository
import com.mvl.assignment.domain.repository.CurrentMonthProvider
import com.mvl.assignment.domain.repository.GeocodingRepository
import com.mvl.assignment.domain.repository.LocationRepository
import com.mvl.assignment.domain.repository.PlaceCacheRepository
import com.mvl.assignment.domain.repository.SelectionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds every domain repository / provider interface to its data-layer implementation. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAirQualityRepository(impl: AirQualityRepositoryImpl): AirQualityRepository

    @Binds
    abstract fun bindGeocodingRepository(impl: GeocodingRepositoryImpl): GeocodingRepository

    @Binds
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds
    abstract fun bindSelectionRepository(impl: SelectionRepositoryImpl): SelectionRepository

    @Binds
    abstract fun bindPlaceCacheRepository(impl: PlaceCacheRepositoryImpl): PlaceCacheRepository

    @Binds
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    abstract fun bindTimeProvider(impl: SystemTimeProvider): TimeProvider

    @Binds
    abstract fun bindCurrentMonthProvider(impl: CurrentMonthProviderImpl): CurrentMonthProvider
}
