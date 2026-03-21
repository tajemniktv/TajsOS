package com.tajemniktv.tajsos.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * NodeDao provides methods for accessing the core "Node" entities.
 */
@Dao
interface NodeDao {

    @Transaction
    @Query("SELECT * FROM nodes WHERE status != 'archived' ORDER BY createdAt DESC")
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

    @Query("SELECT * FROM nodes WHERE type = :type AND status != 'archived'")
    fun getNodesByType(type: String): Flow<List<NodeEntity>>

    @Query("SELECT * FROM nodes WHERE projectId = :projectId AND status != 'archived'")
    fun getNodesByProject(projectId: Long): Flow<List<NodeEntity>>

    @Query("SELECT * FROM nodes WHERE areaId = :areaId AND status != 'archived'")
    fun getNodesByArea(areaId: Long): Flow<List<NodeEntity>>

    @Query("SELECT * FROM nodes WHERE id = :id")
    suspend fun getNodeById(id: Long): NodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: NodeEntity): Long

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
    suspend fun insertTrackEntry(entry: TrackEntryEntity)
}

@Dao
interface RelationDao {
    @Query("SELECT * FROM relations WHERE fromNodeId = :nodeId OR toNodeId = :nodeId")
    fun getRelationsForNode(nodeId: Long): Flow<List<RelationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelation(relation: RelationEntity)

    @Delete
    suspend fun deleteRelation(relation: RelationEntity)
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: EventLogEntity)
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
