/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import android.content.Context
import androidx.room.Room

/**
 * Instantiates the Room AppDatabase for the Android target.
 *
 * Uses the standard Android Context to resolve the application's internal database directory.
 * Configures the database to fall back to destructive migration on schema changes.
 *
 * @param context The Android Application Context.
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
