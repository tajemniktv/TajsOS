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

    val inboxNodes: StateFlow<List<NodeWithPin>> = allNodes.map { list ->
        list.filter { 
            it.node.inboxState && 
            it.node.status != "archived" && 
            it.node.type != "project" && 
            it.node.type != "area" 
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedNodes: StateFlow<List<NodeWithPin>> = allNodes.map { list ->
        list.filter { it.node.status == "archived" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSession: StateFlow<FocusSessionEntity?> = repository.getActiveSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSessions: StateFlow<List<FocusSessionEntity>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
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

        val neglectedProjects = projects.filter { project ->
            val projectNodes = nodes.filter { it.node.projectId == project.id }
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
            repository.updateNode(node.copy(updatedAt = kotlin.time.Clock.System.now().toEpochMilliseconds()))
        }
    }

    fun updateNodeStatus(node: NodeEntity, status: String) {
        viewModelScope.launch {
            val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
            repository.updateNode(
                node.copy(
                    status = status, 
                    updatedAt = now,
                    completedAt = if (status == "done") now else node.completedAt,
                    archivedAt = if (status == "archived") now else node.archivedAt
                )
            )
        }
    }

    fun archiveNode(node: NodeEntity) {
        viewModelScope.launch {
            repository.updateNode(node.copy(status = "archived", updatedAt = kotlin.time.Clock.System.now().toEpochMilliseconds()))
        }
    }

    fun togglePin(node: NodeEntity, isPinned: Boolean) {
        viewModelScope.launch {
            if (isPinned) { repository.pinToToday(node.id) } else { repository.unpinFromToday(node.id) }
        }
    }

    fun togglePermanentPin(node: NodeEntity) {
        viewModelScope.launch {
            repository.updateNode(node.copy(isPinned = !node.isPinned, updatedAt = kotlin.time.Clock.System.now().toEpochMilliseconds()))
        }
    }

    fun addProject(name: String, description: String = "", areaId: Long? = null) {
        addNode(title = name, content = description, type = "project", areaId = areaId)
    }

    fun addArea(name: String) {
        addNode(title = name, type = "area")
    }

    fun getNodesForProject(projectId: Long): Flow<List<NodeWithPin>> {
        return allNodes.map { list -> 
            list.filter { it.node.projectId == projectId }
        }
    }

    fun getNodesForArea(areaId: Long): Flow<List<NodeWithPin>> {
        return allNodes.map { list ->
            list.filter { it.node.areaId == areaId }
        }
    }

    fun getProjectsForArea(areaId: Long): Flow<List<NodeEntity>> {
        return allProjects.map { list ->
            list.filter { it.areaId == areaId }
        }
    }

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
                    date = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString(),
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
                        startedAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
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
            nodes.filter { 
                it.node.title.contains(query, ignoreCase = true) || 
                it.node.content.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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
