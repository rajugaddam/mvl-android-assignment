package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.repository.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
) {
    suspend operator fun invoke(): Result<Coordinate> = locationRepository.getCurrentLocation()
}
