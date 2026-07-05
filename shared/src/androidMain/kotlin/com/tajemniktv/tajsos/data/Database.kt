/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import android.content.Context
import androidx.room.Room

/**
 * Creates and configures the Room database instance.
 *
 * Note: The database is currently configured with `fallbackToDestructiveMigration(true)`
 * as a pre-alpha safety posture. This permits rapid schema iteration without enforcing
 * strict migrations, but will wipe local data if the schema version changes.
 */
fun createDatabase(context: Context): AppDatabase {
    val dbFile = context.getDatabasePath("tajsos.db")
    return Room
        .databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath,
            factory = AppDatabaseConstructor::initialize,
        ).fallbackToDestructiveMigration(true)
        .build()
}
