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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
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
    val neglectedProjects: List<NodeEntity> = emptyList()
)

class MainViewModel(
    private val repository: AppRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val allNodes: StateFlow<List<NodeWithPin>> = repository.getAllNodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayNodes: StateFlow<List<NodeEntity>> = repository.getTodayNodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trackEntries: StateFlow<List<TrackEntryEntity>> = repository.getAllTrackEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjects: StateFlow<List<NodeEntity>> = repository.getNodesByType("project")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAreas: StateFlow<List<NodeEntity>> = repository.getNodesByType("area")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    data class NodeCategorization(
        val inbox: List<NodeWithPin> = emptyList(),
        val archived: List<NodeWithPin> = emptyList(),
        val reminders: List<NodeEntity> = emptyList()
    )

    private val categorizedNodes: StateFlow<NodeCategorization> = allNodes.map { list ->
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NodeCategorization())

    val inboxNodes: StateFlow<List<NodeWithPin>> = categorizedNodes.map { it.inbox }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isInitialLoadComplete = MutableStateFlow(false)
    val isInitialLoadComplete: StateFlow<Boolean> = _isInitialLoadComplete.asStateFlow()

    val archivedNodes: StateFlow<List<NodeWithPin>> = categorizedNodes.map { it.archived }
        .onEach { _isInitialLoadComplete.value = true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeReminders: StateFlow<List<NodeEntity>> = categorizedNodes.map { it.reminders }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())





    val activeSession: StateFlow<FocusSessionEntity?> = repository.getActiveSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSessions: StateFlow<List<FocusSessionEntity>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            allNodes.filter { it.isNotEmpty() }.firstOrNull() ?: seedOnboardingData()
        }
    }

    private suspend fun seedOnboardingData() {
        if (allNodes.value.isNotEmpty()) return

        val welcomeId = repository.insertNode(
            NodeEntity(
                title = "Welcome to TajsOS",
                content = "This is your new Second Brain. Capture everything, organize later.",
                type = "note",
                inboxState = false,
                isPinned = true
            )
        )

        val taskId = repository.insertNode(
            NodeEntity(
                title = "Explore the Dashboard",
                type = "task",
                inboxState = true
            )
        )

        repository.insertNode(
            NodeEntity(
                title = "Personal",
                type = "area",
                inboxState = false
            )
        )

        repository.insertRelation(
            RelationEntity(
                fromNodeId = welcomeId,
                toNodeId = taskId,
                relationType = "RELATED"
            )
        )
    }

    val insights: StateFlow<InsightsData> = combine(
        allNodes,
        allSessions,
        trackEntries,
        allProjects
    ) { nodes, sessions, tracks, projects ->
        calculateInsights(nodes, sessions, tracks, projects)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsData())

    private fun calculateInsights(
        nodes: List<NodeWithPin>,
        sessions: List<FocusSessionEntity>,
        tracks: List<TrackEntryEntity>,
        projects: List<NodeEntity>
    ): InsightsData {
        val now = Clock.System.now().toEpochMilliseconds()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)

        val recentNodes = nodes.filter { it.node.createdAt >= sevenDaysAgo }
        val recentCompletions = nodes.filter { it.node.status == "done" && it.node.updatedAt >= sevenDaysAgo }

        val weeklyFocusSec = sessions.filter { it.startedAt >= sevenDaysAgo && it.endedAt != null }
            .sumOf { it.durationSec.toLong() }

        val hourlyDistribution = IntArray(24)
        sessions.filter { it.endedAt != null }.forEach {
            val hour = Instant.fromEpochMilliseconds(it.startedAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .hour
            hourlyDistribution[hour]++
        }

        val bestFocusHour = hourlyDistribution.indices.maxByOrNull { hourlyDistribution[it] } ?: -1

        val recentTracks = tracks.filter { 
            runCatching {
                LocalDate.parse(it.date).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds() >= sevenDaysAgo
            }.getOrDefault(false)
        }

        val avgMood = if (recentTracks.isNotEmpty()) recentTracks.mapNotNull { it.moodScore }.average() else 0.0
        val avgEnergy = if (recentTracks.isNotEmpty()) recentTracks.mapNotNull { it.energyScore }.average() else 0.0
        val avgFocus = if (recentTracks.isNotEmpty()) recentTracks.mapNotNull { it.focusScore }.average() else 0.0

        val nodesByProjectId = nodes.groupBy { it.node.projectId }
        val neglectedProjects = projects.filter { project ->
            val projectNodes = nodesByProjectId[project.id] ?: emptyList()
            val hasActiveItems = projectNodes.any { it.node.status == "active" }
            val hasRecentCompletions = projectNodes.any { it.node.status == "done" && it.node.updatedAt >= sevenDaysAgo }
            hasActiveItems && !hasRecentCompletions
        }

        return InsightsData(
            weeklyCaptures = recentNodes.size,
            weeklyCompletions = recentCompletions.size,
            weeklyFocusHours = weeklyFocusSec / 3600.0,
            bestFocusHour = bestFocusHour,
            avgMood = avgMood,
            avgEnergy = avgEnergy,
            avgFocus = avgFocus,
            neglectedProjects = neglectedProjects
        )
    }

    val isBiometricEnabled: StateFlow<Boolean?> = preferencesRepository.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isBiometricHardwareAvailable = MutableStateFlow(false)
    val isBiometricHardwareAvailable: StateFlow<Boolean> = _isBiometricHardwareAvailable.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun setBiometricHardwareAvailable(available: Boolean) { _isBiometricHardwareAvailable.value = available }
    fun setAuthenticated(authenticated: Boolean) { _isAuthenticated.value = authenticated }

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
        inboxState: Boolean? = null
    ) {
        viewModelScope.launch {
            repository.insertNode(
                NodeEntity(
                    title = title,
                    content = content,
                    type = type,
                    projectId = projectId,
                    areaId = areaId,
                    isRecurring = isRecurring,
                    recurringInterval = recurringInterval,
                    reminderAt = reminderAt,
                    color = color,
                    icon = icon,
                    inboxState = inboxState ?: (type != "project" && type != "area")
                )
            )
        }
    }

    fun updateNode(node: NodeEntity) {
        viewModelScope.launch {
            repository.updateNode(node.copy(updatedAt = Clock.System.now().toEpochMilliseconds()))
        }
    }

    fun updateNodeStatus(node: NodeEntity, status: String) {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            repository.updateNode(
                node.copy(
                    status = status, 
                    updatedAt = now,
                    completedAt = if (status == "done") now else node.completedAt,
                    archivedAt = if (status == "archived") now else node.archivedAt
                )
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
                        inboxState = false
                    )
                )
            }
        }
    }

    private fun calculateNextRecurringDate(currentDue: Long, interval: String): Long {
        val instant = Instant.fromEpochMilliseconds(currentDue)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val nextDateTime = when (interval.uppercase()) {
            "DAILY" -> dateTime.toInstant(TimeZone.currentSystemDefault())
                .plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())

            "WEEKLY" -> dateTime.toInstant(TimeZone.currentSystemDefault())
                .plus(1, DateTimeUnit.WEEK, TimeZone.currentSystemDefault())

            "MONTHLY" -> dateTime.toInstant(TimeZone.currentSystemDefault())
                .plus(1, DateTimeUnit.MONTH, TimeZone.currentSystemDefault())

            else -> dateTime.toInstant(TimeZone.currentSystemDefault())
                .plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
        }
        return nextDateTime.toEpochMilliseconds()
    }

    fun archiveNode(node: NodeEntity) {
        viewModelScope.launch {
            repository.updateNode(
                node.copy(
                    status = "archived",
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )
            )
        }
    }

    fun togglePin(node: NodeEntity, isPinned: Boolean) {
        viewModelScope.launch {
            if (isPinned) { repository.pinToToday(node.id) } else { repository.unpinFromToday(node.id) }
        }
    }

    fun togglePermanentPin(node: NodeEntity) {
        viewModelScope.launch {
            repository.updateNode(
                node.copy(
                    isPinned = !node.isPinned,
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )
            )
        }
    }

    fun addProject(name: String, description: String = "", areaId: Long? = null) {
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
fun getNodesForProject(projectId: Long): Flow<List<NodeWithPin>> = repository.getNodesByProjectWithPins(projectId)

    /**
 * Retrieves nodes (including pin state) that belong to the specified area.
 *
 * @param areaId The id of the area to fetch nodes for.
 * @return A Flow that emits lists of `NodeWithPin` belonging to the specified area.
 */
fun getNodesForArea(areaId: Long): Flow<List<NodeWithPin>> = repository.getNodesByAreaWithPins(areaId)

    /**
 * Provides a reactive stream of projects assigned to the specified area.
 *
 * @param areaId The id of the area whose projects to retrieve.
 * @return A Flow that emits lists of `NodeEntity` representing projects belonging to the given area.
 */
fun getProjectsForArea(areaId: Long): Flow<List<NodeEntity>> = repository.getProjectsByArea(areaId)

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
        sleep: Float? = null,
        tookMeds: Boolean = false,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.insertTrackEntry(
                TrackEntryEntity(
                    date = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString(),
                    moodScore = mood,
                    energyScore = energy,
                    focusScore = focus,
                    sleepScore = sleep,
                    tookMeds = tookMeds,
                    symptomNote = note
                )
            )
        }
    }

    fun startFocusSession(nodeId: Long) {
        viewModelScope.launch {
            if (activeSession.value == null) {
                repository.insertSession(
                    FocusSessionEntity(
                        nodeId = nodeId,
                        startedAt = Clock.System.now().toEpochMilliseconds()
                    )
                )
            }
        }
    }

    fun stopFocusSession(completed: Boolean = true, interrupted: Boolean = false, note: String? = null) {
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
                        note = note
                    )
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<NodeWithPin>> = combine(
        allNodes,
        _searchQuery
    ) { nodes, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            nodes.filter { matchesQuery(it, query) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun matchesQuery(nodeWithPin: NodeWithPin, query: String): Boolean {
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

    fun getFilteredNodes(query: String): Flow<List<NodeWithPin>> {
        return allNodes.map { nodes ->
            if (query.isBlank()) {
                nodes
            } else {
                nodes.filter { matchesQuery(it, query) }
            }
        }
    }

    fun getRelationsForNode(nodeId: Long): Flow<List<RelationEntity>> = repository.getRelationsForNode(nodeId)
    fun addRelation(fromNodeId: Long, toNodeId: Long, type: String) {
        viewModelScope.launch {
            repository.insertRelation(RelationEntity(fromNodeId = fromNodeId, toNodeId = toNodeId, relationType = type))
        }
    }

    val allTags: StateFlow<List<TagEntity>> = repository.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTagsForNode(nodeId: Long): Flow<List<TagEntity>> = repository.getTagsForNode(nodeId)
    fun addTag(name: String, color: Int? = null) {
        viewModelScope.launch {
            repository.insertTag(TagEntity(name = name, normalizedName = name.lowercase().trim(), color = color))
        }
    }
    fun attachTagToNode(nodeId: Long, tagId: Long) {
        viewModelScope.launch {
            repository.attachTagToNode(nodeId, tagId)
        }
    }

    fun getAttachmentsForNode(nodeId: Long): Flow<List<AttachmentEntity>> = repository.getAttachmentsForNode(nodeId)
    fun addAttachment(nodeId: Long, type: String, uri: String, title: String? = null) {
        viewModelScope.launch {
            repository.insertAttachment(AttachmentEntity(nodeId = nodeId, assetType = type, uriOrPath = uri, title = title))
        }
    }

    suspend fun exportDataJson(): String = withContext(Dispatchers.Default) {
        val nodes = allNodes.value.map { it.node }
        val data = ExportData(version = 2, nodes = nodes)
        Json.encodeToString(data)
    }
}

@Serializable
data class ExportData(
    val version: Int,
    val nodes: List<NodeEntity>
)
