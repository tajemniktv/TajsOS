/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.ConstructedBy
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [
        NodeEntity::class,
        TodayPinEntity::class,
        FocusSessionEntity::class,
        TrackEntryEntity::class,
        RelationEntity::class,
        TagEntity::class,
        NodeTagEntity::class,
        EventLogEntity::class,
        AttachmentEntity::class,
        TemplateEntity::class,
        CalendarProviderEntity::class,
        CalendarEventEntity::class
    ],
    version = 12,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nodeDao(): NodeDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun trackDao(): TrackDao
    abstract fun relationDao(): RelationDao
    abstract fun tagDao(): TagDao
    abstract fun eventLogDao(): EventLogDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun templateDao(): TemplateDao
    abstract fun calendarProviderDao(): CalendarProviderDao
    abstract fun calendarEventDao(): CalendarEventDao
}

// The Room compiler generates the implementation of this class
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>

fun getDatabaseBuilder(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .fallbackToDestructiveMigration(true)
        .build()
}
