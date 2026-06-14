/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

private fun currentEpochMillis(): Long =
    kotlin.time.Clock.System
        .now()
        .toEpochMilliseconds()

/**
 * Raw capture inbox entry stored before the system commits to a semantic item type.
 *
 * This is the primary "capture fast, structure later" primitive for the new LifeOS model.
 * Represents a raw capture entry inside the inbox before being structured or triaged.
 *
 * @property id The unique auto-generated database ID.
 * @property rawText The raw, unprocessed text captured by the user.
 * @property source The origin of the capture (e.g., 'manual').
 * @property suggestedKind An optional suggested node kind (e.g., task, note) for future processing.
 * @property homeAreaId An optional area ID this capture might belong to.
 * @property activeProjectId An optional active project ID this capture might belong to.
 * @property contextScreen An optional string recording what screen the user was on during capture.
 * @property capturedAt The epoch timestamp in milliseconds when this entry was captured.
 * @property processedAt An optional epoch timestamp in milliseconds when this entry was processed.
 * @property dismissedAt An optional epoch timestamp in milliseconds when this entry was dismissed.
 * @property triagedItemId The ID of the resulting node item after this capture was triaged.
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
 *
 * @property itemId The ID of the associated shared LifeOS item.
 * @property state The execution state of the task (e.g., ACTIVE, COMPLETED).
 * @property energyLevel An optional integer representing the estimated energy required.
 * @property friction An optional string identifying potential blockers or friction points.
 * @property nextStep An optional description of the immediate next action.
 * @property estimatedMinutes An optional estimated time to completion in minutes.
 * @property completionNote An optional note or reflection added when the task was completed.
 * @property completedAt An optional epoch timestamp in milliseconds when the task was completed.
 * @property isRecurring Boolean flag indicating if this task is a recurring template.
 * @property recurringInterval The recurrence rule/interval string if the task is recurring.
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
 * Note-specific semantics stored beside the shared item row.
 *
 * @property itemId The ID of the associated shared LifeOS item.
 * @property kind The specific category of the note (e.g., GENERAL).
 * @property state The lifecycle state of the note.
 * @property sourceTitle An optional string storing the title of referenced source material.
 * @property sourceAuthor An optional string storing the author of referenced source material.
 * @property lastReviewedAt An optional epoch timestamp in milliseconds when the note was last reviewed.
 */
@Entity(tableName = "note_facets")
@Serializable
data class NoteFacetEntity(
    @PrimaryKey val itemId: Long,
    val kind: String = NoteKind.GENERAL.storageKey,
    val state: String = NoteState.ACTIVE.storageKey,
    val sourceTitle: String? = null,
    val sourceAuthor: String? = null,
    val lastReviewedAt: Long? = null,
)

/**
 * Project-specific coordination state stored separately from the shared item row.
 *
 * @property itemId The ID of the associated shared LifeOS item.
 * @property state The execution state of the project (e.g., ACTIVE, COMPLETED).
 * @property purpose An optional description of the project's goal or desired outcome.
 * @property isFrozen Boolean flag indicating if the project is currently suspended or on hold.
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
 * Area-specific stewardship data kept out of the shared node table.
 *
 * @property itemId The ID of the associated shared LifeOS item.
 * @property healthStatus The current health status of the area (e.g., STABLE, ATTENTION_NEEDED).
 * @property standardOfCare An optional description defining what "good" looks like for this area.
 * @property vision An optional long-term vision or desired state for this area.
 */
@Entity(tableName = "area_facets")
@Serializable
data class AreaFacetEntity(
    @PrimaryKey val itemId: Long,
    val healthStatus: String = AreaHealthStatus.STABLE.storageKey,
    val standardOfCare: String? = null,
    val vision: String? = null,
)

/**
 * Record-specific chronological data attached to an item row.
 *
 * @property itemId The ID of the associated shared LifeOS item.
 * @property kind The specific category of the record (e.g., GENERAL, HEALTH).
 * @property occurredAt The epoch timestamp in milliseconds when the recorded event occurred.
 */
@Entity(tableName = "record_facets")
@Serializable
data class RecordFacetEntity(
    @PrimaryKey val itemId: Long,
    val kind: String = RecordKind.GENERAL.storageKey,
    val occurredAt: Long =
        currentEpochMillis(),
)

/**
 * Lens-oriented domain associations for any life object.
 *
 * Domains remain read-model classifications over shared objects rather than hard containers.
 *
 * Note: These explicit mappings are currently stored but intentionally bypassed by primary UI lenses
 * (e.g., DomainLensQueries), which prefer implicit terminology matching (tags, titles, content,
 * maintenance type, and note type) over these explicit DB associations. This provides resilience
 * against users forgetting to assign domains.
 */
@Entity(
    tableName = "item_domains",
    primaryKeys = ["itemId", "domainKey"],
    indices = [
        Index(value = ["itemId"]),
        Index(value = ["domainKey"]),
    ],
)
/**
 * Represents the cross-cutting assignment of a shared item to a first-class LifeOS domain.
 * NOTE: Current design prefers implicit categorization via `DomainLensQueries`,
 * but explicit database associations are preserved here for overrides and future expansion.
 *
 * @property itemId The ID of the associated shared LifeOS item.
 * @property domainKey The specific domain key assigned to the item.
 * @property isPrimary Boolean flag indicating if this domain is the primary domain for the item.
 * @property assignedAt The epoch timestamp in milliseconds when the assignment was made.
 */
@Serializable
data class ItemDomainEntity(
    val itemId: Long,
    val domainKey: String,
    val isPrimary: Boolean = false,
    val assignedAt: Long = currentEpochMillis(),
)

/**
 * Optional rich-content document attached to a typed life object.
 *
 * The life object remains primary; this document provides extensible long-form body support.
 */
@Entity(
    tableName = "rich_content_documents",
    indices = [Index(value = ["updatedAt"])],
)
/**
 * Stores long-form structured content (such as markdown bodies or embedded objects)
 * alongside a parent Node without bloating the primary generic node table.
 *
 * @property itemId The ID of the LifeOS item owning this document.
 * @property format The format type of the rich content (e.g., Markdown).
 * @property body The raw textual body of the document.
 * @property structuredContentJson An optional JSON string representing a structured layout.
 * @property schemaVersion The version of the content schema used.
 * @property updatedAt The epoch timestamp in milliseconds when the document was last updated.
 */
@Serializable
data class RichContentDocumentEntity(
    @PrimaryKey val itemId: Long,
    val format: String = RichContentFormat.MARKDOWN.storageKey,
    val body: String = "",
    val structuredContentJson: String? = null,
    val schemaVersion: Int = 1,
    val updatedAt: Long = currentEpochMillis(),
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
    val localDateEpochDay: Int? = null,
    val timezoneId: String? = null,
    val isAllDay: Boolean = false,
    val endAt: Long? = null,
    val recurrenceRule: String? = null,
    val note: String? = null,
    val completedAt: Long? = null,
)

/**
 * Saved projection over shared life objects.
 *
 * The view describes how to slice typed data without inventing a competing ontology.
 */
@Entity(
    tableName = "saved_views",
    indices = [Index(value = ["lens"]), Index(value = ["updatedAt"])],
)
@Serializable
data class SavedViewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val lens: String = SavedViewLens.OPERATE.storageKey,
    val layout: String = SavedViewLayout.LIST.storageKey,
    val rowDimension: String? = null,
    val columnDimension: String? = null,
    val measure: String? = null,
    val createdAt: Long = currentEpochMillis(),
    val updatedAt: Long = currentEpochMillis(),
)

/**
 * Source object kinds targeted by a saved view.
 */
@Entity(
    tableName = "saved_view_source_kinds",
    primaryKeys = ["viewId", "itemKind"],
    indices = [Index(value = ["viewId"])],
)
@Serializable
data class SavedViewSourceKindEntity(
    val viewId: Long,
    val itemKind: String,
)

/**
 * Persisted filters for a saved view.
 */
@Entity(
    tableName = "saved_view_filters",
    primaryKeys = ["viewId", "position"],
    indices = [Index(value = ["viewId"])],
)
@Serializable
data class SavedViewFilterEntity(
    val viewId: Long,
    val position: Int,
    val fieldKey: String,
    val operatorKey: String,
    val value: String? = null,
    val valueType: String = SavedViewValueType.STRING.storageKey,
)

/**
 * Persisted sort instructions for a saved view.
 */
@Entity(
    tableName = "saved_view_sorts",
    primaryKeys = ["viewId", "position"],
    indices = [Index(value = ["viewId"])],
)
@Serializable
data class SavedViewSortEntity(
    val viewId: Long,
    val position: Int,
    val fieldKey: String,
    val direction: String = SavedViewSortDirection.ASCENDING.storageKey,
)

/**
 * Persisted visible-column configuration for a saved view.
 */
@Entity(
    tableName = "saved_view_visible_fields",
    primaryKeys = ["viewId", "position"],
    indices = [Index(value = ["viewId"])],
)
@Serializable
data class SavedViewVisibleFieldEntity(
    val viewId: Long,
    val position: Int,
    val fieldKey: String,
)
