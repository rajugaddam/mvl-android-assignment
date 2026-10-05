package com.mvl.assignment.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_places")
data class CachedPlaceEntity(
    /** PlaceCacheKey: coordinates truncated to 3 decimals. */
    @PrimaryKey val cacheKey: String,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val aqi: Int?,
    val updatedAtMillis: Long,
)
