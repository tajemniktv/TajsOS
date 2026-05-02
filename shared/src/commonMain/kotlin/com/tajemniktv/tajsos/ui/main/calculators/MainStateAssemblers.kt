/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.main.calculators

import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.CalendarEventEntity
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.PackRegistry
import com.tajemniktv.tajsos.data.ProtocolHistoryEntity
import com.tajemniktv.tajsos.data.ScheduleEntryEntity
import com.tajemniktv.tajsos.data.ScheduleEntryKind
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.TrackEntryEntity
import com.tajemniktv.tajsos.data.buildModeQueryProfile
import com.tajemniktv.tajsos.data.isAreaItem
import com.tajemniktv.tajsos.data.isKnowledgeItem
import com.tajemniktv.tajsos.data.isNoteItem
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.DashboardUIState
import com.tajemniktv.tajsos.ui.main.state.CalendarEntry
import com.tajemniktv.tajsos.ui.main.state.EntryType
import com.tajemniktv.tajsos.ui.main.state.NodeCategorization
import com.tajemniktv.tajsos.ui.main.state.PlaybookItem
import com.tajemniktv.tajsos.ui.main.state.PlaybookSnapshot
import com.tajemniktv.tajsos.ui.main.state.PlaybookTemplate
import com.tajemniktv.tajsos.ui.main.state.ProtocolHistoryItem
import com.tajemniktv.tajsos.ui.main.state.TransitionProtocolItem
import com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate
import com.tajemniktv.tajsos.ui.main.state.TransitionProtocolsSnapshot
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

fun buildCalendarEntries(
    nodes: List<NodeWithPin>,
    scheduleEntries: List<ScheduleEntryEntity>,
    externalEvents: List<CalendarEventEntity>,
): List<CalendarEntry> {
    val entries = mutableListOf<CalendarEntry>()
    val nodesById = nodes.associateBy { it.node.id }

    scheduleEntries.forEach { entry ->
        val node = nodesById[entry.itemId]?.node ?: return@forEach
        if (node.status == "archived") return@forEach

        val labelPrefix =
            when (ScheduleEntryKind.fromStorageKey(entry.kind))
            {
                ScheduleEntryKind.DUE -> "Due"

                // NON-NLS
                ScheduleEntryKind.REMINDER -> "Reminder"

                // NON-NLS
                ScheduleEntryKind.START -> "Start"

                // NON-NLS
                else -> null
            }

        val title =
            buildString {
                if (node.status == "done") append("✓ ")
                if (labelPrefix != null) {
                    append(labelPrefix)
                    append(": ")
                }
                append(node.title)
            }

        entries.add(
            CalendarEntry(
                id = "schedule_${entry.id}",
                title = title,
                description = node.content.ifBlank { entry.note },
                startAt = entry.scheduledAt,
                endAt = entry.endAt ?: (entry.scheduledAt + (3600 * 1000)),
                isAllDay = false,
                type = EntryType.INTERNAL,
                originalId = node.id,
            ),
        )
    }

    val itemsWithSchedule = scheduleEntries.mapTo(mutableSetOf()) { it.itemId }
    nodes.forEach { item ->
        val node = item.node
        if (node.id !in itemsWithSchedule) {
            val time = node.startAt ?: node.dueAt ?: node.reminderAt
            if (time != null && node.status != "archived") {
                entries.add(
                    CalendarEntry(
                        id = "node_${node.id}",
                        title = if (node.status == "done") "✓ ${node.title}" else node.title,
                        description = node.content,
                        startAt = time,
                        endAt = time + (3600 * 1000),
                        isAllDay = false,
                        type = EntryType.INTERNAL,
                        originalId = node.id,
                    ),
                )
            }
        }
    }

    externalEvents.forEach { event ->
        entries.add(
            CalendarEntry(
                id = "ext_${event.id}",
                title = event.title,
                description = event.description,
                startAt = event.startAt,
                endAt = event.endAt,
                isAllDay = event.isAllDay,
                type = EntryType.EXTERNAL,
                originalId = event.id,
            ),
        )
    }

    return entries.sortedBy { it.startAt }
}

fun categorizeNodes(list: List<NodeWithPin>): NodeCategorization {
    val now = Clock.System.now().toEpochMilliseconds()
    val inbox = mutableListOf<NodeWithPin>()
    val archived = mutableListOf<NodeWithPin>()
    val reminders = mutableListOf<NodeEntity>()

    for (item in list) {
        val node = item.node

        if (node.status == "archived") { // NON-NLS
            archived.add(item)
        } else {
            if (node.inboxState && node.type != "project" && node.type != "area") { // NON-NLS
                inbox.add(item)
            }

            if (node.status == "active" && node.reminderAt != null && node.reminderAt <= now) { // NON-NLS
                reminders.add(node)
            }
        }
    }

    return NodeCategorization(inbox, archived, reminders)
}

fun buildProtocolHistoryItems(
    history: List<ProtocolHistoryEntity>,
    nodes: List<NodeWithPin>,
): List<ProtocolHistoryItem> {
    val byId = nodes.associateBy { it.node.id }
    return history.map { item ->
        ProtocolHistoryItem(
            historyId = item.id,
            protocolNodeId = item.protocolNodeId,
            protocolLabel = byId[item.protocolNodeId]?.node?.title ?: "Unknown protocol",
            executedAt = item.executedAt,
            notes = item.notes,
        )
    }
}

fun buildTransitionProtocolsSnapshot(
    protocolNodes: List<NodeWithPin>,
    historyItems: List<ProtocolHistoryItem>,
    templates: List<TransitionProtocolTemplate>,
): TransitionProtocolsSnapshot {
    val usageByLabel = historyItems.groupBy { normalizeProtocolLabel(it.protocolLabel) }
    val protocolItems =
        protocolNodes
            .map { protocol ->
                val (done, total) = protocolChecklistProgress(protocol.node.content)
                val usage = usageByLabel[normalizeProtocolLabel(protocol.node.title)].orEmpty()
                TransitionProtocolItem(
                    node = protocol,
                    checklistDone = done,
                    checklistTotal = total,
                    triggerCount = usage.size,
                    lastTriggeredAt = usage.maxOfOrNull { it.executedAt },
                )
            }.sortedWith(
                compareByDescending<TransitionProtocolItem> { it.lastTriggeredAt ?: 0L }
                    .thenBy {
                        it.node.node.title
                            .lowercase()
                    },
            )

    return TransitionProtocolsSnapshot(
        protocols = protocolItems,
        templates = templates,
        recommendedLabel = recommendProtocolLabel(templates),
    )
}

fun buildPlaybookSnapshot(
    protocolNodes: List<NodeWithPin>,
    historyItems: List<ProtocolHistoryItem>,
    mode: ModeEntity?,
    entries: List<TrackEntryEntity>,
    templates: List<PlaybookTemplate>,
): PlaybookSnapshot {
    val playbookNodes =
        protocolNodes.filter { node ->
            val normalized = normalizeProtocolLabel(node.node.title)
            templates.any { normalizeProtocolLabel(it.label) == normalized } ||
                    node.tags.any { it.normalizedName == "playbook" } || // NON-NLS
                node.node.relationshipContext?.contains(
                    "playbook",
                    ignoreCase = true,
                ) == true // NON-NLS
        }
    val usageByLabel = historyItems.groupBy { normalizeProtocolLabel(it.protocolLabel) }
    val playbooks =
        playbookNodes
            .map { playbook ->
                val (done, total) = protocolChecklistProgress(playbook.node.content)
                val usage = usageByLabel[normalizeProtocolLabel(playbook.node.title)].orEmpty()
                val linkedMode = parsePlaybookModeKey(playbook.node.relationshipContext)
                PlaybookItem(
                    node = playbook,
                    checklistDone = done,
                    checklistTotal = total,
                    triggerCount = usage.size,
                    linkedModeKey = linkedMode,
                    linkedAreaId = playbook.node.areaId,
                    isCustom =
                        templates.none {
                            normalizeProtocolLabel(it.label) == normalizeProtocolLabel(playbook.node.title)
                        },
                )
            }.sortedWith(
                compareByDescending<PlaybookItem> { it.triggerCount }
                    .thenBy {
                        it.node.node.title
                            .lowercase()
                    },
            )

    return PlaybookSnapshot(
        playbooks = playbooks,
        templates = templates,
        suggestedPlaybookLabel = suggestPlaybookLabel(mode, entries),
    )
}

/**
 * Assembles the comprehensive view state required for the Dashboard.
 *
 * This function orchestrates a complex, non-obvious data flow by combining raw database nodes
 * with current Operating Mode filters (e.g., excluding specific areas/types). It evaluates
 * heuristic rules to calculate critical state projections such as system load, open loop decay,
 * and area health imbalances. It also derives contextual suggestions (like mode or context hints)
 * based on the current time and available tasks.
 */
suspend fun buildDashboardUIState(
    repository: AppRepository,
    nodes: List<NodeWithPin>,
    modesList: List<ModeEntity>,
    activeId: Long?,
    areasList: List<NodeEntity>,
    packs: PackRegistry,
): DashboardUIState {
    val accessibleModes = modesList.filter { packs.canUseMode(it.key) }
    val mode = accessibleModes.find { it.id == activeId }
    val now = Clock.System.now().toEpochMilliseconds()
    val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
    val fourteenDaysAgo = now - (14 * 24 * 60 * 60 * 1000L)

    val prefs = if (mode != null) repository.getPreferencesForMode(mode.id).first() else null
    val areaFilters =
        if (mode != null && mode.key != "ALL") { // NON-NLS
            repository.getAreaFiltersForMode(mode.id).first()
        } else {
            emptyList()
        }
    val includedAreaIds = areaFilters.filter { it.include }.map { it.areaId }
    val excludedAreaIds = areaFilters.filter { !it.include }.map { it.areaId }

    var filteredNodes = nodes
    if (mode?.key != "ALL")
    { // NON-NLS
        if (includedAreaIds.isNotEmpty()) {
                filteredNodes =
                filteredNodes.filter { it.node.areaId in includedAreaIds || it.node.isAreaItem() }
        }
        if (excludedAreaIds.isNotEmpty()) {
            filteredNodes = filteredNodes.filter { it.node.areaId !in excludedAreaIds }
        }
    }

    val typeFilters =
        if (mode != null && mode.key != "ALL") { // NON-NLS
            repository.getTypeFiltersForMode(mode.id).first()
        } else {
            emptyList()
        }
    val includedTypes = typeFilters.filter { it.include }.map { it.nodeType }
    val excludedTypes = typeFilters.filter { !it.include }.map { it.nodeType }

    if (mode?.key != "ALL")
    { // NON-NLS
        if (includedTypes.isNotEmpty()) {
                filteredNodes = filteredNodes.filter { it.node.type in includedTypes }
        }
        if (excludedTypes.isNotEmpty()) {
            filteredNodes = filteredNodes.filter { it.node.type !in excludedTypes }
        }
    }

    if (mode?.key == "RECOVERY" || mode?.key == "LOW_BATTERY" || mode?.key == "CANT_THINK") { // NON-NLS
        filteredNodes =
            filteredNodes.filter {
                it.node.type != "task" || (it.node.energyLevel == 1 && it.node.friction == "easy") // NON-NLS
            }
    }

    val activeTasks =
        filteredNodes.filter { it.node.isTaskItem() && it.node.taskStateOrNull() == TaskState.ACTIVE }
    val overdue =
        filteredNodes.filter { it.node.dueAt != null && it.node.dueAt < now && it.node.status == "active" }
    val pinnedK =
        filteredNodes.filter {
            it.node.isPinned && it.node.isKnowledgeItem()
        }

    val openLoops =
        filteredNodes.filter { it.node.type == "open_loop" && it.node.status == "active" } // NON-NLS
    val decisions =
        filteredNodes.filter { it.node.type == "decision" && it.node.status == "active" } // NON-NLS
    val maintenance =
        filteredNodes.filter { it.node.type == "maintenance" && it.node.status == "active" } // NON-NLS
    val maintenanceSnapshot =
        calculateMaintenanceSnapshot(
            nodes,
        )
    val protocols =
        filteredNodes.filter { it.node.type == "protocol" && it.node.status == "active" } // NON-NLS
    val people =
        filteredNodes.filter { it.node.type == "person" && it.node.status == "active" } // NON-NLS
    val openLoopDecayScores =
        openLoops.map {
            openLoopDecayScore(
                it.node,
                now,
            )
        }
    val openLoopsDecayAverage =
        if (openLoopDecayScores.isNotEmpty()) openLoopDecayScores.average().toInt() else 0
    val openLoopsOverloadWarning =
        when
            {
                openLoops.size >= 12 -> "OPEN LOOPS OVERLOAD // CLOSE LOOPS BEFORE NEW INTAKE"
                openLoopsDecayAverage >= 60 -> "OPEN LOOPS DECAYING // RUN OPEN LOOP REVIEW"
                else -> null
            }

    val areaSnapshot =
        calculateAreaHealthSnapshot(
            nodes,
            areasList,
        )
    val areaHealthMap = areaSnapshot.areas.associate { it.areaId to it.status }
    val areaHealthMetrics = areaSnapshot.areas.associateBy { it.areaId }

    val loadScore = (activeTasks.size * 2) + (openLoops.size * 3) + (overdue.size * 5)
    val fragmentation = activeTasks.groupBy { it.node.projectId }.size * 5
    val capWarning =
        when
            {
                loadScore > 100 -> "SYSTEM OVERLOADED // REDUCE INTAKE"
                fragmentation > 40 -> "ATTENTION FRAGMENTED // FOCUS ON ONE AREA"
                else -> null
            }

    val contexts =
        filteredNodes
            .filter { it.node.status == "active" && it.node.type == "task" } // NON-NLS
            .groupBy { it.node.locationContext ?: "general" } // NON-NLS

    val localNow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val suggestion =
        when
            {
                localNow.hour >= 22 && mode?.key != "SHUTDOWN" && packs.canUseMode("SHUTDOWN") -> "SHUTDOWN"

            // NON-NLS
                loadScore > 80 && mode?.key != "RECOVERY" && mode?.key != "LOW_BATTERY" -> "RECOVERY"

            // NON-NLS
                else -> null
            }
    val contextPriorityKeys =
        when (localNow.hour)
        {
            in 22..23, in 0..5 ->
            {
                listOf("at_home", "low_energy", "10_minute")
                }

            // NON-NLS
            in 6..9, in 16..18 -> {
                listOf(
                    "commute_friendly",
                    "waiting_room",
                    "phone_okay",
                )
            }

            // NON-NLS
            else -> {
                listOf("on_campus", "laptop_required", "high_focus")
            } // NON-NLS
        }

    fun matchesContextKey(
        node: NodeEntity,
        key: String,
    ): Boolean =
        node.locationContext == key ||
            node.energyContext == key ||
            node.deviceContext == key ||
            node.socialContext == key ||
            node.timeWindowContext == key

    val suggestedContextKey =
        contextPriorityKeys.firstOrNull { key ->
            activeTasks.any {
                matchesContextKey(
                    it.node,
                    key,
                )
            }
        }
    val suggestedContextTasks =
        if (suggestedContextKey != null) {
            activeTasks.filter { matchesContextKey(it.node, suggestedContextKey) }.take(5)
        } else {
            emptyList()
        }

    return DashboardUIState(
        tasksCount = activeTasks.size,
        notesCount = filteredNodes.count { it.node.isKnowledgeItem() },
        staleTasksCount = calculateStaleTasks(nodes.map { it.node }, Clock.System.now()).size,
        pinnedKnowledge = pinnedK,
        upcomingDeadlines =
            filteredNodes
                .filter { it.node.dueAt != null && it.node.status == "active" }
                .sortedBy { it.node.dueAt }
                .take(3),
        overdueNodes = overdue,
        relevantNote =
            filteredNodes
                .filter { it.node.isNoteItem() && it.node.status == "active" } // NON-NLS
                .sortedByDescending { it.node.updatedAt }
                .firstOrNull(),
        lowEnergyTasks =
            filteredNodes.filter {
                it.node.isTaskItem() && it.node.taskStateOrNull() == TaskState.ACTIVE && it.node.energyLevel == 1
            },
        batchableTasks = activeTasks.groupBy { it.node.areaId },
        quickWins =
            filteredNodes.filter {
                it.node.isTaskItem() &&
                    it.node.taskStateOrNull() == TaskState.ACTIVE &&
                    it.node.energyLevel == 1 &&
                    it.node.friction == "easy" // NON-NLS
            },
        deepWork =
            filteredNodes.filter {
                it.node.isTaskItem() && it.node.taskStateOrNull() == TaskState.ACTIVE && it.node.energyLevel == 3
            },
        topTakeaways = filteredNodes.filter { it.node.isNoteItem() && it.node.noteState == "takeaway" }, // NON-NLS
        readLaterVault = filteredNodes.filter { it.node.noteType == "read_later" && it.node.status == "active" }, // NON-NLS
        quoteVault = filteredNodes.filter { it.node.noteType == "quote" && it.node.status == "active" }, // NON-NLS
        ideaIncubator = filteredNodes.filter { it.node.type == "idea" && it.node.status == "active" && it.node.projectId == null }, // NON-NLS
        archivedThisWeek =
            nodes.filter {
                it.node.status == "archived" && (it.node.archivedAt ?: 0) >= sevenDaysAgo // NON-NLS
            },
        neglectedThisWeek =
            filteredNodes.filter {
                it.node.isTaskItem() && it.node.taskStateOrNull() == TaskState.ACTIVE && it.node.updatedAt < sevenDaysAgo
            },
        foundationalNotes =
            filteredNodes
                .filter {
                    (it.node.type == "note" || it.node.type == "idea") && // NON-NLS
                        it.tags.any { tag ->
                            tag.name.equals(
                                "foundational", // NON-NLS
                                ignoreCase = true,
                            )
                        }
                }.take(1),
        resourceHighlights =
            filteredNodes
                .filter { it.node.type == "resource" && it.node.status == "active" } // NON-NLS
                .shuffled()
                .take(2),
        stickyNotes = filteredNodes.filter { it.node.isSticky && it.node.status == "active" }, // NON-NLS
        criticalProjects =
            filteredNodes
                .filter { it.node.type == "project" && it.node.status == "active" } // NON-NLS
                .map { it.node }
                .filter { proj ->
                    val projectNodes = nodes.filter { it.node.projectId == proj.id }
                    val hasCritical =
                        projectNodes.any {
                            it.node.status == "active" && it.node.isHardDeadline && it.node.dueAt != null && it.node.dueAt < now // NON-NLS
                        }
                    val isNeglected =
                        proj.status == "active" && !proj.isFrozen && projectNodes.none { it.node.updatedAt >= fourteenDaysAgo } // NON-NLS
                    hasCritical || isNeglected
                },
        forgottenWisdom =
            filteredNodes
                .filter {
                    (it.node.type == "note" || it.node.type == "idea") && // NON-NLS
                            it.node.status == "active" && // NON-NLS
                        (it.node.noteType == "evergreen" || it.node.updatedAt < (now - 30 * 24 * 60 * 60 * 1000L)) // NON-NLS
                }.shuffled()
                .firstOrNull(),
        deservesAttention =
            filteredNodes
                .filter {
                    it.node.status == "active" &&
                            it.node.type == "task" && // NON-NLS
                        !it.node.isPinned &&
                        it.node.dueAt == null &&
                        it.node.updatedAt < sevenDaysAgo
                }.take(2),
        areaHealth = areaHealthMap,
        areaHealthMetrics = areaHealthMetrics,
        dominantAreaId = areaSnapshot.dominantAreaId,
        disappearingAreaIds = areaSnapshot.disappearingAreaIds,
        areaImbalanceScore = areaSnapshot.imbalanceScore,
        areaImbalanceLabel = areaSnapshot.imbalanceLabel,
        openLoopsOverloadWarning = openLoopsOverloadWarning,
        openLoopsDecayAverage = openLoopsDecayAverage,
        maintenanceAdminDebtMeter = maintenanceSnapshot.adminDebtMeter,
        maintenanceOverdueWarning = maintenanceSnapshot.overdueWarning,
        systemLoad = loadScore.coerceIn(0, 100),
        fragmentation = fragmentation.coerceIn(0, 100),
        capacityWarning = capWarning,
        openLoops = openLoops,
        pendingDecisions = decisions,
        maintenanceQueue = maintenance,
        activeProtocols = protocols,
        relationshipsToContact = people.filter { (it.node.lastContactAt ?: 0) < fourteenDaysAgo },
        contextClusteredTasks = contexts,
        currentMode = mode,
        modePreferences = prefs,
        modeQueryProfile =
            if (mode != null && prefs != null) {
                buildModeQueryProfile(
                    preference = prefs,
                    areaFilters = areaFilters,
                    typeFilters = typeFilters,
                )
            } else {
                null
            },
        tinyVictories =
            nodes
                .filter { it.node.status == "done" && it.node.completedAt != null && it.node.completedAt >= sevenDaysAgo } // NON-NLS
                .take(5),
        shoppingList = nodes.filter { it.node.status == "active" && it.tags.any { t -> t.name.lowercase() == "shopping" } }, // NON-NLS
        unresolvedBureaucracy =
            nodes.filter {
                it.node.type == "maintenance" &&
                    it.node.status == "active" && // NON-NLS
                    it.node.createdAt < sevenDaysAgo
            },
        modeSuggestion = suggestion,
        suggestedContextKey = suggestedContextKey,
        suggestedContextTasks = suggestedContextTasks,
    )
}
