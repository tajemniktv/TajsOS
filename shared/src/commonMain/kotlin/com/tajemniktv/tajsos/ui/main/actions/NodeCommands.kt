/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.actions

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.ItemKind
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeSnapshotEntity
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.defaultInboxState
import com.tajemniktv.tajsos.ui.main.calculators.calculateNextRecurringDate
import com.tajemniktv.tajsos.ui.main.calculators.calculateStaleTasks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * A central action dispatcher containing encapsulated commands for mutating the lifecycle,
 * state, and content of primary life objects (Nodes).
 *
 * It provides safe boundaries around common operations like archiving, state updates,
 * open-loop conversions, subtask splitting, and scheduled sweeps.
 *
 * @property repository The main entrypoint for accessing and updating the local SQLite database.
 * @property scope The CoroutineScope within which all background database writes are launched.
 * @property currentTodayNodes A lambda supplying the current, explicitly pinned 'Today' nodes.
 * @property currentAllNodes A lambda supplying the complete set of visible active nodes.
 * @property parseInternalLinks A lambda invoking wiki-link parsing to establish node relations.
 * @property setTagOnNode A lambda to safely apply or remove string tags on a target node.
 * @property defaultNextStepLabel A supplier for fallback text when next steps are blank.
 * @property defaultUntitledLabel A supplier for fallback text when nodes lack titles.
 */
class NodeCommands(
    private val repository: AppRepository,
    private val scope: CoroutineScope,
    private val currentTodayNodes: () -> List<NodeEntity>,
    private val currentAllNodes: () -> List<NodeEntity>,
    private val parseInternalLinks: (Long) -> Unit,
    private val setTagOnNode: suspend (Long, String, Boolean) -> Unit,
    private val defaultNextStepLabel: () -> String = { "Next step" },
    private val defaultUntitledLabel: () -> String = { "Untitled" },
) {

    companion object {
        /** Cached regex for splitting markdown content by top-level headings to prevent recompilation. */
        private val headingSplitRegex = Regex("(?=^# )", RegexOption.MULTILINE)
    }

    /**
     * Automatically reviews the inbox and currently active task lists to identify items that have
     * been ignored or deferred multiple times (based on [cutoffDays]). It sweeps these stale items
     * back into the general inbox state to force a re-evaluation or triage.
     *
     * @param cutoffDays The number of days a task must sit un-updated before being considered stale.
     */
    fun sweepStaleTasks(cutoffDays: Int = 3) {
        scope.launch {
            val now = Clock.System.now()
            val nodes = currentAllNodes()
            val staleTasks = calculateStaleTasks(nodes, now, cutoffDays)

            if (staleTasks.isNotEmpty()) {
                val updatedNodes = staleTasks.map { node ->
                    node.copy(
                        status = TaskState.SOMEDAY.storageKey,
                        postponeCount = node.postponeCount + 1,
                        updatedAt = now.toEpochMilliseconds(),
                    )
                }
                repository.updateNodes(updatedNodes)
            }
        }
    }

    /**
     * Constructs and inserts a new Node entity into the database.
     * Handles establishing initial tags, relations to parents, explicit property assignments
     * (e.g. decision links), and immediately requests wiki-link parsing on the created content.
     */
    fun addNode(
        title: String,
        content: String = "",
        type: String = "task", // NON-NLS
        projectId: Long? = null,
        areaId: Long? = null,
        isRecurring: Boolean = false,
        recurringInterval: String? = null,
        reminderAt: Long? = null,
        color: Int? = null,
        icon: String? = null,
        inboxState: Boolean? = null,
        contextScreen: String? = null,
        isSticky: Boolean = false,
        decisionCategory: String? = null,
    ) {
        scope.launch {
            val autoType =
                if (type == "task" && // NON-NLS
                    (
                        title.startsWith(
                            "http://", // NON-NLS
                        ) ||
                            title.startsWith("https://") // NON-NLS
                    )
                ) {
                    "resource" // NON-NLS
                } else {
                    type
                }

            val itemKind =
                when (autoType)
                {
                    "task" -> ItemKind.TASK

                    // NON-NLS
                    "note" -> ItemKind.NOTE

                    // NON-NLS
                    "record" -> ItemKind.RECORD

                    // NON-NLS
                    "project" -> ItemKind.PROJECT

                    // NON-NLS
                    "area" -> ItemKind.AREA

                    // NON-NLS
                    else -> null
                }

            if (itemKind != null) {
                repository.insertLifeItem(
                    kind = itemKind,
                    title = title,
                    content = content,
                    activeProjectId = projectId,
                    homeAreaId = areaId,
                    isRecurring = isRecurring,
                    recurringInterval = recurringInterval,
                    reminderAt = reminderAt,
                    color = color,
                    icon = icon,
                    inboxState = inboxState ?: itemKind.defaultInboxState(),
                    contextScreen = contextScreen,
                    isSticky = isSticky,
                )
            } else {
                repository.insertNode(
                    NodeEntity(
                        title = title,
                        content = content,
                        type = autoType,
                        projectId = projectId,
                        areaId = areaId,
                        isRecurring = isRecurring,
                        recurringInterval = recurringInterval,
                        reminderAt = reminderAt,
                        color = color,
                        icon = icon,
                        inboxState =
                            inboxState
                                ?: (autoType != "project" && autoType != "area"),
                        // NON-NLS
                        contextScreen = contextScreen,
                        isSticky = isSticky,
                        decisionStatus = if (autoType == "decision") "pending" else null, // NON-NLS
                        decisionCategory =
                            if (autoType == "decision") { // NON-NLS
                                decisionCategory
                                    ?: "major" // NON-NLS
                            } else {
                                null
                            },
                        openLoopType = if (autoType == "open_loop") "unresolved_problem" else null, // NON-NLS
                        openLoopStalenessAt =
                            if (autoType == "open_loop") { // NON-NLS
                                Clock.System
                                    .now()
                                    .plus(3, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                                    .toEpochMilliseconds()
                            } else {
                                null
                            },
                        maintenanceType = if (autoType == "maintenance") "form" else null, // NON-NLS
                    ),
                )
            }
        }
    }

    suspend fun addNodeForResult(
        title: String,
        content: String = "",
        type: String = "task", // NON-NLS
        projectId: Long? = null,
        areaId: Long? = null,
        inboxState: Boolean? = null,
    ): Long =
        withContext(Dispatchers.Default) {
            when (type)
            {
                "task" -> { // NON-NLS
                    repository.insertLifeItem(
                        kind = ItemKind.TASK,
                        title = title,
                        content = content,
                        activeProjectId = projectId,
                        homeAreaId = areaId,
                        inboxState = inboxState ?: ItemKind.TASK.defaultInboxState(),
                    )
                }

                "note" -> { // NON-NLS
                    repository.insertLifeItem(
                        kind = ItemKind.NOTE,
                        title = title,
                        content = content,
                        activeProjectId = projectId,
                        homeAreaId = areaId,
                        inboxState = inboxState ?: ItemKind.NOTE.defaultInboxState(),
                    )
                }

                "record" -> { // NON-NLS
                    repository.insertLifeItem(
                        kind = ItemKind.RECORD,
                        title = title,
                        content = content,
                        activeProjectId = projectId,
                        homeAreaId = areaId,
                        inboxState = inboxState ?: ItemKind.RECORD.defaultInboxState(),
                    )
                }

                "project" -> { // NON-NLS
                    repository.insertLifeItem(
                        kind = ItemKind.PROJECT,
                        title = title,
                        content = content,
                        homeAreaId = areaId,
                        inboxState = inboxState ?: ItemKind.PROJECT.defaultInboxState(),
                    )
                }

                "area" -> { // NON-NLS
                    repository.insertLifeItem(
                        kind = ItemKind.AREA,
                        title = title,
                        content = content,
                        inboxState = inboxState ?: ItemKind.AREA.defaultInboxState(),
                    )
                }

                else -> {
                    repository.insertNode(
                        NodeEntity(
                            title = title,
                            content = content,
                            type = type,
                            projectId = projectId,
                            areaId = areaId,
                            inboxState =
                                inboxState
                                    ?: (type != "project" && type != "area"),
                            // NON-NLS
                            openLoopType = if (type == "open_loop") "unresolved_problem" else null, // NON-NLS
                            openLoopStalenessAt =
                                if (type == "open_loop") { // NON-NLS
                                    Clock.System
                                        .now()
                                        .plus(3, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                                        .toEpochMilliseconds()
                                } else {
                                    null
                                },
                            maintenanceType = if (type == "maintenance") "form" else null, // NON-NLS
                        ),
                    )
                }
            }
        }

    fun updateNode(node: NodeEntity) {
        scope.launch {
            val oldNode = repository.getNodeById(node.id)
            var updatedNode = node.copy(updatedAt = Clock.System.now().toEpochMilliseconds())

            if (oldNode != null &&
                oldNode.dueAt != null &&
                node.dueAt != null &&
                node.dueAt > oldNode.dueAt
            ) {
                updatedNode = updatedNode.copy(postponeCount = oldNode.postponeCount + 1)
            }

            repository.updateNode(updatedNode)

            if (oldNode == null || oldNode.content != node.content) {
                parseInternalLinks(node.id)
            }
        }
    }

    /**
     * Extracts a portion of a note or unstructured text into its own explicit, actionable
     * task node, linking it back to its origin via a "NEXT_STEP" relation.
     */
    fun extractNextStep(nodeId: Long) {
        scope.launch {
            repository.getNodeById(nodeId)?.let { node ->
                if (node.nextSmallestStep.isNullOrBlank() && node.content.isNotBlank()) {
                    val lines = node.content.lines().filter { it.isNotBlank() }
                    if (lines.isNotEmpty()) {
                        val firstLine =
                            lines.firstOrNull()?.trim()?.removePrefix("-")?.removePrefix("*")?.trim()?.ifBlank { defaultNextStepLabel() } ?: defaultNextStepLabel()
                        repository.updateNode(
                            node.copy(
                                nextSmallestStep = firstLine,
                                updatedAt = Clock.System.now().toEpochMilliseconds(),
                            ),
                        )
                    }
                }
            }
        }
    }

    /**
     * Takes a single large node, usually containing a bulleted or numbered list, and shatters
     * its content into distinct, granular sub-nodes (like checklist items) bound to a new
     * parent project node, effectively promoting the original node's intent to a larger scope.
     */
    fun splitIntoSubtasks(nodeId: Long) {
        scope.launch {
            repository.getNodeById(nodeId)?.let { node ->
                val lines =
                    node.content
                        .lines()
                        .map { it.trim() }
                        .filter { it.isNotBlank() && (it.startsWith("-") || it.startsWith("*")) }

                if (lines.isNotEmpty()) {
                    for (line in lines) {
                        val subtaskTitle = line.removePrefix("-").removePrefix("*").trim()
                        val subtaskId =
                            repository.insertNode(
                                NodeEntity(
                                    title = subtaskTitle,
                                    type = "task", // NON-NLS
                                    projectId = node.projectId,
                                    areaId = node.areaId,
                                    parentNodeId = node.id,
                                ),
                            )
                        repository.insertRelation(
                            RelationEntity(
                                fromNodeId = node.id,
                                toNodeId = subtaskId,
                                relationType = "DEPENDS_ON", // NON-NLS
                            ),
                        )
                    }
                    repository.updateNode(
                        node.copy(
                            content = "// SPLIT INTO SUBTASKS\n" + node.content, // NON-NLS
                            updatedAt = Clock.System.now().toEpochMilliseconds(),
                        ),
                    )
                }
            }
        }
    }

    /**
     * Captures the current title, content, and metadata of a specific node and saves it
     * as a historical `NodeSnapshotEntity`. This serves as a lightweight version control
     * mechanism, allowing users to revert destructive text edits.
     */
    fun createSnapshot(nodeId: Long) {
        scope.launch {
            repository.getNodeById(nodeId)?.let { node ->
                repository.insertSnapshot(
                    NodeSnapshotEntity(
                        nodeId = node.id,
                        title = node.title,
                        content = node.content,
                    ),
                )
            }
        }
    }

    /**
     * Reverts a node's title and content to a previously saved historical snapshot,
     * effectively undoing subsequent edits.
     */
    fun restoreSnapshot(snapshot: NodeSnapshotEntity) {
        scope.launch {
            repository.getNodeById(snapshot.nodeId)?.let { node ->
                repository.updateNode(
                    node.copy(
                        title = snapshot.title,
                        content = snapshot.content,
                        updatedAt = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            }
        }
    }

    /**
     * Combines the content and relational metadata of two existing nodes into a single, unified node.
     * The nodes in otherNodeIds are subsequently archived to prevent data duplication.
     */
    fun mergeNodes(
        primaryNodeId: Long,
        otherNodeIds: List<Long>,
    ) {
        scope.launch {
            val primary = repository.getNodeById(primaryNodeId) ?: return@launch
            var mergedContent = primary.content

            for (otherId in otherNodeIds) {
                repository.getNodeById(otherId)?.let { other ->
                    mergedContent += "\n\n--- MERGED FROM ${other.title} ---\n${other.content}"
                    archiveNodeInternal(other)
                    val relations = repository.getRelationsForNode(otherId).first()
                    relations.forEach { rel ->
                        if (rel.fromNodeId == otherId) {
                            repository.insertRelation(
                                RelationEntity(
                                    fromNodeId = primaryNodeId,
                                    toNodeId = rel.toNodeId,
                                    relationType = rel.relationType,
                                ),
                            )
                        } else if (rel.toNodeId == otherId) {
                            repository.insertRelation(
                                RelationEntity(
                                    fromNodeId = rel.fromNodeId,
                                    toNodeId = primaryNodeId,
                                    relationType = rel.relationType,
                                ),
                            )
                        }
                    }
                }
            }

            repository.updateNode(
                primary.copy(
                    content = mergedContent,
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
        }
    }

    fun splitNote(nodeId: Long) {
        scope.launch {
            repository.getNodeById(nodeId)?.let { node ->
                val sections =
                    node.content
                        .split(headingSplitRegex)
                        .filter { it.isNotBlank() }

                if (sections.size > 1) {
                    for (section in sections) {
                        val lines = section.lines()
                        val title =
                            lines.firstOrNull()?.removePrefix("# ")?.trim()?.ifBlank { defaultUntitledLabel() } ?: defaultUntitledLabel()
                        val content = lines.drop(1).joinToString("\n").trim()

                        repository.insertNode(
                            NodeEntity(
                                title = title,
                                content = content,
                                type = "note", // NON-NLS
                                projectId = node.projectId,
                                areaId = node.areaId,
                            ),
                        )
                    }
                    archiveNodeInternal(node)
                }
            }
        }
    }

    fun updateNodeStatus(
        node: NodeEntity,
        status: String,
    )
    {
        scope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            repository.updateNode(
                    node.copy(
                        status = status,
                    updatedAt = now,
                    completedAt = if (status == "done") now else node.completedAt, // NON-NLS
                    archivedAt = if (status == "archived") now else node.archivedAt, // NON-NLS
                    ),
                )

                if (status == "done" && node.isRecurring && node.recurringInterval != null) { // NON-NLS
                    val nextDue = calculateNextRecurringDate(node.dueAt ?: now, node.recurringInterval)
                    repository.insertNode(
                        node.copy(
                            id = 0,
                        status = "active", // NON-NLS
                        createdAt = now,
                        updatedAt = now,
                        completedAt = null,
                            dueAt = nextDue,
                            inboxState = false,
                    ),
                )
            }
        }
    }

    fun archiveNode(node: NodeEntity) {
        scope.launch { archiveNodeInternal(node) }
    }

    fun deleteNodePermanently(node: NodeEntity) {
        scope.launch {
            repository.deleteNode(node)
        }
    }

    fun togglePin(
        node: NodeEntity,
        isPinned: Boolean,
    )
    {
        scope.launch {
            if (isPinned) {
                repository.pinToToday(node.id)
                } else {
                    repository.unpinFromToday(node.id)
            }
        }
    }

    fun togglePermanentPin(node: NodeEntity) {
        scope.launch {
            repository.updateNode(
                node.copy(
                    isPinned = !node.isPinned,
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
        }
    }

    fun markAsProcessed(nodeId: Long) {
        scope.launch {
            repository.getNodeById(nodeId)?.let { node ->
                repository.updateNode(
                    node.copy(
                        inboxState = false,
                        updatedAt = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            }
        }
    }

    fun addProject(
        name: String,
        description: String = "",
        areaId: Long? = null,
    )
    {
        addNode(title = name, content = description, type = "project", areaId = areaId) // NON-NLS
        }

    fun addArea(name: String) {
        addNode(title = name, type = "area") // NON-NLS
    }

    fun updateOpenLoopType(
        node: NodeEntity,
        openLoopType: String,
    )
    {
        if (node.type != "open_loop") return // NON-NLS
            updateNode(
                node.copy(
                    openLoopType = openLoopType,
                    openLoopStalenessAt =
                        node.openLoopStalenessAt
                        ?: Clock.System
                            .now()
                            .plus(3, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                            .toEpochMilliseconds(),
            ),
        )
    }

    /**
     * Morphs an unstructured "open loop" node into a formal, actionable "task" node,
     * maintaining a relational paper trail to document the origin of the task.
     */
    fun convertOpenLoopToTask(nodeId: Long) {
        convertOpenLoop(nodeId, "task") // NON-NLS
    }

    fun convertOpenLoopToDecision(nodeId: Long) {
        convertOpenLoop(nodeId, "decision") // NON-NLS
    }

    fun convertOpenLoopToNote(nodeId: Long) {
        convertOpenLoop(nodeId, "note") // NON-NLS
    }

    /**
     * Marks an open loop as fully resolved and removes it from the active inbox state,
     * optionally appending a final resolution note detailing how the loop was closed.
     */
    fun resolveOpenLoop(
        nodeId: Long,
        resolutionNote: String? = null,
    ) {
        scope.launch {
            val node = repository.getNodeById(nodeId) ?: return@launch
            if (node.type != "open_loop") return@launch // NON-NLS
            val now = Clock.System.now().toEpochMilliseconds()
            repository.updateNode(
                node.copy(
                    status = "done", // NON-NLS
                    inboxState = false,
                    completedAt = now,
                    updatedAt = now,
                    completionNote =
                        resolutionNote?.trim()?.ifBlank { null }
                            ?: node.completionNote,
                ),
            )
        }
    }

    /**
     * Sweeps the system for all nodes typed as "open_loop" that have been marked as "done",
     * and transitions them to the "archived" state to declutter the user interface.
     */
    fun archiveResolvedOpenLoops() {
        scope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            currentAllNodes()
                .filter { it.type == "open_loop" && it.status == "done" } // NON-NLS
                .forEach { loop ->
                    repository.updateNode(
                        loop.copy(
                            status = "archived", // NON-NLS
                            archivedAt = now,
                            updatedAt = now,
                        ),
                    )
                }
        }
    }

    /**
     * Updates the maintenance type classification for a maintenance node.
     *
     * Validates that the node is of type "maintenance" before applying the update.
     * This classification drives how the item is surfaced in specific domain lenses
     * (e.g., categorizing an item as "bill" routes it to the Finance lens).
     *
     * @param node The node entity to modify.
     * @param maintenanceType The new maintenance type string (e.g., "bill", "appointment").
     */
    fun updateMaintenanceType(
        node: NodeEntity,
        maintenanceType: String,
    )
    {
        if (node.type != "maintenance") return // NON-NLS
            updateNode(node.copy(maintenanceType = maintenanceType))
        }

    /**
     * Manually overrides or sets the overdue timestamp for a maintenance node.
     *
     * Validates that the node is of type "maintenance" before applying the update.
     * If set, this timestamp allows the system to determine when a scheduled
     * recurring or one-off maintenance task requires urgent attention.
     *
     * @param node The node entity to modify.
     * @param timestamp The explicit epoch timestamp when the item becomes overdue, or null to clear it.
     */
    fun setMaintenanceOverdueAt(
        node: NodeEntity,
        timestamp: Long?,
    )
    {
        if (node.type != "maintenance") return // NON-NLS
            updateNode(node.copy(maintenanceOverdueAt = timestamp))
        }

    /**
     * Configures a maintenance node as recurring by setting its scheduling interval.
     *
     * Validates that the node is of type "maintenance" before updating. If a non-null
     * interval is provided, the node is marked as recurring and the interval strings
     * (recurringInterval and maintenanceInterval) are populated. If null, recurrence is disabled.
     *
     * @param node The node entity to modify.
     * @param interval The recurring interval definition (e.g., "1w", "1m"), or null to disable.
     */
    fun setMaintenanceRecurring(
        node: NodeEntity,
        interval: String?,
    )
    {
        if (node.type != "maintenance") return // NON-NLS
            updateNode(
                node.copy(
                    isRecurring = interval != null,
                    recurringInterval = interval,
                maintenanceInterval = interval,
            ),
        )
    }

    /**
     * Sets a project node's execution status between active pursuit and a paused state.
     *
     * Validates that the node is of type "project" before updating. This primarily dictates
     * if the project surfaces in daily actionable views ("active") or is deferred for later review ("on_hold").
     *
     * @param project The project node entity to modify.
     * @param active True to set the project status to "active", false for "on_hold".
     */
    fun setProjectActivePhase(
        project: NodeEntity,
        active: Boolean,
    ) {
        if (project.type != "project") return // NON-NLS
        updateNode(project.copy(projectStatus = if (active) "active" else "on_hold")) // NON-NLS
    }

    /**
     * Assigns a fixed, temporary active window (defined in days) to a node, establishing
     * an implicit deadline. Often used to force action on items that might otherwise
     * linger indefinitely without explicit due dates.
     */
    fun setTemporaryFocusPeriod(
        node: NodeEntity,
        days: Int,
    ) {
        val safeDays = days.coerceIn(1, 30)
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        updateNode(
            node.copy(
                startAt = now.toEpochMilliseconds(),
                dueAt = now.plus(safeDays, DateTimeUnit.DAY, zone).toEpochMilliseconds(),
                status = "active", // NON-NLS
            ),
        )
    }

    fun clearTemporaryFocusPeriod(node: NodeEntity) {
        updateNode(node.copy(startAt = null))
    }

    /**
     * Schedules or reschedules the specific starting execution date for a task node.
     *
     * Validates that the node is of type "task" before updating. This defines when the
     * task officially becomes active or available for execution, preventing it from
     * surfacing prematurely.
     *
     * @param node The task node entity to modify.
     * @param workAt The epoch timestamp indicating when the task should begin, or null to clear.
     */
    fun setWorkDate(
        node: NodeEntity,
        workAt: Long?,
    )
    {
        if (node.type != "task") return // NON-NLS
            updateNode(node.copy(startAt = workAt))
        }

    /**
     * Upgrades or downgrades a standard node to a "seasonal_goal" by applying the appropriate
     * tags and modifying its internal `noteType`. This dictates how the node is surfaced
     * in high-level dashboard summaries.
     */
    fun toggleSeasonalGoal(
        node: NodeEntity,
        enabled: Boolean,
    )
    {
        scope.launch {
            setTagOnNode(node.id, "seasonal_goal", enabled) // NON-NLS
            updateNode(node.copy(noteType = if (enabled) "goal_seasonal" else node.noteType)) // NON-NLS
        }
    }

    /**
     * Inserts a specialized "period_marker" note, which acts as a temporal milestone in
     * the system (e.g., "Moved to New City", "Started New Job") to help contextualize
     * other events surrounding that timeframe.
     */
    fun addLifePeriodMarker(
        title: String,
        content: String = "",
    )
    {
        scope.launch {
            val markerId = addNodeForResult(title, content, "note", inboxState = false) // NON-NLS
            val markerNode = repository.getNodeById(markerId) ?: return@launch
            repository.updateNode(
                markerNode.copy(
                    noteType = "period_marker", // NON-NLS
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            setTagOnNode(markerId, "life_period_marker", true) // NON-NLS
        }
    }

    /**
     * An automated system maintenance routine that unpins all current active items, aggressively
     * archives old completed tasks (older than 30 days), and drops a reflection marker to
     * signal the start of a new month.
     */
    fun runMonthlyReset() {
        scope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val monthAgo = now - (30L * 24 * 60 * 60 * 1000)
            currentTodayNodes().forEach { node ->
                repository.unpinFromToday(node.id)
            }
            currentAllNodes()
                .filter {
                    it.status == "done" &&
                            (
                                    it.completedAt
                                        ?: 0L
                                    ) < monthAgo &&
                            it.type == "task" // NON-NLS
                }.forEach { doneTask ->
                    repository.updateNode(
                        doneTask.copy(
                            status = "archived", // NON-NLS
                            archivedAt = now,
                            updatedAt = now,
                        ),
                    )
                }
            repository.insertNode(
                NodeEntity(
                    type = "note", // NON-NLS
                    title = "Monthly reset ${
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    }",
                    content = "Auto-generated monthly reset summary and cleanup marker.",
                    noteType = "reflection", // NON-NLS
                    inboxState = false,
                ),
            )
        }
    }

    private fun convertOpenLoop(
        nodeId: Long,
        targetType: String,
    )
    {
        scope.launch {
            val source = repository.getNodeById(nodeId) ?: return@launch
            if (source.type != "open_loop") return@launch // NON-NLS
            val now = Clock.System.now().toEpochMilliseconds()

            val createdId =
                repository.insertNode(
                    NodeEntity(
                        title =
                            when (targetType)
                            {
                                "task" -> "Follow-up: ${source.title}"

                                // NON-NLS
                                "decision" -> "Decision: ${source.title}"

                                // NON-NLS
                                "note" -> "Open loop note: ${source.title}"

                                // NON-NLS
                                else -> source.title
                            },
                        content =
                            buildString {
                                append(source.content)
                                if (source.content.isNotBlank()) append("\n\n")
                                append(
                                    "Converted from open loop (${source.openLoopType ?: "untyped"}).", // NON-NLS
                                )
                            },
                        type = targetType,
                        projectId = source.projectId,
                        areaId = source.areaId,
                        decisionStatus = if (targetType == "decision") "pending" else null, // NON-NLS
                        decisionCategory = if (targetType == "decision") "major" else null, // NON-NLS
                    ),
                )

            repository.insertRelation(
                RelationEntity(
                    fromNodeId = source.id,
                    toNodeId = createdId,
                    relationType = "DERIVED_FROM", // NON-NLS
                ),
            )

            repository.updateNode(
                source.copy(
                    status = "done", // NON-NLS
                    inboxState = false,
                    completedAt = now,
                    updatedAt = now,
                    completionNote = "Converted to ${targetType.uppercase()}", // NON-NLS
                ),
            )
        }
    }

    private suspend fun archiveNodeInternal(node: NodeEntity) {
        repository.updateNode(
            node.copy(
                status = "archived", // NON-NLS
                updatedAt = Clock.System.now().toEpochMilliseconds(),
            ),
        )
    }
}
