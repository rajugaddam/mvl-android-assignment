package com.mvl.assignment.data.repository

import com.mvl.assignment.data.local.CachedPlaceDao
import com.mvl.assignment.data.mapper.toCachedEntity
import com.mvl.assignment.data.mapper.toDomain
import com.mvl.assignment.data.time.TimeProvider
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.repository.PlaceCacheRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Room-backed cache. Room runs suspend DAO calls and Flow queries off the main thread itself. */
class PlaceCacheRepositoryImpl @Inject constructor(
    private val dao: CachedPlaceDao,
    private val timeProvider: TimeProvider,
) : PlaceCacheRepository {

    override suspend fun get(key: String): Place? = dao.get(key)?.toDomain()

    override suspend fun save(key: String, place: Place) {
        dao.upsert(place.toCachedEntity(cacheKey = key, updatedAtMillis = timeProvider.nowMillis()))
    }

    override fun observeAll(): Flow<List<Place>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }
}
