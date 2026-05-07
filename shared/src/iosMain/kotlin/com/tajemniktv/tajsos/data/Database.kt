package com.tajemniktv.tajsos.data

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun createDatabase(): AppDatabase {
    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val dbFilePath = requireNotNull(documentDirectory).path + "/tajsos.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
        factory = AppDatabaseConstructor::initialize
    ).setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
        .fallbackToDestructiveMigration(true)
        .build()
}
