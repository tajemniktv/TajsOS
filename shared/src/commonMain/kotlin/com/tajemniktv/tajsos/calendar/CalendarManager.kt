/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.calendar

import com.tajemniktv.tajsos.data.*
import io.ktor.client.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.Duration.Companion.days

class CalendarManager(
    private val repository: AppRepository,
    httpClient: HttpClient
) {
    private val providers = listOf(
        IcsCalendarProvider(httpClient)
    )

    suspend fun syncAll() = coroutineScope {
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
                    val deduplicatedEvents = events.distinctBy { it.externalId ?: "${it.title}_${it.startAt}" }

                    repository.deleteCalendarEventsByProvider(providerEntity.id)
                    repository.insertCalendarEvents(deduplicatedEvents)
                    repository.updateCalendarProvider(providerEntity.copy(lastSyncedAt = now.toEpochMilliseconds()))
                }
            }
        }
    }
}

private fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()
