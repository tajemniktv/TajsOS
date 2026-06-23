/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import android.content.Context
import androidx.room.Room

fun createDatabase(context: Context): AppDatabase {
    val dbFile = context.getDatabasePath("tajsos.db")
    return Room
        .databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath,
            factory = AppDatabaseConstructor::initialize,
        )
        // Enables destructive migration as a pre-alpha safety posture.
        // Any schema growth during this phase is high-risk but acceptable without strict migrations.
        // Replace this with proper migrations once the architecture matures.
        .fallbackToDestructiveMigration(true)
        .build()
}
