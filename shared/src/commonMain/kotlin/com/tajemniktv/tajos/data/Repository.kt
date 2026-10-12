/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.data

import com.tajemniktv.tajos.domain.DomainKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Fallback, no-operation implementation for [InboxEntryDao] used when the actual DAO is unavailable or uninitialized.
 */
private object NoOpInboxEntryDao : InboxEntryDao {
    override fun getAllInboxEntries(): Flow<List<InboxEntryEntity>> = flowOf(emptyList())

    override fun getActiveInboxEntries(): Flow<List<InboxEntryEntity>> = flowOf(emptyList())

    override suspend fun getInboxEntryById(id: Long): InboxEntryEntity? = null

    override suspend fun insertInboxEntry(entry: InboxEntryEntity): Long = 0L

    override suspend fun updateInboxEntry(entry: InboxEntryEntity) = Unit
}

/**
 * Fallback, no-operation implementation for [TaskFacetDao] used when the actual DAO is unavailable or uninitialized.
 */
private object NoOpTaskFacetDao : TaskFacetDao {
    override fun getAllTaskFacets(): Flow<List<TaskFacetEntity>> = flowOf(emptyList())

    override suspend fun getTaskFacetByItemId(itemId: Long): TaskFacetEntity? = null

    override fun observeTaskFacet(itemId: Long): Flow<TaskFacetEntity?> = flowOf(null)

    override suspend fun upsertTaskFacet(facet: TaskFacetEntity) = Unit

    override suspend fun deleteTaskFacetForItem(itemId: Long) = Unit
}

/**
 * Fallback, no-operation implementation for [NoteFacetDao] used when the actual DAO is unavailable or uninitialized.
 */
private object NoOpNoteFacetDao : NoteFacetDao {
    override fun getAllNoteFacets(): Flow<List<NoteFacetEntity>> = flowOf(emptyList())

    override suspend fun getNoteFacetByItemId(itemId: Long): NoteFacetEntity? = null

    override fun observeNoteFacet(itemId: Long): Flow<NoteFacetEntity?> = flowOf(null)

    override suspend fun upsertNoteFacet(facet: NoteFacetEntity) = Unit

    override suspend fun deleteNoteFacetForItem(itemId: Long) = Unit
}

/**
 * Fallback, no-operation implementation for [ProjectFacetDao] used when the actual DAO is unavailable or uninitialized.
 */
private object NoOpProjectFacetDao : ProjectFacetDao {
    override fun getAllProjectFacets(): Flow<List<ProjectFacetEntity>> = flowOf(emptyList())

    override suspend fun getProjectFacetByItemId(itemId: Long): ProjectFacetEntity? = null

    override fun observeProjectFacet(itemId: Long): Flow<ProjectFacetEntity?> = flowOf(null)

    override suspend fun upsertProjectFacet(facet: ProjectFacetEntity) = Unit

    override suspend fun deleteProjectFacetForItem(itemId: Long) = Unit
}

/**
 * Fallback, no-operation implementation for [AreaFacetDao] used when the actual DAO is unavailable or uninitialized.
 */
private object NoOpAreaFacetDao : AreaFacetDao {
    override fun getAllAreaFacets(): Flow<List<AreaFacetEntity>> = flowOf(emptyList())

    override suspend fun getAreaFacetByItemId(itemId: Long): AreaFacetEntity? = null

    override fun observeAreaFacet(itemId: Long): Flow<AreaFacetEntity?> = flowOf(null)

    override suspend fun upsertAreaFacet(facet: AreaFacetEntity) = Unit

    override suspend fun deleteAreaFacetForItem(itemId: Long) = Unit
}

/**
 * Fallback, no-operation implementation for [RecordFacetDao] used when the actual DAO is unavailable or uninitialized.
 */
private object NoOpRecordFacetDao : RecordFacetDao {
    override fun getAllRecordFacets(): Flow<List<RecordFacetEntity>> = flowOf(emptyList())

    override suspend fun getRecordFacetByItemId(itemId: Long): RecordFacetEntity? = null

    override fun observeRecordFacet(itemId: Long): Flow<RecordFacetEntity?> = flowOf(null)

    override suspend fun upsertRecordFacet(facet: RecordFacetEntity) = Unit

    override suspend fun deleteRecordFacetForItem(itemId: Long) = Unit
}

/**
 * Fallback, no-operation implementation for [ItemDomainDao] used when the actual DAO is unavailable or uninitialized.
 */
private object NoOpItemDomainDao : ItemDomainDao {
    /**
     * Fallback implementation that emits an empty list of domain associations.
     */
    override fun getAllItemDomains(): Flow<List<ItemDomainEntity>> = flowOf(emptyList())

    /**
     * Fallback implementation that emits an empty list of domains for the requested item.
     */
    override fun getDomainsForItem(itemId: Long): Flow<List<ItemDomainEntity>> = flowOf(emptyList())

    /**
     * Fallback, no-operation implementation for upserting a domain.
     */
    override suspend fun upsertDomain(domain: ItemDomainEntity) = Unit

    /**
     * Fallback, no-operation implementation for batch upserting domains.
     */
    override suspend fun upsertDomains(domains: List<ItemDomainEntity>) = Unit

    /**
     * Fallback, no-operation implementation for deleting a specific domain association.
     */
    override suspend fun deleteDomain(
        itemId: Long,
        domainKey: String,
    ) = Unit

    /**
     * Fallback, no-operation implementation for clearing all domain associations for an item.
     */
    override suspend fun deleteDomainsForItem(itemId: Long) = Unit

    /**
     * Fallback, no-operation implementation for clearing the primary domain flag.
     */
    override suspend fun clearPrimaryFlag(itemId: Long) = Unit
}

/**
 * Fallback, no-operation implementation for [RichContentDocumentDao] used when the actual DAO is unavailable or uninitialized.
 */
private object NoOpRichContentDocumentDao : RichContentDocumentDao {
    override fun getAllDocuments(): Flow<List<RichContentDocumentEntity>> = flowOf(emptyList())

    override fun observeDocumentForItem(itemId: Long): Flow<RichContentDocumentEntity?> = flowOf(null)

    override suspend fun getDocumentForItem(itemId: Long): RichContentDocumentEntity? = null

    override suspend fun upsertDocument(document: RichContentDocumentEntity) = Unit

    override suspend fun deleteDocumentForItem(itemId: Long) = Unit
}

/**
 * Fallback, no-operation implementation for [ScheduleEntryDao] used when the actual DAO is unavailable or uninitialized.
 */
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

    override suspend fun deleteScheduleEntry(id: Long) = Unit

    override suspend fun insertScheduleEntry(entry: ScheduleEntryEntity): Long = 0L

    override suspend fun insertScheduleEntries(entries: List<ScheduleEntryEntity>) = Unit
}

/**
 * Fallback, no-operation implementation for [SavedViewDao] used when the actual DAO is unavailable or uninitialized.
 */
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
 * Production callers construct it from [AppDatabase], so aggregate commands share a writer
 * transaction. Full-table backup ownership belongs to [BackupDao], not reactive UI queries.
 */
class AppRepository internal constructor(
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
    private val transactions: RepositoryTransactions = DirectRepositoryTransactions,
    private val backupDao: BackupDao? = null,
) {
    /** Production construction always wires real companion DAOs, backups, and transactions. */
    constructor(
        database: AppDatabase,
        nodeDao: NodeDao = database.nodeDao(),
        calendarEventDao: CalendarEventDao = database.calendarEventDao(),
        savedViewDao: SavedViewDao = database.savedViewDao(),
    ) : this(
        nodeDao,
        database.focusSessionDao(),
        database.trackDao(),
        database.relationDao(),
        database.tagDao(),
        database.eventLogDao(),
        database.attachmentDao(),
        database.templateDao(),
        database.nodeSnapshotDao(),
        database.reviewDao(),
        database.calendarProviderDao(),
        calendarEventDao,
        database.modeDao(),
        database.protocolDao(),
        database.decisionDao(),
        database.userDao(),
        database.medicationDao(),
        database.inboxEntryDao(),
        database.taskFacetDao(),
        database.noteFacetDao(),
        database.projectFacetDao(),
        database.areaFacetDao(),
        database.recordFacetDao(),
        database.itemDomainDao(),
        database.richContentDocumentDao(),
        database.scheduleEntryDao(),
        savedViewDao,
        RoomRepositoryTransactions(database),
        database.backupDao(),
    )

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
     * Retrieves existing nodes in batches below SQLite's bind-parameter limit.
     * Missing IDs are omitted and result order is unspecified.
     */
    suspend fun getNodesByIds(ids: List<Long>): List<NodeEntity> =
        ids.distinct().chunked(900).flatMap { nodeDao.getNodesByIds(it) }

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
    ): Long = transactions.write {
        val trimmed = rawText.trim()
        if (trimmed.isBlank()) return@write 0L
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
        return@write entryId
    }

    /**
     * Dismisses a pending capture using its current row. Stale dismissal cannot unlink triage.
     */
    suspend fun dismissInboxEntry(entry: InboxEntryEntity): Unit = transactions.write {
        val current = inboxEntryDao.getInboxEntryById(entry.id) ?: return@write
        if (current.triagedItemId != null) return@write
        if (current.processedAt != null) return@write
        if (current.dismissedAt != null) return@write
        inboxEntryDao.updateInboxEntry(
            current.copy(
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
     * @return The new or previously triaged item ID. Returns `0L` for missing, dismissed,
     * processed-but-unlinked, or blank captures. Retries never recreate a deleted target.
     */
    suspend fun triageInboxEntry(
        entryId: Long,
        kind: ItemKind,
    ): Long = transactions.write {
        val entry = inboxEntryDao.getInboxEntryById(entryId) ?: return@write 0L
        entry.triagedItemId?.let { return@write it }
        if (entry.processedAt != null || entry.dismissedAt != null) return@write 0L
        val parsed = parseCapturedText(entry.rawText)
        if (parsed.title.isBlank()) return@write 0L

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
        return@write createdId
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
    ): Long = transactions.write {
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
        val domainEntities = domains.mapIndexed { index, domain ->
            ItemDomainEntity(
                itemId = id,
                domainKey = domain.name,
                isPrimary = index == 0,
            )
        }
        if (domainEntities.isNotEmpty()) itemDomainDao.upsertDomains(domainEntities)
        if (kind == ItemKind.RECORD) {
            recordFacetDao.upsertRecordFacet(
                RecordFacetEntity(
                    itemId = id,
                    kind = recordKind?.storageKey ?: RecordKind.GENERAL.storageKey,
                ),
            )
        }
        return@write id
    }

    // ... existing methods ...

    // Calendar
    /**
     * Observes all configured [com.tajemniktv.tajos.data.CalendarProviderEntity] records.
     */
    fun getAllCalendarProviders() = calendarProviderDao.getAllProviders()

    /**
     * Inserts a new calendar provider configuration.
     * @return The ID of the newly inserted provider.
     */
    suspend fun insertCalendarProvider(provider: CalendarProviderEntity): Long {
        var id = calendarProviderDao.insertProvider(provider)
        if (id == -1L) id = provider.id
        return id
    }

    /**
     * Updates an existing calendar provider configuration.
     */
    suspend fun updateCalendarProvider(provider: CalendarProviderEntity) = calendarProviderDao.updateProvider(provider)

    /**
     * Deletes a calendar provider configuration.
     */
    suspend fun deleteCalendarProvider(provider: CalendarProviderEntity) = calendarProviderDao.deleteProvider(provider)

    /**
     * Observes calendar events falling within the specified time range.
     *
     * @param from The start of the range (epoch milliseconds, inclusive).
     * @param to The end of the range (epoch milliseconds, inclusive).
     */
    fun getCalendarEventsInRange(
        from: Long,
        to: Long,
    ) = calendarEventDao.getEventsInRange(from, to)

    /**
     * Inserts a batch of synced calendar events.
     */
    suspend fun insertCalendarEvents(events: List<CalendarEventEntity>) = calendarEventDao.insertEvents(events)

    /**
     * Deletes all cached calendar events associated with a specific provider.
     */
    suspend fun deleteCalendarEventsByProvider(providerId: Long) = calendarEventDao.deleteEventsByProvider(providerId)

    /** Commits a successful refresh atomically; callers must never pass failed fetches here. */
    suspend fun replaceCalendarEvents(
        provider: CalendarProviderEntity,
        events: List<CalendarEventEntity>,
        syncedAt: Long,
    ): Unit = transactions.write {
        require(events.all { it.providerId == provider.id }) { "Calendar event provider mismatch" }
        calendarEventDao.deleteEventsByProvider(provider.id)
        calendarEventDao.insertEvents(events)
        calendarProviderDao.updateProvider(provider.copy(lastSyncedAt = syncedAt))
    }

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
    suspend fun insertNode(node: NodeEntity): Long = transactions.write {
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
        return@write id
    }

    /**
     * Inserts multiple nodes while preserving the same side effects as [insertNode].
     */
    suspend fun insertNodes(nodes: List<NodeEntity>): List<Long> =
        transactions.write { nodes.map { insertNode(it) } }

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



    suspend fun updateNode(node: NodeEntity): Unit = transactions.write {
        val oldNode = nodeDao.getNodeById(node.id) ?: return@write
        nodeDao.updateNode(node)
        executeNodeSideEffects(oldNode, node)
    }

    /**
     * Applies a lifecycle transition to the current persisted node, preserving unrelated edits.
     * Repeating the current status is a no-op. Completion and a recurring successor share one
     * transaction. Archiving retains completion history; reopening clears completion metadata
     * and permits a new completion transition. Archived nodes reject completion until reopened.
     * Recurrence retains the existing fixed-duration interval policy.
     */
    suspend fun updateNodeStatus(nodeId: Long, status: TaskState): Unit = transactions.write {
        val current = nodeDao.getNodeById(nodeId) ?: return@write
        if (!current.canTransitionTo(status)) return@write
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        updateNode(current.withLifecycleState(status, now))
        current.recurringSuccessor(status, now)?.let { insertNode(it) }
    }

    /** Validates legacy string callers before any write; typed callers use [TaskState] directly. */
    suspend fun updateNodeStatus(nodeId: Long, status: String): Unit =
        updateNodeStatus(nodeId, requireNotNull(TaskState.fromStorageKey(status)) { "Unsupported node lifecycle state" })

    /**
     * Updates multiple existing nodes in the database in a batch operation.
     * Side-effects (like logging, facet syncing, etc.) are executed for each node in the
     * same transaction as the batch write. Repeated IDs retain the last supplied value and
     * reconcile side effects once, matching the final row rather than intermediate updates.
     *
     * @param nodes The updated node entities to save.
     */
    suspend fun updateNodes(nodes: List<NodeEntity>): Unit = transactions.write {
        if (nodes.isEmpty()) return@write
        val updates = nodes.associateBy { it.id }.values.toList()

        val oldNodesMap = nodeDao.getNodesByIds(updates.map { it.id }).associateBy { it.id }

        nodeDao.updateNodes(updates)

        updates.forEach { node ->
            val oldNode = oldNodesMap[node.id] ?: return@forEach
            executeNodeSideEffects(oldNode, node)
        }
    }

    private suspend fun updateDecisionOptionsBatch(
        options: List<DecisionOptionEntity>,
        selectedOptionId: Long?
    ) {
        val updatedOptions = options.mapNotNull { option ->
            val isSelected = option.id == selectedOptionId
            if (option.isSelected != isSelected) {
                option.copy(isSelected = isSelected)
            } else {
                null
            }
        }
        if (updatedOptions.isNotEmpty()) {
            decisionDao.updateDecisionOptions(updatedOptions)
        }
    }

    private suspend fun executeNodeSideEffects(oldNode: NodeEntity, node: NodeEntity) {
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
        syncChangedScheduleEntries(oldNode, node)
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
                // Node updates are complete values: null clears a field, it is not "unchanged".
                taskFacetDao.upsertTaskFacet(
                    TaskFacetEntity(
                        itemId = node.id,
                        state =
                            TaskState.fromStorageKey(node.status)?.storageKey
                                ?: TaskState.ACTIVE.storageKey,
                        energyLevel = node.energyLevel,
                        friction = node.friction,
                        nextStep = node.nextSmallestStep,
                        estimatedMinutes = node.estimatedMinutes,
                        completionNote = node.completionNote,
                        completedAt = node.completedAt,
                        isRecurring = node.isRecurring,
                        recurringInterval = node.recurringInterval,
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
        val domainEntities = associatedDomains.mapIndexed { index, domain ->
            ItemDomainEntity(
                itemId = node.id,
                domainKey = domain.name,
                isPrimary = index == 0,
            )
        }
        if (domainEntities.isNotEmpty()) itemDomainDao.upsertDomains(domainEntities)
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
     * Reconciles only changed legacy schedule fields, preserving schedule-only rows and detail.
     * A scalar edit targets the row matching its previous instant; additional same-kind entries
     * remain independent. Time edits retain an existing end's duration and explicit timezone.
     * Recurrence edits update only scalar-backed rows. Creates and legacy
     * imports use [syncScheduleEntriesForNode] to initialize their compatibility schedule.
     */
    private suspend fun syncChangedScheduleEntries(oldNode: NodeEntity, node: NodeEntity) {
        val recurrenceChanged = oldNode.recurringInterval != node.recurringInterval
        for (change in node.legacyScheduleChanges(oldNode)) {
            if (change.timeChanged || recurrenceChanged) reconcileScheduleEntry(node, change, recurrenceChanged)
        }
    }

    /** Writes one scalar-backed replacement or deletion inside the caller's aggregate transaction. */
    private suspend fun reconcileScheduleEntry(node: NodeEntity, change: LegacyScheduleChange, recurrenceChanged: Boolean) {
        val entries = scheduleEntryDao.getScheduleEntriesByKind(node.id, change.kind.storageKey)
        val mirrored = change.mirroredEntry(entries)
        if (!change.timeChanged && mirrored == null) return
        val replacement = change.replacement(node.id, mirrored, node.recurringInterval, recurrenceChanged)
        if (replacement != null) {
            scheduleEntryDao.insertScheduleEntry(replacement)
        } else {
            mirrored?.let { scheduleEntryDao.deleteScheduleEntry(it.id) }
        }
    }

    /** Initializes scalar-backed schedule rows for new nodes and legacy imports. */
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

    /**
     * Permanently deletes a node from the database.
     * Its Today pins are removed in the same transaction, including assignments on other dates.
     *
     * @param node The node to delete.
     */
    suspend fun deleteNode(node: NodeEntity): Unit = transactions.write {
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
        nodeDao.unpinFromToday(node.id)
        nodeDao.deleteNode(node)
    }

    /**
     * Pins a node to the current local day, retaining one row per node/date across retries.
     * Historical duplicates for that pair are collapsed; other dates remain untouched.
     * Missing/deleted targets are ignored so stale callbacks cannot create orphan pins.
     *
     * **Side effects:**
     * - Logs a "TODAY_ASSIGNED" event only when no pin exists for that pair.
     *
     * @param nodeId The ID of the node to pin.
     */
    suspend fun pinToToday(nodeId: Long): Unit =
        pinToDate(
            nodeId,
            kotlin.time.Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date,
        )

    /** Applies pin deduplication and rollback for an explicit local date, including fixed-date fixtures. */
    internal suspend fun pinToDate(nodeId: Long, date: LocalDate): Unit = transactions.write {
        if (nodeDao.getNodeById(nodeId) == null) return@write
        val dateKey = date.toString()
        val existing = nodeDao.getPinsForNodeOnDate(nodeId, dateKey)
        val keeper = existing.minByOrNull { it.id }
        if (existing.size == 1 && keeper?.position == 0) return@write
        nodeDao.unpinFromDate(nodeId, dateKey)
        nodeDao.pinToToday(keeper?.copy(position = 0) ?: TodayPinEntity(nodeId = nodeId, date = dateKey, position = 0))
        if (keeper == null) logEvent("TODAY_ASSIGNED", nodeId)
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

    suspend fun insertSession(session: FocusSessionEntity): Long = transactions.write {
        var id = focusSessionDao.insertSession(session)
        if (id == -1L) id = session.id
        logEvent("SESSION_STARTED", session.nodeId)
        return@write id
    }

    suspend fun updateSession(session: FocusSessionEntity): Unit = transactions.write {
        focusSessionDao.updateSession(session)
        if (session.endedAt != null) {
            logEvent("SESSION_ENDED", session.nodeId)
        }
    }

    /**
     * Returns a Flow emitting all track entries (e.g. measurements, habit completions).
     */
    fun getAllTrackEntries(): Flow<List<TrackEntryEntity>> = trackDao.getAllTrackEntries()

    suspend fun insertTrackEntry(entry: TrackEntryEntity): Long = transactions.write {
        var id = trackDao.insertTrackEntry(entry)
        if (id == -1L) id = entry.id
        logEvent("CHECKIN_CREATED")
        return@write id
    }

    // Relations
    /**
     * Returns a Flow emitting the complete relation graph containing all edges.
     */
    fun getAllRelations() = relationDao.getAllRelations()

    /**
     * Returns a Flow emitting all relation edges connected to the specified node.
     */
    fun getRelationsForNode(nodeId: Long) = relationDao.getRelationsForNode(nodeId)

    suspend fun insertRelation(relation: RelationEntity): Unit = transactions.write {
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
    /**
     * Returns a Flow emitting all tag entities used in the system.
     */
    fun getAllTags() = tagDao.getAllTags()

    /**
     * Returns a Flow emitting all tags assigned to the specified node.
     */
    fun getTagsForNode(nodeId: Long) = tagDao.getTagsForNode(nodeId)

    suspend fun insertTag(tag: TagEntity): Long = transactions.write {
        var id = tagDao.insertTag(tag)
        if (id == -1L) id = tag.id
        return@write id
    }

    suspend fun attachTagToNode(
        nodeId: Long,
        tagId: Long,
    ) = tagDao.attachTagToNode(NodeTagEntity(nodeId, tagId))

    suspend fun detachTagFromNode(
        nodeId: Long,
        tagId: Long,
    ) = tagDao.detachTagFromNode(nodeId, tagId)

    // Log
    /**
     * Returns a Flow emitting the most recent event logs up to the specified limit.
     */
    fun getRecentLogs(limit: Int = 100) = eventLogDao.getRecentLogs(limit)

    /**
     * Returns a Flow emitting all event logs associated with the specified node.
     */
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
    /**
     * Returns a Flow emitting all attachments (e.g. files, images) linked to the specified node.
     */
    fun getAttachmentsForNode(nodeId: Long) = attachmentDao.getAttachmentsForNode(nodeId)

    suspend fun insertAttachment(attachment: AttachmentEntity) = attachmentDao.insertAttachment(attachment)

    suspend fun deleteAttachment(attachment: AttachmentEntity) = attachmentDao.deleteAttachment(attachment)

    // Domains
    /**
     * Returns a Flow emitting all domain assignments associated with the specified item.
     */
    fun getDomainsForItem(itemId: Long): Flow<List<DomainAssignment>> =
        itemDomainDao.getDomainsForItem(itemId).map { domains ->
            domains.mapNotNull { it.toModel() }
        }

    suspend fun assignDomainToItem(
        itemId: Long,
        domain: DomainKind,
        isPrimary: Boolean = false,
    ): Unit = transactions.write {
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
    /**
     * Returns a Flow emitting the rich content document associated with the specified item, if any.
     */
    fun getDocumentForItem(itemId: Long): Flow<RichContentDocument?> =
        richContentDocumentDao.observeDocumentForItem(itemId).map { document ->
            document?.toModel()
        }

    suspend fun upsertDocument(
        itemId: Long,
        body: String,
        format: RichContentFormat = RichContentFormat.MARKDOWN,
        structuredContentJson: String? = null,
    ): Unit = transactions.write {
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
    /**
     * Returns a Flow emitting all user-defined saved views and their configurations.
     */
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

    suspend fun saveSavedView(definition: SavedViewDefinition): Long = transactions.write {
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
        return@write viewId
    }

    suspend fun deleteSavedView(id: Long): Unit = transactions.write {
        val existing = savedViewDao.getSavedViewById(id) ?: return@write
        savedViewDao.deleteSourceKindsForView(id)
        savedViewDao.deleteFiltersForView(id)
        savedViewDao.deleteSortsForView(id)
        savedViewDao.deleteVisibleFieldsForView(id)
        savedViewDao.deleteSavedView(existing)
    }

    // Templates
    /**
     * Returns a Flow emitting all stored templates.
     */
    fun getAllTemplates() = templateDao.getAllTemplates()

    suspend fun insertTemplate(template: TemplateEntity) = templateDao.insertTemplate(template)

    suspend fun insertTemplates(templates: List<TemplateEntity>) = templateDao.insertTemplates(templates)

    suspend fun updateTemplate(template: TemplateEntity) = templateDao.updateTemplate(template)

    suspend fun deleteTemplate(template: TemplateEntity) = templateDao.deleteTemplate(template)

    // Snapshots
    /**
     * Returns a Flow emitting point-in-time snapshots of the specified node.
     */
    fun getSnapshotsForNode(nodeId: Long) = nodeSnapshotDao.getSnapshotsForNode(nodeId)

    suspend fun insertSnapshot(snapshot: NodeSnapshotEntity) = nodeSnapshotDao.insertSnapshot(snapshot)

    suspend fun deleteSnapshot(snapshot: NodeSnapshotEntity) = nodeSnapshotDao.deleteSnapshot(snapshot)

    // Reviews
    /**
     * Returns a Flow emitting all review sessions.
     */
    fun getAllReviews() = reviewDao.getAllReviews()

    suspend fun insertReview(review: ReviewEntity): Long = transactions.write {
        var id = reviewDao.insertReview(review)
        if (id == -1L) id = review.id
        return@write id
    }

    suspend fun getLastReviewByType(type: String) = reviewDao.getLastReviewByType(type)

    // Operating Modes
    /**
     * Returns a Flow emitting all modes (e.g. COMMAND, FOCUS, RECOVERY) configured in the system.
     */
    fun getAllModes() = modeDao.getAllModes()

    suspend fun insertMode(mode: ModeEntity): Long {
        var id = modeDao.insertMode(mode)
        if (id == -1L) id = mode.id
        return id
    }

    suspend fun updateMode(mode: ModeEntity) = modeDao.updateMode(mode)

    fun getPreferencesForMode(modeId: Long) = modeDao.getPreferencesForMode(modeId)

    suspend fun insertPreference(preference: ModePreferenceEntity) = modeDao.insertPreference(preference)

    fun getAreaFiltersForMode(modeId: Long) = modeDao.getAreaFiltersForMode(modeId)

    suspend fun insertAreaFilter(filter: ModeAreaFilterEntity) = modeDao.insertAreaFilter(filter)

    fun getTypeFiltersForMode(modeId: Long) = modeDao.getTypeFiltersForMode(modeId)

    suspend fun insertTypeFilter(filter: ModeTypeFilterEntity) = modeDao.insertTypeFilter(filter)

    fun getAllModeUsageLogs() = modeDao.getAllUsageLogs()

    suspend fun insertModeUsageLog(log: ModeUsageLogEntity): Long {
        var id = modeDao.insertUsageLog(log)
        if (id == -1L) id = log.id
        return id
    }

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

    suspend fun insertProtocolHistory(history: ProtocolHistoryEntity): Long {
        var id = protocolDao.insertProtocolHistory(history)
        if (id == -1L) id = history.id
        return id
    }

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
    ): Unit = transactions.write {
        val node = nodeDao.getNodeById(nodeId) ?: return@write
        val options = decisionDao.getOptionsSnapshot(nodeId)

        updateDecisionOptionsBatch(options, selectedOptionId)

        updateNode(
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
    suspend fun convertDecisionToProject(nodeId: Long): Long = transactions.write {
        val node = nodeDao.getNodeById(nodeId) ?: return@write -1
        val newProject =
            NodeEntity(
                type = "project",
                title = "Action Plan: ${node.title}",
                content = "Derived from decision: ${node.decisionOutcome ?: node.content}",
                areaId = node.areaId,
            )
        val projectId = insertNode(newProject)

        insertRelation(
            RelationEntity(
                fromNodeId = nodeId,
                toNodeId = projectId,
                relationType = "DERIVED_FROM",
            ),
        )

        return@write projectId
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
    suspend fun convertDecisionToTask(nodeId: Long): Long = transactions.write {
        val node = nodeDao.getNodeById(nodeId) ?: return@write -1
        val newTask =
            NodeEntity(
                type = "task",
                title = "Follow-up: ${node.title}",
                content = "Outcome: ${node.decisionOutcome ?: ""}\n\n${node.content}",
                areaId = node.areaId,
                projectId = node.projectId,
            )
        val taskId = insertNode(newTask)

        insertRelation(
            RelationEntity(
                fromNodeId = nodeId,
                toNodeId = taskId,
                relationType = "DERIVED_FROM",
            ),
        )

        return@write taskId
    }

    fun getOptionsForDecision(nodeId: Long) = decisionDao.getOptionsForDecision(nodeId)

    suspend fun insertDecisionOption(option: DecisionOptionEntity): Long = transactions.write {
        var id = decisionDao.insertDecisionOption(option)
        if (id == -1L) id = option.id
        return@write id
    }

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

    suspend fun insertMedication(medication: MedicationEntity): Long = transactions.write {
        var id = medicationDao.insertMedication(medication)
        if (id == -1L) id = medication.id
        return@write id
    }

    suspend fun updateMedication(medication: MedicationEntity) = medicationDao.updateMedication(medication)

    suspend fun deleteMedication(medication: MedicationEntity) = medicationDao.deleteMedication(medication)

    suspend fun getTrackEntryByDate(date: String): TrackEntryEntity? = trackDao.getTrackEntryByDate(date)

    suspend fun insertTrackMedication(join: TrackMedicationJoinEntity) = trackDao.insertTrackMedication(join)

    fun getTrackMedications(trackEntryId: Long): Flow<List<TrackMedicationJoinEntity>> = trackDao.getTrackMedications(trackEntryId)

    /** Captures the complete Room backup contract; DAO-only fakes cannot produce full backups. */
    suspend fun buildExportBundle(enabledPacks: Set<String> = emptySet()): ExportBundle =
        checkNotNull(backupDao) { "Full backups require the database backup DAO" }.snapshot(enabledPacks)

    /**
     * Atomically merges a backup. Older formats hydrate node-derived state without action logs;
     * current backups restore authoritative companion rows instead of regenerating them.
     * Payload credentials are ignored; matching local authorization is preserved.
     */
    suspend fun importBundle(bundle: ExportBundle): ImportReport = transactions.write {
        require(bundle.schemaVersion in 1..EXPORT_SCHEMA_VERSION) { "Unsupported backup schema" }
        if (bundle.schemaVersion == EXPORT_SCHEMA_VERSION) {
            checkNotNull(backupDao) { "Full restore requires the database backup DAO" }.restore(bundle)
        } else {
            val providers = bundle.providers.map { it.forBackupImport(calendarProviderDao.getProviderById(it.id)) }
            // Restore relation identity, not just unique logical edges. Room upsert cannot
            // replace an edge whose unique (from, to, type) key has a different primary ID.
            bundle.nodes.forEach { relationDao.deleteRelationsForNode(it.id) }
            importLegacyNodes(bundle.nodes)
            bundle.relations.filter { it.relationType == "BELONGS_TO" }
                .map { it.fromNodeId }.distinct()
                .forEach { relationDao.deleteBelongsToRelations(it) }
            relationDao.insertRelations(bundle.relations)
            tagDao.insertTags(bundle.tags)
            templateDao.insertTemplates(bundle.templates)
            reviewDao.insertReviews(bundle.reviews)
            trackDao.insertTrackEntries(bundle.tracks)
            focusSessionDao.insertSessions(bundle.sessions)
            calendarProviderDao.insertProviders(providers)
            calendarEventDao.insertEvents(bundle.calendars)
            eventLogDao.insertLogs(bundle.recentEvents)
        }
        ImportReport(
            nodes = bundle.nodes.size,
            relations = bundle.relations.size,
            tracks = bundle.tracks.size,
            sessions = bundle.sessions.size,
            events = bundle.recentEvents.size,
        )
    }

    /** Imports old node-only backups and rebuilds all node-derived tables without creation logs. */
    suspend fun importLegacyNodes(nodes: List<NodeEntity>): Int = transactions.write {
        nodes.forEach { node ->
            val insertedId = nodeDao.insertNode(node)
            val restored = node.copy(id = if (insertedId == -1L) node.id else insertedId)
            syncBelongsToRelations(restored.id, restored.projectId, restored.areaId)
            syncTypedFacetsFromNode(restored)
            syncDomainsFromNodeMetadata(restored)
            syncDocumentFromNode(restored)
            syncScheduleEntriesForNode(
                nodeId = restored.id,
                reminderAt = restored.reminderAt,
                startAt = restored.startAt,
                dueAt = restored.dueAt,
                recurrenceRule = restored.recurringInterval,
            )
        }
        nodes.size
    }
}

/**
 * A summary report detailing the number of entities successfully imported.
 *
 * @property nodes The total number of nodes imported.
 * @property relations The total number of relations imported.
 * @property tracks The total number of tracks imported.
 * @property sessions The total number of sessions imported.
 * @property events The total number of events imported.
 */
data class ImportReport(
    val nodes: Int,
    val relations: Int,
    val tracks: Int,
    val sessions: Int,
    val events: Int,
)
