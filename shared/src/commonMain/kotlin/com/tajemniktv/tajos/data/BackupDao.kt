/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

/**
 * Owns database backup snapshots independently of live UI queries.
 *
 * Suspend queries run on the transaction's connection, so a snapshot cannot mix revisions.
 * Attachment rows preserve references; external file bytes and platform permissions are not
 * owned by Room and are deliberately not embedded in this database backup.
 */
@Dao
abstract class BackupDao {
    /** Captures all persisted tables in one consistent read transaction, including history. */
    @Transaction
    open suspend fun snapshot(enabledPacks: Set<String>): ExportBundle =
        ExportBundle(
            exportedAt = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            enabledPacks = enabledPacks,
            nodes = readNodes(),
            relations = readRelations(),
            tags = readTags(),
            templates = readTemplates(),
            reviews = readReviews(),
            tracks = readTracks(),
            sessions = readSessions(),
            calendars = readCalendars(),
            providers = readProviders(),
            recentEvents = readRecentEvents(),
            nodeTags = readNodeTags(),
            pins = readPins(),
            attachments = readAttachments(),
            snapshots = readSnapshots(),
            modes = readModes(),
            modePreferences = readModePreferences(),
            modeAreaFilters = readModeAreaFilters(),
            modeTypeFilters = readModeTypeFilters(),
            modeUsageLogs = readModeUsageLogs(),
            protocolHistory = readProtocolHistory(),
            decisionOptions = readDecisionOptions(),
            users = readUsers(),
            medications = readMedications(),
            trackMedications = readTrackMedications(),
            inboxEntries = readInboxEntries(),
            taskFacets = readTaskFacets(),
            noteFacets = readNoteFacets(),
            recordFacets = readRecordFacets(),
            projectFacets = readProjectFacets(),
            areaFacets = readAreaFacets(),
            itemDomains = readItemDomains(),
            documents = readDocuments(),
            scheduleEntries = readScheduleEntries(),
            savedViews = readSavedViews(),
            savedViewSourceKinds = readSavedViewSourceKinds(),
            savedViewFilters = readSavedViewFilters(),
            savedViewSorts = readSavedViewSorts(),
            savedViewVisibleFields = readSavedViewVisibleFields(),
        )

    /**
     * Merges a current-schema backup by persisted identity, replacing owned companion state.
     * Unrelated roots remain untouched. Any failed write rolls back the entire restore.
     */
    @Transaction
    open suspend fun restore(bundle: ExportBundle) {
        require(bundle.schemaVersion == EXPORT_SCHEMA_VERSION) { "Expected current backup schema" }
        bundle.nodes.map { it.id }.chunked(900).forEach { ids ->
            clearRelations(ids)
            clearNodeTags(ids)
            clearTodayPins(ids)
            clearAttachments(ids)
            clearNodeSnapshots(ids)
            clearDecisionOptions(ids)
            clearTaskFacets(ids)
            clearNoteFacets(ids)
            clearRecordFacets(ids)
            clearProjectFacets(ids)
            clearAreaFacets(ids)
            clearItemDomains(ids)
            clearRichContentDocuments(ids)
            clearScheduleEntries(ids)
        }
        bundle.savedViews.map { it.id }.chunked(900).forEach { ids ->
            clearSavedViewSourceKinds(ids)
            clearSavedViewFilters(ids)
            clearSavedViewSorts(ids)
            clearSavedViewVisibleFields(ids)
        }
        bundle.modes.map { it.id }.chunked(900).forEach { ids ->
            clearModePreferences(ids)
            clearModeAreaFilters(ids)
            clearModeTypeFilters(ids)
        }
        bundle.tracks.map { it.id }.chunked(900).forEach { clearTrackMedications(it) }
        writeNodes(bundle.nodes)
        writeRelations(bundle.relations)
        writeTags(bundle.tags)
        writeTemplates(bundle.templates)
        writeReviews(bundle.reviews)
        writeTracks(bundle.tracks)
        writeSessions(bundle.sessions)
        writeCalendars(bundle.calendars)
        writeProviders(bundle.providers)
        writeRecentEvents(bundle.recentEvents)
        writeNodeTags(bundle.nodeTags)
        writePins(bundle.pins)
        writeAttachments(bundle.attachments)
        writeSnapshots(bundle.snapshots)
        writeModes(bundle.modes)
        writeModePreferences(bundle.modePreferences)
        writeModeAreaFilters(bundle.modeAreaFilters)
        writeModeTypeFilters(bundle.modeTypeFilters)
        writeModeUsageLogs(bundle.modeUsageLogs)
        writeProtocolHistory(bundle.protocolHistory)
        writeDecisionOptions(bundle.decisionOptions)
        writeUsers(bundle.users)
        writeMedications(bundle.medications)
        writeTrackMedications(bundle.trackMedications)
        writeInboxEntries(bundle.inboxEntries)
        writeTaskFacets(bundle.taskFacets)
        writeNoteFacets(bundle.noteFacets)
        writeRecordFacets(bundle.recordFacets)
        writeProjectFacets(bundle.projectFacets)
        writeAreaFacets(bundle.areaFacets)
        writeItemDomains(bundle.itemDomains)
        writeDocuments(bundle.documents)
        writeScheduleEntries(bundle.scheduleEntries)
        writeSavedViews(bundle.savedViews)
        writeSavedViewSourceKinds(bundle.savedViewSourceKinds)
        writeSavedViewFilters(bundle.savedViewFilters)
        writeSavedViewSorts(bundle.savedViewSorts)
        writeSavedViewVisibleFields(bundle.savedViewVisibleFields)
    }

    @Query("SELECT * FROM nodes")
    abstract suspend fun readNodes(): List<NodeEntity>

    @Upsert
    abstract suspend fun writeNodes(rows: List<NodeEntity>)

    @Query("SELECT * FROM relations")
    abstract suspend fun readRelations(): List<RelationEntity>

    @Upsert
    abstract suspend fun writeRelations(rows: List<RelationEntity>)

    @Query("SELECT * FROM tags")
    abstract suspend fun readTags(): List<TagEntity>

    @Upsert
    abstract suspend fun writeTags(rows: List<TagEntity>)

    @Query("SELECT * FROM templates")
    abstract suspend fun readTemplates(): List<TemplateEntity>

    @Upsert
    abstract suspend fun writeTemplates(rows: List<TemplateEntity>)

    @Query("SELECT * FROM reviews")
    abstract suspend fun readReviews(): List<ReviewEntity>

    @Upsert
    abstract suspend fun writeReviews(rows: List<ReviewEntity>)

    @Query("SELECT * FROM track_entries")
    abstract suspend fun readTracks(): List<TrackEntryEntity>

    @Upsert
    abstract suspend fun writeTracks(rows: List<TrackEntryEntity>)

    @Query("SELECT * FROM focus_sessions")
    abstract suspend fun readSessions(): List<FocusSessionEntity>

    @Upsert
    abstract suspend fun writeSessions(rows: List<FocusSessionEntity>)

    @Query("SELECT * FROM calendar_events")
    abstract suspend fun readCalendars(): List<CalendarEventEntity>

    @Upsert
    abstract suspend fun writeCalendars(rows: List<CalendarEventEntity>)

    @Query("SELECT * FROM calendar_providers")
    abstract suspend fun readProviders(): List<CalendarProviderEntity>

    @Upsert
    abstract suspend fun writeProviders(rows: List<CalendarProviderEntity>)

    @Query("SELECT * FROM event_log")
    abstract suspend fun readRecentEvents(): List<EventLogEntity>

    @Upsert
    abstract suspend fun writeRecentEvents(rows: List<EventLogEntity>)

    @Query("SELECT * FROM node_tags")
    abstract suspend fun readNodeTags(): List<NodeTagEntity>

    @Upsert
    abstract suspend fun writeNodeTags(rows: List<NodeTagEntity>)

    @Query("SELECT * FROM today_pins")
    abstract suspend fun readPins(): List<TodayPinEntity>

    @Upsert
    abstract suspend fun writePins(rows: List<TodayPinEntity>)

    @Query("SELECT * FROM attachments")
    abstract suspend fun readAttachments(): List<AttachmentEntity>

    @Upsert
    abstract suspend fun writeAttachments(rows: List<AttachmentEntity>)

    @Query("SELECT * FROM node_snapshots")
    abstract suspend fun readSnapshots(): List<NodeSnapshotEntity>

    @Upsert
    abstract suspend fun writeSnapshots(rows: List<NodeSnapshotEntity>)

    @Query("SELECT * FROM modes")
    abstract suspend fun readModes(): List<ModeEntity>

    @Upsert
    abstract suspend fun writeModes(rows: List<ModeEntity>)

    @Query("SELECT * FROM mode_preferences")
    abstract suspend fun readModePreferences(): List<ModePreferenceEntity>

    @Upsert
    abstract suspend fun writeModePreferences(rows: List<ModePreferenceEntity>)

    @Query("SELECT * FROM mode_area_filters")
    abstract suspend fun readModeAreaFilters(): List<ModeAreaFilterEntity>

    @Upsert
    abstract suspend fun writeModeAreaFilters(rows: List<ModeAreaFilterEntity>)

    @Query("SELECT * FROM mode_type_filters")
    abstract suspend fun readModeTypeFilters(): List<ModeTypeFilterEntity>

    @Upsert
    abstract suspend fun writeModeTypeFilters(rows: List<ModeTypeFilterEntity>)

    @Query("SELECT * FROM mode_usage_logs")
    abstract suspend fun readModeUsageLogs(): List<ModeUsageLogEntity>

    @Upsert
    abstract suspend fun writeModeUsageLogs(rows: List<ModeUsageLogEntity>)

    @Query("SELECT * FROM protocol_history")
    abstract suspend fun readProtocolHistory(): List<ProtocolHistoryEntity>

    @Upsert
    abstract suspend fun writeProtocolHistory(rows: List<ProtocolHistoryEntity>)

    @Query("SELECT * FROM decision_options")
    abstract suspend fun readDecisionOptions(): List<DecisionOptionEntity>

    @Upsert
    abstract suspend fun writeDecisionOptions(rows: List<DecisionOptionEntity>)

    @Query("SELECT * FROM users")
    abstract suspend fun readUsers(): List<UserEntity>

    @Upsert
    abstract suspend fun writeUsers(rows: List<UserEntity>)

    @Query("SELECT * FROM medications")
    abstract suspend fun readMedications(): List<MedicationEntity>

    @Upsert
    abstract suspend fun writeMedications(rows: List<MedicationEntity>)

    @Query("SELECT * FROM track_medications")
    abstract suspend fun readTrackMedications(): List<TrackMedicationJoinEntity>

    @Upsert
    abstract suspend fun writeTrackMedications(rows: List<TrackMedicationJoinEntity>)

    @Query("SELECT * FROM inbox_entries")
    abstract suspend fun readInboxEntries(): List<InboxEntryEntity>

    @Upsert
    abstract suspend fun writeInboxEntries(rows: List<InboxEntryEntity>)

    @Query("SELECT * FROM task_facets")
    abstract suspend fun readTaskFacets(): List<TaskFacetEntity>

    @Upsert
    abstract suspend fun writeTaskFacets(rows: List<TaskFacetEntity>)

    @Query("SELECT * FROM note_facets")
    abstract suspend fun readNoteFacets(): List<NoteFacetEntity>

    @Upsert
    abstract suspend fun writeNoteFacets(rows: List<NoteFacetEntity>)

    @Query("SELECT * FROM record_facets")
    abstract suspend fun readRecordFacets(): List<RecordFacetEntity>

    @Upsert
    abstract suspend fun writeRecordFacets(rows: List<RecordFacetEntity>)

    @Query("SELECT * FROM project_facets")
    abstract suspend fun readProjectFacets(): List<ProjectFacetEntity>

    @Upsert
    abstract suspend fun writeProjectFacets(rows: List<ProjectFacetEntity>)

    @Query("SELECT * FROM area_facets")
    abstract suspend fun readAreaFacets(): List<AreaFacetEntity>

    @Upsert
    abstract suspend fun writeAreaFacets(rows: List<AreaFacetEntity>)

    @Query("SELECT * FROM item_domains")
    abstract suspend fun readItemDomains(): List<ItemDomainEntity>

    @Upsert
    abstract suspend fun writeItemDomains(rows: List<ItemDomainEntity>)

    @Query("SELECT * FROM rich_content_documents")
    abstract suspend fun readDocuments(): List<RichContentDocumentEntity>

    @Upsert
    abstract suspend fun writeDocuments(rows: List<RichContentDocumentEntity>)

    @Query("SELECT * FROM schedule_entries")
    abstract suspend fun readScheduleEntries(): List<ScheduleEntryEntity>

    @Upsert
    abstract suspend fun writeScheduleEntries(rows: List<ScheduleEntryEntity>)

    @Query("SELECT * FROM saved_views")
    abstract suspend fun readSavedViews(): List<SavedViewEntity>

    @Upsert
    abstract suspend fun writeSavedViews(rows: List<SavedViewEntity>)

    @Query("SELECT * FROM saved_view_source_kinds")
    abstract suspend fun readSavedViewSourceKinds(): List<SavedViewSourceKindEntity>

    @Upsert
    abstract suspend fun writeSavedViewSourceKinds(rows: List<SavedViewSourceKindEntity>)

    @Query("SELECT * FROM saved_view_filters")
    abstract suspend fun readSavedViewFilters(): List<SavedViewFilterEntity>

    @Upsert
    abstract suspend fun writeSavedViewFilters(rows: List<SavedViewFilterEntity>)

    @Query("SELECT * FROM saved_view_sorts")
    abstract suspend fun readSavedViewSorts(): List<SavedViewSortEntity>

    @Upsert
    abstract suspend fun writeSavedViewSorts(rows: List<SavedViewSortEntity>)

    @Query("SELECT * FROM saved_view_visible_fields")
    abstract suspend fun readSavedViewVisibleFields(): List<SavedViewVisibleFieldEntity>

    @Upsert
    abstract suspend fun writeSavedViewVisibleFields(rows: List<SavedViewVisibleFieldEntity>)

    @Query("DELETE FROM node_tags WHERE nodeId IN (:ids)")
    abstract suspend fun clearNodeTags(ids: List<Long>)

    @Query("DELETE FROM relations WHERE fromNodeId IN (:ids) OR toNodeId IN (:ids)")
    abstract suspend fun clearRelations(ids: List<Long>)

    @Query("DELETE FROM today_pins WHERE nodeId IN (:ids)")
    abstract suspend fun clearTodayPins(ids: List<Long>)

    @Query("DELETE FROM attachments WHERE nodeId IN (:ids)")
    abstract suspend fun clearAttachments(ids: List<Long>)

    @Query("DELETE FROM node_snapshots WHERE nodeId IN (:ids)")
    abstract suspend fun clearNodeSnapshots(ids: List<Long>)

    @Query("DELETE FROM decision_options WHERE decisionNodeId IN (:ids)")
    abstract suspend fun clearDecisionOptions(ids: List<Long>)

    @Query("DELETE FROM task_facets WHERE itemId IN (:ids)")
    abstract suspend fun clearTaskFacets(ids: List<Long>)

    @Query("DELETE FROM note_facets WHERE itemId IN (:ids)")
    abstract suspend fun clearNoteFacets(ids: List<Long>)

    @Query("DELETE FROM record_facets WHERE itemId IN (:ids)")
    abstract suspend fun clearRecordFacets(ids: List<Long>)

    @Query("DELETE FROM project_facets WHERE itemId IN (:ids)")
    abstract suspend fun clearProjectFacets(ids: List<Long>)

    @Query("DELETE FROM area_facets WHERE itemId IN (:ids)")
    abstract suspend fun clearAreaFacets(ids: List<Long>)

    @Query("DELETE FROM item_domains WHERE itemId IN (:ids)")
    abstract suspend fun clearItemDomains(ids: List<Long>)

    @Query("DELETE FROM rich_content_documents WHERE itemId IN (:ids)")
    abstract suspend fun clearRichContentDocuments(ids: List<Long>)

    @Query("DELETE FROM schedule_entries WHERE itemId IN (:ids)")
    abstract suspend fun clearScheduleEntries(ids: List<Long>)

    @Query("DELETE FROM saved_view_source_kinds WHERE viewId IN (:ids)")
    abstract suspend fun clearSavedViewSourceKinds(ids: List<Long>)

    @Query("DELETE FROM saved_view_filters WHERE viewId IN (:ids)")
    abstract suspend fun clearSavedViewFilters(ids: List<Long>)

    @Query("DELETE FROM saved_view_sorts WHERE viewId IN (:ids)")
    abstract suspend fun clearSavedViewSorts(ids: List<Long>)

    @Query("DELETE FROM saved_view_visible_fields WHERE viewId IN (:ids)")
    abstract suspend fun clearSavedViewVisibleFields(ids: List<Long>)

    @Query("DELETE FROM mode_preferences WHERE modeId IN (:ids)")
    abstract suspend fun clearModePreferences(ids: List<Long>)

    @Query("DELETE FROM mode_area_filters WHERE modeId IN (:ids)")
    abstract suspend fun clearModeAreaFilters(ids: List<Long>)

    @Query("DELETE FROM mode_type_filters WHERE modeId IN (:ids)")
    abstract suspend fun clearModeTypeFilters(ids: List<Long>)

    @Query("DELETE FROM track_medications WHERE trackEntryId IN (:ids)")
    abstract suspend fun clearTrackMedications(ids: List<Long>)
}

