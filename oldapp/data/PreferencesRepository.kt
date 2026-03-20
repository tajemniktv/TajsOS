/*
 * Copyright (c) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.text.get

/**
 * PreferencesRepository handles all of TajsOS's configuration data, 
 * starting with biometric authentication status.
 */
class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    /**
     * Keys are strongly typed to prevent errors when reading or writing 
     * common preferences across the app.
     */
    private object PreferencesKeys {
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    /**
     * isBiometricEnabled reads from the DataStore and defaults to false 
     * if the key hasn't been set by the user yet.
     */
    val isBiometricEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
        }

    /**
     * Updates the biometric setting asynchronously using the edit() function.
     */
    suspend fun updateBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }
}
