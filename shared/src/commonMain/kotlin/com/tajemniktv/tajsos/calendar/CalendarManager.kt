/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.calendar

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.CalendarEventEntity
import com.tajemniktv.tajsos.data.CalendarProviderEntity
import io.ktor.client.HttpClient
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/**
 * An orchestrator that manages synchronization across all configured remote calendar providers.
 *
 * It uses a local-first design strategy by pulling external events into the shared data layer via
 * [CalendarProviderEntity] and [CalendarEventEntity]. Synchronization works by deduplicating and merging remote events
 * with local representations. This ensures that the app
 * relies exclusively on the local Room database for rapid UI rendering, decoupling feature correctness
 * from live network availability.
 *
 * @param repository The central data repository providing database operations for calendar objects.
 * @param httpClient The configured HTTP client to perform external API queries.
 */
class CalendarManager(
    private val repository: AppRepository,
    httpClient: HttpClient,
    private val providers: List<CalendarProvider> = listOf(IcsCalendarProvider(httpClient)),
) {

    /**
     * Synchronizes local calendar events for all enabled calendar providers.
     *
     * For each enabled provider this fetches remote events within a fixed sliding window
     * (from 30 days ago to 90 days from now), deduplicates fetched events by `externalId`
     * (falling back to `"${title}_${startAt}"` when `externalId` is null), replaces that
     * provider's local events with the deduplicated set, and updates the provider's
     * `lastSyncedAt` timestamp.
     *
     * Note: This process is designed to run silently in the background, fully decoupled from
     * user interface blocking, to adhere to TajsOS's strict local-first caching principles.
     */
    suspend fun syncAll() =
        coroutineScope {
            val allProviders = repository.getAllCalendarProviders().first().orEmpty()
            val now = Clock.System.now()
            val from = now.minus(30.days)
            val to = now.plus(90.days)

            allProviders.forEach { providerEntity ->
                if (providerEntity.isEnabled) {
                    val provider = providers.find { it.type == providerEntity.type }
                    if (provider != null) {
                        val events = provider.sync(providerEntity, from, to)

                        // Deduplicate events by externalId to ensure no duplicates from malformed sources
                        val deduplicatedEvents =
                            events.distinctBy { it.externalId ?: "${it.title}_${it.startAt}" }

                        repository.deleteCalendarEventsByProvider(providerEntity.id)
                        repository.insertCalendarEvents(deduplicatedEvents)
                        repository.updateCalendarProvider(providerEntity.copy(lastSyncedAt = now.toEpochMilliseconds()))
                    }
                }
            }
        }
}

/**
 * Safe fallback returning an empty list if the nullable collection receiver is null.
 *
 * @receiver The nullable collection to unwrap.
 * @return The original list, or an empty list if null.
 */
private fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()
