/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** A changed scalar schedule field, distinct from independent schedule-only rows. */
internal data class LegacyScheduleChange(
    val kind: ScheduleEntryKind,
    val previous: Long?,
    val next: Long?,
) {
    /** Whether the scalar instant changed, rather than only its recurrence metadata. */
    val timeChanged: Boolean get() = previous != next

    /** Selects one stable scalar-backed row; other entries of this kind remain independent. */
    fun mirroredEntry(entries: List<ScheduleEntryEntity>): ScheduleEntryEntity? {
        val previousTime = previous ?: return null
        return entries.filter { it.scheduledAt == previousTime }.minByOrNull { it.id }
    }

    /** Preserves rich metadata and unresolved timezones when the instant itself did not change. */
    fun replacement(
        itemId: Long,
        mirrored: ScheduleEntryEntity?,
        recurrenceRule: String?,
        recurrenceChanged: Boolean,
    ): ScheduleEntryEntity? {
        val timestamp = next ?: return null
        val original = mirrored ?: ScheduleEntryEntity(itemId = itemId, kind = kind.storageKey, scheduledAt = timestamp)
        val rule = if (recurrenceChanged || mirrored == null) recurrenceRule else mirrored.recurrenceRule
        val replacement = original.copy(recurrenceRule = rule)
        return if (timeChanged) replacement.withChangedTime(timestamp, mirrored == null) else replacement
    }
}

/** Projects existing node fields into typed reconciliation commands without changing persistence. */
internal fun NodeEntity.legacyScheduleChanges(previous: NodeEntity): List<LegacyScheduleChange> = listOf(
    LegacyScheduleChange(ScheduleEntryKind.START, previous.startAt, startAt),
    LegacyScheduleChange(ScheduleEntryKind.DUE, previous.dueAt, dueAt),
    LegacyScheduleChange(ScheduleEntryKind.REMINDER, previous.reminderAt, reminderAt),
)

/** Timestamp edits validate an explicit timezone, retain unspecified metadata, and shift end duration. */
private fun ScheduleEntryEntity.withChangedTime(timestamp: Long, newEntry: Boolean): ScheduleEntryEntity {
    val timezone = timezoneId?.let(TimeZone::of) ?: TimeZone.currentSystemDefault()
    return copy(
        scheduledAt = timestamp,
        localDateEpochDay = epochDayFromInstant(timestamp, timezone),
        timezoneId = if (newEntry) timezone.id else timezoneId,
        endAt = endAt?.let { it + (timestamp - scheduledAt) },
    )
}

/** Converts a schedule instant into its local calendar day using the explicitly resolved timezone. */
internal fun epochDayFromInstant(timestamp: Long, timezone: TimeZone): Int =
    LocalDate(1970, 1, 1).daysUntil(Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(timezone).date)
