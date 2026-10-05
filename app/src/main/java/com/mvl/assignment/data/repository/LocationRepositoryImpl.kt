package com.mvl.assignment.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mvl.assignment.domain.model.Coordinate
import com.mvl.assignment.domain.model.LocationUnavailableException
import com.mvl.assignment.domain.repository.LocationRepository
import com.mvl.assignment.domain.util.runCatchingCancellable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val client: FusedLocationProviderClient,
) : LocationRepository {

    @SuppressLint("MissingPermission") // checked by hasLocationPermission()
    override suspend fun getCurrentLocation(): Result<Coordinate> = runCatchingCancellable {
        if (!hasLocationPermission()) throw LocationUnavailableException("Location permission not granted")

        val cancellation = CancellationTokenSource()
        val location = client
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
            .await(cancellation)
            ?: client.lastLocation.await()
            ?: throw LocationUnavailableException()

        Coordinate(location.latitude, location.longitude)
    }

    private fun hasLocationPermission(): Boolean =
        LOCATION_PERMISSIONS.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    private companion object {
        val LOCATION_PERMISSIONS = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }
}
