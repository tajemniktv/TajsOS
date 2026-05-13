/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSBundle

// Set to false for production builds to prevent data loss
private const val DEBUG = true

/**
 * Creates the Room database instance for iOS.
 *
 * This function initializes the SQLite database in the iOS document directory using
 * the bundled SQLite driver. In debug builds, destructive migration is enabled for
 * convenience during development. Production builds should disable this to prevent
 * data loss on schema changes.
 *
 * @return A configured [AppDatabase] instance
 */
fun createDatabase(): AppDatabase {
    val dbFile = getDocumentDirectory() + "/tajsos.db"
    val builder = Room
        .databaseBuilder<AppDatabase>(
            name = dbFile,
            factory = AppDatabaseConstructor::initialize,
        ).setDriver(BundledSQLiteDriver())

    // Only use destructive migration in debug builds to avoid silently wiping user data in production.
    // In production, schema changes should be handled with proper migrations.
    if (DEBUG) {
        builder.fallbackToDestructiveMigration(true)
    }

    return builder.build()
}
