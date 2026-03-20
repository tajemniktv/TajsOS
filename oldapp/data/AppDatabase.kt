/*
 * Copyright (c) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * AppDatabase is the main Room database configuration.
 * It defines the entities and DAOs available to the app.
 */
@Database(
    entities = [
        ItemEntity::class,
        ProjectEntity::class,
        AreaEntity::class,
        TodayPinEntity::class,
        FocusSessionEntity::class,
        TrackEntryEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun projectDao(): ProjectDao
    abstract fun areaDao(): AreaDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun trackDao(): TrackDao
}
