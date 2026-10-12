/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/** Retains the existing fixed-duration recurrence policy shared by lifecycle commands and UI helpers. */
internal fun nextRecurringDueAt(currentDue: Long, interval: String): Long {
    val offset = when (interval.uppercase()) {
        "DAILY" -> 1.days
        "WEEKLY" -> 7.days
        "MONTHLY" -> 30.days
        else -> 1.days
    }
    return (Instant.fromEpochMilliseconds(currentDue) + offset).toEpochMilliseconds()
}
