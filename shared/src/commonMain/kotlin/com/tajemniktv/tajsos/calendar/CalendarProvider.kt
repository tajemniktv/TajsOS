/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.calendar

import com.tajemniktv.tajsos.data.CalendarEventEntity
import com.tajemniktv.tajsos.data.CalendarProviderEntity
import kotlin.time.Instant

/**
 * Represents a generic calendar provider capable of fetching and synchronizing events.
 */
interface CalendarProvider {
    /**
     * The unique identifier or type name of the calendar provider.
     */
    val type: String

    /**
     * Fetches events for the specified provider within a given time range.
     *
     * @param provider The calendar provider entity containing configuration such as URL.
     * @param from The start of the time range (inclusive).
     * @param to The end of the time range (inclusive).
     * @return A list of fetched [CalendarEventEntity] instances.
     */
    suspend fun fetchEvents(
        provider: CalendarProviderEntity,
        from: Instant,
        to: Instant
    ): List<CalendarEventEntity>

    /**
     * Synchronizes events for the specified provider within a given time range.
     * By default, it delegates to [fetchEvents].
     *
     * @param provider The calendar provider entity containing configuration such as URL.
     * @param from The start of the time range (inclusive).
     * @param to The end of the time range (inclusive).
     * @return A list of synchronized [CalendarEventEntity] instances.
     */
    suspend fun sync(
        provider: CalendarProviderEntity,
        from: Instant,
        to: Instant
    ): List<CalendarEventEntity> {
        return fetchEvents(provider, from, to)
    }
}
