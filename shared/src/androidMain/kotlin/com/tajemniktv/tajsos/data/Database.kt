/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import android.content.Context
import androidx.room.Room

/**
 * Opens the local Android database. During pre-alpha, a missing schema migration path
 * triggers destructive recreation and loss of local data; this is not a backup mechanism.
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
