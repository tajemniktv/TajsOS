/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tajemniktv.tajsos.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class InsightsData(
    val weeklyCaptures: Int = 0,
    val weeklyCompletions: Int = 0,
    val weeklyFocusHours: Double = 0.0,
    val bestFocusHour: Int = -1,
    val avgMood: Double = 0.0,
    val avgEnergy: Double = 0.0,
    val avgFocus: Double = 0.0,
    val neglectedProjects: List<NodeEntity> = emptyList(),
    val captureToActionRatio: Double = 0.0,
    val autoPreparedReview: String = "",
    val avgSessionMinutes: Int = 0,
    val inboxGrowth: Int = 0,
    val archiveRate: Double = 0.0,
    val completionsByArea: Map<Long, Int> = emptyMap(),
    val completionsByProject: Map<Long, Int> = emptyMap(),
    val mostProductiveHour: Int = -1,
    val postponeFrequency: Int = 0,
    val backlogPressure: Double = 0.0,
    val chaosScore: Int = 0,
    val contextSwitchingRate: Double = 0.0,
    val moodVsCompletions: Double = 0.0,
    val sleepVsFocus: Double = 0.0,
    val energyVsCaptures: Double = 0.0,
    val anxietyVsAvoidance: Double = 0.0,
    val medsEffectiveness: Double = 0.0,
    val mostPostponedAreaId: Long? = null,
    val captureTimePattern: String? = null, // Morning, Afternoon, Evening, Night
    val projectsWithoutTasks: List<NodeEntity> = emptyList(),
    val neglectedAreas: List<NodeEntity> = emptyList(),
    val projectEntropy: Map<Long, Double> = emptyMap(), // projectId to entropy score
    val contextStability: Double = 0.0, // 0.0 to 1.0
    val passiveBehaviorSummary: String = "",
)

data class DashboardUIState(
    val tasksCount: Int = 0,
    val notesCount: Int = 0,
    val pinnedKnowledge: List<NodeWithPin> = emptyList(),
    val upcomingDeadlines: List<NodeWithPin> = emptyList(),
    val overdueNodes: List<NodeWithPin> = emptyList(),
    val relevantNote: NodeWithPin? = null,
    val lowEnergyTasks: List<NodeWithPin> = emptyList(),
    val batchableTasks: Map<Long?, List<NodeWithPin>> = emptyMap(),
    val quickWins: List<NodeWithPin> = emptyList(),
    val deepWork: List<NodeWithPin> = emptyList(),
    val topTakeaways: List<NodeWithPin> = emptyList(),
    val readLaterVault: List<NodeWithPin> = emptyList(),
    val quoteVault: List<NodeWithPin> = emptyList(),
    val ideaIncubator: List<NodeWithPin> = emptyList(),
    val archivedThisWeek: List<NodeWithPin> = emptyList(),
    val neglectedThisWeek: List<NodeWithPin> = emptyList(),
    val foundationalNotes: List<NodeWithPin> = emptyList(),
    val resourceHighlights: List<NodeWithPin> = emptyList(),
    val stickyNotes: List<NodeWithPin> = emptyList(),
    val criticalProjects: List<NodeEntity> = emptyList(),
    val forgottenWisdom: NodeWithPin? = null,
    val deservesAttention: List<NodeWithPin> = emptyList(),
)

class MainViewModel(
    private val repository: AppRepository,
    private val preferencesRepository: PreferencesRepository,
    private val calendarManager: com.tajemniktv.tajsos.calendar.CalendarManager,
) : ViewModel() {
    val allNodes: StateFlow<List<NodeWithPin>> =
        repository
            .getAllNodes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeNodes: StateFlow<List<NodeWithPin>> =
        allNodes
            .map { list ->
                list.filter { it.node.status != "archived" }
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardUIState: StateFlow<DashboardUIState> =
        activeNodes.map { nodes ->
            val now = Clock.System.now().toEpochMilliseconds()
            val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
            DashboardUIState(
                tasksCount = nodes.count { it.node.type == "task" },
                notesCount = nodes.count { it.node.type == "note" || it.node.type == "idea" || it.node.type == "resource" },
                pinnedKnowledge = nodes.filter { it.node.isPinned && (it.node.type == "note" || it.node.type == "idea" || it.node.type == "resource") },
                upcomingDeadlines = nodes.filter { it.node.dueAt != null && it.node.status == "active" }
                    .sortedBy { it.node.dueAt }.take(3),
                overdueNodes = nodes.filter { it.node.dueAt != null && it.node.dueAt < now && it.node.status == "active" },
                relevantNote = nodes.filter { (it.node.type == "note" || it.node.type == "idea") && it.node.status == "active" }
                    .sortedByDescending { it.node.updatedAt }.firstOrNull(),
                lowEnergyTasks = nodes.filter { it.node.type == "task" && it.node.status == "active" && it.node.energyLevel == 1 },
                batchableTasks = nodes.filter { it.node.type == "task" && it.node.status == "active" }
                    .groupBy { it.node.areaId }.filter { it.value.size >= 3 },
                quickWins = nodes.filter { it.node.type == "task" && it.node.status == "active" && it.node.energyLevel == 1 && it.node.friction == "easy" },
                deepWork = nodes.filter { it.node.type == "task" && it.node.status == "active" && it.node.energyLevel == 3 },
                topTakeaways = nodes.filter { (it.node.type == "note" || it.node.type == "idea") && it.node.noteState == "takeaway" },
                readLaterVault = nodes.filter { it.node.noteType == "read_later" && it.node.status == "active" },
                quoteVault = nodes.filter { it.node.noteType == "quote" && it.node.status == "active" },
                ideaIncubator = nodes.filter { it.node.type == "idea" && it.node.status == "active" && it.node.projectId == null },
                archivedThisWeek = nodes.filter {
                    it.node.status == "archived" && (it.node.archivedAt ?: 0) >= sevenDaysAgo
                },
                neglectedThisWeek = nodes.filter { it.node.status == "active" && it.node.type == "task" && it.node.updatedAt < sevenDaysAgo },
                foundationalNotes = nodes.filter {
                    (it.node.type == "note" || it.node.type == "idea") && it.tags.any { tag ->
                        tag.name.equals(
                            "foundational",
                            ignoreCase = true
                        )
                    }
                }.take(1),
                resourceHighlights = nodes.filter { it.node.type == "resource" && it.node.status == "active" }
                    .shuffled().take(2),
                stickyNotes = nodes.filter { it.node.isSticky && it.node.status == "active" },
                criticalProjects = nodes.filter { it.node.type == "project" && it.node.status == "active" }
                    .map { it.node }.filter { proj ->
                        val projectNodes = nodes.filter { it.node.projectId == proj.id }
                        val hasCritical = projectNodes.any {
                            it.node.status == "active" && it.node.isHardDeadline && it.node.dueAt != null && it.node.dueAt < now
                        }
                        val isNeglected =
                            proj.status == "active" && !proj.isFrozen && projectNodes.none { it.node.updatedAt >= (now - 14 * 24 * 60 * 60 * 1000L) }
                        hasCritical || isNeglected
                    },
                forgottenWisdom = nodes.filter {
                    (it.node.type == "note" || it.node.type == "idea") &&
                            it.node.status == "active" &&
                            (it.node.noteType == "evergreen" || it.node.updatedAt < (now - 30 * 24 * 60 * 60 * 1000L))
                }.shuffled().firstOrNull(),
                deservesAttention = nodes.filter {
                    it.node.status == "active" && it.node.type == "task" &&
                            !it.node.isPinned && it.node.dueAt == null &&
                            it.node.updatedAt < (now - 7 * 24 * 60 * 60 * 1000L)
                }.take(2),
            )
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUIState())

    val todayNodes: StateFlow<List<NodeEntity>> =
        repository
            .getTodayNodes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trackEntries: StateFlow<List<TrackEntryEntity>> =
        repository
            .getAllTrackEntries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjects: StateFlow<List<NodeEntity>> =
        repository
            .getNodesByType("project")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAreas: StateFlow<List<NodeEntity>> =
        repository
            .getNodesByType("area")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarProviders: StateFlow<List<CalendarProviderEntity>> =
        repository
            .getAllCalendarProviders()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRelations: StateFlow<List<RelationEntity>> =
        repository
            .getAllRelations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarEntries: StateFlow<List<CalendarEntry>> =
        combine(
            allNodes,
            repository.getCalendarEventsInRange(
                0,
                Long.MAX_VALUE,
            ), // In MVP we can fetch all or a large range
        ) { nodes, externalEvents ->
            val entries = mutableListOf<CalendarEntry>()

            // Map internal nodes
            nodes.forEach { item ->
                val node = item.node
                val time = node.startAt ?: node.dueAt ?: node.reminderAt
                if (time != null && node.status != "archived") {
                    entries.add(
                        CalendarEntry(
                            id = "node_${node.id}",
                            title = if (node.status == "done") "✓ ${node.title}" else node.title,
                            description = node.content,
                            startAt = time,
                            endAt = time + (3600 * 1000), // Default 1 hour
                            isAllDay = false,
                            type = EntryType.INTERNAL,
                            originalId = node.id,
                        ),
                    )
                }
            }

            // Map external events
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

            entries.sortedBy { it.startAt }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCalendarProvider(
        name: String,
        type: String,
        url: String? = null,
    ) {
        viewModelScope.launch {
            repository.insertCalendarProvider(
                CalendarProviderEntity(name = name, type = type, url = url),
            )
        }
    }

    fun deleteCalendarProvider(provider: CalendarProviderEntity) {
        viewModelScope.launch {
            repository.deleteCalendarProvider(provider)
        }
    }

    fun syncCalendars() {
        viewModelScope.launch {
            calendarManager.syncAll()
        }
    }

    data class NodeCategorization(
        val inbox: List<NodeWithPin> = emptyList(),
        val archived: List<NodeWithPin> = emptyList(),
        val reminders: List<NodeEntity> = emptyList(),
    )

    private val categorizedNodes: StateFlow<NodeCategorization> =
        allNodes
            .map { list ->
                val now = Clock.System.now().toEpochMilliseconds()
                val inbox = mutableListOf<NodeWithPin>()
                val archived = mutableListOf<NodeWithPin>()
                val reminders = mutableListOf<NodeEntity>()

                for (item in list) {
                    val node = item.node

                    if (node.status == "archived") {
                        archived.add(item)
                    } else {
                        if (node.inboxState && node.type != "project" && node.type != "area") {
                            inbox.add(item)
                        }

                        if (node.status == "active" && node.reminderAt != null && node.reminderAt <= now) {
                            reminders.add(node)
                        }
                    }
                }
                NodeCategorization(inbox, archived, reminders)
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NodeCategorization())

    val inboxNodes: StateFlow<List<NodeWithPin>> =
        categorizedNodes
            .map { it.inbox }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isInitialLoadComplete = MutableStateFlow(false)
    val isInitialLoadComplete: StateFlow<Boolean> = _isInitialLoadComplete.asStateFlow()

    val archivedNodes: StateFlow<List<NodeWithPin>> =
        categorizedNodes
            .map { it.archived }
            .onEach { _isInitialLoadComplete.value = true }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeReminders: StateFlow<List<NodeEntity>> =
        categorizedNodes
            .map { it.reminders }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSession: StateFlow<FocusSessionEntity?> =
        repository
            .getActiveSession()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSessions: StateFlow<List<FocusSessionEntity>> =
        repository
            .getAllSessions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            allNodes.filter { it.isNotEmpty() }.firstOrNull() ?: seedOnboardingData()
        }
        syncCalendars()
    }

    private suspend fun seedOnboardingData() {
        if (allNodes.value.isNotEmpty()) return

        val welcomeId =
            repository.insertNode(
                NodeEntity(
                    title = "Welcome to TajsOS",
                    content = "This is your new Second Brain. Capture everything, organize later.",
                    type = "note",
                    inboxState = false,
                    isPinned = true,
                ),
            )

        val taskId =
            repository.insertNode(
                NodeEntity(
                    title = "Explore the Dashboard",
                    type = "task",
                    inboxState = true,
                ),
            )

        repository.insertNode(
            NodeEntity(
                title = "Personal",
                type = "area",
                inboxState = false,
            ),
        )

        repository.insertRelation(
            RelationEntity(
                fromNodeId = welcomeId,
                toNodeId = taskId,
                relationType = "RELATED",
            ),
        )
    }

    val insights: StateFlow<InsightsData> =
        combine(
            allNodes,
            allSessions,
            trackEntries,
            allProjects,
        ) { nodes, sessions, tracks, projects ->
            calculateInsights(nodes, sessions, tracks, projects)
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsData())

    private fun calculateInsights(
        nodes: List<NodeWithPin>,
        sessions: List<FocusSessionEntity>,
        tracks: List<TrackEntryEntity>,
        projects: List<NodeEntity>,
    ): InsightsData {
        val now = Clock.System.now().toEpochMilliseconds()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)

        val recentNodes = nodes.filter { it.node.createdAt >= sevenDaysAgo }
        val recentCompletions = nodes.filter {
            it.node.status == "done" && (it.node.completedAt ?: 0) >= sevenDaysAgo
        }

        val recentSessions = sessions.filter { it.startedAt >= sevenDaysAgo && it.endedAt != null }
        val weeklyFocusSec = recentSessions.sumOf { it.durationSec.toLong() }
        val avgSessionMin = if (recentSessions.isNotEmpty()) {
            (recentSessions.map { it.durationSec }.average() / 60).toInt()
        } else 0

        val hourlyDistribution = IntArray(24)
        sessions.filter { it.endedAt != null }.forEach {
            val hour =
                Instant
                    .fromEpochMilliseconds(it.startedAt)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .hour
            hourlyDistribution[hour]++
        }

        val bestFocusHour = hourlyDistribution.indices.maxByOrNull { hourlyDistribution[it] } ?: -1

        val completionHourlyDist = IntArray(24)
        recentCompletions.forEach {
            val hour = Instant.fromEpochMilliseconds(it.node.completedAt ?: 0)
                .toLocalDateTime(TimeZone.currentSystemDefault()).hour
            completionHourlyDist[hour]++
        }
        val mostProductiveHour =
            completionHourlyDist.indices.maxByOrNull { completionHourlyDist[it] } ?: -1

        val sevenDaysAgoDate = Instant.fromEpochMilliseconds(sevenDaysAgo)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        val recentTracks = tracks.filter { it.date >= sevenDaysAgoDate.toString() }

        val avgMood = recentTracks.mapNotNull { it.moodScore }.average().takeIf { !it.isNaN() } ?: 0.0
        val avgEnergy = recentTracks.mapNotNull { it.energyScore }.average().takeIf { !it.isNaN() } ?: 0.0
        val avgFocus = recentTracks.mapNotNull { it.focusScore }.average().takeIf { !it.isNaN() } ?: 0.0

        val nodesByProjectId = nodes.groupBy { it.node.projectId }
        val neglectedProjects =
            projects.filter { project ->
                val projectNodes = nodesByProjectId[project.id] ?: emptyList()
                val hasActiveItems = projectNodes.any { it.node.status == "active" }
                val hasRecentCompletions =
                    projectNodes.any {
                        it.node.status == "done" && (it.node.completedAt ?: 0) >= sevenDaysAgo
                    }
                hasActiveItems && !hasRecentCompletions
            }

        val completionsByArea = recentCompletions.filter { it.node.areaId != null }
            .groupBy { it.node.areaId!! }.mapValues { it.value.size }
        val completionsByProject = recentCompletions.filter { it.node.projectId != null }
            .groupBy { it.node.projectId!! }.mapValues { it.value.size }

        val inboxGrowth = recentNodes.count { it.node.inboxState }
        val archivedCount = nodes.count {
            it.node.status == "archived" && (it.node.archivedAt ?: 0) >= sevenDaysAgo
        }
        val archiveRate =
            if (recentNodes.isNotEmpty()) archivedCount.toDouble() / recentNodes.size else 0.0

        val activeTasks = nodes.count { it.node.status == "active" && it.node.type == "task" }
        val recentTaskCompletions = recentCompletions.count { it.node.type == "task" }
        val backlogPressure =
            if (recentTaskCompletions > 0) activeTasks.toDouble() / recentTaskCompletions else activeTasks.toDouble()

        val overdueCount =
            nodes.count { it.node.dueAt != null && it.node.dueAt < now && it.node.status == "active" }
        val chaosScore =
            (overdueCount * 10) + (inboxGrowth * 5) + (if (backlogPressure > 5) 50 else 0)

        val uniqueContextsPerDay = recentSessions.groupBy {
            Instant.fromEpochMilliseconds(it.startedAt)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
        }.mapValues {
            it.value.mapNotNull { s -> nodes.find { n -> n.node.id == s.nodeId }?.node?.projectId }
                .distinct().size
        }
        val contextSwitchingRate =
            if (uniqueContextsPerDay.isNotEmpty()) uniqueContextsPerDay.values.average() else 0.0

        // Light Manual Statistics (Roadmap Section 7)
        // Correlating track entries with activity
        val dailyCompletions = recentCompletions.groupBy {
            Instant.fromEpochMilliseconds(it.node.completedAt ?: 0)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        }.mapValues { it.value.size }

        val dailyCaptures = recentNodes.groupBy {
            Instant.fromEpochMilliseconds(it.node.createdAt)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        }.mapValues { it.value.size }

        val dailyFocus = recentSessions.groupBy {
            Instant.fromEpochMilliseconds(it.startedAt)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        }.mapValues { it.value.sumOf { s -> s.durationSec } / 3600.0 }

        val moodVsCompletions = if (recentTracks.isNotEmpty()) {
            val moodOnBusyDays = recentTracks.filter { (dailyCompletions[it.date] ?: 0) >= 3 }
                .mapNotNull { it.moodScore }.average()
            val moodOnSlowDays = recentTracks.filter { (dailyCompletions[it.date] ?: 0) == 0 }
                .mapNotNull { it.moodScore }.average()
            if (!moodOnBusyDays.isNaN() && !moodOnSlowDays.isNaN()) moodOnBusyDays - moodOnSlowDays else 0.0
        } else 0.0

        val sleepVsFocus = if (recentTracks.isNotEmpty()) {
            val focusOnGoodSleep = recentTracks.filter { (it.sleepScore ?: 0f) >= 7f }
                .map { dailyFocus[it.date] ?: 0.0 }.average()
            val focusOnBadSleep = recentTracks.filter { (it.sleepScore ?: 0f) < 7f }
                .map { dailyFocus[it.date] ?: 0.0 }.average()
            if (!focusOnGoodSleep.isNaN() && !focusOnBadSleep.isNaN()) focusOnGoodSleep - focusOnBadSleep else 0.0
        } else 0.0

        val energyVsCaptures = if (recentTracks.isNotEmpty()) {
            val capturesOnHighEnergy = recentTracks.filter { (it.energyScore ?: 0) >= 4 }
                .map { dailyCaptures[it.date] ?: 0 }.average()
            val capturesOnLowEnergy = recentTracks.filter { (it.energyScore ?: 0) <= 2 }
                .map { dailyCaptures[it.date] ?: 0 }.average()
            if (!capturesOnHighEnergy.isNaN() && !capturesOnLowEnergy.isNaN()) capturesOnHighEnergy - capturesOnLowEnergy else 0.0
        } else 0.0

        val anxietyVsAvoidance = if (recentTracks.isNotEmpty()) {
            // Using low mood/energy as a proxy for high anxiety/stress if not explicitly tracked
            val postponesOnBadDays =
                recentTracks.filter { (it.moodScore ?: 5) <= 2 }.sumOf { track ->
                    recentNodes.filter {
                        val d = Instant.fromEpochMilliseconds(it.node.updatedAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        d == track.date && it.node.postponeCount > 0
                    }.size
                }
            postponesOnBadDays.toDouble()
        } else 0.0

        val medsEffectiveness = if (recentTracks.isNotEmpty()) {
            val focusWithMeds =
                recentTracks.filter { it.tookMeds }.mapNotNull { it.focusScore }.average()
            val focusWithoutMeds =
                recentTracks.filter { !it.tookMeds }.mapNotNull { it.focusScore }.average()
            if (!focusWithMeds.isNaN() && !focusWithoutMeds.isNaN()) focusWithMeds - focusWithoutMeds else 0.0
        } else 0.0

        // Insight Cards Logic (Roadmap Section 7)
        val mostPostponedAreaId =
            nodes.filter { it.node.areaId != null && it.node.postponeCount > 0 }
                .groupBy { it.node.areaId!! }
                .maxByOrNull { entry -> entry.value.sumOf { it.node.postponeCount } }?.key

        val ideaTimes = nodes.filter { it.node.type == "idea" && it.node.createdAt >= sevenDaysAgo }
            .map {
                Instant.fromEpochMilliseconds(it.node.createdAt)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).hour
            }

        val captureTimePattern = if (ideaTimes.isNotEmpty()) {
            val morning = ideaTimes.count { it in 6..11 }
            val afternoon = ideaTimes.count { it in 12..17 }
            val evening = ideaTimes.count { it in 18..23 }
            val night = ideaTimes.count { it in 0..5 }
            val max = listOf(morning, afternoon, evening, night).maxOrNull() ?: 0
            when (max) {
                morning -> "Morning"
                afternoon -> "Afternoon"
                evening -> "Evening"
                else -> "Night"
            }
        } else null

        val projectsWithoutTasks = projects.filter { project ->
            val projectNodes = nodes.filter { it.node.projectId == project.id }
            val hasNotes = projectNodes.any { it.node.type == "note" || it.node.type == "idea" }
            val hasTasks = projectNodes.any { it.node.type == "task" && it.node.status == "active" }
            hasNotes && !hasTasks
        }

        val areas = nodes.filter { it.node.type == "area" }.map { it.node }
        val neglectedAreas = areas.filter { area ->
            val areaNodes = nodes.filter { it.node.areaId == area.id }
            val hasRecentActivity = areaNodes.any { it.node.updatedAt >= sevenDaysAgo }
            !hasRecentActivity
        }

        // Advanced Insight Concepts (Roadmap Section 7)
        val projectEntropy = projects.associate { project ->
            val projectNodes =
                nodes.filter { it.node.projectId == project.id && it.node.status == "active" }
            if (projectNodes.isEmpty()) {
                project.id to 0.0
            } else {
                val messyNodes = projectNodes.count {
                    it.node.dueAt == null || it.node.postponeCount > 2 || it.tags.isEmpty()
                }
                project.id to (messyNodes.toDouble() / projectNodes.size)
            }
        }

        val contextStability =
            if (contextSwitchingRate > 0) 1.0 / (1.0 + contextSwitchingRate) else 1.0

        val behaviorSummary = buildString {
            if (mostProductiveHour != -1) {
                append("You typically finish tasks around ${mostProductiveHour}:00. ")
            }
            if (archiveRate > 0.3) {
                append("You have a healthy habit of archiving items. ")
            } else if (backlogPressure > 10) {
                append("Your backlog is growing faster than you can process it. Consider a cleanup. ")
            }
            if (contextStability < 0.3) {
                append("You context-switch frequently. Deep focus sessions might be harder to maintain. ")
            }
        }

        val review = buildString {
            append("This week you captured ${recentNodes.size} items and completed ${recentCompletions.size}. ")
            val recentResources = recentNodes.count { it.node.type == "resource" }
            if (recentResources > 0) {
                append("You also added $recentResources new resources to your library. ")
            }
            if (weeklyFocusSec > 0) {
                append("You spent ${((weeklyFocusSec / 3600.0) * 10).toInt() / 10.0} hours in deep focus. ")
            }
            if (neglectedProjects.isNotEmpty()) {
                append("Note that ${neglectedProjects.size} projects are slipping through the cracks. ")
            }
            if (avgMood > 0) {
                append("Your average mood was ${((avgMood * 10).toInt() / 10.0)}/5.0. ")
            }
            if (recentNodes.isNotEmpty()) {
                val ratio =
                    (recentCompletions.size.toDouble() / recentNodes.size.toDouble() * 100).toInt()
                append("Current execution ratio: $ratio%. ")
            }
            if (backlogPressure > 5.0) {
                append("Warning: Your backlog pressure is high ($backlogPressure). ")
            }
            if (medsEffectiveness > 0.5) {
                append("Focus seems significantly better on days you take medication. ")
            }
            if (captureTimePattern != null) {
                append("You are most creative in the $captureTimePattern. ")
            }
        }

        return InsightsData(
            weeklyCaptures = recentNodes.size,
            weeklyCompletions = recentCompletions.size,
            weeklyFocusHours = weeklyFocusSec / 3600.0,
            bestFocusHour = bestFocusHour,
            avgMood = avgMood,
            avgEnergy = avgEnergy,
            avgFocus = avgFocus,
            neglectedProjects = neglectedProjects,
            captureToActionRatio = if (recentNodes.isNotEmpty()) recentCompletions.size.toDouble() / recentNodes.size.toDouble() else 0.0,
            autoPreparedReview = review,
            avgSessionMinutes = avgSessionMin,
            inboxGrowth = inboxGrowth,
            archiveRate = archiveRate,
            completionsByArea = completionsByArea,
            completionsByProject = completionsByProject,
            mostProductiveHour = mostProductiveHour,
            postponeFrequency = recentNodes.sumOf { it.node.postponeCount },
            backlogPressure = backlogPressure,
            chaosScore = chaosScore,
            contextSwitchingRate = contextSwitchingRate,
            moodVsCompletions = moodVsCompletions,
            sleepVsFocus = sleepVsFocus,
            energyVsCaptures = energyVsCaptures,
            anxietyVsAvoidance = anxietyVsAvoidance,
            medsEffectiveness = medsEffectiveness,
            mostPostponedAreaId = mostPostponedAreaId,
            captureTimePattern = captureTimePattern,
            projectsWithoutTasks = projectsWithoutTasks,
            neglectedAreas = neglectedAreas,
            projectEntropy = projectEntropy,
            contextStability = contextStability,
            passiveBehaviorSummary = behaviorSummary
        )
    }

    val isBiometricEnabled: StateFlow<Boolean?> =
        preferencesRepository.isBiometricEnabled
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isBiometricHardwareAvailable = MutableStateFlow(false)
    val isBiometricHardwareAvailable: StateFlow<Boolean> = _isBiometricHardwareAvailable.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun setBiometricHardwareAvailable(available: Boolean) {
        _isBiometricHardwareAvailable.value = available
    }

    fun setAuthenticated(authenticated: Boolean) {
        _isAuthenticated.value = authenticated
    }

    fun lockApp() {
        if (isBiometricEnabled.value == true) {
            _isAuthenticated.value = false
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateBiometricEnabled(enabled)
        }
    }

    fun addNode(
        title: String,
        content: String = "",
        type: String = "task",
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
    ) {
        viewModelScope.launch {
            val autoType =
                if (type == "task" && (title.startsWith("http://") || title.startsWith("https://"))) {
                    "resource"
                } else {
                    type
                }

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
                    inboxState = inboxState ?: (autoType != "project" && autoType != "area"),
                    contextScreen = contextScreen,
                    isSticky = isSticky,
                ),
            )
        }
    }

    suspend fun addNodeForResult(
        title: String,
        content: String = "",
        type: String = "task",
        projectId: Long? = null,
        areaId: Long? = null,
        inboxState: Boolean? = null,
    ): Long =
        withContext(Dispatchers.Default) {
            repository.insertNode(
                NodeEntity(
                    title = title,
                    content = content,
                    type = type,
                    projectId = projectId,
                    areaId = areaId,
                    inboxState = inboxState ?: (type != "project" && type != "area"),
                ),
            )
        }

    fun updateNode(node: NodeEntity) {
        viewModelScope.launch {
            val oldNode = repository.getNodeById(node.id)
            var updatedNode = node.copy(updatedAt = Clock.System.now().toEpochMilliseconds())

            // Check for postponement
            if (oldNode != null && oldNode.dueAt != null && node.dueAt != null && node.dueAt > oldNode.dueAt) {
                updatedNode = updatedNode.copy(postponeCount = oldNode.postponeCount + 1)
            }

            repository.updateNode(updatedNode)

            // Parse internal links [[Note Title]]
            if (oldNode == null || oldNode.content != node.content) {
                parseInternalLinks(node.id)
            }
        }
    }

    private fun parseInternalLinks(nodeId: Long) {
        viewModelScope.launch {
            repository.getNodeById(nodeId)?.let { node ->
                // Support both [[Title]] and [[Title|Alias]]
                val regex = Regex("\\[\\[(.*?)\\]\\]")
                val matches = regex.findAll(node.content).map { match ->
                    val fullMatch = match.groupValues[1]
                    if (fullMatch.contains("|")) fullMatch.split("|")[0] else fullMatch
                }.toList()

                if (matches.isNotEmpty()) {
                    val nodes = allNodes.value
                    for (match in matches) {
                        nodes.find { it.node.title.equals(match.trim(), ignoreCase = true) }
                            ?.let { target ->
                                addRelation(nodeId, target.node.id, "MENTION")
                            }
                    }
                }
            }
        }
    }

    fun extractNextStep(nodeId: Long) {
        viewModelScope.launch {
            repository.getNodeById(nodeId)?.let { node ->
                if (node.nextSmallestStep.isNullOrBlank() && node.content.isNotBlank()) {
                    val lines = node.content.lines().filter { it.isNotBlank() }
                    if (lines.isNotEmpty()) {
                        val firstLine =
                            lines.first().trim().removePrefix("-").removePrefix("*").trim()
                        repository.updateNode(
                            node.copy(
                                nextSmallestStep = firstLine,
                                updatedAt = Clock.System.now().toEpochMilliseconds()
                            )
                        )
                    }
                }
            }
        }
    }

    fun splitIntoSubtasks(nodeId: Long) {
        viewModelScope.launch {
            repository.getNodeById(nodeId)?.let { node ->
                val lines = node.content.lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() && (it.startsWith("-") || it.startsWith("*")) }

                if (lines.isNotEmpty()) {
                    for (line in lines) {
                        val subtaskTitle = line.removePrefix("-").removePrefix("*").trim()
                        val subtaskId = repository.insertNode(
                            NodeEntity(
                                title = subtaskTitle,
                                type = "task",
                                projectId = node.projectId,
                                areaId = node.areaId,
                                parentNodeId = node.id
                            )
                        )
                        repository.insertRelation(
                            RelationEntity(
                                fromNodeId = node.id,
                                toNodeId = subtaskId,
                                relationType = "DEPENDS_ON"
                            )
                        )
                    }
                    // Optionally clear content or prefix it with "SPLIT"
                    repository.updateNode(
                        node.copy(
                            content = "// SPLIT INTO SUBTASKS\n" + node.content,
                            updatedAt = Clock.System.now().toEpochMilliseconds()
                        )
                    )
                }
            }
        }
    }

    fun createSnapshot(nodeId: Long) {
        viewModelScope.launch {
            repository.getNodeById(nodeId)?.let { node ->
                repository.insertSnapshot(
                    NodeSnapshotEntity(
                        nodeId = node.id,
                        title = node.title,
                        content = node.content
                    )
                )
            }
        }
    }

    fun restoreSnapshot(snapshot: NodeSnapshotEntity) {
        viewModelScope.launch {
            repository.getNodeById(snapshot.nodeId)?.let { node ->
                repository.updateNode(
                    node.copy(
                        title = snapshot.title,
                        content = snapshot.content,
                        updatedAt = Clock.System.now().toEpochMilliseconds()
                    )
                )
            }
        }
    }

    fun mergeNodes(primaryNodeId: Long, otherNodeIds: List<Long>) {
        viewModelScope.launch {
            val primary = repository.getNodeById(primaryNodeId) ?: return@launch
            var mergedContent = primary.content
            var mergedTitle = primary.title

            for (otherId in otherNodeIds) {
                repository.getNodeById(otherId)?.let { other ->
                    mergedContent += "\n\n--- MERGED FROM ${other.title} ---\n${other.content}"
                    archiveNode(other)
                    // Move relations
                    val relations = repository.getRelationsForNode(otherId).first()
                    relations.forEach { rel ->
                        if (rel.fromNodeId == otherId) {
                            addRelation(primaryNodeId, rel.toNodeId, rel.relationType)
                        } else if (rel.toNodeId == otherId) {
                            addRelation(rel.fromNodeId, primaryNodeId, rel.relationType)
                        }
                    }
                }
            }

            repository.updateNode(
                primary.copy(
                    content = mergedContent,
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )
            )
        }
    }

    fun splitNote(nodeId: Long) {
        viewModelScope.launch {
            repository.getNodeById(nodeId)?.let { node ->
                val sections = node.content.split(Regex("(?=^# )", RegexOption.MULTILINE))
                    .filter { it.isNotBlank() }

                if (sections.size > 1) {
                    for (section in sections) {
                        val lines = section.lines()
                        val title = lines.first().removePrefix("# ").trim()
                        val content = lines.drop(1).joinToString("\n").trim()

                        repository.insertNode(
                            NodeEntity(
                                title = title,
                                content = content,
                                type = "note",
                                projectId = node.projectId,
                                areaId = node.areaId
                            )
                        )
                    }
                    archiveNode(node)
                }
            }
        }
    }

    fun updateNodeStatus(
        node: NodeEntity,
        status: String,
    ) {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            repository.updateNode(
                node.copy(
                    status = status,
                    updatedAt = now,
                    completedAt = if (status == "done") now else node.completedAt,
                    archivedAt = if (status == "archived") now else node.archivedAt,
                ),
            )

            // Recurrence logic
            if (status == "done" && node.isRecurring && node.recurringInterval != null) {
                val nextDue = calculateNextRecurringDate(node.dueAt ?: now, node.recurringInterval)
                repository.insertNode(
                    node.copy(
                        id = 0,
                        status = "active",
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

    private fun calculateNextRecurringDate(
        currentDue: Long,
        interval: String,
    ): Long {
        val instant = Instant.fromEpochMilliseconds(currentDue)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val nextDateTime =
            when (interval.uppercase()) {
                "DAILY" -> {
                    dateTime
                        .toInstant(TimeZone.currentSystemDefault())
                        .plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                }

                "WEEKLY" -> {
                    dateTime
                        .toInstant(TimeZone.currentSystemDefault())
                        .plus(1, DateTimeUnit.WEEK, TimeZone.currentSystemDefault())
                }

                "MONTHLY" -> {
                    dateTime
                        .toInstant(TimeZone.currentSystemDefault())
                        .plus(1, DateTimeUnit.MONTH, TimeZone.currentSystemDefault())
                }

                else -> {
                    dateTime
                        .toInstant(TimeZone.currentSystemDefault())
                        .plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                }
            }
        return nextDateTime.toEpochMilliseconds()
    }

    suspend fun getNodeById(id: Long): NodeEntity? = repository.getNodeById(id)

    fun archiveNode(node: NodeEntity) {
        viewModelScope.launch {
            repository.updateNode(
                node.copy(
                    status = "archived",
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
        }
    }

    fun deleteNodePermanently(node: NodeEntity) {
        viewModelScope.launch {
            repository.deleteNode(node)
        }
    }

    fun togglePin(
        node: NodeEntity,
        isPinned: Boolean,
    ) {
        viewModelScope.launch {
            if (isPinned) {
                repository.pinToToday(node.id)
            } else {
                repository.unpinFromToday(node.id)
            }
        }
    }

    fun togglePermanentPin(node: NodeEntity) {
        viewModelScope.launch {
            repository.updateNode(
                node.copy(
                    isPinned = !node.isPinned,
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
        }
    }

    fun markAsProcessed(nodeId: Long) {
        viewModelScope.launch {
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
    ) {
        addNode(title = name, content = description, type = "project", areaId = areaId)
    }

    /**
     * Creates a new area node with the given name.
     *
     * @param name The area's display name.
     */
    fun addArea(name: String) {
        addNode(title = name, type = "area")
    }

    /**
     * Provides a stream of nodes (with pin metadata) that belong to the given project.
     *
     * @param projectId The id of the project whose nodes should be returned.
     * @return A Flow that emits lists of NodeWithPin for the specified project.
     */
    fun getNodesForProject(projectId: Long): Flow<List<NodeWithPin>> =
        repository.getNodesByProjectWithPins(projectId)

    /**
     * Retrieves nodes (including pin state) that belong to the specified area.
     *
     * @param areaId The id of the area to fetch nodes for.
     * @return A Flow that emits lists of `NodeWithPin` belonging to the specified area.
     */
    fun getNodesForArea(areaId: Long): Flow<List<NodeWithPin>> =
        repository.getNodesByAreaWithPins(areaId)

    /**
     * Provides a reactive stream of projects assigned to the specified area.
     *
     * @param areaId The id of the area whose projects to retrieve.
     * @return A Flow that emits lists of `NodeEntity` representing projects belonging to the given area.
     */
    fun getProjectsForArea(areaId: Long): Flow<List<NodeEntity>> =
        repository.getProjectsByArea(areaId)

    /**
     * Creates and inserts a track entry for the current local date using the provided scores and metadata.
     *
     * The entry's `date` is generated from the current local date/time; callers need only supply the measured values.
     *
     * @param mood Optional mood score.
     * @param energy Optional energy score.
     * @param focus Optional focus score.
     * @param sleep Optional sleep value (hours).
     * @param tookMeds Whether medication was taken.
     * @param note Optional symptom or free-form note.
     */
    fun addTrackEntry(
        mood: Int? = null,
        energy: Int? = null,
        focus: Int? = null,
        anxiety: Int? = null,
        sleep: Float? = null,
        tookMeds: Boolean = false,
        note: String = "",
    ) {
        viewModelScope.launch {
            repository.insertTrackEntry(
                TrackEntryEntity(
                    date =
                        Clock.System
                            .now()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                            .toString(),
                    moodScore = mood,
                    energyScore = energy,
                    focusScore = focus,
                    anxietyScore = anxiety,
                    sleepScore = sleep,
                    tookMeds = tookMeds,
                    symptomNote = note,
                ),
            )
        }
    }

    fun startFocusSession(nodeId: Long) {
        viewModelScope.launch {
            if (activeSession.value == null) {
                repository.insertSession(
                    FocusSessionEntity(
                        nodeId = nodeId,
                        startedAt = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            }
        }
    }

    fun stopFocusSession(
        completed: Boolean = true,
        interrupted: Boolean = false,
        note: String? = null,
    ) {
        viewModelScope.launch {
            activeSession.value?.let { session ->
                val now = Clock.System.now().toEpochMilliseconds()
                val duration = ((now - session.startedAt) / 1000).toInt()
                repository.updateSession(
                    session.copy(
                        endedAt = now,
                        durationSec = duration,
                        completed = completed,
                        interrupted = interrupted,
                        note = note,
                    ),
                )
            }
        }
    }

    fun resumeLastSession() {
        viewModelScope.launch {
            val lastSession = allSessions.value.firstOrNull { it.endedAt != null }
            lastSession?.let { startFocusSession(it.nodeId) }
        }
    }

    private val _lastActiveProjectId = MutableStateFlow<Long?>(null)
    val lastActiveProjectId: StateFlow<Long?> = _lastActiveProjectId.asStateFlow()

    private val _lastActiveAreaId = MutableStateFlow<Long?>(null)
    val lastActiveAreaId: StateFlow<Long?> = _lastActiveAreaId.asStateFlow()

    fun setLastActiveContext(
        projectId: Long?,
        areaId: Long?,
    ) {
        if (projectId != null) _lastActiveProjectId.value = projectId
        if (areaId != null) _lastActiveAreaId.value = areaId
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchTypeFilter = MutableStateFlow<String?>(null)
    val searchTypeFilter: StateFlow<String?> = _searchTypeFilter.asStateFlow()

    private val _searchStatusFilter = MutableStateFlow<String?>("active") // Default: active
    val searchStatusFilter: StateFlow<String?> = _searchStatusFilter.asStateFlow()

    private val _searchProjectFilter = MutableStateFlow<Long?>(null)
    val searchProjectFilter: StateFlow<Long?> = _searchProjectFilter.asStateFlow()

    private val _searchAreaFilter = MutableStateFlow<Long?>(null)
    val searchAreaFilter: StateFlow<Long?> = _searchAreaFilter.asStateFlow()

    private val _searchLinkedToFilter = MutableStateFlow<Long?>(null)
    val searchLinkedToFilter: StateFlow<Long?> = _searchLinkedToFilter.asStateFlow()

    private val _searchMaxMinutesFilter = MutableStateFlow<Int?>(null)
    val searchMaxMinutesFilter: StateFlow<Int?> = _searchMaxMinutesFilter.asStateFlow()

    private val _searchEnergyFilter = MutableStateFlow<Int?>(null)
    val searchEnergyFilter: StateFlow<Int?> = _searchEnergyFilter.asStateFlow()

    private val _searchFrictionFilter = MutableStateFlow<String?>(null)
    val searchFrictionFilter: StateFlow<String?> = _searchFrictionFilter.asStateFlow()

    val searchResults: StateFlow<List<NodeWithPin>> =
        combine(
            allNodes,
            _searchQuery,
            _searchTypeFilter,
            _searchStatusFilter,
            _searchProjectFilter,
            _searchAreaFilter,
            _searchLinkedToFilter,
            _searchMaxMinutesFilter,
            _searchEnergyFilter,
            _searchFrictionFilter,
            allRelations,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val nodes = args[0] as List<NodeWithPin>
            val query = args[1] as String
            val type = args[2] as String?
            val status = args[3] as String?
            val projectId = args[4] as Long?
            val areaId = args[5] as Long?
            val linkedToId = args[6] as Long?
            val maxMins = args[7] as Int?
            val energy = args[8] as Int?
            val friction = args[9] as String?

            @Suppress("UNCHECKED_CAST")
            val relations = args[10] as List<RelationEntity>

            nodes.filter { nodeWithPin ->
                val node = nodeWithPin.node
                val matchesQuery = if (query.isBlank()) true else matchesQuery(nodeWithPin, query)
                val matchesType = type == null || node.type == type
                val matchesStatus = status == null || node.status == status
                val matchesProject = projectId == null || node.projectId == projectId
                val matchesArea = areaId == null || node.areaId == areaId
                val matchesMins = maxMins == null || (node.estimatedMinutes ?: 0) <= maxMins
                val matchesEnergy = energy == null || node.energyLevel == energy
                val matchesFriction = friction == null || node.friction == friction
                val matchesLinkedTo = linkedToId == null || relations.any {
                    (it.fromNodeId == node.id && it.toNodeId == linkedToId) ||
                            (it.fromNodeId == linkedToId && it.toNodeId == node.id)
                }
                matchesQuery && matchesType && matchesStatus && matchesProject && matchesArea && matchesLinkedTo && matchesMins && matchesEnergy && matchesFriction
            }.sortedByDescending { it.node.updatedAt }
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun matchesQuery(
        nodeWithPin: NodeWithPin,
        query: String,
    ): Boolean {
        if (query.isBlank()) return false
        return if (query.startsWith("#")) {
            val tagQuery = query.substring(1)
            if (tagQuery.isBlank()) {
                false
            } else {
                nodeWithPin.tags.any { it.name.contains(tagQuery, ignoreCase = true) }
            }
        } else {
            nodeWithPin.node.title.contains(query, ignoreCase = true) ||
                    nodeWithPin.node.content.contains(query, ignoreCase = true) ||
                    nodeWithPin.tags.any { tag -> tag.name.contains(query, ignoreCase = true) }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSearchTypeFilter(type: String?) {
        _searchTypeFilter.value = type
    }

    fun updateSearchStatusFilter(status: String?) {
        _searchStatusFilter.value = status
    }

    fun updateSearchProjectFilter(projectId: Long?) {
        _searchProjectFilter.value = projectId
    }

    fun updateSearchAreaFilter(areaId: Long?) {
        _searchAreaFilter.value = areaId
    }

    fun updateSearchLinkedToFilter(nodeId: Long?) {
        _searchLinkedToFilter.value = nodeId
    }

    fun updateSearchMaxMinutesFilter(mins: Int?) {
        _searchMaxMinutesFilter.value = mins
    }

    fun updateSearchEnergyFilter(energy: Int?) {
        _searchEnergyFilter.value = energy
    }

    fun updateSearchFrictionFilter(friction: String?) {
        _searchFrictionFilter.value = friction
    }

    fun clearSearchFilters() {
        _searchQuery.value = ""
        _searchTypeFilter.value = null
        _searchStatusFilter.value = "active"
        _searchProjectFilter.value = null
        _searchAreaFilter.value = null
        _searchLinkedToFilter.value = null
        _searchMaxMinutesFilter.value = null
        _searchEnergyFilter.value = null
        _searchFrictionFilter.value = null
    }

    fun getFilteredNodes(query: String): Flow<List<NodeWithPin>> =
        allNodes.map { nodes ->
            if (query.isBlank()) {
                nodes
            } else {
                nodes.filter { matchesQuery(it, query) }
            }
        }

    /**
     * Suggests potentially related nodes based on shared tags or title keywords.
     */
    fun getNoteSuggestions(nodeId: Long): Flow<List<NodeWithPin>> =
        allNodes.map { nodes ->
            val currentNodeWithPin = nodes.find { it.node.id == nodeId } ?: return@map emptyList()
            val currentNode = currentNodeWithPin.node
            val currentTags = currentNodeWithPin.tags.map { it.id }.toSet()

            nodes.filter { other ->
                other.node.id != nodeId &&
                        other.node.status != "archived" &&
                        other.node.type in listOf("note", "idea", "resource", "project") &&
                        (
                                other.tags.any { it.id in currentTags } ||
                                        other.node.title.split(" ").any { word ->
                                            word.length > 3 && currentNode.title.contains(
                                                word,
                                                ignoreCase = true
                                            )
                                        }
                                )
            }.take(5)
        }

    fun getRelationsForNode(nodeId: Long): Flow<List<RelationEntity>> = repository.getRelationsForNode(nodeId)

    fun getSnapshotsForNode(nodeId: Long): Flow<List<NodeSnapshotEntity>> =
        repository.getSnapshotsForNode(nodeId)

    fun getLogsForNode(nodeId: Long): Flow<List<EventLogEntity>> = repository.getLogsForNode(nodeId)

    fun addRelation(
        fromNodeId: Long,
        toNodeId: Long,
        type: String,
    ) {
        viewModelScope.launch {
            repository.insertRelation(RelationEntity(fromNodeId = fromNodeId, toNodeId = toNodeId, relationType = type))
        }
    }

    fun deleteRelation(relation: RelationEntity) {
        viewModelScope.launch {
            repository.deleteRelation(relation)
        }
    }

    val allTags: StateFlow<List<TagEntity>> =
        repository
            .getAllTags()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTagsForNode(nodeId: Long): Flow<List<TagEntity>> = repository.getTagsForNode(nodeId)

    fun addTag(
        name: String,
        color: Int? = null,
    ) {
        viewModelScope.launch {
            repository.insertTag(TagEntity(name = name, normalizedName = name.lowercase().trim(), color = color))
        }
    }

    fun attachTagToNode(
        nodeId: Long,
        tagId: Long,
    ) {
        viewModelScope.launch {
            repository.attachTagToNode(nodeId, tagId)
        }
    }

    fun detachTagFromNode(
        nodeId: Long,
        tagId: Long,
    ) {
        viewModelScope.launch {
            repository.detachTagFromNode(nodeId, tagId)
        }
    }

    fun getAttachmentsForNode(nodeId: Long): Flow<List<AttachmentEntity>> = repository.getAttachmentsForNode(nodeId)

    fun addAttachment(
        nodeId: Long,
        type: String,
        uri: String,
        title: String? = null,
    ) {
        viewModelScope.launch {
            repository.insertAttachment(AttachmentEntity(nodeId = nodeId, assetType = type, uriOrPath = uri, title = title))
        }
    }

    fun deleteAttachment(attachment: AttachmentEntity) {
        viewModelScope.launch {
            repository.deleteAttachment(attachment)
        }
    }

    val recentLogs: StateFlow<List<EventLogEntity>> =
        repository
            .getRecentLogs(50)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTemplates: StateFlow<List<TemplateEntity>> =
        repository
            .getAllTemplates()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTemplate(
        name: String,
        type: String,
        title: String? = null,
        content: String? = null,
    ) {
        viewModelScope.launch {
            repository.insertTemplate(
                TemplateEntity(
                    name = name,
                    nodeType = type,
                    defaultTitle = title,
                    defaultContent = content,
                ),
            )
        }
    }

    fun updateTemplate(template: TemplateEntity) {
        viewModelScope.launch {
            repository.updateTemplate(template)
        }
    }

    fun deleteTemplate(template: TemplateEntity) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
        }
    }

    fun deleteSnapshot(snapshot: NodeSnapshotEntity) {
        viewModelScope.launch {
            repository.deleteSnapshot(snapshot)
        }
    }

    val allReviews: StateFlow<List<ReviewEntity>> =
        repository
            .getAllReviews()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun completeReview(
        type: String,
        content: String,
        mood: Int? = null,
        energy: Int? = null,
    ) {
        viewModelScope.launch {
            val now = Clock.System.now()
            val dateStr = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

            val nodeId = repository.insertNode(
                NodeEntity(
                    title = "${type.uppercase()} REVIEW - $dateStr",
                    content = content,
                    type = "note",
                    noteType = "reflection",
                    inboxState = false,
                )
            )

            repository.insertReview(
                ReviewEntity(
                    type = type,
                    date = dateStr,
                    resultNodeId = nodeId,
                    moodScore = mood,
                    energyScore = energy
                )
            )

            if (mood != null || energy != null) {
                addTrackEntry(mood = mood, energy = energy, note = "Linked to $type review")
            }
        }
    }

    suspend fun getLastReviewByType(type: String) = repository.getLastReviewByType(type)

    suspend fun exportDataJson(): String =
        withContext(Dispatchers.Default) {
            val nodes = allNodes.value.map { it.node }
            val data = ExportData(version = 2, nodes = nodes)
            Json.encodeToString(data)
        }
}

@Serializable
data class ExportData(
    val version: Int,
    val nodes: List<NodeEntity>,
)

data class CalendarEntry(
    val id: String,
    val title: String,
    val description: String?,
    val startAt: Long,
    val endAt: Long,
    val isAllDay: Boolean,
    val type: EntryType,
    val color: Int? = null,
    val originalId: Long? = null,
)

enum class EntryType {
    INTERNAL,
    EXTERNAL,
}
