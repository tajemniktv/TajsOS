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
 * Note-specific semantics stored beside the shared item row.
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
 *
 * NOTE: The system employs 'zero-configuration implicit categorization' through heuristic
 * matching (using tags, keywords, and maintenance types via `DomainLensQueries`) rather
 * than strict, explicit database associations like `ItemDomainEntity`. Explicitly assigning
 * a domain via this entity is essentially a legacy/override fallback that is largely bypassed
 * by the primary UI lenses to lower the friction of capturing new items.
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
