/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

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
}
