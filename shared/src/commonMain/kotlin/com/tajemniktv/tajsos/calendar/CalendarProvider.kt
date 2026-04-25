/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.calendar

import com.tajemniktv.tajsos.data.CalendarEventEntity
import com.tajemniktv.tajsos.data.CalendarProviderEntity
import kotlin.time.Instant

/**
 * Represents a generic calendar provider capable of fetching and synchronizing events.
 *
 * Implementations are responsible for parsing remote data into the common [CalendarEventEntity] format,
 * handling specific connectivity protocols (e.g., HTTP for iCal), and respecting the provided
 * date ranges.
 */
interface CalendarProvider {
    /**
     * The unique identifier or type name of the calendar provider.
     */
    val type: String

    /**
     * Fetches calendar events for the specified provider within the inclusive time range.
     *
     * @param provider The calendar provider entity containing configuration for the source.
     * @param from The start of the time range (inclusive).
     * @param to The end of the time range (inclusive).
     * @return A list of `CalendarEventEntity` instances occurring between `from` and `to`.
     */
    suspend fun fetchEvents(
        provider: CalendarProviderEntity,
        from: Instant,
        to: Instant,
    ): List<CalendarEventEntity>

    /**
     * Synchronizes events for the specified provider within the given inclusive time range.
     *
     * @param provider The calendar provider entity containing configuration (for example, URL and credentials).
     * @param from The start of the time range (inclusive).
     * @param to The end of the time range (inclusive).
     * @return A list of synchronized CalendarEventEntity instances.
     */
    suspend fun sync(
        provider: CalendarProviderEntity,
        from: Instant,
        to: Instant,
    ): List<CalendarEventEntity> = fetchEvents(provider, from, to)
}
