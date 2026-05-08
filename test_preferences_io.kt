package com.tajemniktv.tajsos.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.turbine.test
import com.tajemniktv.tajsos.ui.SidebarMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import okio.IOException

class PreferencesRepositoryIoExceptionTest {

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
            // Should fallback to default instead of crashing
            assertEquals(SidebarMode.EXPANDED, awaitItem())
            awaitComplete()
        }
    }
}
