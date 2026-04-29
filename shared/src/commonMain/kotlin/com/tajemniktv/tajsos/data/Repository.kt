/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.data

import com.tajemniktv.tajsos.domain.DomainKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * AppRepository is the single source of truth for TajsOS's Room database.
 */
private object NoOpInboxEntryDao : InboxEntryDao {
    override fun getAllInboxEntries(): Flow<List<InboxEntryEntity>> = flowOf(emptyList())

    override fun getActiveInboxEntries(): Flow<List<InboxEntryEntity>> = flowOf(emptyList())

    override suspend fun getInboxEntryById(id: Long): InboxEntryEntity? = null

    override suspend fun insertInboxEntry(entry: InboxEntryEntity): Long = 0L

    override suspend fun updateInboxEntry(entry: InboxEntryEntity) = Unit
}

private object NoOpTaskFacetDao : TaskFacetDao {
    override fun getAllTaskFacets(): Flow<List<TaskFacetEntity>> = flowOf(emptyList())

    override suspend fun getTaskFacetByItemId(itemId: Long): TaskFacetEntity? = null

    override fun observeTaskFacet(itemId: Long): Flow<TaskFacetEntity?> = flowOf(null)

    override suspend fun upsertTaskFacet(facet: TaskFacetEntity) = Unit

    override suspend fun deleteTaskFacetForItem(itemId: Long) = Unit
}

private object NoOpNoteFacetDao : NoteFacetDao {
    override fun getAllNoteFacets(): Flow<List<NoteFacetEntity>> = flowOf(emptyList())

    override suspend fun getNoteFacetByItemId(itemId: Long): NoteFacetEntity? = null

    override fun observeNoteFacet(itemId: Long): Flow<NoteFacetEntity?> = flowOf(null)

    override suspend fun upsertNoteFacet(facet: NoteFacetEntity) = Unit

    override suspend fun deleteNoteFacetForItem(itemId: Long) = Unit
}

private object NoOpProjectFacetDao : ProjectFacetDao {
    override fun getAllProjectFacets(): Flow<List<ProjectFacetEntity>> = flowOf(emptyList())

    override suspend fun getProjectFacetByItemId(itemId: Long): ProjectFacetEntity? = null

    override fun observeProjectFacet(itemId: Long): Flow<ProjectFacetEntity?> = flowOf(null)

    override suspend fun upsertProjectFacet(facet: ProjectFacetEntity) = Unit

    override suspend fun deleteProjectFacetForItem(itemId: Long) = Unit
}

private object NoOpAreaFacetDao : AreaFacetDao {
    override fun getAllAreaFacets(): Flow<List<AreaFacetEntity>> = flowOf(emptyList())

    override suspend fun getAreaFacetByItemId(itemId: Long): AreaFacetEntity? = null

    override fun observeAreaFacet(itemId: Long): Flow<AreaFacetEntity?> = flowOf(null)

    override suspend fun upsertAreaFacet(facet: AreaFacetEntity) = Unit

    override suspend fun deleteAreaFacetForItem(itemId: Long) = Unit
}

private object NoOpRecordFacetDao : RecordFacetDao {
    override fun getAllRecordFacets(): Flow<List<RecordFacetEntity>> = flowOf(emptyList())

    override suspend fun getRecordFacetByItemId(itemId: Long): RecordFacetEntity? = null

    override fun observeRecordFacet(itemId: Long): Flow<RecordFacetEntity?> = flowOf(null)

    override suspend fun upsertRecordFacet(facet: RecordFacetEntity) = Unit

    override suspend fun deleteRecordFacetForItem(itemId: Long) = Unit
}

private object NoOpItemDomainDao : ItemDomainDao {
    override fun getAllItemDomains(): Flow<List<ItemDomainEntity>> = flowOf(emptyList())

    override fun getDomainsForItem(itemId: Long): Flow<List<ItemDomainEntity>> = flowOf(emptyList())

    override suspend fun upsertDomain(domain: ItemDomainEntity) = Unit

    override suspend fun deleteDomain(
        itemId: Long,
        domainKey: String,
    ) = Unit

    override suspend fun deleteDomainsForItem(itemId: Long) = Unit

    override suspend fun clearPrimaryFlag(itemId: Long) = Unit
}

private object NoOpRichContentDocumentDao : RichContentDocumentDao {
    override fun getAllDocuments(): Flow<List<RichContentDocumentEntity>> = flowOf(emptyList())

    override fun observeDocumentForItem(itemId: Long): Flow<RichContentDocumentEntity?> = flowOf(null)

    override suspend fun getDocumentForItem(itemId: Long): RichContentDocumentEntity? = null

    override suspend fun upsertDocument(document: RichContentDocumentEntity) = Unit

    override suspend fun deleteDocumentForItem(itemId: Long) = Unit
}

private object NoOpScheduleEntryDao : ScheduleEntryDao {
    override fun getAllScheduleEntries(): Flow<List<ScheduleEntryEntity>> = flowOf(emptyList())

    override fun getScheduleEntriesForItem(itemId: Long): Flow<List<ScheduleEntryEntity>> = flowOf(emptyList())

    override fun getOpenScheduleEntriesByKindAndDayRange(
        kind: String,
        fromEpochDay: Int,
        toEpochDay: Int,
    ): Flow<List<ScheduleEntryEntity>> = flowOf(emptyList())

    override suspend fun getScheduleEntriesByKind(
        itemId: Long,
        kind: String,
    ): List<ScheduleEntryEntity> = emptyList()

    override suspend fun deleteScheduleEntriesByKind(
        itemId: Long,
        kind: String,
    ) = Unit

    override suspend fun deleteScheduleEntriesForItem(itemId: Long) = Unit

    override suspend fun insertScheduleEntry(entry: ScheduleEntryEntity): Long = 0L

    override suspend fun insertScheduleEntries(entries: List<ScheduleEntryEntity>) = Unit
}

private object NoOpSavedViewDao : SavedViewDao {
    override fun getAllSavedViews(): Flow<List<SavedViewEntity>> = flowOf(emptyList())

    override suspend fun getSavedViewById(id: Long): SavedViewEntity? = null

    override suspend fun insertSavedView(view: SavedViewEntity): Long = 0L

    override suspend fun updateSavedView(view: SavedViewEntity) = Unit

    override suspend fun deleteSavedView(view: SavedViewEntity) = Unit

    override fun getAllSavedViewSourceKinds(): Flow<List<SavedViewSourceKindEntity>> = flowOf(emptyList())

    override suspend fun getSourceKindsForView(viewId: Long): List<SavedViewSourceKindEntity> = emptyList()

    override suspend fun insertSavedViewSourceKinds(sourceKinds: List<SavedViewSourceKindEntity>) = Unit

    override suspend fun deleteSourceKindsForView(viewId: Long) = Unit

    override fun getAllSavedViewFilters(): Flow<List<SavedViewFilterEntity>> = flowOf(emptyList())

    override suspend fun getFiltersForView(viewId: Long): List<SavedViewFilterEntity> = emptyList()

    override suspend fun insertSavedViewFilters(filters: List<SavedViewFilterEntity>) = Unit

    override suspend fun deleteFiltersForView(viewId: Long) = Unit

    override fun getAllSavedViewSorts(): Flow<List<SavedViewSortEntity>> = flowOf(emptyList())

    override suspend fun getSortsForView(viewId: Long): List<SavedViewSortEntity> = emptyList()

    override suspend fun insertSavedViewSorts(sorts: List<SavedViewSortEntity>) = Unit

    override suspend fun deleteSortsForView(viewId: Long) = Unit

    override fun getAllSavedViewVisibleFields(): Flow<List<SavedViewVisibleFieldEntity>> = flowOf(emptyList())

    override suspend fun getVisibleFieldsForView(viewId: Long): List<SavedViewVisibleFieldEntity> = emptyList()

    override suspend fun insertSavedViewVisibleFields(fields: List<SavedViewVisibleFieldEntity>) = Unit

    override suspend fun deleteVisibleFieldsForView(viewId: Long) = Unit
}

private data class LifeObjectFacetSnapshot(
    val task: TaskFacetEntity?,
    val note: NoteFacetEntity?,
    val record: RecordFacetEntity?,
    val project: ProjectFacetEntity?,
    val area: AreaFacetEntity?,
)

/**
 * AppRepository is the central coordinator for all local persistence.
 *
 * It acts as the single source of truth for the Room database, combining multiple DAOs
 * (such as [NodeDao], [FocusSessionDao], and various facet DAOs) to seamlessly manage
 * core LifeOS entities (`NodeEntity`) alongside their typed extension states (`TaskFacetEntity`,
 * `NoteFacetEntity`, etc.). This pattern avoids maintaining a single massive nullable row
 * and safely orchestrates creation, updates, and complex queries across related database tables.
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
    private val inboxEntryDao: InboxEntryDao = NoOpInboxEntryDao,
    private val taskFacetDao: TaskFacetDao = NoOpTaskFacetDao,
    private val noteFacetDao: NoteFacetDao = NoOpNoteFacetDao,
    private val projectFacetDao: ProjectFacetDao = NoOpProjectFacetDao,
    private val areaFacetDao: AreaFacetDao = NoOpAreaFacetDao,
    private val recordFacetDao: RecordFacetDao = NoOpRecordFacetDao,
    private val itemDomainDao: ItemDomainDao = NoOpItemDomainDao,
    private val richContentDocumentDao: RichContentDocumentDao = NoOpRichContentDocumentDao,
    private val scheduleEntryDao: ScheduleEntryDao = NoOpScheduleEntryDao,
    private val savedViewDao: SavedViewDao = NoOpSavedViewDao,
) {
    /**
     * Retrieves a stream of all nodes stored in the database, including their today-pin status.
     *
     * @return A Flow emitting a list of [NodeWithPin] objects.
     */
    fun getAllNodes(): Flow<List<NodeWithPin>> = nodeDao.getAllNodesWithPins()

    /**
     * Retrieves a stream of active nodes that are assigned to the current local date.
     *
     * @return A Flow emitting a list of active [NodeEntity] objects for today.
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

    /**
     * Observes a typed local aggregate for a specific life object.
     *
     * This read model keeps the core node spine intact while joining typed facets,
     * schedules, relations, documents, and domain assignments beside it.
     */
    fun observeLifeObject(id: Long): Flow<LifeObjectAggregate?> {
        val facetsFlow =
            combine(
                taskFacetDao.observeTaskFacet(id),
                noteFacetDao.observeNoteFacet(id),
                recordFacetDao.observeRecordFacet(id),
                projectFacetDao.observeProjectFacet(id),
                areaFacetDao.observeAreaFacet(id),
            ) { task, note, record, project, area ->
                LifeObjectFacetSnapshot(
                    task = task,
                    note = note,
                    record = record,
                    project = project,
                    area = area,
                )
            }

        val schedulingAndDocumentsFlow =
            combine(
                scheduleEntryDao.getScheduleEntriesForItem(id),
                richContentDocumentDao.observeDocumentForItem(id),
                itemDomainDao.getDomainsForItem(id),
            ) { schedule, document, domains ->
                Triple(schedule, document, domains)
            }

        val linksFlow =
            combine(
                tagDao.getTagsForNode(id),
                attachmentDao.getAttachmentsForNode(id),
                relationDao.getRelationsForNode(id),
            ) { tags, attachments, relations ->
                Triple(tags, attachments, relations)
            }

        return combine(
            getAllNodes(),
            facetsFlow,
            schedulingAndDocumentsFlow,
            linksFlow,
        ) { nodes, facets, schedulingAndDocuments, links ->
            val nodeEntity = nodes.firstOrNull { it.node.id == id }?.node ?: return@combine null
            LifeObjectAggregate(
                node = nodeEntity,
                task = facets.task?.toModel(),
                note = facets.note?.toModel(),
                record = facets.record?.toModel(),
                project = facets.project?.toModel(),
                area = facets.area?.toModel(),
                schedule = schedulingAndDocuments.first.map { it.toModel() },
                document = schedulingAndDocuments.second?.toModel(),
                domains = schedulingAndDocuments.third.mapNotNull { it.toModel() },
                tags = links.first,
                attachments = links.second,
                relations = links.third,
            )
        }
    }

    /**
     * Loads the current typed local aggregate for a single life object.
     */
    suspend fun getLifeObject(id: Long): LifeObjectAggregate? = observeLifeObject(id).first()

    /**
     * Observes raw inbox captures that still need semantic triage.
     */
    fun getAllInboxEntries(): Flow<List<InboxEntryEntity>> = inboxEntryDao.getAllInboxEntries()

    /**
     * Observes raw inbox captures that still need semantic triage.
     */
    fun getActiveInboxEntries(): Flow<List<InboxEntryEntity>> = inboxEntryDao.getActiveInboxEntries()

    /**
     * Stores a raw capture entry before it becomes a typed life object.
     * Raw captures sit in an unprocessed state until semantic triage turns them into actionable items.
     *
     * @return The auto-generated inbox entry identifier.
     */
    suspend fun captureInboxEntry(
        rawText: String,
        source: String = "manual",
        suggestedKind: ItemKind? = null,
        homeAreaId: Long? = null,
        activeProjectId: Long? = null,
        contextScreen: String? = null,
    ): Long {
        val trimmed = rawText.trim()
        if (trimmed.isBlank()) return 0L
        val entryId =
            inboxEntryDao.insertInboxEntry(
                InboxEntryEntity(
                    rawText = trimmed,
                    source = source,
                    suggestedKind = suggestedKind?.storageKey,
                    homeAreaId = homeAreaId,
                    activeProjectId = activeProjectId,
                    contextScreen = contextScreen,
                ),
            )
        logEvent("INBOX_CAPTURED")
        return entryId
    }

    /**
     * Marks a raw inbox capture as intentionally dismissed without creating an item.
     */
    suspend fun dismissInboxEntry(entry: InboxEntryEntity) {
        inboxEntryDao.updateInboxEntry(
            entry.copy(
                dismissedAt =
                    kotlin.time.Clock.System
                        .now()
                        .toEpochMilliseconds(),
            ),
        )
    }

    /**
     * Converts a raw, unstructured inbox capture into a typed, actionable life object.
     *
     * This is a critical business rule for the "capture -> triage -> execute" flow.
     * The first non-blank line of the raw text becomes the new item's title, and remaining lines become the body content.
     * This establishes the entry point for turning fleeting thoughts into durable nodes (tasks, notes, projects).
     *
     * **State and Data Flow Transitions:**
     * - The un-triaged raw capture transitions from `processedAt = null` to having a concrete processing timestamp.
     * - A new typed node is generated based on the inferred or user-selected [ItemKind].
     * - The original raw entry retains a reference (`triagedItemId`) to the newly spawned node, completing the pipeline.
     *
     * **Side effects:**
     * - Inserts the newly created typed item into the nodes table and synchronizes its associated facets, domains, and schedules.
     * - Updates the origin [InboxEntryEntity] to mark it as processed, setting the `processedAt` timestamp and linking it via `triagedItemId`.
     * - Logs an `INBOX_TRIAGED` event to the system activity log.
     *
     * @param entryId The unique ID of the raw pending inbox entry to triage.
     * @param kind The explicit system [ItemKind] to convert the raw capture into.
     * @return The generated ID of the newly inserted item, or `0L` if the entry could not be found or the parsed title is blank.
     */
    suspend fun triageInboxEntry(
        entryId: Long,
        kind: ItemKind,
    ): Long {
        val entry = inboxEntryDao.getInboxEntryById(entryId) ?: return 0L
        val parsed = parseCapturedText(entry.rawText)
        if (parsed.title.isBlank()) return 0L

        val createdId =
            insertLifeItem(
                kind = kind,
                title = parsed.title,
                content = parsed.content,
                homeAreaId = entry.homeAreaId,
                activeProjectId = entry.activeProjectId,
                source = "capture",
                inboxState = false,
            )

        inboxEntryDao.updateInboxEntry(
            entry.copy(
                processedAt =
                    kotlin.time.Clock.System
                        .now()
                        .toEpochMilliseconds(),
                triagedItemId = createdId,
            ),
        )
        logEvent("INBOX_TRIAGED", createdId)
        return createdId
    }

    /**
     * Creates a typed LifeOS item while mirroring the minimum legacy node fields still needed by current UI.
     *
     * @param kind The primary item kind (e.g., TASK, NOTE, PROJECT).
     * @param title The display title for the new item.
     * @param content Optional body content or description.
     * @param homeAreaId The optional ID of the area this item belongs to.
     * @param activeProjectId The optional ID of the project this item belongs to.
     * @param inboxState Whether this item sits in the triage inbox. Defaults based on [ItemKind].
     * @param source How the item was created (e.g., "manual", "capture").
     * @param noteKind For note items, the specific semantic subtype (e.g., JOURNAL, CONCEPT).
     * @param recordKind For record items, the specific subtype (e.g., HEALTH_LOG). Defaults to GENERAL.
     * @param taskState For task items, the execution state. Defaults to ACTIVE.
     * @param projectState For project items, the lifecycle state. Defaults to ACTIVE.
     * @param isRecurring Whether the item should spawn a new instance when completed.
     * @param recurringInterval The recurrence interval (e.g., "daily", "weekly", "monthly") if [isRecurring] is true.
     * @param reminderAt Epoch milliseconds for when the user should be reminded.
     * @param startAt Epoch milliseconds for when work on this item is scheduled to start.
     * @param dueAt Epoch milliseconds for the item's deadline or target completion.
     * @param color Optional ARGB color integer used for visual representation, mostly for projects and areas.
     * @param icon Optional icon identifier used for visual representation.
     * @param contextScreen The screen context where this item was originally created.
     * @param isSticky Whether the item should be pinned prominently in dashboards.
     * @param domains Optional product-lens classifications attached to the item.
     * @param purpose For project items, an explicit "why" or goal statement.
     * @return The auto-generated database ID of the newly inserted item.
     */
    suspend fun insertLifeItem(
        kind: ItemKind,
        title: String,
        content: String = "",
        homeAreaId: Long? = null,
        activeProjectId: Long? = null,
        inboxState: Boolean = kind.defaultInboxState(),
        source: String = "manual",
        noteKind: NoteKind? = null,
        recordKind: RecordKind? = null,
        taskState: TaskState = TaskState.ACTIVE,
        projectState: ProjectState = ProjectState.ACTIVE,
        isRecurring: Boolean = false,
        recurringInterval: String? = null,
        reminderAt: Long? = null,
        startAt: Long? = null,
        dueAt: Long? = null,
        color: Int? = null,
        icon: String? = null,
        contextScreen: String? = null,
        isSticky: Boolean = false,
        domains: Set<DomainKind> = emptySet(),
        purpose: String? = null,
    ): Long {
        val node =
            when (kind)
            {
                ItemKind.TASK -> {
                    NodeEntity(
                        type = kind.storageKey,
                        title = title,
                        content = content,
                        status = taskState.toNodeStatus(),
                        projectId = activeProjectId,
                        areaId = homeAreaId,
                        source = source,
                        inboxState = inboxState,
                        contextScreen = contextScreen,
                        isSticky = isSticky,
                        dueAt = dueAt,
                        startAt = startAt,
                        reminderAt = reminderAt,
                        isRecurring = isRecurring,
                        recurringInterval = recurringInterval,
                    )
                }

                ItemKind.NOTE -> {
                    NodeEntity(
                        type = kind.storageKey,
                        title = title,
                        content = content,
                        projectId = activeProjectId,
                        areaId = homeAreaId,
                        source = source,
                        inboxState = inboxState,
                        contextScreen = contextScreen,
                        isSticky = isSticky,
                        noteType = noteKind?.storageKey,
                    )
                }

                ItemKind.RECORD -> {
                    NodeEntity(
                        type = kind.storageKey,
                        title = title,
                        content = content,
                        projectId = activeProjectId,
                        areaId = homeAreaId,
                        source = source,
                        inboxState = inboxState,
                        contextScreen = contextScreen,
                        isSticky = isSticky,
                    )
                }

                ItemKind.PROJECT -> {
                    NodeEntity(
                        type = kind.storageKey,
                        title = title,
                        content = content,
                        status = projectState.toNodeStatus(),
                        areaId = homeAreaId,
                        source = source,
                        inboxState = inboxState,
                        color = color,
                        icon = icon,
                        projectWhy = purpose,
                        projectStatus = projectState.storageKey,
                        isSticky = isSticky,
                    )
                }

                ItemKind.AREA -> {
                    NodeEntity(
                        type = kind.storageKey,
                        title = title,
                        content = content,
                        source = source,
                        inboxState = inboxState,
                        color = color,
                        icon = icon,
                        isSticky = isSticky,
                    )
                }
            }

        val id = insertNode(node)
        domains.forEachIndexed { index, domain ->
            assignDomainToItem(
                itemId = id,
                domain = domain,
                isPrimary = index == 0,
            )
        }
        if (kind == ItemKind.RECORD) {
            recordFacetDao.upsertRecordFacet(
                RecordFacetEntity(
                    itemId = id,
                    kind = recordKind?.storageKey ?: RecordKind.GENERAL.storageKey,
                ),
            )
        }
        return id
    }

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
     * Observes all attached schedule entries across the local system.
     */
    fun getAllScheduleEntries(): Flow<List<ScheduleEntryEntity>> = scheduleEntryDao.getAllScheduleEntries()

    /**
     * Observes schedule entries attached to a specific life object.
     */
    fun getScheduleEntriesForItem(itemId: Long): Flow<List<ScheduleEntryEntity>> = scheduleEntryDao.getScheduleEntriesForItem(itemId)

    /**
     * Observes open schedule entries in a local day range for a specific schedule layer.
     */
    fun getOpenScheduleEntriesByKindAndDayRange(
        kind: ScheduleEntryKind,
        fromEpochDay: Int,
        toEpochDay: Int,
    ): Flow<List<ScheduleEntry>> =
        scheduleEntryDao
            .getOpenScheduleEntriesByKindAndDayRange(
                kind = kind.storageKey,
                fromEpochDay = fromEpochDay,
                toEpochDay = toEpochDay,
            ).map { entries -> entries.map { it.toModel() } }
    /**
     * Inserts a new node into the database.
     *
     * Handles @Upsert behaviour where it returns -1L if it's updating an existing entity instead of inserting.
     *
     * **Side effects:**
     * - Logs a "NODE_CREATED" event.
     * - Synchronizes "BELONGS_TO" relations for the node's associated project and area.
     *
     * @param node The node entity to insert.
     * @return The auto-generated ID of the newly inserted node, or the existing ID if updated.
     */
    suspend fun insertNode(node: NodeEntity): Long {
        var id = nodeDao.insertNode(node)
        if (id == -1L) id = node.id
        logEvent("NODE_CREATED", id)
        syncBelongsToRelations(id, node.projectId, node.areaId)
        syncTypedFacetsFromNode(node.copy(id = id))
        syncDomainsFromNodeMetadata(node.copy(id = id))
        syncDocumentFromNode(node.copy(id = id))
        syncScheduleEntriesForNode(
            nodeId = id,
            reminderAt = node.reminderAt,
            startAt = node.startAt,
            dueAt = node.dueAt,
            recurrenceRule = node.recurringInterval,
        )
        return id
    }

    /**
     * Inserts multiple nodes while preserving the same side effects as [insertNode].
     */
    suspend fun insertNodes(nodes: List<NodeEntity>): List<Long> = nodes.map { insertNode(it) }

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
        val oldNode = nodeDao.getNodeById(node.id) ?: return
        nodeDao.updateNode(node)

        if (oldNode.status != node.status) {
            when (node.status)
            {
                "done" -> logEvent("NODE_COMPLETED", node.id)
                "archived" -> logEvent("NODE_ARCHIVED", node.id)
            }
        }

        if (oldNode.isFrozen != node.isFrozen) {
            logEvent(if (node.isFrozen) "NODE_FROZEN" else "NODE_UNFROZEN", node.id)
        }

        if (oldNode.projectId != node.projectId || oldNode.areaId != node.areaId) {
            syncBelongsToRelations(node.id, node.projectId, node.areaId)
        }

        syncTypedFacetsFromNode(node)
        syncDomainsFromNodeMetadata(node)
        syncDocumentFromNode(node)
        syncScheduleEntriesForNode(
            nodeId = node.id,
            reminderAt = node.reminderAt,
            startAt = node.startAt,
            dueAt = node.dueAt,
            recurrenceRule = node.recurringInterval,
        )
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
     * Mirrors current node status into the typed facet tables introduced by the LifeOS redesign.
     */
    private suspend fun syncTypedFacetsFromNode(node: NodeEntity) {
        when (node.itemKindOrNull())
        {
            ItemKind.TASK -> {
                val existing = taskFacetDao.getTaskFacetByItemId(node.id)
                taskFacetDao.upsertTaskFacet(
                    TaskFacetEntity(
                        itemId = node.id,
                        state =
                            TaskState.fromStorageKey(node.status)?.storageKey
                                ?: TaskState.ACTIVE.storageKey,
                        energyLevel = node.energyLevel ?: existing?.energyLevel,
                        friction = node.friction ?: existing?.friction,
                        nextStep = node.nextSmallestStep ?: existing?.nextStep,
                        estimatedMinutes = node.estimatedMinutes ?: existing?.estimatedMinutes,
                        completionNote = node.completionNote ?: existing?.completionNote,
                        completedAt = node.completedAt ?: existing?.completedAt,
                        isRecurring = node.isRecurring,
                        recurringInterval = node.recurringInterval ?: existing?.recurringInterval,
                    ),
                )
            }

            ItemKind.NOTE -> {
                val existing = noteFacetDao.getNoteFacetByItemId(node.id)
                noteFacetDao.upsertNoteFacet(
                    NoteFacetEntity(
                        itemId = node.id,
                        kind =
                            NoteKind.fromStorageKey(node.noteType)?.storageKey
                                ?: existing?.kind
                                ?: NoteKind.GENERAL.storageKey,
                        state =
                            NoteState.fromStorageKey(node.noteState)?.storageKey
                                ?: existing?.state
                                ?: NoteState.ACTIVE.storageKey,
                        sourceTitle = existing?.sourceTitle,
                        sourceAuthor = existing?.sourceAuthor,
                        lastReviewedAt = existing?.lastReviewedAt,
                    ),
                )
            }

            ItemKind.PROJECT -> {
                val existing = projectFacetDao.getProjectFacetByItemId(node.id)
                projectFacetDao.upsertProjectFacet(
                    ProjectFacetEntity(
                        itemId = node.id,
                        state =
                            node.projectStatus ?: existing?.state
                                ?: ProjectState.ACTIVE.storageKey,
                        purpose =
                            node.projectWhy ?: existing?.purpose
                                ?: node.content.ifBlank { null },
                        isFrozen = node.isFrozen,
                    ),
                )
            }

            ItemKind.AREA -> {
                val existing = areaFacetDao.getAreaFacetByItemId(node.id)
                areaFacetDao.upsertAreaFacet(
                    AreaFacetEntity(
                        itemId = node.id,
                        healthStatus =
                            AreaHealthStatus.fromStorageKey(node.areaHealthStatus)?.storageKey
                                ?: existing?.healthStatus
                                ?: AreaHealthStatus.STABLE.storageKey,
                        standardOfCare = existing?.standardOfCare,
                        vision = existing?.vision,
                    ),
                )
            }

            ItemKind.RECORD -> {
                val existing = recordFacetDao.getRecordFacetByItemId(node.id)
                recordFacetDao.upsertRecordFacet(
                    RecordFacetEntity(
                        itemId = node.id,
                        kind = existing?.kind ?: RecordKind.GENERAL.storageKey,
                        occurredAt = existing?.occurredAt ?: node.createdAt,
                    ),
                )
            }

            else -> {
                Unit
            }
        }
    }

    /**
     * Mirrors optional area/domain metadata into the dedicated domain-assignment table.
     *
     * The metadata envelope remains available for pack-specific extensions, but domain lenses
     * now have a first-class SQL surface.
     */
    private suspend fun syncDomainsFromNodeMetadata(node: NodeEntity) {
        val associatedDomains = node.areaMetadataOrNull()?.associatedDomains ?: return
        itemDomainDao.deleteDomainsForItem(node.id)
        associatedDomains.forEachIndexed { index, domain ->
            itemDomainDao.upsertDomain(
                ItemDomainEntity(
                    itemId = node.id,
                    domainKey = domain.name,
                    isPrimary = index == 0,
                ),
            )
        }
    }

    /**
     * Mirrors the current node body into the optional document layer for document-friendly kinds.
     */
    private suspend fun syncDocumentFromNode(node: NodeEntity) {
        if (node.itemKindOrNull() == null) return
        val existing = richContentDocumentDao.getDocumentForItem(node.id)
        if (node.content.isBlank() && existing == null) return

        richContentDocumentDao.upsertDocument(
            RichContentDocumentEntity(
                itemId = node.id,
                format = existing?.format ?: RichContentFormat.MARKDOWN.storageKey,
                body = node.content,
                structuredContentJson = existing?.structuredContentJson,
                schemaVersion = existing?.schemaVersion ?: 1,
                updatedAt =
                    kotlin.time.Clock.System
                        .now()
                        .toEpochMilliseconds(),
            ),
        )
    }

    /**
     * Persists attach-able schedule state while current UI still reads mirrored node time fields.
     */
    private suspend fun syncScheduleEntriesForNode(
        nodeId: Long,
        reminderAt: Long?,
        startAt: Long?,
        dueAt: Long?,
        recurrenceRule: String?,
    ) {
        val entries = mutableListOf<ScheduleEntryEntity>()
        val timezone = TimeZone.currentSystemDefault()
        val timezoneId = timezone.id
        if (startAt != null) {
            scheduleEntryDao.deleteScheduleEntriesByKind(nodeId, ScheduleEntryKind.START.storageKey)
            entries +=
                ScheduleEntryEntity(
                    itemId = nodeId,
                    kind = ScheduleEntryKind.START.storageKey,
                    scheduledAt = startAt,
                    localDateEpochDay = epochDayFromInstant(startAt, timezone),
                    timezoneId = timezoneId,
                    recurrenceRule = recurrenceRule,
                )
        }
        if (dueAt != null) {
            scheduleEntryDao.deleteScheduleEntriesByKind(nodeId, ScheduleEntryKind.DUE.storageKey)
            entries +=
                ScheduleEntryEntity(
                    itemId = nodeId,
                    kind = ScheduleEntryKind.DUE.storageKey,
                    scheduledAt = dueAt,
                    localDateEpochDay = epochDayFromInstant(dueAt, timezone),
                    timezoneId = timezoneId,
                    recurrenceRule = recurrenceRule,
                )
        }
        if (reminderAt != null) {
            scheduleEntryDao.deleteScheduleEntriesByKind(
                nodeId,
                ScheduleEntryKind.REMINDER.storageKey,
            )
            entries +=
                ScheduleEntryEntity(
                    itemId = nodeId,
                    kind = ScheduleEntryKind.REMINDER.storageKey,
                    scheduledAt = reminderAt,
                    localDateEpochDay = epochDayFromInstant(reminderAt, timezone),
                    timezoneId = timezoneId,
                    recurrenceRule = recurrenceRule,
                )
        }
        if (startAt == null) {
            scheduleEntryDao.deleteScheduleEntriesByKind(
                nodeId,
                ScheduleEntryKind.START.storageKey,
            )
        }
        if (dueAt == null) {
            scheduleEntryDao.deleteScheduleEntriesByKind(
                nodeId,
                ScheduleEntryKind.DUE.storageKey,
            )
        }
        if (reminderAt == null) {
            scheduleEntryDao.deleteScheduleEntriesByKind(
                nodeId,
                ScheduleEntryKind.REMINDER.storageKey,
            )
        }
        if (entries.isNotEmpty()) {
            scheduleEntryDao.insertScheduleEntries(entries)
        }
    }

    private fun epochDayFromInstant(
        timestamp: Long,
        timezone: TimeZone,
    ): Int =
        LocalDate(1970, 1, 1).daysUntil(
            Instant
                .fromEpochMilliseconds(timestamp)
                .toLocalDateTime(timezone)
                .date,
        )

    /**
     * Permanently deletes a node from the database.
     *
     * @param node The node to delete.
     */
    suspend fun deleteNode(node: NodeEntity) {
        relationDao.deleteRelationsForNode(node.id)
        tagDao.detachAllTagsFromNode(node.id)
        attachmentDao.deleteAttachmentsForNode(node.id)
        taskFacetDao.deleteTaskFacetForItem(node.id)
        noteFacetDao.deleteNoteFacetForItem(node.id)
        projectFacetDao.deleteProjectFacetForItem(node.id)
        areaFacetDao.deleteAreaFacetForItem(node.id)
        recordFacetDao.deleteRecordFacetForItem(node.id)
        itemDomainDao.deleteDomainsForItem(node.id)
        richContentDocumentDao.deleteDocumentForItem(node.id)
        scheduleEntryDao.deleteScheduleEntriesForItem(node.id)
        nodeDao.deleteNode(node)
    }

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
        var id = focusSessionDao.insertSession(session)
        if (id == -1L) id = session.id
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
        var id = trackDao.insertTrackEntry(entry)
        if (id == -1L) id = entry.id
        logEvent("CHECKIN_CREATED")
        return id
    }

    // Relations
    fun getAllRelations() = relationDao.getAllRelations()

    fun getRelationsForNode(nodeId: Long) = relationDao.getRelationsForNode(nodeId)

    suspend fun insertRelation(relation: RelationEntity) {
        if (!relationDao.anyRelationExists(
                relation.fromNodeId,
                relation.toNodeId,
                relation.relationType,
            )
        ) {
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

    // Domains
    fun getDomainsForItem(itemId: Long): Flow<List<DomainAssignment>> =
        itemDomainDao.getDomainsForItem(itemId).map { domains ->
            domains.mapNotNull { it.toModel() }
        }

    suspend fun assignDomainToItem(
        itemId: Long,
        domain: DomainKind,
        isPrimary: Boolean = false,
    ) {
        if (isPrimary) {
            itemDomainDao.clearPrimaryFlag(itemId)
        }
        itemDomainDao.upsertDomain(
            ItemDomainEntity(
                itemId = itemId,
                domainKey = domain.name,
                isPrimary = isPrimary,
            ),
        )
    }

    suspend fun removeDomainFromItem(
        itemId: Long,
        domain: DomainKind,
    ) = itemDomainDao.deleteDomain(itemId, domain.name)

    // Documents
    fun getDocumentForItem(itemId: Long): Flow<RichContentDocument?> =
        richContentDocumentDao.observeDocumentForItem(itemId).map { document ->
            document?.toModel()
        }

    suspend fun upsertDocument(
        itemId: Long,
        body: String,
        format: RichContentFormat = RichContentFormat.MARKDOWN,
        structuredContentJson: String? = null,
    ) {
        val existing = richContentDocumentDao.getDocumentForItem(itemId)
        richContentDocumentDao.upsertDocument(
            RichContentDocumentEntity(
                itemId = itemId,
                format = format.storageKey,
                body = body,
                structuredContentJson = structuredContentJson ?: existing?.structuredContentJson,
                schemaVersion = existing?.schemaVersion ?: 1,
                updatedAt =
                    kotlin.time.Clock.System
                        .now()
                        .toEpochMilliseconds(),
            ),
        )
        nodeDao.getNodeById(itemId)?.let { node ->
            if (node.content != body) {
                nodeDao.updateNode(
                    node.copy(
                        content = body,
                        updatedAt =
                            kotlin.time.Clock.System
                                .now()
                                .toEpochMilliseconds(),
                    ),
                )
            }
        }
    }

    suspend fun deleteDocumentForItem(itemId: Long) = richContentDocumentDao.deleteDocumentForItem(itemId)

    // Saved views
    fun getSavedViews(): Flow<List<SavedViewDefinition>> =
        combine(
            savedViewDao.getAllSavedViews(),
            savedViewDao.getAllSavedViewSourceKinds(),
            savedViewDao.getAllSavedViewFilters(),
            savedViewDao.getAllSavedViewSorts(),
            savedViewDao.getAllSavedViewVisibleFields(),
        ) { views, sourceKinds, filters, sorts, visibleFields ->
            views.map { view ->
                view.toDefinition(
                    sourceKinds = sourceKinds.filter { it.viewId == view.id },
                    filters = filters.filter { it.viewId == view.id },
                    sorts = sorts.filter { it.viewId == view.id },
                    visibleFields = visibleFields.filter { it.viewId == view.id },
                )
            }
        }

    suspend fun getSavedView(id: Long): SavedViewDefinition? {
        val view = savedViewDao.getSavedViewById(id) ?: return null
        return view.toDefinition(
            sourceKinds = savedViewDao.getSourceKindsForView(id),
            filters = savedViewDao.getFiltersForView(id),
            sorts = savedViewDao.getSortsForView(id),
            visibleFields = savedViewDao.getVisibleFieldsForView(id),
        )
    }

    suspend fun saveSavedView(definition: SavedViewDefinition): Long {
        val now =
            kotlin.time.Clock.System
                .now()
                .toEpochMilliseconds()
        val existing = definition.id.takeIf { it > 0 }?.let { savedViewDao.getSavedViewById(it) }
        val entity =
            SavedViewEntity(
                id = existing?.id ?: definition.id,
                name = definition.name,
                description = definition.description,
                lens = definition.lens.storageKey,
                layout = definition.layout.storageKey,
                rowDimension = definition.rowDimension?.storageKey,
                columnDimension = definition.columnDimension?.storageKey,
                measure = definition.measure?.storageKey,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            )
        val viewId =
            if (existing == null) {
                savedViewDao.insertSavedView(entity)
            } else {
                savedViewDao.updateSavedView(entity)
                entity.id
            }

        savedViewDao.deleteSourceKindsForView(viewId)
        savedViewDao.deleteFiltersForView(viewId)
        savedViewDao.deleteSortsForView(viewId)
        savedViewDao.deleteVisibleFieldsForView(viewId)

        if (definition.sourceKinds.isNotEmpty()) {
            savedViewDao.insertSavedViewSourceKinds(
                definition.sourceKinds.map { kind ->
                    SavedViewSourceKindEntity(
                        viewId = viewId,
                        itemKind = kind.storageKey,
                    )
                },
            )
        }
        if (definition.filters.isNotEmpty()) {
            savedViewDao.insertSavedViewFilters(
                definition.filters.mapIndexed { index, filter ->
                    SavedViewFilterEntity(
                        viewId = viewId,
                        position = index,
                        fieldKey = filter.fieldKey.storageKey,
                        operatorKey = filter.operator.storageKey,
                        value = filter.value,
                        valueType = filter.valueType.storageKey,
                    )
                },
            )
        }
        if (definition.sorts.isNotEmpty()) {
            savedViewDao.insertSavedViewSorts(
                definition.sorts.mapIndexed { index, sort ->
                    SavedViewSortEntity(
                        viewId = viewId,
                        position = index,
                        fieldKey = sort.fieldKey.storageKey,
                        direction = sort.direction.storageKey,
                    )
                },
            )
        }
        if (definition.visibleFields.isNotEmpty()) {
            savedViewDao.insertSavedViewVisibleFields(
                definition.visibleFields.mapIndexed { index, field ->
                    SavedViewVisibleFieldEntity(
                        viewId = viewId,
                        position = index,
                        fieldKey = field.storageKey,
                    )
                },
            )
        }
        return viewId
    }

    suspend fun deleteSavedView(id: Long) {
        val existing = savedViewDao.getSavedViewById(id) ?: return
        savedViewDao.deleteSourceKindsForView(id)
        savedViewDao.deleteFiltersForView(id)
        savedViewDao.deleteSortsForView(id)
        savedViewDao.deleteVisibleFieldsForView(id)
        savedViewDao.deleteSavedView(existing)
    }

    // Templates
    fun getAllTemplates() = templateDao.getAllTemplates()

    suspend fun insertTemplate(template: TemplateEntity) = templateDao.insertTemplate(template)

    suspend fun insertTemplates(templates: List<TemplateEntity>) = templateDao.insertTemplates(templates)

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

    /**
     * Retrieves a reactive stream of decision nodes filtered by their specific decision status.
     *
     * @param status The decision status to filter by (e.g., 'decided', 'pending').
     * @return A Flow emitting a list of matching decision [NodeEntity] objects.
     */
    fun getDecisionsByStatus(status: String): Flow<List<NodeEntity>> =
        nodeDao.getNodesByType("decision").map { nodes ->
            nodes.filter { it.decisionStatus == status }
        }

    /**
     * Observes decision nodes that have not yet been triaged or processed from the inbox.
     *
     * @return A Flow emitting a list of decision [NodeEntity] objects with an active inbox state.
     */
    fun getDecisionInbox(): Flow<List<NodeEntity>> =
        nodeDao.getNodesByType("decision").map { nodes ->
            nodes.filter { it.inboxState }
        }

    /**
     * Resolves a decision by finalizing its outcome, marking the selected option, and updating its state to 'done'.
     *
     * **Side effects:**
     * - Flags the matching [DecisionOptionEntity] as selected and unselects others.
     * - Updates the decision's status to `decided` and marks the overall node status as `done`.
     * - Removes the decision from the inbox (`inboxState = false`) and timestamps its completion.
     *
     * @param nodeId The ID of the decision node to finalize.
     * @param outcome A freeform string recording the rationale or outcome of the decision.
     * @param selectedOptionId The optional ID of a predefined [DecisionOptionEntity] chosen by the user.
     */
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

    /**
     * Converts a completed decision into a new, actionable project node.
     *
     * **Side effects:**
     * - Inserts a new project node initialized with the decision's title, outcome (falling back to content), and area.
     * - Establishes a `DERIVED_FROM` relation linking the parent decision to the new project.
     *
     * @param nodeId The ID of the parent decision node.
     * @return The ID of the newly created project node, or -1 if the parent decision was not found.
     */
    suspend fun convertDecisionToProject(nodeId: Long): Long {
        val node = nodeDao.getNodeById(nodeId) ?: return -1
        val newProject =
            NodeEntity(
                type = "project",
                title = "Action Plan: ${node.title}",
                content = "Derived from decision: ${node.decisionOutcome ?: node.content}",
                areaId = node.areaId,
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

    /**
     * Converts a completed decision into a new, actionable task node.
     *
     * **Side effects:**
     * - Inserts a new task node initialized with the decision's title, area, project, and content from its outcome and original content.
     * - Establishes a `DERIVED_FROM` relation linking the parent decision to the new task.
     *
     * @param nodeId The ID of the parent decision node.
     * @return The ID of the newly created task node, or -1 if the parent decision was not found.
     */
    suspend fun convertDecisionToTask(nodeId: Long): Long {
        val node = nodeDao.getNodeById(nodeId) ?: return -1
        val newTask =
            NodeEntity(
                type = "task",
                title = "Follow-up: ${node.title}",
                content = "Outcome: ${node.decisionOutcome ?: ""}\n\n${node.content}",
                areaId = node.areaId,
                projectId = node.projectId,
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

    /**
     * Observes the persisted operator profile as a typed local identity model.
     */
    fun getUserProfile(): Flow<UserProfile> =
        userDao.getUser().map { userEntity ->
            userEntity?.toUserProfile() ?: UserEntity().toUserProfile()
        }

    /**
     * Persists the operator profile, preserving existing creation metadata.
     */
    suspend fun saveUserProfile(profile: UserProfile) {
        val existing = userDao.getUser().first()
        userDao.insertUser(profile.toEntity(existing))
    }

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
