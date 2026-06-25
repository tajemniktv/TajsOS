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
 * @property id The unique identifier for this inbox entry.
 * @property rawText The unprocessed text captured by the user.
 * @property source The origin of this capture (e.g., manual, widget, external_intent).
 * @property suggestedKind An optional heuristic guess at what item kind this should become.
 * @property homeAreaId An optional area ID inferred during capture to pre-fill triage.
 * @property activeProjectId An optional project ID inferred during capture to pre-fill triage.
 * @property contextScreen An optional identifier of the UI screen where the capture occurred.
 * @property capturedAt The epoch timestamp when this entry was initially recorded.
 * @property processedAt The epoch timestamp when a user actively started triaging this entry.
 * @property dismissedAt The epoch timestamp when this entry was discarded without being structured.
 * @property triagedItemId The ID of the fully structured node created from this capture, if successful.
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
 * @property itemId The foreign key linking this facet to the shared node entity.
 * @property state The execution lifecycle state (e.g., active, done). Maps to [TaskState].
 * @property energyLevel Optional self-reported rating of energy required to complete the task.
 * @property friction Optional descriptor of the cognitive or emotional friction involved.
 * @property nextStep An optional concrete description of the immediate next physical action.
 * @property estimatedMinutes An optional estimated time duration in minutes.
 * @property completionNote An optional reflection or takeaway added when marking the task as done.
 * @property completedAt Optional epoch timestamp recording when the task transitioned to a done state.
 * @property isRecurring Indicates whether this task spawns new instances upon completion.
 * @property recurringInterval Optional schedule definition (e.g., cron or simple interval) for recurring tasks.
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
 * @property itemId The foreign key linking this facet to the shared node entity.
 * @property kind The categorical flavor of the note (e.g., general, reference). Maps to [NoteKind].
 * @property state The maturity state of the knowledge item (e.g., raw, distilled). Maps to [NoteState].
 * @property sourceTitle Optional title of an external source (e.g., book or article name) this note references.
 * @property sourceAuthor Optional author of the external source.
 * @property lastReviewedAt Optional epoch timestamp of the last time this note was surfaced for review.
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
 * @property itemId The foreign key linking this facet to the shared node entity.
 * @property state The lifecycle state of the project (e.g., active, on_hold). Maps to [ProjectState].
 * @property purpose An optional high-level statement detailing why this project matters or its desired outcome.
 * @property isFrozen Indicates if the project and its children are temporarily suspended from active UI lenses.
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
 * @property itemId The foreign key linking this facet to the shared node entity.
 * @property healthStatus The subjective status of the area's maintenance (e.g., stable, overloaded). Maps to [AreaHealthStatus].
 * @property standardOfCare An optional definition of what "good" looks like for this area.
 * @property vision An optional aspirational description of the desired long-term state for this area.
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
 * @property itemId The foreign key linking this facet to the shared node entity.
 * @property kind The category of the record (e.g., journal, health_log). Maps to [RecordKind].
 * @property intensity An optional subjective numeric score rating the intensity of the recorded event.
 * @property eventStartedAt Optional epoch timestamp of when the recorded event actually started, distinct from when it was logged.
 * @property eventEndedAt Optional epoch timestamp of when the recorded event ended.
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
 * @property itemId The foreign key linking this domain assignment to the shared node entity.
 * @property domainKey The string identifier of the domain (e.g., FINANCES, HEALTH). Maps to [com.tajemniktv.tajsos.domain.DomainKind].
 * @property isPrimary Indicates if this domain should take precedence when multiple domains apply.
 * @property assignedAt The epoch timestamp when this domain was explicitly assigned.
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
