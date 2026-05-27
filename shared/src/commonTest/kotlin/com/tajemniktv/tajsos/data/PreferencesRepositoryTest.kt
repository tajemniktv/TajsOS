/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import com.tajemniktv.tajsos.ui.SidebarMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import okio.IOException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferencesRepositoryTest {

    private class FaultyDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow {
            throw IOException("Corrupted file")
        }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            return emptyPreferences()
        }
    }

    @Test
    fun catchIoException_emitsEmptyPreferences() = runTest {
        val dataStore = FaultyDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.sidebarMode.test {
            assertEquals(SidebarMode.EXPANDED, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun updateData_throwsIoException_propagatesError() = runTest {
        val dataStore = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flowOf(emptyPreferences())

            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
                throw IOException("Write failed")
            }
        }
        val repository = PreferencesRepository(dataStore)

        var exceptionThrown = false
        try {
            repository.updateSidebarMode(SidebarMode.COLLAPSED)
        } catch (e: IOException) {
            exceptionThrown = true
        }

        kotlin.test.assertTrue(exceptionThrown, "Expected IOException to be thrown")
    }



    private class FakeDataStore : DataStore<Preferences> {
        private val _data = MutableStateFlow(emptyPreferences())
        override val data: Flow<Preferences> = _data

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val newData = transform(_data.value)
            _data.value = newData
            return newData
        }
    }

    @Test
    fun sidebarMode_defaultsToExpanded() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.sidebarMode.test {
            assertEquals(SidebarMode.EXPANDED, awaitItem())
        }
    }

    @Test
    fun sidebarMode_returnsCorrectValue() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.sidebarMode.test {
            assertEquals(SidebarMode.EXPANDED, awaitItem())

            repository.updateSidebarMode(SidebarMode.COLLAPSED)
            assertEquals(SidebarMode.COLLAPSED, awaitItem())

            repository.updateSidebarMode(SidebarMode.HOVER_EXPAND)
            assertEquals(SidebarMode.HOVER_EXPAND, awaitItem())
        }
    }

    @Test
    fun sidebarMode_fallsBackToExpanded_onInvalidValue() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)
        val sidebarModeKey = stringPreferencesKey("sidebar_mode")

        repository.sidebarMode.test {
            assertEquals(SidebarMode.EXPANDED, awaitItem())

            dataStore.edit { prefs ->
                prefs[sidebarModeKey] = "INVALID_VALUE"
            }
            assertEquals(SidebarMode.EXPANDED, awaitItem())
        }
    }

    @Test
    fun desktopWindowStartupMode_defaultsToRestoreLast() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.desktopWindowStartupMode.test {
            assertEquals(DesktopWindowStartupMode.RESTORE_LAST, awaitItem())
        }
    }

    @Test
    fun desktopWindowStartupMode_returnsCorrectValue() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.desktopWindowStartupMode.test {
            assertEquals(DesktopWindowStartupMode.RESTORE_LAST, awaitItem())

            repository.updateDesktopWindowStartupMode(DesktopWindowStartupMode.ALWAYS_MAXIMIZED)
            assertEquals(DesktopWindowStartupMode.ALWAYS_MAXIMIZED, awaitItem())
        }
    }

    @Test
    fun desktopWindowStartupMode_fallsBackToRestoreLast_onInvalidValue() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)
        val key = stringPreferencesKey("desktop_window_startup_mode")

        repository.desktopWindowStartupMode.test {
            assertEquals(DesktopWindowStartupMode.RESTORE_LAST, awaitItem())

            dataStore.edit { prefs ->
                prefs[key] = "NOT_A_MODE"
            }
            assertEquals(DesktopWindowStartupMode.RESTORE_LAST, awaitItem())
        }
    }

    private class FatalErrorDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow {
            throw IllegalStateException("Database is closed")
        }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            return emptyPreferences()
        }
    }

    @Test
    fun catchIoException_rethrowsNonIoExceptions() = runTest {
        val dataStore = FatalErrorDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.sidebarMode.test {
            val error = awaitError()
            assertEquals("Database is closed", error.message)
            assertEquals(true, error is IllegalStateException)
        }
    }


    @Test
    fun isBiometricEnabled_returnsCorrectValue() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.isBiometricEnabled.test {
            assertEquals(false, awaitItem())
            repository.updateBiometricEnabled(true)
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun activeModeId_returnsCorrectValue() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.activeModeId.test {
            assertEquals(null, awaitItem())
            repository.updateActiveModeId(42L)
            assertEquals(42L, awaitItem())
            repository.updateActiveModeId(null)
            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun isDarkThemeEnabled_returnsCorrectValue() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.isDarkThemeEnabled.test {
            assertEquals(true, awaitItem())
            repository.updateDarkThemeEnabled(false)
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun accentColorHex_returnsCorrectValue() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.accentColorHex.test {
            assertEquals("#BA9EFF", awaitItem())
            repository.updateAccentColor("#FF0000")
            assertEquals("#FF0000", awaitItem())
        }
    }

    @Test
    fun isGlassmorphismEnabled_returnsCorrectValue() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.isGlassmorphismEnabled.test {
            assertEquals(true, awaitItem())
            repository.updateGlassmorphismEnabled(false)
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun reduceMotion_returnsCorrectValue() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.reduceMotion.test {
            assertEquals(false, awaitItem())
            repository.updateReduceMotion(true)
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun sidebarExpandedWidthDp_clampsToSafeRange() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.sidebarExpandedWidthDp.test {
            assertEquals(PreferencesRepository.DEFAULT_SIDEBAR_EXPANDED_WIDTH_DP, awaitItem())

            repository.updateSidebarExpandedWidthDp(250)
            assertEquals(250, awaitItem())

            repository.updateSidebarExpandedWidthDp(100) // Below MIN_SIDEBAR_EXPANDED_WIDTH_DP (220)
            assertEquals(220, awaitItem())

            repository.updateSidebarExpandedWidthDp(500) // Above MAX_SIDEBAR_EXPANDED_WIDTH_DP (360)
            assertEquals(360, awaitItem())
        }
    }

    @Test
    fun desktopWindowPlacement_returnsCorrectValue() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        repository.desktopWindowPlacement.test {
            assertEquals(PreferencesRepository.DesktopWindowPlacement(null, null, null, null, false), awaitItem())

            val placement = PreferencesRepository.DesktopWindowPlacement(100, 200, 800, 600, true)
            repository.updateDesktopWindowPlacement(placement)
            assertEquals(placement, awaitItem())

            // Test clearing values by setting nulls
            repository.updateDesktopWindowPlacement(PreferencesRepository.DesktopWindowPlacement(null, null, null, null, false))
            assertEquals(PreferencesRepository.DesktopWindowPlacement(null, null, null, null, false), awaitItem())
        }
    }

    @Test
    fun packManagement_updatesOwnedAndEnabledPacks() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)
        val customPack = AppPack.STUDENT
        val freePack = AppPack.MAINTENANCE

        val defaultRegistry = repository.enabledPacks.first()
        assertEquals(AppPack.defaultFreePackKeys, defaultRegistry.ownedPackKeys)
        assertEquals(AppPack.defaultFreePackKeys, defaultRegistry.enabledPackKeys)

        // Buy a premium pack
        repository.setPackOwned(customPack, true)
        val updatedOwned = repository.enabledPacks.first()
        kotlin.test.assertTrue(updatedOwned.ownedPackKeys.contains(customPack.key))

        // Enable it
        repository.setPackEnabled(customPack, true)
        val updatedEnabled = repository.enabledPacks.first()
        kotlin.test.assertTrue(updatedEnabled.enabledPackKeys.contains(customPack.key))

        // Disable it
        repository.setPackEnabled(customPack, false)
        val updatedDisabled = repository.enabledPacks.first()
        kotlin.test.assertFalse(updatedDisabled.enabledPackKeys.contains(customPack.key))

        // Un-own it (which should also disable it if it were enabled)
        repository.setPackEnabled(customPack, true)
        repository.setPackOwned(customPack, false)
        val unowned = repository.enabledPacks.first()
        kotlin.test.assertFalse(unowned.ownedPackKeys.contains(customPack.key))
        kotlin.test.assertFalse(unowned.enabledPackKeys.contains(customPack.key))

        // Test free pack logic
        repository.setPackOwned(freePack, true)
        repository.setPackOwned(freePack, false)
        val afterFreeUnown = repository.enabledPacks.first()
        kotlin.test.assertTrue(afterFreeUnown.ownedPackKeys.contains(freePack.key))
    }

    @Test
    fun ensureDefaultPackAccess_addsDefaults() = runTest {
        val dataStore = FakeDataStore()
        val repository = PreferencesRepository(dataStore)

        // Clear everything first to simulate missing defaults
        dataStore.updateData { prefs ->
            val mutablePrefs = prefs.toMutablePreferences()
            mutablePrefs.remove(stringSetPreferencesKey("owned_packs"))
            mutablePrefs.remove(stringSetPreferencesKey("enabled_packs"))
            mutablePrefs
        }

        repository.ensureDefaultPackAccess()
        val result = repository.enabledPacks.first()

        assertEquals(AppPack.defaultFreePackKeys, result.ownedPackKeys)
        assertEquals(AppPack.defaultFreePackKeys, result.enabledPackKeys)
    }

}
