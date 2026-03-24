/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * PreferencesRepository handles all of TajsOS's configuration data.
 */
class PreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val ACTIVE_MODE_ID = longPreferencesKey("active_mode_id")
    }

    val isBiometricEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
        }

    val activeModeId: Flow<Long?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ACTIVE_MODE_ID]
        }

    suspend fun updateBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun updateActiveModeId(modeId: Long?) {
        dataStore.edit { preferences ->
            if (modeId != null) {
                preferences[PreferencesKeys.ACTIVE_MODE_ID] = modeId
            } else {
                preferences.remove(PreferencesKeys.ACTIVE_MODE_ID)
            }
        }
    }
}
