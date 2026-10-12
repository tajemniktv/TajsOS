/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi

const val EXPORT_SCHEMA_VERSION = 4

/**
 * Represents a snapshot of the user's core data for backup and migration.
 *
 * Current exports contain every Room table, including owned associations and typed state.
 * Calendar authorization tokens are excluded; providers with credentials are exported disabled.
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
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ExportBundle(
    @EncodeDefault
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

/**
 * Keeps calendar configuration while excluding the access/refresh token columns.
 * Private feed URLs may themselves grant access and remain part of this private recovery file.
 * Token-bearing portable copies are disabled; imports may retain matching local authorization.
 * This returns a copy; creating a backup never changes the live provider or its cached events.
 */
internal fun CalendarProviderEntity.withoutBackupTokens(): CalendarProviderEntity =
    copy(
        accessToken = null,
        refreshToken = null,
        isEnabled = isEnabled && !hasAuthorizationTokens,
    )

/**
 * Imports configuration without trusting payload credentials or erasing matching local authorization.
 * New or changed source identities are disabled until locally enabled, including token-free feeds.
 * Local tokens and enablement belong to this device. A credential-bearing ID collision with a
 * different account/type/URL rejects restore rather than attaching secrets to another identity.
 */
internal fun CalendarProviderEntity.forBackupImport(local: CalendarProviderEntity?): CalendarProviderEntity {
    val sanitized = withoutBackupTokens().copy(isEnabled = false)
    if (local == null) return sanitized
    if (backupIdentity() != local.backupIdentity()) {
        require(!local.hasAuthorizationTokens) { "Backup calendar provider identity conflicts with local authorization" }
        return sanitized
    }
    return sanitized.copy(
        accessToken = local.accessToken,
        refreshToken = local.refreshToken,
        isEnabled = local.isEnabled,
    )
}

/** Token presence controls credential-bearing identity protection, not whether a feed URL is public. */
private val CalendarProviderEntity.hasAuthorizationTokens: Boolean
    get() = accessToken != null || refreshToken != null

/** Stable source identity excludes display metadata and device-local authorization. */
private data class CalendarBackupIdentity(val id: Long, val type: String, val account: String?, val url: String?)

/** Compares identities without relying on a compound nullable-field condition. */
private fun CalendarProviderEntity.backupIdentity() = CalendarBackupIdentity(id, type, accountEmail, url)
