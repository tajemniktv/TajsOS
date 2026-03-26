/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import kotlinx.serialization.Serializable

data class NodeCategorization(
    val inbox: List<NodeWithPin> = emptyList(),
    val archived: List<NodeWithPin> = emptyList(),
    val reminders: List<NodeEntity> = emptyList(),
)

@Serializable
data class ExportData(
    val version: Int,
    val nodes: List<NodeEntity>,
)

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

enum class EntryType {
    INTERNAL,
    EXTERNAL,
}
