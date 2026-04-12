/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.tajemniktv.tajsos.ui.SidebarMode
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
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val GLASSMORPHISM_ENABLED = booleanPreferencesKey("glassmorphism_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val ACTIVE_MODE_ID = longPreferencesKey("active_mode_id")
        val SIDEBAR_MODE = stringPreferencesKey("sidebar_mode")
        val OWNED_PACKS = stringSetPreferencesKey("owned_packs")
        val ENABLED_PACKS = stringSetPreferencesKey("enabled_packs")
    }

    /**
     * Whether biometric authentication (like fingerprint or Face ID) is enabled.
     */
    val isBiometricEnabled: Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
            }

    /**
     * The unique identifier of the currently active operating mode, or null if using the default mode.
     */
    val activeModeId: Flow<Long?> =
        dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.ACTIVE_MODE_ID]
            }

    /**
     * Whether the dark theme is enabled globally across the application.
     */
    val isDarkThemeEnabled: Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.DARK_THEME_ENABLED] ?: true
            }

    /**
     * Selected accent color hex string (e.g., "#BA9EFF").
     */
    val accentColorHex: Flow<String> =
        dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.ACCENT_COLOR] ?: "#BA9EFF"
            }

    /**
     * Whether glassmorphism effects are enabled.
     */
    val isGlassmorphismEnabled: Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.GLASSMORPHISM_ENABLED] ?: true
            }

    /**
     * Whether to reduce system animations and transitions.
     */
    val reduceMotion: Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.REDUCE_MOTION] ?: false
            }

    /**
     * Persisted sidebar behavior mode.
     */
    val sidebarMode: Flow<SidebarMode> =
        dataStore.data.map { preferences ->
            val modeStr = preferences[PreferencesKeys.SIDEBAR_MODE]
            try {
                if (modeStr != null) SidebarMode.valueOf(modeStr) else SidebarMode.EXPANDED
            } catch (e: Exception) {
                SidebarMode.EXPANDED
            }
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

    /**
     * Updates the active operating mode.
     */
    suspend fun updateActiveModeId(modeId: Long?) {
        dataStore.edit { preferences ->
            if (modeId != null) {
                preferences[PreferencesKeys.ACTIVE_MODE_ID] = modeId
            } else {
                preferences.remove(PreferencesKeys.ACTIVE_MODE_ID)
            }
        }
    }

    /**
     * Updates the sidebar behavior mode.
     */
    suspend fun updateSidebarMode(mode: SidebarMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SIDEBAR_MODE] = mode.name
        }
    }

    suspend fun updateDarkThemeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME_ENABLED] = enabled
        }
    }

    /**
     * Persists the selected accent color.
     *
     * @param colorHex The color in hex format (e.g., "#BA9EFF").
     */
    suspend fun updateAccentColor(colorHex: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCENT_COLOR] = colorHex
        }
    }

    /**
     * Updates the glassmorphism preference.
     */
    suspend fun updateGlassmorphismEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GLASSMORPHISM_ENABLED] = enabled
        }
    }

    /**
     * Updates the reduce motion preference.
     */
    suspend fun updateReduceMotion(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REDUCE_MOTION] = enabled
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
