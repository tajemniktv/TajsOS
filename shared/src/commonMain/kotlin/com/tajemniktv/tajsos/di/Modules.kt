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
 * Core manual Dependency Injection (DI) module for shared multiplatform dependencies.
 *
 * It manages the lifecycle and initialization of core infrastructure components such as
 * the database instance, Ktor HTTP client, and various repositories. It provides these
 * singletons to platform-specific application entry points.
 *
 * @property database The cross-platform SQLite database implementation.
 * @property dataStore The multiplatform Jetpack DataStore instance for user preferences.
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
     * Central repository handling persistence, queries, and complex operations over core
     * life objects (nodes, facets, relations) and app state (preferences, modes).
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
     * Manager responsible for syncing and orchestrating external calendar providers
     * (e.g., ICS feeds) into the local event database.
     */
    val calendarManager: CalendarManager by lazy {
        CalendarManager(repository, httpClient)
    }

    /**
     * Repository handling lightweight, key-value application settings backed by DataStore.
     */
    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(dataStore)
    }

    /**
     * Factory function to instantiate the root [MainViewModel].
     *
     * @param nextStepFallbackLabel Fallback text when a project lacks a defined next action.
     * @param untitledFallbackLabel Fallback text when an item lacks a title.
     * @return The constructed [MainViewModel] wired with necessary repositories.
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
