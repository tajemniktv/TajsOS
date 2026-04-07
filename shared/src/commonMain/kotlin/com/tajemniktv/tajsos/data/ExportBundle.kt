/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.serialization.Serializable

const val EXPORT_SCHEMA_VERSION = 3

/**
 * Represents a snapshot of the user's core data for backup and migration.
 *
 * This bundle is serialized to JSON and acts as the official format for full repository exports
 * and imports. When schemas change, [EXPORT_SCHEMA_VERSION] must be incremented.
 *
 * @property schemaVersion The structure version of the payload (defaults to [EXPORT_SCHEMA_VERSION]).
 * @property exportedAt The timestamp (in epoch milliseconds) of when the backup was created.
 * @property enabledPacks Set of feature packs that were active during export.
 * @property nodes The core life items (tasks, notes, records, projects, etc.).
 * @property relations The connections and links between nodes.
 * @property tags The tags used for categorization.
 * @property templates The user-defined templates for creating new items.
 * @property reviews The periodic reflections and life reviews.
 * @property tracks The daily tracking entries for mood, energy, and sleep.
 * @property sessions The Pomodoro/focus session logs.
 * @property calendars The synchronized calendar events.
 * @property providers The calendar sources configured by the user.
 * @property recentEvents A limited set of recent system event logs.
 */
@Serializable
data class ExportBundle(
    val schemaVersion: Int = EXPORT_SCHEMA_VERSION,
    val exportedAt: Long,
    val enabledPacks: Set<String> = emptySet(),
    val nodes: List<NodeEntity>,
    val relations: List<RelationEntity>,
    val tags: List<TagEntity>,
    val templates: List<TemplateEntity>,
    val reviews: List<ReviewEntity>,
    val tracks: List<TrackEntryEntity>,
    val sessions: List<FocusSessionEntity>,
    val calendars: List<CalendarEventEntity>,
    val providers: List<CalendarProviderEntity>,
    val recentEvents: List<EventLogEntity>,
)
