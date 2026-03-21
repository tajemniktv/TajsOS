package com.tajemniktv.tajsos.di

import com.tajemniktv.tajsos.data.*
import com.tajemniktv.tajsos.ui.MainViewModel
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * Simple manual DI module.
 */
class SharedModule(
    private val database: AppDatabase,
    private val dataStore: DataStore<Preferences>
) {
    val repository: AppRepository by lazy {
        AppRepository(
            database.nodeDao(),
            database.focusSessionDao(),
            database.trackDao(),
            database.relationDao(),
            database.tagDao(),
            database.eventLogDao(),
            database.attachmentDao(),
            database.templateDao()
        )
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(dataStore)
    }

    fun createViewModel(): MainViewModel {
        return MainViewModel(repository, preferencesRepository)
    }
}
