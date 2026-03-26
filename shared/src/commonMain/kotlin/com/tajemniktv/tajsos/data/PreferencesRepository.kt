/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * PreferencesRepository handles all of TajsOS's configuration data.
 */
class PreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    private object PreferencesKeys {
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val DARK_THEME_ENABLED = booleanPreferencesKey("dark_theme_enabled")
        val ACTIVE_MODE_ID = longPreferencesKey("active_mode_id")
        val OWNED_PACKS = stringSetPreferencesKey("owned_packs")
        val ENABLED_PACKS = stringSetPreferencesKey("enabled_packs")
    }

    val isBiometricEnabled: Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
            }

    /** Emits whether the app should render the dark theme; defaults to true. */
    val isDarkThemeEnabled: Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.DARK_THEME_ENABLED] ?: true
            }

    val activeModeId: Flow<Long?> =
        dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.ACTIVE_MODE_ID]
            }

    val enabledPacks: Flow<PackRegistry> =
        dataStore.data.map { preferences ->
            val owned = preferences[PreferencesKeys.OWNED_PACKS] ?: AppPack.defaultFreePackKeys
            val enabled = preferences[PreferencesKeys.ENABLED_PACKS] ?: AppPack.defaultFreePackKeys
            PackRegistry(
                ownedPackKeys = owned,
                enabledPackKeys = enabled.intersect(owned),
            )
        }

    val ownedPacks: Flow<Set<String>> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.OWNED_PACKS] ?: AppPack.defaultFreePackKeys
        }

    suspend fun updateBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    /** Persists the user's dark-theme preference. */
    suspend fun updateDarkThemeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME_ENABLED] = enabled
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

    suspend fun setPackEnabled(
        pack: AppPack,
        enabled: Boolean,
    ) {
        dataStore.edit { preferences ->
            val owned = preferences[PreferencesKeys.OWNED_PACKS] ?: AppPack.defaultFreePackKeys
            val current = preferences[PreferencesKeys.ENABLED_PACKS].orEmpty().toMutableSet()
            if (enabled && !owned.contains(pack.key)) return@edit
            if (enabled) {
                current += pack.key
            } else {
                current -= pack.key
            }
            preferences[PreferencesKeys.ENABLED_PACKS] = current
        }
    }

    suspend fun setPackOwned(
        pack: AppPack,
        owned: Boolean,
    ) {
        dataStore.edit { preferences ->
            val ownedSet =
                (preferences[PreferencesKeys.OWNED_PACKS] ?: AppPack.defaultFreePackKeys)
                    .toMutableSet()
            val enabledSet =
                (preferences[PreferencesKeys.ENABLED_PACKS] ?: AppPack.defaultFreePackKeys)
                    .toMutableSet()

            if (owned) {
                ownedSet += pack.key
            } else {
                if (!pack.isFree) {
                    ownedSet -= pack.key
                    enabledSet -= pack.key
                }
            }
            preferences[PreferencesKeys.OWNED_PACKS] = ownedSet
            preferences[PreferencesKeys.ENABLED_PACKS] = enabledSet.intersect(ownedSet)
        }
    }

    suspend fun ensureDefaultPackAccess() {
        dataStore.edit { preferences ->
            val owned = (preferences[PreferencesKeys.OWNED_PACKS] ?: emptySet()).toMutableSet()
            val enabled = (preferences[PreferencesKeys.ENABLED_PACKS] ?: emptySet()).toMutableSet()
            val defaults = AppPack.defaultFreePackKeys

            var changed = false
            if (!owned.containsAll(defaults)) {
                owned += defaults
                changed = true
            }
            if (!enabled.containsAll(defaults)) {
                enabled += defaults
                changed = true
            }
            if (changed) {
                preferences[PreferencesKeys.OWNED_PACKS] = owned
                preferences[PreferencesKeys.ENABLED_PACKS] = enabled.intersect(owned)
            }
        }
    }
}
