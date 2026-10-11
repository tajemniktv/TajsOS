/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import kotlinx.serialization.Serializable

const val EXPORT_SCHEMA_VERSION = 4

/**
 * Represents a snapshot of the user's core data for backup and migration.
 *
 * Current exports contain every Room table, including owned associations and typed state.
 * Attachment references are included, not external file bytes or platform access permissions.
 * Device-local DataStore settings are excluded; enabled pack keys are carried as portable metadata.
 * When the backup contract changes, [EXPORT_SCHEMA_VERSION] must be incremented.
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
 * @property recentEvents All persisted event logs; the name is retained for older JSON bundles.
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
    val nodeTags: List<NodeTagEntity> = emptyList(),
    val pins: List<TodayPinEntity> = emptyList(),
    val attachments: List<AttachmentEntity> = emptyList(),
    val snapshots: List<NodeSnapshotEntity> = emptyList(),
    val modes: List<ModeEntity> = emptyList(),
    val modePreferences: List<ModePreferenceEntity> = emptyList(),
    val modeAreaFilters: List<ModeAreaFilterEntity> = emptyList(),
    val modeTypeFilters: List<ModeTypeFilterEntity> = emptyList(),
    val modeUsageLogs: List<ModeUsageLogEntity> = emptyList(),
    val protocolHistory: List<ProtocolHistoryEntity> = emptyList(),
    val decisionOptions: List<DecisionOptionEntity> = emptyList(),
    val users: List<UserEntity> = emptyList(),
    val medications: List<MedicationEntity> = emptyList(),
    val trackMedications: List<TrackMedicationJoinEntity> = emptyList(),
    val inboxEntries: List<InboxEntryEntity> = emptyList(),
    val taskFacets: List<TaskFacetEntity> = emptyList(),
    val noteFacets: List<NoteFacetEntity> = emptyList(),
    val recordFacets: List<RecordFacetEntity> = emptyList(),
    val projectFacets: List<ProjectFacetEntity> = emptyList(),
    val areaFacets: List<AreaFacetEntity> = emptyList(),
    val itemDomains: List<ItemDomainEntity> = emptyList(),
    val documents: List<RichContentDocumentEntity> = emptyList(),
    val scheduleEntries: List<ScheduleEntryEntity> = emptyList(),
    val savedViews: List<SavedViewEntity> = emptyList(),
    val savedViewSourceKinds: List<SavedViewSourceKindEntity> = emptyList(),
    val savedViewFilters: List<SavedViewFilterEntity> = emptyList(),
    val savedViewSorts: List<SavedViewSortEntity> = emptyList(),
    val savedViewVisibleFields: List<SavedViewVisibleFieldEntity> = emptyList(),
)
