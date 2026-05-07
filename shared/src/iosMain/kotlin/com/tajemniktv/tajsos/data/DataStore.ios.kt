package com.tajemniktv.tajsos.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import okio.Path.Companion.toPath

@OptIn(ExperimentalForeignApi::class)
fun createDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
    produceFile = {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        (requireNotNull(documentDirectory).path + "/tajsos.preferences_pb").toPath()
    }
)
