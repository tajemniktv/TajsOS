/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Raw capture inbox entry stored before the system commits to a semantic item type.
 *
 * This is the primary "capture fast, structure later" primitive for the new LifeOS model.
 */
@Entity(
    tableName = "inbox_entries",
    indices = [
        Index(value = ["triagedItemId"]),
        Index(value = ["homeAreaId"]),
        Index(value = ["activeProjectId"]),
    ],
)
@Serializable
data class InboxEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rawText: String,
    val source: String = "manual",
    val suggestedKind: String? = null,
    val homeAreaId: Long? = null,
    val activeProjectId: Long? = null,
    val contextScreen: String? = null,
    val capturedAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
    val processedAt: Long? = null,
    val dismissedAt: Long? = null,
    val triagedItemId: Long? = null,
)

/**
 * Task-specific execution and recurrence data associated with an item row.
 */
@Entity(tableName = "task_facets")
@Serializable
data class TaskFacetEntity(
    @PrimaryKey val itemId: Long,
    val state: String = TaskState.ACTIVE.storageKey,
    val energyLevel: Int? = null,
    val friction: String? = null,
    val nextStep: String? = null,
    val estimatedMinutes: Int? = null,
    val completionNote: String? = null,
    val completedAt: Long? = null,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null,
)

/**
 * Project-specific coordination state stored separately from the shared item row.
 */
@Entity(tableName = "project_facets")
@Serializable
data class ProjectFacetEntity(
    @PrimaryKey val itemId: Long,
    val state: String = ProjectState.ACTIVE.storageKey,
    val purpose: String? = null,
    val isFrozen: Boolean = false,
)

/**
 * Record-specific chronological data attached to an item row.
 */
@Entity(tableName = "record_facets")
@Serializable
data class RecordFacetEntity(
    @PrimaryKey val itemId: Long,
    val kind: String = RecordKind.GENERAL.storageKey,
    val occurredAt: Long =
        kotlin.time.Clock.System
            .now()
            .toEpochMilliseconds(),
)

/**
 * Attach-able schedule layer for any item.
 *
 * Existing node time fields remain mirrored for legacy UI compatibility, but new code should
 * prefer this explicit table.
 */
@Entity(
    tableName = "schedule_entries",
    indices = [Index(value = ["itemId"]), Index(value = ["scheduledAt"])],
)
@Serializable
data class ScheduleEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val kind: String,
    val scheduledAt: Long,
    val endAt: Long? = null,
    val recurrenceRule: String? = null,
    val note: String? = null,
    val completedAt: Long? = null,
)
