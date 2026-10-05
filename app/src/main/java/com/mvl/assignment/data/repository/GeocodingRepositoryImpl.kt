package com.mvl.assignment.data.repository

import com.mvl.assignment.data.mapper.toDomain
import com.mvl.assignment.data.remote.api.GeocodingApi
import com.mvl.assignment.data.remote.apiCall
import com.mvl.assignment.di.IoDispatcher
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.GeocodedAddress
import com.mvl.assignment.domain.repository.GeocodingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeocodingRepositoryImpl @Inject constructor(
    private val api: GeocodingApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GeocodingRepository {

    override suspend fun reverseGeocode(coordinate: Coordinate): Result<GeocodedAddress> =
        withContext(ioDispatcher) {
            apiCall {
                api.reverseGeocode(coordinate.latitude, coordinate.longitude, LANGUAGE).toDomain()
            }
        }

    private companion object {
        const val LANGUAGE = "en"
    }
}
