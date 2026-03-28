/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import androidx.room.*
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
    """
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: NodeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<NodeEntity>): List<Long>

    @Update
    suspend fun updateNode(node: NodeEntity)

    @Delete
    suspend fun deleteNode(node: NodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pinToToday(pin: TodayPinEntity)

    @Query("DELETE FROM today_pins WHERE nodeId = :nodeId")
    suspend fun unpinFromToday(nodeId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM today_pins WHERE nodeId = :nodeId)")
    fun isPinnedToToday(nodeId: Long): Flow<Boolean>
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Update
    suspend fun updateSession(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE endedAt IS NULL LIMIT 1")
    fun getActiveSession(): Flow<FocusSessionEntity?>
}

@Dao
interface TrackDao {
    @Query("SELECT * FROM track_entries ORDER BY date DESC, createdAt DESC")
    fun getAllTrackEntries(): Flow<List<TrackEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackEntry(entry: TrackEntryEntity): Long

    @Query("SELECT * FROM track_entries WHERE date = :date LIMIT 1")
    suspend fun getTrackEntryByDate(date: String): TrackEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackMedication(join: TrackMedicationJoinEntity)

    @Query("SELECT * FROM track_medications WHERE trackEntryId = :trackEntryId")
    fun getTrackMedications(trackEntryId: Long): Flow<List<TrackMedicationJoinEntity>>
}

@Dao
interface RelationDao {
    @Query("SELECT * FROM relations WHERE fromNodeId = :nodeId OR toNodeId = :nodeId")
    fun getRelationsForNode(nodeId: Long): Flow<List<RelationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelation(relation: RelationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelations(relations: List<RelationEntity>)

    @Delete
    suspend fun deleteRelation(relation: RelationEntity)

    @Query("DELETE FROM relations WHERE fromNodeId = :nodeId AND relationType = 'BELONGS_TO'")
    suspend fun deleteBelongsToRelations(nodeId: Long)

    @Query("DELETE FROM relations WHERE fromNodeId IN (:nodeIds) AND relationType = 'BELONGS_TO'")
    suspend fun deleteBelongsToRelations(nodeIds: List<Long>)

    @Query("SELECT * FROM relations WHERE fromNodeId = :nodeId AND relationType = 'BELONGS_TO'")
    suspend fun getBelongsToRelations(nodeId: Long): List<RelationEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM relations WHERE ((fromNodeId = :from AND toNodeId = :to) OR (fromNodeId = :to AND toNodeId = :from)))")
    suspend fun anyRelationExists(from: Long, to: Long): Boolean

    @Query("SELECT * FROM relations")
    fun getAllRelations(): Flow<List<RelationEntity>>
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Transaction
    @Query(
        """
        SELECT tags.* FROM tags 
        INNER JOIN node_tags ON tags.id = node_tags.tagId 
        WHERE node_tags.nodeId = :nodeId
    """
    )
    fun getTagsForNode(nodeId: Long): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun attachTagToNode(nodeTag: NodeTagEntity)

    @Query("DELETE FROM node_tags WHERE nodeId = :nodeId AND tagId = :tagId")
    suspend fun detachTagFromNode(nodeId: Long, tagId: Long)
}

@Dao
interface EventLogDao {
    @Query("SELECT * FROM event_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<EventLogEntity>>

    @Query("SELECT * FROM event_log WHERE nodeId = :nodeId OR relatedNodeId = :nodeId ORDER BY timestamp DESC")
    fun getLogsForNode(nodeId: Long): Flow<List<EventLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: EventLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<EventLogEntity>)
}

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE nodeId = :nodeId")
    fun getAttachmentsForNode(nodeId: Long): Flow<List<AttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity)

    @Delete
    suspend fun deleteAttachment(attachment: AttachmentEntity)
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity)

    @Update
    suspend fun updateTemplate(template: TemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: TemplateEntity)
}

@Dao
interface NodeSnapshotDao {
    @Query("SELECT * FROM node_snapshots WHERE nodeId = :nodeId ORDER BY timestamp DESC")
    fun getSnapshotsForNode(nodeId: Long): Flow<List<NodeSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: NodeSnapshotEntity)

    @Delete
    suspend fun deleteSnapshot(snapshot: NodeSnapshotEntity)

    @Query("DELETE FROM node_snapshots WHERE nodeId = :nodeId")
    suspend fun deleteSnapshotsForNode(nodeId: Long)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY completedAt DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Query("SELECT * FROM reviews WHERE type = :type ORDER BY completedAt DESC LIMIT 1")
    suspend fun getLastReviewByType(type: String): ReviewEntity?
}

@Dao
interface CalendarProviderDao {
    @Query("SELECT * FROM calendar_providers")
    fun getAllProviders(): Flow<List<CalendarProviderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: CalendarProviderEntity): Long

    @Update
    suspend fun updateProvider(provider: CalendarProviderEntity)

    @Delete
    suspend fun deleteProvider(provider: CalendarProviderEntity)

    @Query("SELECT * FROM calendar_providers WHERE id = :id")
    suspend fun getProviderById(id: Long): CalendarProviderEntity?
}

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events WHERE startAt >= :from AND startAt <= :to")
    fun getEventsInRange(from: Long, to: Long): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_events WHERE providerId = :providerId")
    suspend fun deleteEventsByProvider(providerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Update
    suspend fun updateEvent(event: CalendarEventEntity)

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)
}

@Dao
interface ModeDao {
    @Query("SELECT * FROM modes ORDER BY sortOrder ASC")
    fun getAllModes(): Flow<List<ModeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMode(mode: ModeEntity): Long

    @Update
    suspend fun updateMode(mode: ModeEntity)

    @Delete
    suspend fun deleteMode(mode: ModeEntity)

    @Query("SELECT * FROM mode_preferences WHERE modeId = :modeId")
    fun getPreferencesForMode(modeId: Long): Flow<ModePreferenceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: ModePreferenceEntity)

    @Query("SELECT * FROM mode_area_filters WHERE modeId = :modeId")
    fun getAreaFiltersForMode(modeId: Long): Flow<List<ModeAreaFilterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAreaFilter(filter: ModeAreaFilterEntity)

    @Query("SELECT * FROM mode_type_filters WHERE modeId = :modeId")
    fun getTypeFiltersForMode(modeId: Long): Flow<List<ModeTypeFilterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTypeFilter(filter: ModeTypeFilterEntity)

    @Query("SELECT * FROM mode_usage_logs ORDER BY activatedAt DESC")
    fun getAllUsageLogs(): Flow<List<ModeUsageLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageLog(log: ModeUsageLogEntity): Long

    @Query("UPDATE mode_usage_logs SET deactivatedAt = :timestamp WHERE id = :id")
    suspend fun deactivateLog(id: Long, timestamp: Long)
}

@Dao
interface ProtocolDao {
    @Query("SELECT * FROM protocol_history ORDER BY executedAt DESC")
    fun getAllProtocolHistory(): Flow<List<ProtocolHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocolHistory(history: ProtocolHistoryEntity): Long
}

@Dao
interface DecisionDao {
    @Query("SELECT * FROM decision_options WHERE decisionNodeId = :nodeId")
    fun getOptionsForDecision(nodeId: Long): Flow<List<DecisionOptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecisionOption(option: DecisionOptionEntity): Long

    @Update
    suspend fun updateDecisionOption(option: DecisionOptionEntity)

    @Delete
    suspend fun deleteDecisionOption(option: DecisionOptionEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE isEnabled = 1")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)
}
