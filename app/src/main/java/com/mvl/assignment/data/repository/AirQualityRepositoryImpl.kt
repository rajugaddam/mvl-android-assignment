package com.mvl.assignment.data.repository

import com.mvl.assignment.data.mapper.toAqi
import com.mvl.assignment.data.mapper.toWaqiGeo
import com.mvl.assignment.data.remote.api.AirQualityApi
import com.mvl.assignment.data.remote.apiCall
import com.mvl.assignment.di.IoDispatcher
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.repository.AirQualityRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AirQualityRepositoryImpl @Inject constructor(
    private val api: AirQualityApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AirQualityRepository {

    override suspend fun getAirQuality(coordinate: Coordinate): Result<Int?> = withContext(ioDispatcher) {
        apiCall { api.getFeed(coordinate.toWaqiGeo()).toAqi() }
    }
}
