/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * AppRepository is the single source of truth for TajsOS's Room database.
 */
class AppRepository(
    private val nodeDao: NodeDao,
    private val focusSessionDao: FocusSessionDao,
    private val trackDao: TrackDao,
    private val relationDao: RelationDao,
    private val tagDao: TagDao,
    private val eventLogDao: EventLogDao,
    private val attachmentDao: AttachmentDao,
    private val templateDao: TemplateDao,
    private val calendarProviderDao: CalendarProviderDao,
    private val calendarEventDao: CalendarEventDao
) {
    fun getAllNodes(): Flow<List<NodeWithPin>> = nodeDao.getAllNodesWithPins()

    fun getTodayNodes(): Flow<List<NodeEntity>> {
        val today = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        return nodeDao.getTodayNodes(today)
    }

    suspend fun getNodeById(id: Long): NodeEntity? = nodeDao.getNodeById(id)

    // ... existing methods ...

    // Calendar
    fun getAllCalendarProviders() = calendarProviderDao.getAllProviders()
    suspend fun insertCalendarProvider(provider: CalendarProviderEntity) =
        calendarProviderDao.insertProvider(provider)

    suspend fun updateCalendarProvider(provider: CalendarProviderEntity) =
        calendarProviderDao.updateProvider(provider)

    suspend fun deleteCalendarProvider(provider: CalendarProviderEntity) =
        calendarProviderDao.deleteProvider(provider)

    fun getCalendarEventsInRange(from: Long, to: Long) = calendarEventDao.getEventsInRange(from, to)
    suspend fun insertCalendarEvents(events: List<CalendarEventEntity>) =
        calendarEventDao.insertEvents(events)

    suspend fun deleteCalendarEventsByProvider(providerId: Long) =
        calendarEventDao.deleteEventsByProvider(providerId)

    suspend fun insertNode(node: NodeEntity): Long {
        val id = nodeDao.insertNode(node)
        logEvent("NODE_CREATED", id)
        syncBelongsToRelations(id, node.projectId, node.areaId)
        return id
    }

    suspend fun updateNode(node: NodeEntity) {
        val oldNode = nodeDao.getNodeById(node.id)
        nodeDao.updateNode(node)

        if (oldNode != null) {
            if (oldNode.status != "done" && node.status == "done") {
                logEvent("NODE_COMPLETED", node.id)
            } else if (oldNode.status != "archived" && node.status == "archived") {
                logEvent("NODE_ARCHIVED", node.id)
            }

            if (oldNode.isFrozen != node.isFrozen) {
                logEvent(if (node.isFrozen) "NODE_FROZEN" else "NODE_UNFROZEN", node.id)
            }
        }

        syncBelongsToRelations(node.id, node.projectId, node.areaId)
    }

    private suspend fun syncBelongsToRelations(nodeId: Long, projectId: Long?, areaId: Long?) {
        relationDao.deleteBelongsToRelations(nodeId)
        if (projectId != null) {
            relationDao.insertRelation(
                RelationEntity(
                    fromNodeId = nodeId,
                    toNodeId = projectId,
                    relationType = "BELONGS_TO"
                )
            )
        }
        if (areaId != null) {
            relationDao.insertRelation(
                RelationEntity(
                    fromNodeId = nodeId,
                    toNodeId = areaId,
                    relationType = "BELONGS_TO"
                )
            )
        }
    }

    suspend fun deleteNode(node: NodeEntity) = nodeDao.deleteNode(node)

    suspend fun pinToToday(nodeId: Long) {
        val today = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        nodeDao.pinToToday(TodayPinEntity(nodeId = nodeId, date = today, position = 0))
        logEvent("TODAY_ASSIGNED", nodeId)
    }

    suspend fun unpinFromToday(nodeId: Long) = nodeDao.unpinFromToday(nodeId)

    fun isPinnedToToday(nodeId: Long): Flow<Boolean> = nodeDao.isPinnedToToday(nodeId)

    /**
 * Retrieves nodes filtered by their type.
 *
 * @param type The node type to filter by (for example, "task" or "project").
 * @return A flow that emits lists of nodes matching the specified type.
 */
fun getNodesByType(type: String): Flow<List<NodeEntity>> = nodeDao.getNodesByType(type)
    /**
 * Retrieve nodes that belong to the specified project.
 *
 * @param projectId The ID of the project to filter nodes by.
 * @return Lists of nodes belonging to the project.
 */
fun getNodesByProject(projectId: Long): Flow<List<NodeEntity>> = nodeDao.getNodesByProject(projectId)
    /**
 * Retrieves nodes that belong to the specified project, including their today-pin information.
 *
 * @param projectId The id of the project whose nodes should be returned.
 * @return Lists of `NodeWithPin` representing nodes in the project along with their today-pin data.
 */
fun getNodesByProjectWithPins(projectId: Long): Flow<List<NodeWithPin>> = nodeDao.getNodesByProjectWithPins(projectId)
    /**
 * Retrieve nodes that belong to a specific area.
 *
 * @param areaId The id of the area to filter nodes by.
 * @return A flow that emits lists of NodeEntity belonging to the specified area.
 */
fun getNodesByArea(areaId: Long): Flow<List<NodeEntity>> = nodeDao.getNodesByArea(areaId)
    /**
 * Retrieves nodes in the specified area together with their today-pin information.
 *
 * @param areaId ID of the area whose nodes should be returned.
 * @return A list of nodes in the given area paired with their today-pin information.
 */
fun getNodesByAreaWithPins(areaId: Long): Flow<List<NodeWithPin>> = nodeDao.getNodesByAreaWithPins(areaId)
    /**
 * Retrieves project nodes that belong to the specified area.
 *
 * @param areaId The id of the area whose projects should be returned.
 * @return A flow that emits lists of project nodes belonging to the specified area.
 */
fun getProjectsByArea(areaId: Long): Flow<List<NodeEntity>> = nodeDao.getProjectsByArea(areaId)

    /**
 * Retrieves a stream of all focus sessions stored in the database.
 *
 * @return A Flow that emits the current list of FocusSessionEntity objects and re-emits when the data changes.
 */
fun getAllSessions(): Flow<List<FocusSessionEntity>> = focusSessionDao.getAllSessions()
    /**
 * Observes the currently active focus session.
 *
 * @return The active FocusSessionEntity if one exists, `null` otherwise.
 */
fun getActiveSession(): Flow<FocusSessionEntity?> = focusSessionDao.getActiveSession()
    suspend fun insertSession(session: FocusSessionEntity): Long {
        val id = focusSessionDao.insertSession(session)
        logEvent("SESSION_STARTED", session.nodeId)
        return id
    }
    suspend fun updateSession(session: FocusSessionEntity) {
        focusSessionDao.updateSession(session)
        if (session.endedAt != null) {
            logEvent("SESSION_ENDED", session.nodeId)
        }
    }

    fun getAllTrackEntries(): Flow<List<TrackEntryEntity>> = trackDao.getAllTrackEntries()
    suspend fun insertTrackEntry(entry: TrackEntryEntity) {
        trackDao.insertTrackEntry(entry)
        logEvent("CHECKIN_CREATED")
    }

    // Relations
    fun getAllRelations() = relationDao.getAllRelations()
    fun getRelationsForNode(nodeId: Long) = relationDao.getRelationsForNode(nodeId)
    suspend fun insertRelation(relation: RelationEntity) {
        if (!relationDao.anyRelationExists(relation.fromNodeId, relation.toNodeId)) {
            relationDao.insertRelation(relation)
            logEvent("NODE_LINKED", relation.fromNodeId, relation.toNodeId)
        }
    }
    suspend fun deleteRelation(relation: RelationEntity) = relationDao.deleteRelation(relation)

    // Tags
    fun getAllTags() = tagDao.getAllTags()
    fun getTagsForNode(nodeId: Long) = tagDao.getTagsForNode(nodeId)
    suspend fun insertTag(tag: TagEntity) = tagDao.insertTag(tag)
    suspend fun attachTagToNode(nodeId: Long, tagId: Long) = tagDao.attachTagToNode(NodeTagEntity(nodeId, tagId))
    suspend fun detachTagFromNode(nodeId: Long, tagId: Long) = tagDao.detachTagFromNode(nodeId, tagId)

    // Log
    fun getRecentLogs(limit: Int = 100) = eventLogDao.getRecentLogs(limit)
    fun getLogsForNode(nodeId: Long) = eventLogDao.getLogsForNode(nodeId)
    private suspend fun logEvent(type: String, nodeId: Long? = null, relatedNodeId: Long? = null) {
        eventLogDao.insertLog(EventLogEntity(eventType = type, nodeId = nodeId, relatedNodeId = relatedNodeId))
    }

    // Attachments
    fun getAttachmentsForNode(nodeId: Long) = attachmentDao.getAttachmentsForNode(nodeId)
    suspend fun insertAttachment(attachment: AttachmentEntity) = attachmentDao.insertAttachment(attachment)
    suspend fun deleteAttachment(attachment: AttachmentEntity) = attachmentDao.deleteAttachment(attachment)

    // Templates
    fun getAllTemplates() = templateDao.getAllTemplates()
    suspend fun insertTemplate(template: TemplateEntity) = templateDao.insertTemplate(template)
    suspend fun updateTemplate(template: TemplateEntity) = templateDao.updateTemplate(template)
    suspend fun deleteTemplate(template: TemplateEntity) = templateDao.deleteTemplate(template)
}
