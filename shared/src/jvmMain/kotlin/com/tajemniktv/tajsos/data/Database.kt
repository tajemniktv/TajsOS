/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import com.tajemniktv.tajsos.utils.AppDirs

/**
 * Instantiates the Room AppDatabase for the JVM target.
 *
 * It resolves the target database file location using [AppDirs.getAppDataDir] and applies
 * the BundledSQLiteDriver to ensure consistent SQLite features are available without relying
 * on system-level SQLite installations.
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
