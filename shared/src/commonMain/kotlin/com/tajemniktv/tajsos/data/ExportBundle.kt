/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.serialization.Serializable

const val EXPORT_SCHEMA_VERSION = 3

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
