package com.mvl.assignment.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedPlaceEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedPlaceDao(): CachedPlaceDao

    companion object {
        const val NAME = "mvl.db"
    }
}
