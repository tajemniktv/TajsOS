/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.main.state

import com.tajemniktv.tajos.data.NodeEntity
import com.tajemniktv.tajos.data.NodeWithPin

/**
 * A data class representing categorized lists of nodes based on their current status and reminders.
 *
 * @param inbox A list of active nodes currently residing in the inbox (unprocessed or unfiled items).
 * @param archived A list of nodes that have been completed, dropped, or otherwise archived.
 * @param reminders A list of nodes that have active reminders associated with them.
 */
data class NodeCategorization(
    val inbox: List<NodeWithPin> = emptyList(),
    val archived: List<NodeWithPin> = emptyList(),
    val reminders: List<NodeEntity> = emptyList(),
)

/**
 * Source-compatible alias for the unchanged legacy JSON contract now owned by the data layer.
 */
typealias ExportData = com.tajemniktv.tajos.data.ExportData

/**
 * Represents an entry within a calendar, encompassing scheduled events or time blocks.
 *
 * @param id A unique identifier for the calendar entry.
 * @param title The primary title or name of the event.
 * @param description Optional details, notes, or descriptions about the event.
 * @param startAt The starting epoch timestamp of the event in milliseconds.
 * @param endAt The ending epoch timestamp of the event in milliseconds.
 * @param isAllDay Indicates whether the entry spans the entire day.
 * @param type Indicates the source or classification of the entry (e.g., INTERNAL or EXTERNAL).
 * @param color An optional integer color code associated with the event for UI rendering.
 * @param originalId The original entity ID if this entry was derived from a local node or external entity.
 */
data class CalendarEntry(
    val id: String,
    val title: String,
    val description: String?,
    val startAt: Long,
    val endAt: Long,
    val isAllDay: Boolean,
    val type: EntryType,
    val color: Int? = null,
    val originalId: Long? = null,
)

/**
 * Defines the source classification of a [CalendarEntry].
 *
 * - [INTERNAL]: The event originates from within the local application.
 * - [EXTERNAL]: The event was imported or synced from an external calendar provider.
 */
enum class EntryType {
    INTERNAL,
    EXTERNAL,
}
