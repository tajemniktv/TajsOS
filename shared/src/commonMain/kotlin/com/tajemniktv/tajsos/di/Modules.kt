/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tajemniktv.tajsos.calendar.CalendarManager
import com.tajemniktv.tajsos.data.*
import com.tajemniktv.tajsos.ui.MainViewModel
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Simple manual DI module.
 */
class SharedModule(
    private val database: AppDatabase,
    private val dataStore: DataStore<Preferences>,
) {
    private val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    },
                )
            }
        }
    }

    /**
     *
     */
    val repository: AppRepository by lazy {
        AppRepository(
            database.nodeDao(),
            database.focusSessionDao(),
            database.trackDao(),
            database.relationDao(),
            database.tagDao(),
            database.eventLogDao(),
            database.attachmentDao(),
            database.templateDao(),
            database.nodeSnapshotDao(),
            database.reviewDao(),
            database.calendarProviderDao(),
            database.calendarEventDao(),
        )
    }

    /**
     *
     */
    val calendarManager: CalendarManager by lazy {
        CalendarManager(repository, httpClient)
    }

    /**
     *
     */
    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(dataStore)
    }

    /**
     *
     */
    fun createViewModel(): MainViewModel =
        MainViewModel(repository, preferencesRepository, calendarManager)
}
