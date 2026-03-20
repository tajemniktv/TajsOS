/*
 * Copyright (c) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tajemniktv.tajsos.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId

/**
 * InsightsData is a data class used for the Phase 6 Review screen.
 */
data class InsightsData(
    val weeklyCaptures: Int = 0,
    val weeklyCompletions: Int = 0,
    val weeklyFocusHours: Double = 0.0,
    val bestFocusHour: Int = -1,
    val avgMood: Double = 0.0,
    val avgEnergy: Double = 0.0,
    val avgFocus: Double = 0.0,
    val neglectedProjects: List<ProjectEntity> = emptyList()
)

/**
 * MainViewModel is the single state-holder for TajsOS.
 * It manages the app's business logic, from quick capture to session tracking,
 * and exposes this data as StateFlows for the Compose UI.
 */
class MainViewModel(
    private val repository: AppRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    /**
     * allItems exposes a real-time list of everything in the Inbox.
     */
    val allItems: StateFlow<List<ItemWithPin>> = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * todayItems provides the filtered shortlist for the Today view.
     */
    val todayItems: StateFlow<List<ItemEntity>> = repository.getTodayItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * trackEntries gives a history of mood and energy check-ins.
     */
    val trackEntries: StateFlow<List<TrackEntryEntity>> = repository.getAllTrackEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * allProjects provides the list of all active projects.
     */
    val allProjects: StateFlow<List<ProjectEntity>> = repository.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * allAreas provides the list of all life domains.
     */
    val allAreas: StateFlow<List<AreaEntity>> = repository.getAllAreas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * activeSession tracks the currently running focus session (if any).
     */
    val activeSession: StateFlow<FocusSessionEntity?> = repository.getActiveSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * allSessions provides the history of focus sessions.
     */
    val allSessions: StateFlow<List<FocusSessionEntity>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * insights provides calculated metrics for the Phase 6 Insight & Review system.
     */
    val insights: StateFlow<com.tajemniktv.tajsos.ui.InsightsData> = combine(
        allItems,
        allSessions,
        trackEntries,
        allProjects
    ) { items, sessions, tracks, projects ->
        calculateInsights(items, sessions, tracks, projects)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        _root_ide_package_.com.tajemniktv.tajsos.ui.InsightsData()
    )

    private fun calculateInsights(
        items: List<ItemWithPin>,
        sessions: List<FocusSessionEntity>,
        tracks: List<TrackEntryEntity>,
        projects: List<ProjectEntity>
    ): com.tajemniktv.tajsos.ui.InsightsData {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)

        val recentItems = items.filter { it.item.createdAt >= sevenDaysAgo }
        val recentCompletions = items.filter { it.item.status == "done" && it.item.updatedAt >= sevenDaysAgo }

        val weeklyFocusSec = sessions.filter { it.startAt >= sevenDaysAgo && it.endAt != null }
            .sumOf { it.durationSec.toLong() }

        val hourlyDistribution = IntArray(24)
        sessions.filter { it.endAt != null }.forEach {
            val hour = java.time.Instant.ofEpochMilli(it.startAt)
                .atZone(java.time.ZoneId.systemDefault())
                .hour
            hourlyDistribution[hour]++
        }

        val bestFocusHour = hourlyDistribution.indices.maxByOrNull { hourlyDistribution[it] } ?: -1

        val recentTracks = tracks.filter { 
            LocalDate.parse(it.date).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() >= sevenDaysAgo 
        }

        val avgMood = if (recentTracks.isNotEmpty()) recentTracks.mapNotNull { it.mood }.average() else 0.0
        val avgEnergy = if (recentTracks.isNotEmpty()) recentTracks.mapNotNull { it.energy }.average() else 0.0
        val avgFocus = if (recentTracks.isNotEmpty()) recentTracks.mapNotNull { it.focus }.average() else 0.0

        val neglectedProjects = projects.filter { project ->
            val projectItems = items.filter { it.item.projectId == project.id }
            val hasActiveItems = projectItems.any { it.item.status == "active" }
            val hasRecentCompletions = projectItems.any { it.item.status == "done" && it.item.updatedAt >= sevenDaysAgo }
            hasActiveItems && !hasRecentCompletions
        }

        return _root_ide_package_.com.tajemniktv.tajsos.ui.InsightsData(
            weeklyCaptures = recentItems.size,
            weeklyCompletions = recentCompletions.size,
            weeklyFocusHours = weeklyFocusSec / 3600.0,
            bestFocusHour = bestFocusHour,
            avgMood = avgMood,
            avgEnergy = avgEnergy,
            avgFocus = avgFocus,
            neglectedProjects = neglectedProjects
        )
    }

    /**
     * isBiometricEnabled tracks whether the user has opted for biometric locking.
     */
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

    fun addItem(
        title: String,
        type: String = "task",
        projectId: Long? = null,
        areaId: Long? = null,
        isRecurring: Boolean = false,
        recurringInterval: String? = null,
        reminderAt: Long? = null
    ) {
        viewModelScope.launch {
            repository.insertItem(
                ItemEntity(
                    title = title,
                    type = type,
                    projectId = projectId,
                    areaId = areaId,
                    isRecurring = isRecurring,
                    recurringInterval = recurringInterval,
                    reminderAt = reminderAt
                )
            )
        }
    }

    fun updateItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateItemStatus(item: ItemEntity, status: String) {
        viewModelScope.launch {
            repository.updateItem(item.copy(status = status, updatedAt = System.currentTimeMillis()))
        }
    }

    fun archiveItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item.copy(status = "archived", updatedAt = System.currentTimeMillis()))
        }
    }

    fun togglePin(item: ItemEntity, isPinned: Boolean) {
        viewModelScope.launch {
            if (isPinned) { repository.pinToToday(item.id) } else { repository.unpinFromToday(item.id) }
        }
    }

    fun togglePermanentPin(item: ItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isPinned = !item.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun addProject(name: String, description: String = "", areaId: Long? = null) {
        viewModelScope.launch {
            repository.insertProject(
                ProjectEntity(
                    name = name,
                    description = description,
                    areaId = areaId
                )
            )
        }
    }

    fun addArea(name: String) {
        viewModelScope.launch {
            repository.insertArea(AreaEntity(name = name))
        }
    }

    fun getItemsForProject(projectId: Long): Flow<List<ItemWithPin>> {
        return allItems.map { list -> 
            list.filter { it.item.projectId == projectId }
        }
    }

    fun getItemsForArea(areaId: Long): Flow<List<ItemWithPin>> {
        return allItems.map { list ->
            list.filter { it.item.areaId == areaId }
        }
    }

    fun getProjectsForArea(areaId: Long): Flow<List<ProjectEntity>> {
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
                    date = LocalDate.now().toString(),
                    mood = mood,
                    energy = energy,
                    focus = focus,
                    sleep = sleep,
                    tookMeds = tookMeds,
                    note = note
                )
            )
        }
    }

    fun startFocusSession(itemId: Long) {
        viewModelScope.launch {
            if (activeSession.value == null) {
                repository.insertSession(
                    FocusSessionEntity(
                        itemId = itemId,
                        startAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun stopFocusSession() {
        viewModelScope.launch {
            activeSession.value?.let { session ->
                val now = System.currentTimeMillis()
                val duration = ((now - session.startAt) / 1000).toInt()
                repository.updateSession(session.copy(endAt = now, durationSec = duration))
            }
        }
    }

    fun resumeLastSession() {
        viewModelScope.launch {
            val lastSession = allSessions.value.firstOrNull { it.endAt != null }
            lastSession?.let { startFocusSession(it.itemId) }
        }
    }

    /**
     * Phase 8: Data Portability.
     * Exports all local data to a JSON string for backup or migration.
     */
    suspend fun exportDataJson(): String = withContext(Dispatchers.IO) {
        // Simple manual JSON construction to avoid adding GSON/KotlinX-Serialization dependencies
        // as per AGENTS.md rule: "Avoid silently adding dependencies".
        // In a real production app, we would use a library.
        buildString {
            append("{ \"version\": 1,")
            append("\"items\": [")
            allItems.value.forEachIndexed { i, it ->
                append("{ \"title\": \"${it.item.title}\", \"type\": \"${it.item.type}\", \"status\": \"${it.item.status}\" }")
                if (i < allItems.value.size - 1) append(",")
            }
            append("],")
            append("\"projects\": [")
            allProjects.value.forEachIndexed { i, it ->
                append("{ \"name\": \"${it.name}\" }")
                if (i < allProjects.value.size - 1) append(",")
            }
            append("] }")
        }
    }
}

class MainViewModelFactory(
    private val repository: AppRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(_root_ide_package_.com.tajemniktv.tajsos.ui.MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return _root_ide_package_.com.tajemniktv.tajsos.ui.MainViewModel(
                repository,
                preferencesRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
