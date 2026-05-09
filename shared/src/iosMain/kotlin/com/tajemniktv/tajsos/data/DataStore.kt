/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import okio.Path.Companion.toPath

/**
 * Creates a DataStore instance for storing user preferences on iOS.
 *
 * This function uses the iOS document directory to persist preferences in a file named
 * "tajsos.preferences_pb". If the file becomes corrupted, it will be replaced with
 * empty preferences.
 *
 * Note: This function uses [ExperimentalForeignApi] for iOS platform interop via
 * the internal [getDocumentDirectory] helper.
 *
 * @return A configured [DataStore] instance for [Preferences]
 */
fun createDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = {
            val path = getDocumentDirectory() + "/tajsos.preferences_pb"
            path.toPath()
        }
    )
}
