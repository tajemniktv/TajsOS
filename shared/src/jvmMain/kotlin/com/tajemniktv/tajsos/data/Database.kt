/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import com.tajemniktv.tajsos.utils.AppDirs

/**
 * Opens the local desktop database. During pre-alpha, a missing schema migration path
 * triggers destructive recreation and loss of local data; this is not a backup mechanism.
 */
fun createDatabase(): AppDatabase {
    val dbFile = File(AppDirs.getAppDataDir(), "tajsos.db")
    return Room
        .databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
            factory = AppDatabaseConstructor::initialize,
        ).setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(true)
        .build()
}
