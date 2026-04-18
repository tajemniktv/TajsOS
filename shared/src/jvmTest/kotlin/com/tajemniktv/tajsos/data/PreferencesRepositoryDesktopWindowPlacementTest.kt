/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PreferencesRepositoryDesktopWindowPlacementTest {
    @Test
    fun desktopWindowStartupMode_defaultsToRestoreLast() =
        runTest {
            val repository = PreferencesRepository(createTestDataStore())
            assertEquals(DesktopWindowStartupMode.RESTORE_LAST, repository.desktopWindowStartupMode.first())
        }

    @Test
    fun updateDesktopWindowPlacement_persistsValuesAndMinimumSize() =
        runTest {
            val repository = PreferencesRepository(createTestDataStore())

            repository.updateDesktopWindowPlacement(
                PreferencesRepository.DesktopWindowPlacement(
                    xDp = 140,
                    yDp = 72,
                    widthDp = 300,
                    heightDp = 220,
                    isMaximized = true,
                ),
            )

            val persisted = repository.desktopWindowPlacement.first()
            assertEquals(140, persisted.xDp)
            assertEquals(72, persisted.yDp)
            assertEquals(640, persisted.widthDp)
            assertEquals(480, persisted.heightDp)
            assertTrue(persisted.isMaximized)
        }

    @Test
    fun updateDesktopWindowPlacement_clearsGeometryWhenUnset() =
        runTest {
            val repository = PreferencesRepository(createTestDataStore())

            repository.updateDesktopWindowPlacement(
                PreferencesRepository.DesktopWindowPlacement(
                    xDp = null,
                    yDp = null,
                    widthDp = null,
                    heightDp = null,
                    isMaximized = false,
                ),
            )

            val persisted = repository.desktopWindowPlacement.first()
            assertNull(persisted.xDp)
            assertNull(persisted.yDp)
            assertNull(persisted.widthDp)
            assertNull(persisted.heightDp)
            assertFalse(persisted.isMaximized)
        }

    @Test
    fun updateDesktopWindowStartupMode_persistsSelectedMode() =
        runTest {
            val repository = PreferencesRepository(createTestDataStore())
            repository.updateDesktopWindowStartupMode(DesktopWindowStartupMode.ALWAYS_MAXIMIZED)

            assertEquals(
                DesktopWindowStartupMode.ALWAYS_MAXIMIZED,
                repository.desktopWindowStartupMode.first(),
            )
        }

    private fun createTestDataStore() =
        PreferenceDataStoreFactory.create(
            produceFile = {
                Files.createTempFile("tajsos-prefs-test", ".preferences_pb").toFile().apply {
                    deleteOnExit()
                }
            },
        )
}
