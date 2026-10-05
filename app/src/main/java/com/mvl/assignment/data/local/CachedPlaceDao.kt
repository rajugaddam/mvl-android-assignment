package com.mvl.assignment.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedPlaceDao {

    @Query("SELECT * FROM cached_places WHERE cacheKey = :key LIMIT 1")
    suspend fun get(key: String): CachedPlaceEntity?

    @Upsert
    suspend fun upsert(place: CachedPlaceEntity)

    @Query("SELECT * FROM cached_places ORDER BY updatedAtMillis DESC")
    fun observeAll(): Flow<List<CachedPlaceEntity>>
}
