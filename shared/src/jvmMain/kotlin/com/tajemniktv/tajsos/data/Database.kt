/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

fun createDatabase(): AppDatabase {
    val dbFile = File(System.getProperty("user.home"), "tajsos.db")
    return Room
        .databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
            factory = AppDatabaseConstructor::initialize,
        ).setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(true)
        .build()
}
