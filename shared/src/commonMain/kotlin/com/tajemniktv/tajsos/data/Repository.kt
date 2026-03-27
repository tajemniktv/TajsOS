/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    private val nodeSnapshotDao: NodeSnapshotDao,
    private val reviewDao: ReviewDao,
    private val calendarProviderDao: CalendarProviderDao,
    private val calendarEventDao: CalendarEventDao,
    private val modeDao: ModeDao,
    private val protocolDao: ProtocolDao,
    private val decisionDao: DecisionDao,
    private val userDao: UserDao,
    private val medicationDao: MedicationDao,
) {
    /**
     * Retrieves a stream of all nodes stored in the database, including their today-pin status.
     *
     * @return A Flow emitting a list of [NodeWithPin] objects.
     */
    fun getAllNodes(): Flow<List<NodeWithPin>> = nodeDao.getAllNodesWithPins()

    /**
     * Retrieves a stream of nodes that are assigned to the current local date.
     *
     * @return A Flow emitting a list of [NodeEntity] objects for today.
     */
    fun getTodayNodes(): Flow<List<NodeEntity>> {
        val today =
            kotlin.time.Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString()
        return nodeDao.getTodayNodes(today)
    }

    /**
     * Retrieves a single node by its ID.
     *
     * @param id The ID of the node to retrieve.
     * @return The [NodeEntity] if found, or `null` otherwise.
     */
    suspend fun getNodeById(id: Long): NodeEntity? = nodeDao.getNodeById(id)

    // ... existing methods ...

    // Calendar
    fun getAllCalendarProviders() = calendarProviderDao.getAllProviders()

    suspend fun insertCalendarProvider(provider: CalendarProviderEntity) = calendarProviderDao.insertProvider(provider)

    suspend fun updateCalendarProvider(provider: CalendarProviderEntity) = calendarProviderDao.updateProvider(provider)

    suspend fun deleteCalendarProvider(provider: CalendarProviderEntity) = calendarProviderDao.deleteProvider(provider)

    fun getCalendarEventsInRange(
        from: Long,
        to: Long,
    ) = calendarEventDao.getEventsInRange(from, to)

    suspend fun insertCalendarEvents(events: List<CalendarEventEntity>) = calendarEventDao.insertEvents(events)

    suspend fun deleteCalendarEventsByProvider(providerId: Long) = calendarEventDao.deleteEventsByProvider(providerId)

    /**
     * Inserts a new node into the database.
     *
     * **Side effects:**
     * - Logs a "NODE_CREATED" event.
     * - Synchronizes "BELONGS_TO" relations for the node's associated project and area.
     *
     * @param node The node entity to insert.
     * @return The auto-generated ID of the newly inserted node.
     */
    suspend fun insertNode(node: NodeEntity): Long {
        val id = nodeDao.insertNode(node)
        logEvent("NODE_CREATED", id)
        syncBelongsToRelations(id, node.projectId, node.areaId)
        return id
    }

    /**
     * Updates an existing node in the database.
     *
     * **Side effects:**
     * - Logs a "NODE_COMPLETED" event if the status changes to "done".
     * - Logs a "NODE_ARCHIVED" event if the status changes to "archived".
     * - Logs "NODE_FROZEN" or "NODE_UNFROZEN" if the frozen state changes.
     * - Synchronizes "BELONGS_TO" relations if the node's projectId or areaId changes.
     *
     * @param node The updated node entity to save.
     */
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

            if (oldNode.projectId != node.projectId || oldNode.areaId != node.areaId) {
                syncBelongsToRelations(node.id, node.projectId, node.areaId)
            }
        } else {
            syncBelongsToRelations(node.id, node.projectId, node.areaId)
        }
    }

    private suspend fun syncBelongsToRelations(
        nodeId: Long,
        projectId: Long?,
        areaId: Long?,
    ) {
        relationDao.deleteBelongsToRelations(nodeId)
        if (projectId != null) {
            relationDao.insertRelation(
                RelationEntity(
                    fromNodeId = nodeId,
                    toNodeId = projectId,
                    relationType = "BELONGS_TO",
                ),
            )
        }
        if (areaId != null) {
            relationDao.insertRelation(
                RelationEntity(
                    fromNodeId = nodeId,
                    toNodeId = areaId,
                    relationType = "BELONGS_TO",
                ),
            )
        }
    }

    /**
     * Permanently deletes a node from the database.
     *
     * @param node The node to delete.
     */
    suspend fun deleteNode(node: NodeEntity) = nodeDao.deleteNode(node)

    /**
     * Pins a node to the current day ("Today" view) by creating a [TodayPinEntity].
     *
     * **Side effects:**
     * - Logs a "TODAY_ASSIGNED" event.
     *
     * @param nodeId The ID of the node to pin.
     */
    suspend fun pinToToday(nodeId: Long) {
        val today =
            kotlin.time.Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString()
        nodeDao.pinToToday(TodayPinEntity(nodeId = nodeId, date = today, position = 0))
        logEvent("TODAY_ASSIGNED", nodeId)
    }

    /**
     * Removes a node's pin from the "Today" view.
     *
     * @param nodeId The ID of the node to unpin.
     */
    suspend fun unpinFromToday(nodeId: Long) = nodeDao.unpinFromToday(nodeId)

    /**
     * Observes whether a specific node is pinned to today.
     *
     * @param nodeId The ID of the node to check.
     * @return A Flow emitting true if the node is pinned to today, false otherwise.
     */
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

    suspend fun insertTrackEntry(entry: TrackEntryEntity): Long {
        val id = trackDao.insertTrackEntry(entry)
        logEvent("CHECKIN_CREATED")
        return id
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

    suspend fun attachTagToNode(
        nodeId: Long,
        tagId: Long,
    ) = tagDao.attachTagToNode(NodeTagEntity(nodeId, tagId))

    suspend fun detachTagFromNode(
        nodeId: Long,
        tagId: Long,
    ) = tagDao.detachTagFromNode(nodeId, tagId)

    // Log
    fun getRecentLogs(limit: Int = 100) = eventLogDao.getRecentLogs(limit)

    fun getLogsForNode(nodeId: Long) = eventLogDao.getLogsForNode(nodeId)

    private suspend fun logEvent(
        type: String,
        nodeId: Long? = null,
        relatedNodeId: Long? = null,
    ) {
        eventLogDao.insertLog(
            EventLogEntity(
                eventType = type,
                nodeId = nodeId,
                relatedNodeId = relatedNodeId,
            ),
        )
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

    // Snapshots
    fun getSnapshotsForNode(nodeId: Long) = nodeSnapshotDao.getSnapshotsForNode(nodeId)

    suspend fun insertSnapshot(snapshot: NodeSnapshotEntity) = nodeSnapshotDao.insertSnapshot(snapshot)

    suspend fun deleteSnapshot(snapshot: NodeSnapshotEntity) = nodeSnapshotDao.deleteSnapshot(snapshot)

    // Reviews
    fun getAllReviews() = reviewDao.getAllReviews()

    suspend fun insertReview(review: ReviewEntity) = reviewDao.insertReview(review)

    suspend fun getLastReviewByType(type: String) = reviewDao.getLastReviewByType(type)

    // Operating Modes
    fun getAllModes() = modeDao.getAllModes()

    suspend fun insertMode(mode: ModeEntity) = modeDao.insertMode(mode)

    suspend fun updateMode(mode: ModeEntity) = modeDao.updateMode(mode)

    fun getPreferencesForMode(modeId: Long) = modeDao.getPreferencesForMode(modeId)

    suspend fun insertPreference(preference: ModePreferenceEntity) = modeDao.insertPreference(preference)

    fun getAreaFiltersForMode(modeId: Long) = modeDao.getAreaFiltersForMode(modeId)

    suspend fun insertAreaFilter(filter: ModeAreaFilterEntity) = modeDao.insertAreaFilter(filter)

    fun getTypeFiltersForMode(modeId: Long) = modeDao.getTypeFiltersForMode(modeId)

    suspend fun insertTypeFilter(filter: ModeTypeFilterEntity) = modeDao.insertTypeFilter(filter)

    fun getAllModeUsageLogs() = modeDao.getAllUsageLogs()

    suspend fun insertModeUsageLog(log: ModeUsageLogEntity) = modeDao.insertUsageLog(log)

    suspend fun deactivateModeUsageLog(
        id: Long,
        timestamp: Long,
    ) = modeDao.deactivateLog(id, timestamp)

    fun getModeQueryProfile(modeId: Long): Flow<ModeQueryProfile?> =
        getPreferencesForMode(modeId).map { preference ->
            if (preference == null) return@map null
            val areaFilters = getAreaFiltersForMode(modeId).first()
            val typeFilters = getTypeFiltersForMode(modeId).first()
            buildModeQueryProfile(
                preference = preference,
                areaFilters = areaFilters,
                typeFilters = typeFilters,
            )
        }

    // Protocols
    fun getAllProtocolHistory() = protocolDao.getAllProtocolHistory()

    suspend fun insertProtocolHistory(history: ProtocolHistoryEntity) = protocolDao.insertProtocolHistory(history)

    // Decisions
    fun getDecisionsByStatus(status: String): Flow<List<NodeEntity>> =
        nodeDao.getNodesByType("decision").map { nodes ->
            nodes.filter { it.decisionStatus == status }
        }

    fun getDecisionInbox(): Flow<List<NodeEntity>> =
        nodeDao.getNodesByType("decision").map { nodes ->
            nodes.filter { it.inboxState }
        }

    suspend fun decideOn(
        nodeId: Long,
        outcome: String,
        selectedOptionId: Long? = null,
    ) {
        val node = nodeDao.getNodeById(nodeId) ?: return
        val options = decisionDao.getOptionsForDecision(nodeId).first()

        options.forEach { option ->
            if (option.id == selectedOptionId) {
                decisionDao.updateDecisionOption(option.copy(isSelected = true))
            } else if (option.isSelected) {
                decisionDao.updateDecisionOption(option.copy(isSelected = false))
            }
        }

        nodeDao.updateNode(
            node.copy(
                decisionStatus = "decided",
                decisionOutcome = outcome,
                status = "done",
                completedAt =
                    kotlin.time.Clock.System
                        .now()
                        .toEpochMilliseconds(),
                inboxState = false,
                updatedAt =
                    kotlin.time.Clock.System
                        .now()
                        .toEpochMilliseconds(),
            ),
        )
    }

    suspend fun convertDecisionToProject(nodeId: Long): Long {
        val node = nodeDao.getNodeById(nodeId) ?: return -1
        val newProject =
            NodeEntity(
                type = "project",
                title = "Action Plan: ${node.title}",
                content = "Derived from decision: ${node.decisionOutcome ?: node.content}",
                areaId = node.areaId,
                status = "active",
                inboxState = true,
            )
        val projectId = nodeDao.insertNode(newProject)

        insertRelation(
            RelationEntity(
                fromNodeId = nodeId,
                toNodeId = projectId,
                relationType = "DERIVED_FROM",
            ),
        )

        return projectId
    }

    suspend fun convertDecisionToTask(nodeId: Long): Long {
        val node = nodeDao.getNodeById(nodeId) ?: return -1
        val newTask =
            NodeEntity(
                type = "task",
                title = "Follow-up: ${node.title}",
                content = "Outcome: ${node.decisionOutcome ?: ""}\n\n${node.content}",
                areaId = node.areaId,
                projectId = node.projectId,
                status = "active",
                inboxState = true,
            )
        val taskId = nodeDao.insertNode(newTask)

        insertRelation(
            RelationEntity(
                fromNodeId = nodeId,
                toNodeId = taskId,
                relationType = "DERIVED_FROM",
            ),
        )

        return taskId
    }

    fun getOptionsForDecision(nodeId: Long) = decisionDao.getOptionsForDecision(nodeId)

    suspend fun insertDecisionOption(option: DecisionOptionEntity) = decisionDao.insertDecisionOption(option)

    suspend fun updateDecisionOption(option: DecisionOptionEntity) = decisionDao.updateDecisionOption(option)

    suspend fun deleteDecisionOption(option: DecisionOptionEntity) = decisionDao.deleteDecisionOption(option)

    fun getUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    fun getAllMedications(): Flow<List<MedicationEntity>> = medicationDao.getAllMedications()

    suspend fun insertMedication(medication: MedicationEntity): Long = medicationDao.insertMedication(medication)

    suspend fun updateMedication(medication: MedicationEntity) = medicationDao.updateMedication(medication)

    suspend fun deleteMedication(medication: MedicationEntity) = medicationDao.deleteMedication(medication)

    suspend fun getTrackEntryByDate(date: String): TrackEntryEntity? = trackDao.getTrackEntryByDate(date)

    suspend fun insertTrackMedication(join: TrackMedicationJoinEntity) = trackDao.insertTrackMedication(join)

    fun getTrackMedications(trackEntryId: Long): Flow<List<TrackMedicationJoinEntity>> = trackDao.getTrackMedications(trackEntryId)

    suspend fun buildExportBundle(
        enabledPacks: Set<String> = emptySet(),
        recentEventLimit: Int = 500,
    ): ExportBundle =
        ExportBundle(
            exportedAt =
                kotlin.time.Clock.System
                    .now()
                    .toEpochMilliseconds(),
            enabledPacks = enabledPacks,
            nodes = getAllNodes().first().map { it.node },
            relations = getAllRelations().first(),
            tags = getAllTags().first(),
            templates = getAllTemplates().first(),
            reviews = getAllReviews().first(),
            tracks = getAllTrackEntries().first(),
            sessions = getAllSessions().first(),
            calendars = calendarEventDao.getEventsInRange(0, Long.MAX_VALUE).first(),
            providers = getAllCalendarProviders().first(),
            recentEvents = getRecentLogs(limit = recentEventLimit).first(),
        )

    suspend fun importBundle(bundle: ExportBundle): ImportReport {
        bundle.nodes.forEach { nodeDao.insertNode(it) }
        bundle.relations.forEach { relationDao.insertRelation(it) }
        bundle.tags.forEach { tagDao.insertTag(it) }
        bundle.templates.forEach { templateDao.insertTemplate(it) }
        bundle.reviews.forEach { reviewDao.insertReview(it) }
        bundle.tracks.forEach { trackDao.insertTrackEntry(it) }
        bundle.sessions.forEach { focusSessionDao.insertSession(it) }
        bundle.providers.forEach { calendarProviderDao.insertProvider(it) }
        if (bundle.calendars.isNotEmpty()) {
            calendarEventDao.insertEvents(bundle.calendars)
        }
        bundle.recentEvents.forEach { eventLogDao.insertLog(it) }

        return ImportReport(
            nodes = bundle.nodes.size,
            relations = bundle.relations.size,
            tracks = bundle.tracks.size,
            sessions = bundle.sessions.size,
            events = bundle.recentEvents.size,
        )
    }

    suspend fun importLegacyNodes(nodes: List<NodeEntity>): Int {
        nodes.forEach { nodeDao.insertNode(it) }
        return nodes.size
    }
}

data class ImportReport(
    val nodes: Int,
    val relations: Int,
    val tracks: Int,
    val sessions: Int,
    val events: Int,
)
