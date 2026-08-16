/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.tajemniktv.tajsos.ui.SidebarMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.catch
import okio.IOException

/**
 * Startup strategy for desktop window placement.
 */
enum class DesktopWindowStartupMode {
    RESTORE_LAST,
    ALWAYS_MAXIMIZED,
}

/**
 * PreferencesRepository handles all of TajsOS's configuration data.
 *
 * It manages application-wide settings such as UI preferences (theme, sidebar mode),
 * operational state (active mode id, active packs), and desktop-specific geometry
 * using the multiplatform [DataStore] library.
 *
 * All properties are exposed as [Flow] streams that emit the current value,
 * and automatically handle [IOException]s by emitting [emptyPreferences] as a fallback.
 *
 * @param dataStore The multiplatform data store managing preference persistence.
 */
class PreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {

    private val safeData: Flow<Preferences> =
        dataStore.data.catchIoException()

    /**
     * Persisted desktop window geometry and placement behavior.
     */
    data class DesktopWindowPlacement(
        val xDp: Int? = null,
        val yDp: Int? = null,
        val widthDp: Int? = null,
        val heightDp: Int? = null,
        val isMaximized: Boolean = false,
    )

    companion object {
        const val DEFAULT_SIDEBAR_EXPANDED_WIDTH_DP: Int = 236
        private const val MIN_SIDEBAR_EXPANDED_WIDTH_DP: Int = 220
        private const val MAX_SIDEBAR_EXPANDED_WIDTH_DP: Int = 360
        private const val MIN_DESKTOP_WINDOW_WIDTH_DP: Int = 640
        private const val MIN_DESKTOP_WINDOW_HEIGHT_DP: Int = 480
    }

    private object PreferencesKeys {
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val DARK_THEME_ENABLED = booleanPreferencesKey("dark_theme_enabled")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val GLASSMORPHISM_ENABLED = booleanPreferencesKey("glassmorphism_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val ACTIVE_MODE_ID = longPreferencesKey("active_mode_id")
        val SIDEBAR_MODE = stringPreferencesKey("sidebar_mode")
        val SIDEBAR_EXPANDED_WIDTH_DP = intPreferencesKey("sidebar_expanded_width_dp")
        val DESKTOP_WINDOW_X_DP = intPreferencesKey("desktop_window_x_dp")
        val DESKTOP_WINDOW_Y_DP = intPreferencesKey("desktop_window_y_dp")
        val DESKTOP_WINDOW_WIDTH_DP = intPreferencesKey("desktop_window_width_dp")
        val DESKTOP_WINDOW_HEIGHT_DP = intPreferencesKey("desktop_window_height_dp")
        val DESKTOP_WINDOW_MAXIMIZED = booleanPreferencesKey("desktop_window_maximized")
        val DESKTOP_WINDOW_STARTUP_MODE = stringPreferencesKey("desktop_window_startup_mode")
        val OWNED_PACKS = stringSetPreferencesKey("owned_packs")
        val ENABLED_PACKS = stringSetPreferencesKey("enabled_packs")
    }

    val isBiometricEnabled: Flow<Boolean> =
        safeData
            .map { preferences ->
                preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
            }

    val activeModeId: Flow<Long?> =
        safeData
            .map { preferences ->
                preferences[PreferencesKeys.ACTIVE_MODE_ID]
            }

    val isDarkThemeEnabled: Flow<Boolean> =
        safeData
            .map { preferences ->
                preferences[PreferencesKeys.DARK_THEME_ENABLED] ?: true
            }

    /**
     * Selected accent color hex string (e.g., "#BA9EFF").
     */
    val accentColorHex: Flow<String> =
        safeData
            .map { preferences ->
                preferences[PreferencesKeys.ACCENT_COLOR] ?: "#BA9EFF"
            }

    /**
     * Whether glassmorphism effects are enabled.
     */
    val isGlassmorphismEnabled: Flow<Boolean> =
        safeData
            .map { preferences ->
                preferences[PreferencesKeys.GLASSMORPHISM_ENABLED] ?: true
            }

    /**
     * Whether to reduce system animations and transitions.
     */
    val reduceMotion: Flow<Boolean> =
        safeData
            .map { preferences ->
                preferences[PreferencesKeys.REDUCE_MOTION] ?: false
            }

    /**
     * Persisted sidebar behavior mode.
     */
    val sidebarMode: Flow<SidebarMode> =
        safeData
            .map { preferences ->
            val modeStr = preferences[PreferencesKeys.SIDEBAR_MODE]
            SidebarMode.entries.find { it.name == modeStr } ?: SidebarMode.EXPANDED
        }

    /**
     * Persisted sidebar expanded width in density-independent pixels.
     */
    val sidebarExpandedWidthDp: Flow<Int> =
        safeData
            .map { preferences ->
            (preferences[PreferencesKeys.SIDEBAR_EXPANDED_WIDTH_DP] ?: DEFAULT_SIDEBAR_EXPANDED_WIDTH_DP)
                .coerceIn(MIN_SIDEBAR_EXPANDED_WIDTH_DP, MAX_SIDEBAR_EXPANDED_WIDTH_DP)
        }

    /**
     * Persisted desktop window placement used to restore geometry after app restart.
     */
    val desktopWindowPlacement: Flow<DesktopWindowPlacement> =
        safeData
            .map { preferences ->
            DesktopWindowPlacement(
                xDp = preferences[PreferencesKeys.DESKTOP_WINDOW_X_DP],
                yDp = preferences[PreferencesKeys.DESKTOP_WINDOW_Y_DP],
                widthDp = preferences[PreferencesKeys.DESKTOP_WINDOW_WIDTH_DP],
                heightDp = preferences[PreferencesKeys.DESKTOP_WINDOW_HEIGHT_DP],
                isMaximized = preferences[PreferencesKeys.DESKTOP_WINDOW_MAXIMIZED] ?: false,
            )
        }

    /**
     * Startup strategy for desktop window placement behavior.
     */
    val desktopWindowStartupMode: Flow<DesktopWindowStartupMode> =
        safeData
            .map { preferences ->
            val rawValue = preferences[PreferencesKeys.DESKTOP_WINDOW_STARTUP_MODE]
            DesktopWindowStartupMode.entries.find { it.name == rawValue } ?: DesktopWindowStartupMode.RESTORE_LAST
        }

    val enabledPacks: Flow<PackRegistry> =
        safeData
            .map { preferences ->
            val owned = preferences[PreferencesKeys.OWNED_PACKS] ?: AppPack.defaultFreePackKeys
            val enabled = preferences[PreferencesKeys.ENABLED_PACKS] ?: AppPack.defaultFreePackKeys
            PackRegistry(
                ownedPackKeys = owned,
                enabledPackKeys = enabled.intersect(owned),
            )
        }

    val ownedPacks: Flow<Set<String>> =
        safeData
            .map { preferences ->
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

    /**
     * Persists sidebar expanded width (dp), clamped to a safe desktop range.
     */
    suspend fun updateSidebarExpandedWidthDp(widthDp: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SIDEBAR_EXPANDED_WIDTH_DP] =
                widthDp.coerceIn(MIN_SIDEBAR_EXPANDED_WIDTH_DP, MAX_SIDEBAR_EXPANDED_WIDTH_DP)
        }
    }

    /**
     * Persists desktop window geometry and placement mode.
     */
    suspend fun updateDesktopWindowPlacement(placement: DesktopWindowPlacement) {
        dataStore.edit { preferences ->
            if (placement.xDp != null && placement.yDp != null) {
                preferences[PreferencesKeys.DESKTOP_WINDOW_X_DP] = placement.xDp
                preferences[PreferencesKeys.DESKTOP_WINDOW_Y_DP] = placement.yDp
            } else {
                preferences.remove(PreferencesKeys.DESKTOP_WINDOW_X_DP)
                preferences.remove(PreferencesKeys.DESKTOP_WINDOW_Y_DP)
            }

            if (placement.widthDp != null && placement.heightDp != null) {
                preferences[PreferencesKeys.DESKTOP_WINDOW_WIDTH_DP] =
                    placement.widthDp.coerceAtLeast(MIN_DESKTOP_WINDOW_WIDTH_DP)
                preferences[PreferencesKeys.DESKTOP_WINDOW_HEIGHT_DP] =
                    placement.heightDp.coerceAtLeast(MIN_DESKTOP_WINDOW_HEIGHT_DP)
            } else {
                preferences.remove(PreferencesKeys.DESKTOP_WINDOW_WIDTH_DP)
                preferences.remove(PreferencesKeys.DESKTOP_WINDOW_HEIGHT_DP)
            }
            preferences[PreferencesKeys.DESKTOP_WINDOW_MAXIMIZED] = placement.isMaximized
        }
    }

    /**
     * Persists desktop startup strategy.
     */
    suspend fun updateDesktopWindowStartupMode(mode: DesktopWindowStartupMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DESKTOP_WINDOW_STARTUP_MODE] = mode.name
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

/**
 * A safety extension for handling data store reads.
 *
 * It traps [okio.IOException] (or its platform equivalents) which occur when the underlying
 * data store file is missing, corrupted, or inaccessible. Instead of crashing the stream,
 * it emits [emptyPreferences] to ensure the application can start using default values.
 * Other non-IO exceptions are rethrown.
 */
private fun Flow<Preferences>.catchIoException(): Flow<Preferences> = catch { e ->
    if (e is IOException) {
        emit(emptyPreferences())
    } else {
        throw e
    }
}
