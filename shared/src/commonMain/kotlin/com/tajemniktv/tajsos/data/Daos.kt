/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * NodeDao provides methods for accessing the core "Node" entities.
 */
@Dao
interface NodeDao {
    @Transaction
    @Query("SELECT * FROM nodes ORDER BY createdAt DESC")
    fun getAllNodesWithPins(): Flow<List<NodeWithPin>>

    @Query(
        """
        SELECT nodes.* FROM nodes 
        INNER JOIN today_pins ON nodes.id = today_pins.nodeId 
        WHERE nodes.status = 'active' AND today_pins.date = :date
        ORDER BY today_pins.position ASC
    """,
    )
    fun getTodayNodes(date: String): Flow<List<NodeEntity>>

    /**
     * Query nodes by type while excluding archived entries.
     *
     * @param type The node `type` to filter by (for example `"project"` or `"area"`).
     * @return A list of nodes matching `type` whose `status` is not `'archived'`.
     */
    @Query("SELECT * FROM nodes WHERE type = :type AND status != 'archived'")
    fun getNodesByType(type: String): Flow<List<NodeEntity>>

    /**
     * Observes non-archived nodes for the specified project, ordered by creation time descending.
     *
     * @param projectId ID of the project whose nodes should be observed.
     * @return Lists of matching NodeEntity objects ordered by `createdAt` descending.
     */
    @Query("SELECT * FROM nodes WHERE projectId = :projectId AND status != 'archived' ORDER BY createdAt DESC")
    fun getNodesByProject(projectId: Long): Flow<List<NodeEntity>>

    /**
     * Retrieves nodes in a project together with their associated pin information, excluding archived nodes and ordered by creation time descending.
     *
     * @param projectId The id of the project whose nodes to retrieve.
     * @return Lists of NodeWithPin for nodes that belong to the specified project, exclude nodes with `status = 'archived'`, ordered by `createdAt` descending.
     */
    @Transaction
    @Query("SELECT * FROM nodes WHERE projectId = :projectId AND status != 'archived' ORDER BY createdAt DESC")
    fun getNodesByProjectWithPins(projectId: Long): Flow<List<NodeWithPin>>

    /**
     * Retrieve nodes in the given area that are not archived, ordered by creation time descending.
     *
     * @param areaId The id of the area to filter nodes by.
     * @return Lists of nodes in the specified area excluding nodes with status `'archived'`, ordered by `createdAt` descending.
     */
    @Query("SELECT * FROM nodes WHERE areaId = :areaId AND status != 'archived' ORDER BY createdAt DESC")
    fun getNodesByArea(areaId: Long): Flow<List<NodeEntity>>

    /**
     * Retrieve nodes with their pin state for a specific area.
     *
     * @param areaId The id of the area whose nodes should be returned.
     * @return Lists of `NodeWithPin` for nodes in the specified area whose `status` is not `'archived'`, ordered by `createdAt` descending.
     */
    @Transaction
    @Query("SELECT * FROM nodes WHERE areaId = :areaId AND status != 'archived' ORDER BY createdAt DESC")
    fun getNodesByAreaWithPins(areaId: Long): Flow<List<NodeWithPin>>

    /**
     * Retrieve project nodes belonging to the specified area, ordered by creation time descending.
     *
     * @param areaId ID of the area whose project nodes should be returned.
     * @return A list of project `NodeEntity` objects in the given area ordered by `createdAt` descending.
     */
    @Query("SELECT * FROM nodes WHERE areaId = :areaId AND type = 'project' AND status != 'archived' ORDER BY createdAt DESC")
    fun getProjectsByArea(areaId: Long): Flow<List<NodeEntity>>

    /**
     * Fetches the node with the given primary key.
     *
     * @param id The node's primary key.
     * @return The matching NodeEntity, or `null` if no node with the given id exists.
     */
    @Query("SELECT * FROM nodes WHERE id = :id")
    suspend fun getNodeById(id: Long): NodeEntity?

    /**
     * Inserts a new node into the database or replaces an existing one if a conflict occurs.
     *
     * @param node The [NodeEntity] to insert.
     * @return The auto-generated or existing primary key ID of the inserted node.
     */
    @Upsert
    suspend fun insertNode(node: NodeEntity): Long

    /**
     * Inserts multiple nodes, returning their generated or existing identifiers.
     */
    @Upsert
    suspend fun insertNodes(nodes: List<NodeEntity>): List<Long>

    /**
     * Updates an existing node in the database.
     *
     * @param node The [NodeEntity] containing the updated values. Its [NodeEntity.id] must match an existing row.
     */
    @Update
    suspend fun updateNode(node: NodeEntity)

    /**
     * Deletes a node from the database.
     *
     * @param node The [NodeEntity] to delete.
     */
    @Delete
    suspend fun deleteNode(node: NodeEntity)

    /**
     * Pins a node to the "Today" list by inserting a [TodayPinEntity].
     * Replaces any existing pin for the same node and date to avoid conflicts.
     *
     * @param pin The [TodayPinEntity] representing the pinned state.
     */
    @Upsert
    suspend fun pinToToday(pin: TodayPinEntity)

    /**
     * Removes the "Today" pin for a specific node.
     *
     * @param nodeId The ID of the node to unpin.
     */
    @Query("DELETE FROM today_pins WHERE nodeId = :nodeId")
    suspend fun unpinFromToday(nodeId: Long)

    /**
     * Observes whether a specific node is currently pinned to "Today".
     *
     * @param nodeId The ID of the node to check.
     * @return A reactive stream emitting `true` if the node has an associated pin, `false` otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM today_pins WHERE nodeId = :nodeId)")
    fun isPinnedToToday(nodeId: Long): Flow<Boolean>
}

/**
 * Provides database access for managing deep work and [FocusSessionEntity] focus sessions linked to specific nodes.
 */
@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Upsert
    suspend fun insertSession(session: FocusSessionEntity): Long

    /**
     * Inserts or updates multiple focus sessions.
     */
    @Upsert
    suspend fun insertSessions(sessions: List<FocusSessionEntity>)

    @Update
    suspend fun updateSession(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE endedAt IS NULL LIMIT 1")
    fun getActiveSession(): Flow<FocusSessionEntity?>
}

/**
 * Provides database access for [TrackEntryEntity] time tracking logs, capturing continuous effort against system nodes.
 */
@Dao
interface TrackDao {
    @Query("SELECT * FROM track_entries ORDER BY date DESC, createdAt DESC")
    fun getAllTrackEntries(): Flow<List<TrackEntryEntity>>

    @Upsert
    suspend fun insertTrackEntry(entry: TrackEntryEntity): Long

    /**
     * Inserts or updates multiple track entries.
     */
    @Upsert
    suspend fun insertTrackEntries(entries: List<TrackEntryEntity>)

    @Query("SELECT * FROM track_entries WHERE date = :date LIMIT 1")
    suspend fun getTrackEntryByDate(date: String): TrackEntryEntity?

    @Upsert
    suspend fun insertTrackMedication(join: TrackMedicationJoinEntity)

    @Query("SELECT * FROM track_medications WHERE trackEntryId = :trackEntryId")
    fun getTrackMedications(trackEntryId: Long): Flow<List<TrackMedicationJoinEntity>>
}

/**
 * Provides database access for resolving bi-directional [RelationEntity] graph relationships between nodes.
 */
@Dao
interface RelationDao {
    @Query("SELECT * FROM relations WHERE fromNodeId = :nodeId OR toNodeId = :nodeId")
    fun getRelationsForNode(nodeId: Long): Flow<List<RelationEntity>>

    @Upsert
    suspend fun insertRelation(relation: RelationEntity)

    @Upsert
    suspend fun insertRelations(relations: List<RelationEntity>)

    @Delete
    suspend fun deleteRelation(relation: RelationEntity)

    @Query("DELETE FROM relations WHERE fromNodeId = :nodeId OR toNodeId = :nodeId")
    suspend fun deleteRelationsForNode(nodeId: Long)

    @Query("DELETE FROM relations WHERE fromNodeId = :nodeId AND relationType = 'BELONGS_TO'")
    suspend fun deleteBelongsToRelations(nodeId: Long)

    @Query("DELETE FROM relations WHERE fromNodeId IN (:nodeIds) AND relationType = 'BELONGS_TO'")
    suspend fun deleteBelongsToRelations(nodeIds: List<Long>)

    @Query("SELECT * FROM relations WHERE fromNodeId = :nodeId AND relationType = 'BELONGS_TO'")
    suspend fun getBelongsToRelations(nodeId: Long): List<RelationEntity>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM relations
            WHERE fromNodeId = :from
              AND toNodeId = :to
              AND relationType = :relationType
        )
    """,
    )
    suspend fun anyRelationExists(
        from: Long,
        to: Long,
        relationType: String,
    ): Boolean

    @Query("SELECT * FROM relations")
    fun getAllRelations(): Flow<List<RelationEntity>>
}

/**
 * Provides database access for global classification [TagEntity] tags mapped across system nodes via [NodeTagEntity].
 */
@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<TagEntity>>

    @Upsert
    suspend fun insertTag(tag: TagEntity): Long

    /**
     * Inserts or updates multiple tags, generating new IDs as necessary.
     */
    @Upsert
    suspend fun insertTags(tags: List<TagEntity>)

    @Transaction
    @Query(
        """
        SELECT tags.* FROM tags 
        INNER JOIN node_tags ON tags.id = node_tags.tagId 
        WHERE node_tags.nodeId = :nodeId
    """,
    )
    fun getTagsForNode(nodeId: Long): Flow<List<TagEntity>>

    @Upsert
    suspend fun attachTagToNode(nodeTag: NodeTagEntity)

    @Query("DELETE FROM node_tags WHERE nodeId = :nodeId AND tagId = :tagId")
    suspend fun detachTagFromNode(
        nodeId: Long,
        tagId: Long,
    )

    @Query("DELETE FROM node_tags WHERE nodeId = :nodeId")
    suspend fun detachAllTagsFromNode(nodeId: Long)
}

/**
 * Provides database access for capturing immutable chronological [EventLogEntity] activity events for system nodes.
 */
@Dao
interface EventLogDao {
    @Query("SELECT * FROM event_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<EventLogEntity>>

    @Query("SELECT * FROM event_log WHERE nodeId = :nodeId OR relatedNodeId = :nodeId ORDER BY timestamp DESC")
    fun getLogsForNode(nodeId: Long): Flow<List<EventLogEntity>>

    @Upsert
    suspend fun insertLog(log: EventLogEntity)

    @Upsert
    suspend fun insertLogs(logs: List<EventLogEntity>)
}

/**
 * Provides database access for tracking external [AttachmentEntity] file attachments connected to nodes.
 */
@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments")
    fun getAllAttachments(): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE nodeId = :nodeId")
    fun getAttachmentsForNode(nodeId: Long): Flow<List<AttachmentEntity>>

    @Upsert
    suspend fun insertAttachment(attachment: AttachmentEntity)

    @Delete
    suspend fun deleteAttachment(attachment: AttachmentEntity)

    @Query("DELETE FROM attachments WHERE nodeId = :nodeId")
    suspend fun deleteAttachmentsForNode(nodeId: Long)
}

/**
 * Persists raw capture inbox entries that have not yet been triaged into life objects.
 */
@Dao
interface InboxEntryDao {
    @Query("SELECT * FROM inbox_entries ORDER BY capturedAt DESC")
    fun getAllInboxEntries(): Flow<List<InboxEntryEntity>>

    @Query(
        """
        SELECT * FROM inbox_entries
        WHERE processedAt IS NULL AND dismissedAt IS NULL
        ORDER BY capturedAt DESC
    """,
    )
    fun getActiveInboxEntries(): Flow<List<InboxEntryEntity>>

    @Query("SELECT * FROM inbox_entries WHERE id = :id")
    suspend fun getInboxEntryById(id: Long): InboxEntryEntity?

    @Upsert
    suspend fun insertInboxEntry(entry: InboxEntryEntity): Long

    @Update
    suspend fun updateInboxEntry(entry: InboxEntryEntity)
}

/**
 * Accesses task-specific execution data that sits beside the shared item row.
 */
@Dao
interface TaskFacetDao {
    @Query("SELECT * FROM task_facets")
    fun getAllTaskFacets(): Flow<List<TaskFacetEntity>>

    @Query("SELECT * FROM task_facets WHERE itemId = :itemId")
    suspend fun getTaskFacetByItemId(itemId: Long): TaskFacetEntity?

    @Query("SELECT * FROM task_facets WHERE itemId = :itemId")
    fun observeTaskFacet(itemId: Long): Flow<TaskFacetEntity?>

    @Upsert
    suspend fun upsertTaskFacet(facet: TaskFacetEntity)

    @Query("DELETE FROM task_facets WHERE itemId = :itemId")
    suspend fun deleteTaskFacetForItem(itemId: Long)
}

/**
 * Accesses note-specific semantics that sit beside the shared item row.
 */
@Dao
interface NoteFacetDao {
    @Query("SELECT * FROM note_facets")
    fun getAllNoteFacets(): Flow<List<NoteFacetEntity>>

    @Query("SELECT * FROM note_facets WHERE itemId = :itemId")
    suspend fun getNoteFacetByItemId(itemId: Long): NoteFacetEntity?

    @Query("SELECT * FROM note_facets WHERE itemId = :itemId")
    fun observeNoteFacet(itemId: Long): Flow<NoteFacetEntity?>

    @Upsert
    suspend fun upsertNoteFacet(facet: NoteFacetEntity)

    @Query("DELETE FROM note_facets WHERE itemId = :itemId")
    suspend fun deleteNoteFacetForItem(itemId: Long)
}

/**
 * Accesses project-specific coordination data that sits beside the shared item row.
 */
@Dao
interface ProjectFacetDao {
    @Query("SELECT * FROM project_facets")
    fun getAllProjectFacets(): Flow<List<ProjectFacetEntity>>

    @Query("SELECT * FROM project_facets WHERE itemId = :itemId")
    suspend fun getProjectFacetByItemId(itemId: Long): ProjectFacetEntity?

    @Query("SELECT * FROM project_facets WHERE itemId = :itemId")
    fun observeProjectFacet(itemId: Long): Flow<ProjectFacetEntity?>

    @Upsert
    suspend fun upsertProjectFacet(facet: ProjectFacetEntity)

    @Query("DELETE FROM project_facets WHERE itemId = :itemId")
    suspend fun deleteProjectFacetForItem(itemId: Long)
}

/**
 * Accesses area-specific stewardship data that sits beside the shared item row.
 */
@Dao
interface AreaFacetDao {
    @Query("SELECT * FROM area_facets")
    fun getAllAreaFacets(): Flow<List<AreaFacetEntity>>

    @Query("SELECT * FROM area_facets WHERE itemId = :itemId")
    suspend fun getAreaFacetByItemId(itemId: Long): AreaFacetEntity?

    @Query("SELECT * FROM area_facets WHERE itemId = :itemId")
    fun observeAreaFacet(itemId: Long): Flow<AreaFacetEntity?>

    @Upsert
    suspend fun upsertAreaFacet(facet: AreaFacetEntity)

    @Query("DELETE FROM area_facets WHERE itemId = :itemId")
    suspend fun deleteAreaFacetForItem(itemId: Long)
}

/**
 * Accesses record-specific chronology data that sits beside the shared item row.
 */
@Dao
interface RecordFacetDao {
    @Query("SELECT * FROM record_facets")
    fun getAllRecordFacets(): Flow<List<RecordFacetEntity>>

    @Query("SELECT * FROM record_facets WHERE itemId = :itemId")
    suspend fun getRecordFacetByItemId(itemId: Long): RecordFacetEntity?

    @Query("SELECT * FROM record_facets WHERE itemId = :itemId")
    fun observeRecordFacet(itemId: Long): Flow<RecordFacetEntity?>

    @Upsert
    suspend fun upsertRecordFacet(facet: RecordFacetEntity)

    @Query("DELETE FROM record_facets WHERE itemId = :itemId")
    suspend fun deleteRecordFacetForItem(itemId: Long)
}

/**
 * Accesses lens-oriented domain classifications over shared life objects.
 */
@Dao
interface ItemDomainDao {
    @Query("SELECT * FROM item_domains")
    fun getAllItemDomains(): Flow<List<ItemDomainEntity>>

    @Query("SELECT * FROM item_domains WHERE itemId = :itemId ORDER BY isPrimary DESC, assignedAt ASC")
    fun getDomainsForItem(itemId: Long): Flow<List<ItemDomainEntity>>

    @Upsert
    suspend fun upsertDomain(domain: ItemDomainEntity)

    @Query("DELETE FROM item_domains WHERE itemId = :itemId AND domainKey = :domainKey")
    suspend fun deleteDomain(
        itemId: Long,
        domainKey: String,
    )

    @Query("DELETE FROM item_domains WHERE itemId = :itemId")
    suspend fun deleteDomainsForItem(itemId: Long)

    @Query("UPDATE item_domains SET isPrimary = 0 WHERE itemId = :itemId")
    suspend fun clearPrimaryFlag(itemId: Long)
}

/**
 * Accesses optional rich-content documents attached to life objects.
 */
@Dao
interface RichContentDocumentDao {
    @Query("SELECT * FROM rich_content_documents ORDER BY updatedAt DESC")
    fun getAllDocuments(): Flow<List<RichContentDocumentEntity>>

    @Query("SELECT * FROM rich_content_documents WHERE itemId = :itemId")
    fun observeDocumentForItem(itemId: Long): Flow<RichContentDocumentEntity?>

    @Query("SELECT * FROM rich_content_documents WHERE itemId = :itemId")
    suspend fun getDocumentForItem(itemId: Long): RichContentDocumentEntity?

    @Upsert
    suspend fun upsertDocument(document: RichContentDocumentEntity)

    @Query("DELETE FROM rich_content_documents WHERE itemId = :itemId")
    suspend fun deleteDocumentForItem(itemId: Long)
}

/**
 * Persists attachable schedule data for life objects.
 */
@Dao
interface ScheduleEntryDao {
    @Query("SELECT * FROM schedule_entries ORDER BY scheduledAt ASC")
    fun getAllScheduleEntries(): Flow<List<ScheduleEntryEntity>>

    @Query("SELECT * FROM schedule_entries WHERE itemId = :itemId ORDER BY scheduledAt ASC")
    fun getScheduleEntriesForItem(itemId: Long): Flow<List<ScheduleEntryEntity>>

    @Query(
        """
        SELECT * FROM schedule_entries
        WHERE kind = :kind
          AND localDateEpochDay IS NOT NULL
          AND localDateEpochDay BETWEEN :fromEpochDay AND :toEpochDay
          AND completedAt IS NULL
        ORDER BY localDateEpochDay ASC, scheduledAt ASC
    """,
    )
    fun getOpenScheduleEntriesByKindAndDayRange(
        kind: String,
        fromEpochDay: Int,
        toEpochDay: Int,
    ): Flow<List<ScheduleEntryEntity>>

    @Query("SELECT * FROM schedule_entries WHERE itemId = :itemId AND kind = :kind ORDER BY scheduledAt ASC")
    suspend fun getScheduleEntriesByKind(
        itemId: Long,
        kind: String,
    ): List<ScheduleEntryEntity>

    @Query("DELETE FROM schedule_entries WHERE itemId = :itemId AND kind = :kind")
    suspend fun deleteScheduleEntriesByKind(
        itemId: Long,
        kind: String,
    )

    @Query("DELETE FROM schedule_entries WHERE itemId = :itemId")
    suspend fun deleteScheduleEntriesForItem(itemId: Long)

    @Upsert
    suspend fun insertScheduleEntry(entry: ScheduleEntryEntity): Long

    @Upsert
    suspend fun insertScheduleEntries(entries: List<ScheduleEntryEntity>)
}

/**
 * Accesses saved local projections over typed shared life objects.
 */
@Dao
interface SavedViewDao {
    @Query("SELECT * FROM saved_views ORDER BY updatedAt DESC")
    fun getAllSavedViews(): Flow<List<SavedViewEntity>>

    @Query("SELECT * FROM saved_views WHERE id = :id")
    suspend fun getSavedViewById(id: Long): SavedViewEntity?

    @Upsert
    suspend fun insertSavedView(view: SavedViewEntity): Long

    @Update
    suspend fun updateSavedView(view: SavedViewEntity)

    @Delete
    suspend fun deleteSavedView(view: SavedViewEntity)

    @Query("SELECT * FROM saved_view_source_kinds")
    fun getAllSavedViewSourceKinds(): Flow<List<SavedViewSourceKindEntity>>

    @Query("SELECT * FROM saved_view_source_kinds WHERE viewId = :viewId")
    suspend fun getSourceKindsForView(viewId: Long): List<SavedViewSourceKindEntity>

    @Upsert
    suspend fun insertSavedViewSourceKinds(sourceKinds: List<SavedViewSourceKindEntity>)

    @Query("DELETE FROM saved_view_source_kinds WHERE viewId = :viewId")
    suspend fun deleteSourceKindsForView(viewId: Long)

    @Query("SELECT * FROM saved_view_filters")
    fun getAllSavedViewFilters(): Flow<List<SavedViewFilterEntity>>

    @Query("SELECT * FROM saved_view_filters WHERE viewId = :viewId ORDER BY position ASC")
    suspend fun getFiltersForView(viewId: Long): List<SavedViewFilterEntity>

    @Upsert
    suspend fun insertSavedViewFilters(filters: List<SavedViewFilterEntity>)

    @Query("DELETE FROM saved_view_filters WHERE viewId = :viewId")
    suspend fun deleteFiltersForView(viewId: Long)

    @Query("SELECT * FROM saved_view_sorts")
    fun getAllSavedViewSorts(): Flow<List<SavedViewSortEntity>>

    @Query("SELECT * FROM saved_view_sorts WHERE viewId = :viewId ORDER BY position ASC")
    suspend fun getSortsForView(viewId: Long): List<SavedViewSortEntity>

    @Upsert
    suspend fun insertSavedViewSorts(sorts: List<SavedViewSortEntity>)

    @Query("DELETE FROM saved_view_sorts WHERE viewId = :viewId")
    suspend fun deleteSortsForView(viewId: Long)

    @Query("SELECT * FROM saved_view_visible_fields")
    fun getAllSavedViewVisibleFields(): Flow<List<SavedViewVisibleFieldEntity>>

    @Query("SELECT * FROM saved_view_visible_fields WHERE viewId = :viewId ORDER BY position ASC")
    suspend fun getVisibleFieldsForView(viewId: Long): List<SavedViewVisibleFieldEntity>

    @Upsert
    suspend fun insertSavedViewVisibleFields(fields: List<SavedViewVisibleFieldEntity>)

    @Query("DELETE FROM saved_view_visible_fields WHERE viewId = :viewId")
    suspend fun deleteVisibleFieldsForView(viewId: Long)
}

/**
 * Provides database access for saving and retrieving reusable parameterized [TemplateEntity] structure templates.
 */
@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Upsert
    suspend fun insertTemplate(template: TemplateEntity)

    @Upsert
    suspend fun insertTemplates(templates: List<TemplateEntity>)

    @Update
    suspend fun updateTemplate(template: TemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: TemplateEntity)
}

/**
 * Provides database access for retaining point-in-time [NodeSnapshotEntity] state snapshots for change analysis.
 */
@Dao
interface NodeSnapshotDao {
    @Query("SELECT * FROM node_snapshots WHERE nodeId = :nodeId ORDER BY timestamp DESC")
    fun getSnapshotsForNode(nodeId: Long): Flow<List<NodeSnapshotEntity>>

    @Upsert
    suspend fun insertSnapshot(snapshot: NodeSnapshotEntity)

    @Delete
    suspend fun deleteSnapshot(snapshot: NodeSnapshotEntity)

    @Query("DELETE FROM node_snapshots WHERE nodeId = :nodeId")
    suspend fun deleteSnapshotsForNode(nodeId: Long)
}

/**
 * Provides database access for periodic [ReviewEntity] reflection sessions and their documented outcomes.
 */
@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY completedAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Upsert
    suspend fun insertReview(review: ReviewEntity): Long

    /**
     * Inserts or updates multiple reviews.
     */
    @Upsert
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Query("SELECT * FROM reviews WHERE type = :type ORDER BY completedAt DESC LIMIT 1")
    suspend fun getLastReviewByType(type: String): ReviewEntity?
}

/**
 * Provides database access for storing configurations of external [CalendarProviderEntity] synchronization sources.
 */
@Dao
interface CalendarProviderDao {
    @Query("SELECT * FROM calendar_providers")
    fun getAllProviders(): Flow<List<CalendarProviderEntity>>

    @Upsert
    suspend fun insertProvider(provider: CalendarProviderEntity): Long

    /**
     * Inserts or updates multiple calendar providers.
     */
    @Upsert
    suspend fun insertProviders(providers: List<CalendarProviderEntity>)

    @Update
    suspend fun updateProvider(provider: CalendarProviderEntity)

    @Delete
    suspend fun deleteProvider(provider: CalendarProviderEntity)

    @Query("SELECT * FROM calendar_providers WHERE id = :id")
    suspend fun getProviderById(id: Long): CalendarProviderEntity?
}

/**
 * Provides database access for querying synchronized external [CalendarEventEntity] calendar events locally.
 */
@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events WHERE startAt >= :from AND startAt <= :to")
    fun getEventsInRange(
        from: Long,
        to: Long,
    ): Flow<List<CalendarEventEntity>>

    @Upsert
    suspend fun insertEvents(events: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_events WHERE providerId = :providerId")
    suspend fun deleteEventsByProvider(providerId: Long)

    @Upsert
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Update
    suspend fun updateEvent(event: CalendarEventEntity)

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)
}

/**
 * Provides database access for user-defined [ModeEntity] operational modes that adjust application behavior.
 */
@Dao
interface ModeDao {
    @Query("SELECT * FROM modes ORDER BY sortOrder ASC")
    fun getAllModes(): Flow<List<ModeEntity>>

    @Upsert
    suspend fun insertMode(mode: ModeEntity): Long

    @Update
    suspend fun updateMode(mode: ModeEntity)

    @Delete
    suspend fun deleteMode(mode: ModeEntity)

    @Query("SELECT * FROM mode_preferences WHERE modeId = :modeId")
    fun getPreferencesForMode(modeId: Long): Flow<ModePreferenceEntity?>

    @Upsert
    suspend fun insertPreference(preference: ModePreferenceEntity)

    @Query("SELECT * FROM mode_area_filters WHERE modeId = :modeId")
    fun getAreaFiltersForMode(modeId: Long): Flow<List<ModeAreaFilterEntity>>

    @Upsert
    suspend fun insertAreaFilter(filter: ModeAreaFilterEntity)

    @Query("SELECT * FROM mode_type_filters WHERE modeId = :modeId")
    fun getTypeFiltersForMode(modeId: Long): Flow<List<ModeTypeFilterEntity>>

    @Upsert
    suspend fun insertTypeFilter(filter: ModeTypeFilterEntity)

    @Query("SELECT * FROM mode_usage_logs ORDER BY activatedAt DESC")
    fun getAllUsageLogs(): Flow<List<ModeUsageLogEntity>>

    @Upsert
    suspend fun insertUsageLog(log: ModeUsageLogEntity): Long

    @Query("UPDATE mode_usage_logs SET deactivatedAt = :timestamp WHERE id = :id")
    suspend fun deactivateLog(
        id: Long,
        timestamp: Long,
    )
}

/**
 * Provides database access for tracking execution history of automated operational protocols as [ProtocolHistoryEntity] instances.
 */
@Dao
interface ProtocolDao {
    @Query("SELECT * FROM protocol_history ORDER BY executedAt DESC")
    fun getAllProtocolHistory(): Flow<List<ProtocolHistoryEntity>>

    @Upsert
    suspend fun insertProtocolHistory(history: ProtocolHistoryEntity): Long
}

/**
 * Provides database access for tracking available [DecisionOptionEntity] options linked to a primary decision node.
 */
@Dao
interface DecisionDao {
    @Query("SELECT * FROM decision_options WHERE decisionNodeId = :nodeId")
    fun getOptionsForDecision(nodeId: Long): Flow<List<DecisionOptionEntity>>

    @Upsert
    suspend fun insertDecisionOption(option: DecisionOptionEntity): Long

    @Update
    suspend fun updateDecisionOption(option: DecisionOptionEntity)

    @Delete
    suspend fun deleteDecisionOption(option: DecisionOptionEntity)
}

/**
 * Provides database access for retrieving the primary application [UserEntity] identity profile.
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Upsert
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

/**
 * Provides database access for managing user [MedicationEntity] tracking.
 */
@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE isEnabled = 1")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Upsert
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)
}
