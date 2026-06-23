/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import com.tajemniktv.tajsos.utils.AppDirs

fun createDatabase(): AppDatabase {
    val dbFile = File(AppDirs.getAppDataDir(), "tajsos.db")
    return Room
        .databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
            factory = AppDatabaseConstructor::initialize,
        ).setDriver(BundledSQLiteDriver())
        // Enables destructive migration as a pre-alpha safety posture.
        // Any schema growth during this phase is high-risk but acceptable without strict migrations.
        // Replace this with proper migrations once the architecture matures.
        .fallbackToDestructiveMigration(true)
        .build()
}
