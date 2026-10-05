package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.repository.AirQualityRepository
import javax.inject.Inject

class GetAirQualityUseCase @Inject constructor(
    private val airQualityRepository: AirQualityRepository,
) {
    suspend operator fun invoke(coordinate: Coordinate): Result<Int?> =
        airQualityRepository.getAirQuality(coordinate)
}
