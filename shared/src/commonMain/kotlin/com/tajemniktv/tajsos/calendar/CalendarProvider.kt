/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.calendar

import com.tajemniktv.tajsos.data.CalendarEventEntity
import com.tajemniktv.tajsos.data.CalendarProviderEntity
import kotlin.time.Instant

interface CalendarProvider {
    val type: String

    suspend fun fetchEvents(
        provider: CalendarProviderEntity,
        from: Instant,
        to: Instant
    ): List<CalendarEventEntity>

    suspend fun sync(
        provider: CalendarProviderEntity,
        from: Instant,
        to: Instant
    ): List<CalendarEventEntity> {
        return fetchEvents(provider, from, to)
    }
}
