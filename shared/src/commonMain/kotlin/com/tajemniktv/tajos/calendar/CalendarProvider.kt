/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.calendar

import com.tajemniktv.tajos.data.CalendarEventEntity
import com.tajemniktv.tajos.data.CalendarProviderEntity
import kotlin.time.Instant
import kotlin.coroutines.cancellation.CancellationException

/** Separates an unavailable source from a successfully fetched, genuinely empty calendar. */
sealed interface CalendarSyncResult {
    data class Success(val events: List<CalendarEventEntity>) : CalendarSyncResult
    data class Failure(val reason: CalendarSyncFailure) : CalendarSyncResult
}

/** Stable failure classifications without leaking source URLs or remote response bodies. */
enum class CalendarSyncFailure { INVALID_SOURCE, HTTP_ERROR, INVALID_FEED, UNAVAILABLE }

/** Internal fetch failure translated into a typed sync result at the provider boundary. */
class CalendarFetchException(val reason: CalendarSyncFailure) : Exception(reason.name)

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
     * @return Success with events (possibly empty), or failure. Cancellation always propagates.
     */
    suspend fun sync(
        provider: CalendarProviderEntity,
        from: Instant,
        to: Instant,
    ): CalendarSyncResult =
        try {
            CalendarSyncResult.Success(fetchEvents(provider, from, to))
        } catch (error: CancellationException) {
            throw error
        } catch (error: CalendarFetchException) {
            CalendarSyncResult.Failure(error.reason)
        } catch (_: Exception) {
            CalendarSyncResult.Failure(CalendarSyncFailure.UNAVAILABLE)
        }
}
