/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tajemniktv.tajsos.calendar.CalendarManager
import com.tajemniktv.tajsos.data.AppDatabase
import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.PreferencesRepository
import com.tajemniktv.tajsos.ui.MainViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
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
            database.modeDao(),
            database.protocolDao(),
            database.decisionDao(),
            database.userDao(),
            database.medicationDao(),
            database.inboxEntryDao(),
            database.taskFacetDao(),
            database.noteFacetDao(),
            database.projectFacetDao(),
            database.areaFacetDao(),
            database.recordFacetDao(),
            database.itemDomainDao(),
            database.richContentDocumentDao(),
            database.scheduleEntryDao(),
            database.savedViewDao(),
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
    fun createViewModel(
        nextStepFallbackLabel: String = "Next step",
        untitledFallbackLabel: String = "Untitled",
    ): MainViewModel =
        MainViewModel(
            repository = repository,
            preferencesRepository = preferencesRepository,
            calendarManager = calendarManager,
            nextStepFallbackLabel = nextStepFallbackLabel,
            untitledFallbackLabel = untitledFallbackLabel,
        )
}
