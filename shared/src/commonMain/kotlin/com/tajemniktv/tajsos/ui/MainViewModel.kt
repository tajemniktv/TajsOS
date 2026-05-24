/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import kotlin.coroutines.cancellation.CancellationException

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tajemniktv.tajsos.data.AppPack
import com.tajemniktv.tajsos.data.AppRepository
import com.tajemniktv.tajsos.data.AttachmentEntity
import com.tajemniktv.tajsos.data.CalendarProviderEntity
import com.tajemniktv.tajsos.data.DecisionOptionEntity
import com.tajemniktv.tajsos.data.DesktopWindowStartupMode
import com.tajemniktv.tajsos.data.EventLogEntity
import com.tajemniktv.tajsos.data.ExportBundle
import com.tajemniktv.tajsos.data.FocusSessionEntity
import com.tajemniktv.tajsos.data.InboxEntryEntity
import com.tajemniktv.tajsos.data.ItemKind
import com.tajemniktv.tajsos.data.MedicationEntity
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.ModeUsageLogEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeSnapshotEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.PackRegistry
import com.tajemniktv.tajsos.data.PreferencesRepository
import com.tajemniktv.tajsos.data.ProtocolHistoryEntity
import com.tajemniktv.tajsos.data.RecordKind
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.data.ReviewEntity
import com.tajemniktv.tajsos.data.TagEntity
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.data.TrackEntryEntity
import com.tajemniktv.tajsos.data.TrackMedicationJoinEntity
import com.tajemniktv.tajsos.data.UserEntity
import com.tajemniktv.tajsos.data.UserProfile
import com.tajemniktv.tajsos.data.isAreaItem
import com.tajemniktv.tajsos.data.isDecisionSupportItem
import com.tajemniktv.tajsos.data.isProjectItem
import com.tajemniktv.tajsos.data.isResolvedDecisionSupportItem
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.main.actions.DecisionCommands
import com.tajemniktv.tajsos.ui.main.actions.MainNodeSupport
import com.tajemniktv.tajsos.ui.main.actions.NodeCommands
import com.tajemniktv.tajsos.ui.main.actions.ProtocolCommands
import com.tajemniktv.tajsos.ui.main.actions.RelationshipCommands
import com.tajemniktv.tajsos.ui.main.actions.StudentCommands
import com.tajemniktv.tajsos.ui.main.bootstrap.AppBootstrapper
import com.tajemniktv.tajsos.ui.main.calculators.buildCalendarEntries
import com.tajemniktv.tajsos.ui.main.calculators.buildDashboardUIState
import com.tajemniktv.tajsos.ui.main.calculators.buildPlaybookSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.buildProtocolHistoryItems
import com.tajemniktv.tajsos.ui.main.calculators.buildTransitionProtocolsSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculateAreaHealthSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculateCapacitySnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculateCombinedDirectionSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculateCoreLifeOSShiftSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculateInsights
import com.tajemniktv.tajsos.ui.main.calculators.calculateLifeOSSecondBrainSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculateLifeOSSignatureSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculateMaintenanceSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculateOpenLoopsSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculatePersonalRulesSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculatePhysicalLogisticsSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculateRelationshipSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculateStudentBoardState
import com.tajemniktv.tajsos.ui.main.calculators.calculateTimeArchitectureSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.calculateVaultsSnapshot
import com.tajemniktv.tajsos.ui.main.calculators.categorizeNodes
import com.tajemniktv.tajsos.ui.main.calculators.matchesQuery
import com.tajemniktv.tajsos.ui.main.state.CalendarEntry
import com.tajemniktv.tajsos.ui.main.state.CapacityInputs
import com.tajemniktv.tajsos.ui.main.state.CapacitySnapshot
import com.tajemniktv.tajsos.ui.main.state.CombinedDirectionInputs
import com.tajemniktv.tajsos.ui.main.state.CombinedDirectionSnapshot
import com.tajemniktv.tajsos.ui.main.state.CoreLifeOSShiftContext
import com.tajemniktv.tajsos.ui.main.state.CoreLifeOSShiftInputs
import com.tajemniktv.tajsos.ui.main.state.CoreLifeOSShiftSnapshot
import com.tajemniktv.tajsos.ui.main.state.DecisionStaleItem
import com.tajemniktv.tajsos.ui.main.state.ExportData
import com.tajemniktv.tajsos.ui.main.state.InsightsData
import com.tajemniktv.tajsos.ui.main.state.LifeOSSecondBrainContext
import com.tajemniktv.tajsos.ui.main.state.LifeOSSecondBrainInputs
import com.tajemniktv.tajsos.ui.main.state.LifeOSSecondBrainSnapshot
import com.tajemniktv.tajsos.ui.main.state.LifeOSSignatureContext
import com.tajemniktv.tajsos.ui.main.state.LifeOSSignatureInputs
import com.tajemniktv.tajsos.ui.main.state.LifeOSSignatureSnapshot
import com.tajemniktv.tajsos.ui.main.state.NodeCategorization
import com.tajemniktv.tajsos.ui.main.state.PersonalRulesSnapshot
import com.tajemniktv.tajsos.ui.main.state.PhysicalLogisticsSnapshot
import com.tajemniktv.tajsos.ui.main.state.PlaybookSnapshot
import com.tajemniktv.tajsos.ui.main.state.PlaybookTemplate
import com.tajemniktv.tajsos.ui.main.state.ProtocolHistoryItem
import com.tajemniktv.tajsos.ui.main.state.RelationshipSnapshot
import com.tajemniktv.tajsos.ui.main.state.SearchFiltersState
import com.tajemniktv.tajsos.ui.main.state.SearchPrimaryFilters
import com.tajemniktv.tajsos.ui.main.state.SearchSecondaryFilters
import com.tajemniktv.tajsos.ui.main.state.SearchTertiaryFilters
import com.tajemniktv.tajsos.ui.main.state.StudentBoardState
import com.tajemniktv.tajsos.ui.main.state.TransitionProtocolTemplate
import com.tajemniktv.tajsos.ui.main.state.TransitionProtocolsSnapshot
import com.tajemniktv.tajsos.ui.main.state.VaultsSnapshot
import com.tajemniktv.tajsos.ui.main.state.defaultPlaybookTemplates
import com.tajemniktv.tajsos.ui.main.state.defaultTransitionProtocolTemplates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.time.Clock

/**
 * Global orchestrator for the application shell.
 *
 * Handles shell-level state (including modes, sync status, application locking, and sidebar state).
 * It delegates feature-heavy orchestration to internal command handlers (e.g., [NodeCommands])
 * but acts as the root provider of core app state.
 */
@Stable
class MainViewModel(
    private val repository: AppRepository,
    private val preferencesRepository: PreferencesRepository,
    private val calendarManager: com.tajemniktv.tajsos.calendar.CalendarManager,
    private val nextStepFallbackLabel: String = "Next step",
    private val untitledFallbackLabel: String = "Untitled",
) : ViewModel() {
    private val appBootstrapper by lazy {
        AppBootstrapper(
            repository = repository,
            preferencesRepository = preferencesRepository,
        )
    }
    private val mainNodeSupport by lazy {
        MainNodeSupport(
            repository = repository,
            scope = viewModelScope,
            currentNodes = { allNodes.value },
            currentTags = { allTags.value },
        )
    }
    private val nodeCommands by lazy {
        NodeCommands(
            repository = repository,
            scope = viewModelScope,
            currentTodayNodes = { todayNodes.value },
            currentAllNodes = { allNodes.value.map { it.node } },
            parseInternalLinks = mainNodeSupport::parseInternalLinks,
            setTagOnNode = mainNodeSupport::setTagOnNode,
            defaultNextStepLabel = { nextStepFallbackLabel },
            defaultUntitledLabel = { untitledFallbackLabel },
        )
    }
    private val protocolCommands by lazy {
        ProtocolCommands(
            repository = repository,
            scope = viewModelScope,
            currentNodes = { allNodes.value },
            currentTags = { allTags.value },
            protocolTemplates = { transitionProtocolTemplates },
            playbookTemplates = { playbookTemplates },
        )
    }
    private val studentCommands by lazy {
        StudentCommands(
            repository = repository,
            scope = viewModelScope,
            currentTags = { allTags.value },
            addNodeForResult = ::addNodeForResult,
            startFocusSession = ::startFocusSession,
            addRelation = ::addRelation,
        )
    }
    private val decisionCommands by lazy {
        DecisionCommands(
            repository = repository,
            scope = viewModelScope,
            addRelation = ::addRelation,
            updateNode = ::updateNode,
        )
    }
    private val relationshipCommands by lazy {
        RelationshipCommands(
            repository = repository,
            scope = viewModelScope,
            currentTemplates = { allTemplates.value },
            addNodeForResult = ::addNodeForResult,
            addRelation = ::addRelation,
            updateNode = ::updateNode,
            setTagOnNode = mainNodeSupport::setTagOnNode,
        )
    }

    val allNodes: StateFlow<List<NodeWithPin>> =
        repository
            .getAllNodes()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val allModesRaw: StateFlow<List<ModeEntity>> =
        repository
            .getAllModes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allModes: StateFlow<List<ModeEntity>> =
        combine(allModesRaw, preferencesRepository.enabledPacks) { modes, packs ->
            modes.filter { packs.canUseMode(it.key) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Flow of all nodes classified as area items, emitting only nodes where [NodeEntity.isAreaItem] is true.
     */
    val allAreas: StateFlow<List<NodeEntity>> =
        allNodes
            .map { nodes -> nodes.mapNotNull { item -> item.node.takeIf { it.isAreaItem() } } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeNodes: StateFlow<List<NodeWithPin>> =
        allNodes
            .map { list ->
                list.filter { it.node.status != "archived" } // NON-NLS
            }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val areaHealthSnapshot: StateFlow<AreaHealthSnapshot> =
        combine(activeNodes, allAreas) { nodes, areas ->
            calculateAreaHealthSnapshot(nodes, areas)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AreaHealthSnapshot())

    val openLoopsSnapshot: StateFlow<OpenLoopsSnapshot> =
        combine(activeNodes, repository.getAllRelations()) { nodes, relations ->
            calculateOpenLoopsSnapshot(nodes, relations)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OpenLoopsSnapshot())

    val maintenanceSnapshot: StateFlow<MaintenanceSnapshot> =
        activeNodes
            .map { nodes -> calculateMaintenanceSnapshot(nodes) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MaintenanceSnapshot())

    val dashboardUIState: StateFlow<DashboardUIState> =
        combine(
            activeNodes,
            allModesRaw,
            preferencesRepository.activeModeId,
            allAreas,
            preferencesRepository.enabledPacks,
        ) { nodes, modesList, activeId, areasList, packs ->
            buildDashboardUIState(
                repository = repository,
                nodes = nodes,
                modesList = modesList,
                activeId = activeId,
                areasList = areasList,
                packs = packs,
            )
        }.distinctUntilChanged()
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

    val user: StateFlow<UserEntity?> =
        repository
            .getUser()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Typed local profile state for the operator identity screen.
     */
    val userProfile: StateFlow<UserProfile> =
        repository
            .getUserProfile()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val medications: StateFlow<List<MedicationEntity>> =
        repository
            .getAllMedications()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Flow of all nodes classified as project items. Uses mapNotNull for optimized filtering and mapping.
     */
    val allProjects: StateFlow<List<NodeEntity>> =
        allNodes
            .map { nodes -> nodes.mapNotNull { item -> item.node.takeIf { it.isProjectItem() } } }
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
            repository.getAllScheduleEntries(),
            repository.getCalendarEventsInRange(
                0,
                Long.MAX_VALUE,
            ), // In MVP we can fetch all or a large range
        ) { nodes, schedules, externalEvents ->
            buildCalendarEntries(nodes, schedules, externalEvents)
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

    private val categorizedNodes: StateFlow<NodeCategorization> =
        allNodes
            .map { list -> categorizeNodes(list) }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NodeCategorization())

    val inboxNodes: StateFlow<List<NodeWithPin>> =
        categorizedNodes
            .map { it.inbox }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Raw capture entries that still need semantic triage into tasks, notes, records, or projects.
     */
    val inboxEntries: StateFlow<List<InboxEntryEntity>> =
        repository
            .getActiveInboxEntries()
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

    val currentModeId: StateFlow<Long?> =
        preferencesRepository
            .activeModeId
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentMode: StateFlow<ModeEntity?> =
        combine(allModes, currentModeId) { modes, id ->
            modes.find { it.id == id } ?: modes.firstOrNull { it.key == "COMMAND" } // NON-NLS
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            appBootstrapper.bootstrap()
        }
        syncCalendars()
    }

    /**
     * Switches the global UI context to the specified mode, provided the mode's required pack is unlocked.
     * Updates the active mode preference in the underlying repository.
     *
     * @param modeId The unique identifier of the target mode.
     */
    fun switchMode(modeId: Long) {
        viewModelScope.launch {
            val mode = allModesRaw.value.find { it.id == modeId } ?: return@launch
            val packs = enabledPacks.value
            if (!packs.canUseMode(mode.key)) return@launch
            preferencesRepository.updateActiveModeId(modeId)
            repository.insertModeUsageLog(
                ModeUsageLogEntity(
                    modeId = modeId,
                    activationSource = "manual", // NON-NLS
                ),
            )
        }
    }

    val insights: StateFlow<InsightsData> =
        combine(
            allNodes,
            allSessions,
            trackEntries,
            allProjects,
        ) { nodes, sessions, tracks, projects ->
            calculateInsights(nodes, sessions, tracks, projects)
        }.distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsData())

    val isBiometricEnabled: StateFlow<Boolean?> =
        preferencesRepository.isBiometricEnabled
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Persisted application theme mode.
     * `true` selects dark theme, `false` selects light theme.
     */
    val isDarkTheme: StateFlow<Boolean> =
        preferencesRepository.isDarkThemeEnabled
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /**
     * Selected accent color hex string (e.g., "#BA9EFF").
     */
    val accentColorHex: StateFlow<String> =
        preferencesRepository.accentColorHex
            .stateIn(viewModelScope, SharingStarted.Eagerly, "#BA9EFF")

    /**
     * Whether glassmorphism effects are enabled.
     */
    val isGlassmorphismEnabled: StateFlow<Boolean> =
        preferencesRepository.isGlassmorphismEnabled
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /**
     * Whether to reduce system animations and transitions.
     */
    val reduceMotion: StateFlow<Boolean> =
        preferencesRepository.reduceMotion
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Persisted sidebar behavior mode.
     */
    val sidebarMode: StateFlow<SidebarMode> =
        preferencesRepository.sidebarMode
            .stateIn(viewModelScope, SharingStarted.Eagerly, SidebarMode.EXPANDED)

    /**
     * Persisted desktop expanded sidebar width in dp.
     */
    val sidebarExpandedWidthDp: StateFlow<Int> =
        preferencesRepository.sidebarExpandedWidthDp
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                PreferencesRepository.DEFAULT_SIDEBAR_EXPANDED_WIDTH_DP,
            )

    /**
     * Startup strategy for desktop window placement behavior.
     */
    val desktopWindowStartupMode: StateFlow<DesktopWindowStartupMode> =
        preferencesRepository.desktopWindowStartupMode
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                DesktopWindowStartupMode.RESTORE_LAST,
            )

    val enabledPacks: StateFlow<PackRegistry> =
        preferencesRepository.enabledPacks
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                PackRegistry(
                    ownedPackKeys = AppPack.defaultFreePackKeys,
                    enabledPackKeys = AppPack.defaultFreePackKeys,
                ),
            )

    val protocolHistory: StateFlow<List<ProtocolHistoryEntity>> =
        repository
            .getAllProtocolHistory()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transitionProtocolTemplates: List<TransitionProtocolTemplate> =
        defaultTransitionProtocolTemplates

    val transitionProtocolNodes: StateFlow<List<NodeWithPin>> =
        allNodes
            .map { nodes ->
                nodes
                    .filter { it.node.type == "protocol" && it.node.status != "archived" } // NON-NLS
                    .sortedByDescending { it.node.updatedAt }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val protocolHistoryItems: StateFlow<List<ProtocolHistoryItem>> =
        combine(protocolHistory, allNodes) { history, nodes ->
            buildProtocolHistoryItems(history, nodes)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transitionProtocolsSnapshot: StateFlow<TransitionProtocolsSnapshot> =
        combine(transitionProtocolNodes, protocolHistoryItems) { protocolNodes, historyItems ->
            buildTransitionProtocolsSnapshot(
                protocolNodes,
                historyItems,
                transitionProtocolTemplates,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            TransitionProtocolsSnapshot(),
        )

    val timeArchitectureSnapshot: StateFlow<TimeArchitectureSnapshot> =
        combine(allNodes, todayNodes, allProjects) { nodes, todayLayerNodes, projects ->
            calculateTimeArchitectureSnapshot(
                nodes = nodes,
                todayLayerNodes = todayLayerNodes,
                projects = projects,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeArchitectureSnapshot())

    val relationshipSnapshot: StateFlow<RelationshipSnapshot> =
        combine(allNodes, allRelations) { nodes, relations ->
            calculateRelationshipSnapshot(nodes, relations)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RelationshipSnapshot())

    val playbookTemplates: List<PlaybookTemplate> = defaultPlaybookTemplates

    val playbookSnapshot: StateFlow<PlaybookSnapshot> =
        combine(
            transitionProtocolNodes,
            protocolHistoryItems,
            currentMode,
            trackEntries,
        ) { protocolNodes, historyItems, mode, entries ->
            buildPlaybookSnapshot(protocolNodes, historyItems, mode, entries, playbookTemplates)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaybookSnapshot())

    val physicalLogisticsSnapshot: StateFlow<PhysicalLogisticsSnapshot> =
        combine(
            allNodes,
            allRelations,
            repository.getAllTemplates(),
        ) { nodes, relations, templates ->
            calculatePhysicalLogisticsSnapshot(nodes, relations, templates)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PhysicalLogisticsSnapshot())

    val personalRulesSnapshot: StateFlow<PersonalRulesSnapshot> =
        combine(allNodes, allRelations) { nodes, relations ->
            calculatePersonalRulesSnapshot(nodes, relations)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PersonalRulesSnapshot())

    val vaultsSnapshot: StateFlow<VaultsSnapshot> =
        allNodes
            .map { nodes -> calculateVaultsSnapshot(nodes) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VaultsSnapshot())

    private val capacityInputs: StateFlow<CapacityInputs> =
        combine(
            activeNodes,
            allProjects,
            allAreas,
            maintenanceSnapshot,
            openLoopsSnapshot,
        ) { nodes, projects, areas, maintenance, openLoops ->
            CapacityInputs(
                nodes = nodes,
                projects = projects,
                areas = areas,
                maintenance = maintenance,
                openLoops = openLoops,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CapacityInputs(
                nodes = emptyList(),
                projects = emptyList(),
                areas = emptyList(),
                maintenance = MaintenanceSnapshot(),
                openLoops = OpenLoopsSnapshot(),
            ),
        )

    val capacitySnapshot: StateFlow<CapacitySnapshot> =
        combine(
            capacityInputs,
            trackEntries,
            currentMode,
            allModes,
        ) { inputs, entries, currentMode, allModes ->
            calculateCapacitySnapshot(
                nodes = inputs.nodes,
                projects = inputs.projects,
                areas = inputs.areas,
                maintenance = inputs.maintenance,
                openLoops = inputs.openLoops,
                trackEntries = entries,
                currentMode = currentMode,
                allModes = allModes,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CapacitySnapshot())

    private val lifeOSSignatureInputs: StateFlow<LifeOSSignatureInputs> =
        combine(
            allModes,
            areaHealthSnapshot,
            openLoopsSnapshot,
            maintenanceSnapshot,
            relationshipSnapshot,
        ) { modes, areaHealth, openLoops, maintenance, relationships ->
            LifeOSSignatureInputs(
                modes = modes,
                areaHealth = areaHealth,
                openLoops = openLoops,
                maintenance = maintenance,
                relationships = relationships,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LifeOSSignatureInputs(
                modes = emptyList(),
                areaHealth = AreaHealthSnapshot(),
                openLoops = OpenLoopsSnapshot(),
                maintenance = MaintenanceSnapshot(),
                relationships = RelationshipSnapshot(),
            ),
        )

    private val lifeOSSignatureContext: StateFlow<LifeOSSignatureContext> =
        combine(
            lifeOSSignatureInputs,
            vaultsSnapshot,
            capacitySnapshot,
            playbookSnapshot,
            currentMode,
        ) { inputs, vaults, capacity, playbooks, currentMode ->
            LifeOSSignatureContext(
                inputs = inputs,
                vaults = vaults,
                capacity = capacity,
                playbooks = playbooks,
                currentMode = currentMode,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LifeOSSignatureContext(
                inputs =
                    LifeOSSignatureInputs(
                        modes = emptyList(),
                        areaHealth = AreaHealthSnapshot(),
                        openLoops = OpenLoopsSnapshot(),
                        maintenance = MaintenanceSnapshot(),
                        relationships = RelationshipSnapshot(),
                    ),
                vaults = VaultsSnapshot(),
                capacity = CapacitySnapshot(),
                playbooks = PlaybookSnapshot(),
                currentMode = null,
            ),
        )

    val lifeOSSignatureSnapshot: StateFlow<LifeOSSignatureSnapshot> =
        combine(
            lifeOSSignatureContext,
            trackEntries,
            activeNodes,
        ) { context, entries, nodes ->
            val pendingDecisions =
                nodes.filter {
                    it.node.type == "decision" &&
                        it.node.status == "active" &&
                        !it.node.inboxState
                }
            calculateLifeOSSignatureSnapshot(
                modes = context.inputs.modes,
                areaHealth = context.inputs.areaHealth,
                openLoops = context.inputs.openLoops,
                pendingDecisions = pendingDecisions,
                maintenance = context.inputs.maintenance,
                relationships = context.inputs.relationships,
                vaults = context.vaults,
                capacity = context.capacity,
                playbooks = context.playbooks,
                currentMode = context.currentMode,
                trackEntries = entries,
                nodes = nodes,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LifeOSSignatureSnapshot())

    private val lifeOSSecondBrainInputs: StateFlow<LifeOSSecondBrainInputs> =
        combine(
            activeNodes,
            allRelations,
            dashboardUIState,
            areaHealthSnapshot,
            openLoopsSnapshot,
        ) { nodes, relations, dashboard, areaHealth, openLoops ->
            LifeOSSecondBrainInputs(
                nodes = nodes,
                relations = relations,
                dashboard = dashboard,
                areaHealth = areaHealth,
                openLoops = openLoops,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LifeOSSecondBrainInputs(
                nodes = emptyList(),
                relations = emptyList(),
                dashboard = DashboardUIState(),
                areaHealth = AreaHealthSnapshot(),
                openLoops = OpenLoopsSnapshot(),
            ),
        )

    private val lifeOSSecondBrainContext: StateFlow<LifeOSSecondBrainContext> =
        combine(
            lifeOSSecondBrainInputs,
            maintenanceSnapshot,
            capacitySnapshot,
            transitionProtocolsSnapshot,
            playbookSnapshot,
        ) { inputs, maintenance, capacity, protocols, playbooks ->
            LifeOSSecondBrainContext(
                inputs = inputs,
                maintenance = maintenance,
                capacity = capacity,
                protocols = protocols,
                playbooks = playbooks,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LifeOSSecondBrainContext(
                inputs =
                    LifeOSSecondBrainInputs(
                        nodes = emptyList(),
                        relations = emptyList(),
                        dashboard = DashboardUIState(),
                        areaHealth = AreaHealthSnapshot(),
                        openLoops = OpenLoopsSnapshot(),
                    ),
                maintenance = MaintenanceSnapshot(),
                capacity = CapacitySnapshot(),
                protocols = TransitionProtocolsSnapshot(),
                playbooks = PlaybookSnapshot(),
            ),
        )

    val lifeOSSecondBrainSnapshot: StateFlow<LifeOSSecondBrainSnapshot> =
        combine(
            lifeOSSecondBrainContext,
            currentMode,
            lifeOSSignatureSnapshot,
            vaultsSnapshot,
        ) { context, mode, signature, vaults ->
            calculateLifeOSSecondBrainSnapshot(
                nodes = context.inputs.nodes,
                relations = context.inputs.relations,
                dashboard = context.inputs.dashboard,
                areaHealth = context.inputs.areaHealth,
                openLoops = context.inputs.openLoops,
                maintenance = context.maintenance,
                capacity = context.capacity,
                protocols = context.protocols,
                playbooks = context.playbooks,
                currentMode = mode,
                signature = signature,
                vaults = vaults,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LifeOSSecondBrainSnapshot())

    private val combinedDirectionInputs: StateFlow<CombinedDirectionInputs> =
        combine(
            lifeOSSecondBrainSnapshot,
            lifeOSSignatureSnapshot,
            dashboardUIState,
            physicalLogisticsSnapshot,
            capacitySnapshot,
        ) { distinction, signature, dashboard, logistics, capacity ->
            CombinedDirectionInputs(
                distinction = distinction,
                signature = signature,
                dashboard = dashboard,
                logistics = logistics,
                capacity = capacity,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CombinedDirectionInputs(
                distinction = LifeOSSecondBrainSnapshot(),
                signature = LifeOSSignatureSnapshot(),
                dashboard = DashboardUIState(),
                logistics = PhysicalLogisticsSnapshot(),
                capacity = CapacitySnapshot(),
            ),
        )

    val combinedDirectionSnapshot: StateFlow<CombinedDirectionSnapshot> =
        combine(
            combinedDirectionInputs,
            relationshipSnapshot,
            transitionProtocolsSnapshot,
            maintenanceSnapshot,
            openLoopsSnapshot,
        ) { inputs, relationships, protocols, maintenance, openLoops ->
            calculateCombinedDirectionSnapshot(
                distinction = inputs.distinction,
                signature = inputs.signature,
                dashboard = inputs.dashboard,
                logistics = inputs.logistics,
                capacity = inputs.capacity,
                relationships = relationships,
                protocols = protocols,
                maintenance = maintenance,
                openLoops = openLoops,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CombinedDirectionSnapshot())

    private val coreLifeOSShiftInputs: StateFlow<CoreLifeOSShiftInputs> =
        combine(
            lifeOSSecondBrainSnapshot,
            lifeOSSignatureSnapshot,
            combinedDirectionSnapshot,
            dashboardUIState,
            timeArchitectureSnapshot,
        ) { distinction, signature, direction, dashboard, time ->
            CoreLifeOSShiftInputs(
                distinction = distinction,
                signature = signature,
                direction = direction,
                dashboard = dashboard,
                time = time,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CoreLifeOSShiftInputs(
                distinction = LifeOSSecondBrainSnapshot(),
                signature = LifeOSSignatureSnapshot(),
                direction = CombinedDirectionSnapshot(),
                dashboard = DashboardUIState(),
                time = TimeArchitectureSnapshot(),
            ),
        )

    private val coreLifeOSShiftContext: StateFlow<CoreLifeOSShiftContext> =
        combine(
            coreLifeOSShiftInputs,
            areaHealthSnapshot,
            openLoopsSnapshot,
            maintenanceSnapshot,
            transitionProtocolsSnapshot,
        ) { inputs, areaHealth, openLoops, maintenance, protocols ->
            CoreLifeOSShiftContext(
                inputs = inputs,
                areaHealth = areaHealth,
                openLoops = openLoops,
                maintenance = maintenance,
                protocols = protocols,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CoreLifeOSShiftContext(
                inputs =
                    CoreLifeOSShiftInputs(
                        distinction = LifeOSSecondBrainSnapshot(),
                        signature = LifeOSSignatureSnapshot(),
                        direction = CombinedDirectionSnapshot(),
                        dashboard = DashboardUIState(),
                        time = TimeArchitectureSnapshot(),
                    ),
                areaHealth = AreaHealthSnapshot(),
                openLoops = OpenLoopsSnapshot(),
                maintenance = MaintenanceSnapshot(),
                protocols = TransitionProtocolsSnapshot(),
            ),
        )

    val coreLifeOSShiftSnapshot: StateFlow<CoreLifeOSShiftSnapshot> =
        combine(
            coreLifeOSShiftContext,
            capacitySnapshot,
            currentMode,
        ) { context, capacity, mode ->
            calculateCoreLifeOSShiftSnapshot(
                distinction = context.inputs.distinction,
                signature = context.inputs.signature,
                direction = context.inputs.direction,
                dashboard = context.inputs.dashboard,
                time = context.inputs.time,
                areaHealth = context.areaHealth,
                openLoops = context.openLoops,
                maintenance = context.maintenance,
                protocols = context.protocols,
                capacity = capacity,
                currentMode = mode,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CoreLifeOSShiftSnapshot())

    private val _isBiometricHardwareAvailable = MutableStateFlow(false)
    val isBiometricHardwareAvailable: StateFlow<Boolean> =
        _isBiometricHardwareAvailable.asStateFlow()

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

    /**
     * Persists application theme preference.
     *
     * @param enabled `true` for dark theme, `false` for light theme.
     */
    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateDarkThemeEnabled(enabled)
        }
    }

    /**
     * Persists application accent color preference.
     *
     * @param colorHex The color in hex format (e.g., "#BA9EFF").
     */
    fun setAccentColor(colorHex: String) {
        viewModelScope.launch {
            preferencesRepository.updateAccentColor(colorHex)
        }
    }

    /**
     * Persists application glassmorphism preference.
     */
    fun setGlassmorphismEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateGlassmorphismEnabled(enabled)
        }
    }

    /**
     * Persists application reduce motion preference.
     */
    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateReduceMotion(enabled)
        }
    }

    /**
     * Updates the sidebar behavior mode.
     */
    fun setSidebarMode(mode: SidebarMode) {
        viewModelScope.launch {
            preferencesRepository.updateSidebarMode(mode)
        }
    }

    /**
     * Updates persisted expanded sidebar width for desktop shell.
     */
    fun setSidebarExpandedWidthDp(widthDp: Int) {
        viewModelScope.launch {
            preferencesRepository.updateSidebarExpandedWidthDp(widthDp)
        }
    }

    /**
     * Updates desktop startup behavior for the application window.
     */
    fun setDesktopWindowStartupMode(mode: DesktopWindowStartupMode) {
        viewModelScope.launch {
            preferencesRepository.updateDesktopWindowStartupMode(mode)
        }
    }

    fun setPackEnabled(
        pack: AppPack,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            preferencesRepository.setPackEnabled(pack, enabled)
        }
    }

    fun setPackOwned(
        pack: AppPack,
        owned: Boolean,
    ) {
        viewModelScope.launch {
            preferencesRepository.setPackOwned(pack, owned)
        }
    }

    /**
     * Delegates to [protocolCommands] to launch a guided sequential workflow or checklist.
     *
     * @param protocolLabel The normalized identifier for the requested protocol.
     * @param source The origin surface triggering the protocol (e.g., "dashboard").
     */
    fun triggerProtocol(
        protocolLabel: String,
        source: String = "dashboard", // NON-NLS
    ) {
        protocolCommands.triggerProtocol(protocolLabel, source)
    }

    fun applyProtocolTemplate(protocolLabel: String) {
        protocolCommands.applyProtocolTemplate(protocolLabel)
    }

    fun applyPlaybookTemplate(
        playbookLabel: String,
        modeKey: String? = null,
        areaId: Long? = null,
    ) {
        protocolCommands.applyPlaybookTemplate(playbookLabel, modeKey, areaId)
    }

    fun saveCustomPlaybook(
        label: String,
        checklistLines: List<String>,
        modeKey: String? = null,
        areaId: Long? = null,
    ) {
        protocolCommands.saveCustomPlaybook(label, checklistLines, modeKey, areaId)
    }

    fun setPlaybookModeLink(
        playbookNode: NodeEntity,
        modeKey: String?,
    ) {
        protocolCommands.setPlaybookModeLink(playbookNode, modeKey, ::updateNode)
    }

    fun setPlaybookAreaLink(
        playbookNode: NodeEntity,
        areaId: Long?,
    ) {
        protocolCommands.setPlaybookAreaLink(playbookNode, areaId, ::updateNode)
    }

    fun toggleProtocolChecklistStep(
        protocolNode: NodeEntity,
        checklistIndex: Int,
        checked: Boolean,
    ) {
        protocolCommands.toggleProtocolChecklistStep(protocolNode, checklistIndex, checked)
    }

    /**
     * Creates and inserts a new node into the repository.
     *
     * **Side-effects:**
     * - If the `type` is "task" and the `title` starts with a URL ("http://" or "https://"),
     *   the type is automatically converted to "resource".
     * - Default `inboxState` is `true` for most node types, placing them in the inbox for later review.
     *
     * @param title The title of the node.
     * @param content Optional content/body of the node.
     * @param type The primary type of the node (e.g., "task", "note", "decision").
     * @param projectId The ID of the project this node belongs to, if any.
     * @param areaId The ID of the area this node belongs to, if any.
     * @param isRecurring Whether the node should automatically generate a new instance when completed.
     * @param recurringInterval The interval string (e.g., "DAILY", "WEEKLY") if `isRecurring` is true.
     * @param reminderAt Timestamp for when the user should be reminded.
     * @param color Optional hex color for UI representation.
     * @param icon Optional Material icon string identifier.
     * @param inboxState Whether the node is in the inbox (needs processing). Defaults based on node type.
     * @param contextScreen The screen context where this node was created.
     * @param isSticky Whether the node is pinned/sticky on dashboards.
     * @param decisionCategory If the type is "decision", categorizes its magnitude (e.g., "major", "tiny").
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
        nodeCommands.addNode(
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
            inboxState = inboxState,
            contextScreen = contextScreen,
            isSticky = isSticky,
            decisionCategory = decisionCategory,
        )
    }

    /**
     * Stores a raw capture entry for later triage instead of forcing an immediate object type.
     */
    fun captureInboxEntry(
        rawText: String,
        areaId: Long? = null,
        projectId: Long? = null,
        suggestedKind: ItemKind? = null,
        contextScreen: String? = null,
    ) {
        viewModelScope.launch {
            repository.captureInboxEntry(
                rawText = rawText,
                suggestedKind = suggestedKind,
                homeAreaId = areaId,
                activeProjectId = projectId,
                contextScreen = contextScreen,
            )
        }
    }

    /**
     * Converts a raw inbox capture into a typed LifeOS item by proxying the request to [AppRepository.triageInboxEntry].
     *
     * The conversion is executed asynchronously within the [viewModelScope].
     *
     * @param entryId The ID of the raw inbox entry to triage.
     * @param kind The target [ItemKind] to convert the entry into.
     */
    fun triageInboxEntry(
        entryId: Long,
        kind: ItemKind,
    ) {
        viewModelScope.launch {
            repository.triageInboxEntry(entryId, kind)
        }
    }

    /**
     * Dismisses a raw inbox capture without creating an item.
     */
    fun dismissInboxEntry(entry: InboxEntryEntity) {
        viewModelScope.launch {
            repository.dismissInboxEntry(entry)
        }
    }

    /**
     * Creates and inserts a new node into the repository, returning its assigned ID.
     * Similar to `addNode`, but operates as a suspend function to await the result.
     *
     * @param title The title of the node.
     * @param content Optional content/body of the node.
     * @param type The primary type of the node (e.g., "task", "note", "decision").
     * @param projectId The ID of the project this node belongs to, if any.
     * @param areaId The ID of the area this node belongs to, if any.
     * @param inboxState Whether the node is in the inbox (needs processing). Defaults based on node type.
     * @return The unique ID of the newly inserted node.
     */
    suspend fun addNodeForResult(
        title: String,
        content: String = "",
        type: String = "task", // NON-NLS
        projectId: Long? = null,
        areaId: Long? = null,
        inboxState: Boolean? = null,
    ): Long = nodeCommands.addNodeForResult(title, content, type, projectId, areaId, inboxState)

    /**
     * Updates an existing node in the repository.
     *
     * **Side-effects:**
     * - If the node's `dueAt` is updated to a later time than its previous `dueAt`,
     *   the `postponeCount` is automatically incremented.
     * - Parses the node's `content` for internal links (e.g., `[[Note Title]]`). If found,
     *   it automatically establishes "MENTION" relations to the matched nodes.
     *
     * @param node The updated `NodeEntity` to save.
     */
    fun updateNode(node: NodeEntity) {
        nodeCommands.updateNode(node)
    }

    /**
     * Extracts the first bullet point or list item from the node's content and sets it as the `nextSmallestStep`.
     *
     * @param nodeId The ID of the node to process.
     */
    fun extractNextStep(nodeId: Long) {
        nodeCommands.extractNextStep(nodeId)
    }

    /**
     * Splits a task node's bulleted content into individual subtask nodes.
     *
     * **Side-effects:**
     * - Parses the node's `content` for lines starting with "-" or "*".
     * - Creates a new child task node for each valid line and establishes a "DEPENDS_ON" relation.
     * - Prepends the original node's content with `// SPLIT INTO SUBTASKS`.
     *
     * @param nodeId The ID of the node to split.
     */
    fun splitIntoSubtasks(nodeId: Long) {
        nodeCommands.splitIntoSubtasks(nodeId)
    }

    /**
     * Creates a historical snapshot of the node's current state.
     *
     * @param nodeId The ID of the node to snapshot.
     */
    fun createSnapshot(nodeId: Long) {
        nodeCommands.createSnapshot(nodeId)
    }

    /**
     * Restores a node's state to a previously saved snapshot.
     *
     * @param snapshot The snapshot entity containing the historical state.
     */
    fun restoreSnapshot(snapshot: NodeSnapshotEntity) {
        nodeCommands.restoreSnapshot(snapshot)
    }

    /**
     * Merges the content and relations of multiple nodes into a single primary node.
     *
     * **Side-effects:**
     * - Appends the content of all `otherNodeIds` to the `primaryNodeId`'s content, separated by merge headers.
     * - Automatically archives the nodes that were merged into the primary node.
     * - Transfers all relations (both incoming and outgoing) from the merged nodes to the primary node.
     *
     * @param primaryNodeId The ID of the node that will receive the merged content.
     * @param otherNodeIds The list of node IDs to merge and archive.
     */
    fun mergeNodes(
        primaryNodeId: Long,
        otherNodeIds: List<Long>,
    ) {
        nodeCommands.mergeNodes(primaryNodeId, otherNodeIds)
    }

    /**
     * Splits a single note node into multiple separate note nodes based on markdown headers.
     *
     * **Side-effects:**
     * - Parses the node's `content` for sections starting with `# `.
     * - If multiple sections exist, creates a new note node for each section, using the header as the title.
     * - Automatically archives the original, unsplit note node if the split was successful.
     *
     * @param nodeId The ID of the note node to split.
     */
    fun splitNote(nodeId: Long) {
        nodeCommands.splitNote(nodeId)
    }

    /**
     * Updates the specific `status` (e.g., "done", "archived", "active") of a given node.
     *
     * **Side-effects:**
     * - Automatically updates `completedAt` or `archivedAt` timestamps based on the new status.
     * - If a node with `isRecurring == true` is marked as "done", a new active instance of the
     *   node is automatically created and scheduled for the next `recurringInterval`.
     *
     * @param node The node to update.
     * @param status The new status value.
     */
    fun updateNodeStatus(
        node: NodeEntity,
        status: String,
    ) {
        nodeCommands.updateNodeStatus(node, status)
    }

    /**
     * Sweeps stale tasks into the someday state.
     * @param cutoffDays The threshold in days before a task is considered stale.
     */
    fun sweepStaleTasks(cutoffDays: Int = 3) {
        nodeCommands.sweepStaleTasks(cutoffDays)
    }

    suspend fun getNodeById(id: Long): NodeEntity? = repository.getNodeById(id)

    /**
     * Marks a node as archived, setting its status and archivedAt timestamp.
     * Archived nodes are typically hidden from active views.
     *
     * @param node The node to archive.
     */
    fun archiveNode(node: NodeEntity) {
        updateNodeStatus(node, "archived") // NON-NLS
    }

    /**
     * Permanently deletes a node from the database. This action cannot be undone.
     *
     * @param node The node to delete.
     */
    fun deleteNodePermanently(node: NodeEntity) {
        nodeCommands.deleteNodePermanently(node)
    }

    /**
     * Sets the pinned status of a node for the "Today" view.
     *
     * @param node The node to update.
     * @param isPinned The new pinned status.
     */
    fun togglePin(
        node: NodeEntity,
        isPinned: Boolean,
    ) {
        nodeCommands.togglePin(node, isPinned)
    }

    /**
     * Sets whether the task should be part of today's execution payload.
     */
    fun setTodayPayload(
        node: NodeEntity,
        included: Boolean,
    ) {
        nodeCommands.togglePin(node, included)
    }

    /**
     * Toggles today payload inclusion for a task.
     */
    fun toggleTodayPayload(node: NodeEntity) {
        nodeCommands.togglePin(node, !todayNodes.value.any { it.id == node.id })
    }

    /**
     * Toggles the permanent pin status of a node. Permanently pinned nodes
     * typically bypass normal unpinning logic (e.g., daily resets).
     *
     * @param node The node to update.
     */
    fun togglePermanentPin(node: NodeEntity) {
        nodeCommands.togglePermanentPin(node)
    }

    /**
     * Marks a node as processed, effectively removing it from the inbox.
     *
     * @param nodeId The ID of the node to mark as processed.
     */
    fun markAsProcessed(nodeId: Long) {
        nodeCommands.markAsProcessed(nodeId)
    }

    /**
     * Creates a new project node.
     *
     * @param name The name of the new project.
     * @param description An optional description for the project.
     * @param areaId The ID of the area this project belongs to, if any.
     */
    fun addProject(
        name: String,
        description: String = "",
        areaId: Long? = null,
    ) {
        nodeCommands.addProject(name, description, areaId)
    }

    /**
     * Creates a new area node with the given name.
     *
     * @param name The area's display name.
     */
    fun addArea(name: String) {
        nodeCommands.addArea(name)
    }


    fun updateOpenLoopType(
        node: NodeEntity,
        openLoopType: String,
    ) {
        nodeCommands.updateOpenLoopType(node, openLoopType)
    }

    /**
     * Triages a raw open loop into a structured, actionable task.
     * Applies default task metadata (e.g., active status) via [nodeCommands].
     *
     * @param nodeId The unique identifier of the open loop node to convert.
     */
    fun convertOpenLoopToTask(nodeId: Long) {
        nodeCommands.convertOpenLoopToTask(nodeId)
    }

    /**
     * Triages a raw open loop into a structured decision matrix.
     * Updates the node type and initializes decision metadata via [nodeCommands].
     *
     * @param nodeId The unique identifier of the open loop node to convert.
     */
    fun convertOpenLoopToDecision(nodeId: Long) {
        nodeCommands.convertOpenLoopToDecision(nodeId)
    }

    /**
     * Triages a raw open loop into a persistent knowledge note.
     * Updates the node type and strips transient triage metadata via [nodeCommands].
     *
     * @param nodeId The unique identifier of the open loop node to convert.
     */
    fun convertOpenLoopToNote(nodeId: Long) {
        nodeCommands.convertOpenLoopToNote(nodeId)
    }

    /**
     * Closes an unprocessed captured item, optionally attaching a resolution note.
     * Marks the loop as processed to remove it from triage surfaces.
     *
     * @param nodeId The unique identifier of the open loop node.
     * @param resolutionNote Optional text explaining how or why the loop was resolved.
     */
    fun resolveOpenLoop(
        nodeId: Long,
        resolutionNote: String? = null,
    ) {
        nodeCommands.resolveOpenLoop(nodeId, resolutionNote)
    }

    fun archiveResolvedOpenLoops() {
        nodeCommands.archiveResolvedOpenLoops()
    }

    fun updateMaintenanceType(
        node: NodeEntity,
        maintenanceType: String,
    ) {
        nodeCommands.updateMaintenanceType(node, maintenanceType)
    }

    fun setMaintenanceOverdueAt(
        node: NodeEntity,
        timestamp: Long?,
    ) {
        nodeCommands.setMaintenanceOverdueAt(node, timestamp)
    }

    fun setMaintenanceRecurring(
        node: NodeEntity,
        interval: String?,
    ) {
        nodeCommands.setMaintenanceRecurring(node, interval)
    }

    fun setProjectActivePhase(
        project: NodeEntity,
        active: Boolean,
    ) {
        nodeCommands.setProjectActivePhase(project, active)
    }

    fun setTemporaryFocusPeriod(
        node: NodeEntity,
        days: Int,
    ) {
        nodeCommands.setTemporaryFocusPeriod(node, days)
    }

    fun clearTemporaryFocusPeriod(node: NodeEntity) {
        nodeCommands.clearTemporaryFocusPeriod(node)
    }

    fun setWorkDate(
        node: NodeEntity,
        workAt: Long?,
    ) {
        nodeCommands.setWorkDate(node, workAt)
    }

    fun toggleSeasonalGoal(
        node: NodeEntity,
        enabled: Boolean,
    ) {
        nodeCommands.toggleSeasonalGoal(node, enabled)
    }

    fun addLifePeriodMarker(
        title: String,
        content: String = "",
    ) {
        nodeCommands.addLifePeriodMarker(title, content)
    }

    fun getRelatedItemsForPerson(personId: Long): Flow<List<NodeWithPin>> =
        combine(allNodes, allRelations) { nodes, relations ->
            val relatedIds =
                relations
                    .mapNotNull { relation ->
                        when (personId)
                        {
                            relation.fromNodeId -> relation.toNodeId
                            relation.toNodeId -> relation.fromNodeId
                            else -> null
                        }
                    }.toSet()
            nodes.filter { it.node.id in relatedIds && it.node.type != "person" }
        }

    fun setPersonLastContactNow(person: NodeEntity) {
        relationshipCommands.setPersonLastContactNow(person)
    }

    fun setPersonFollowUpInDays(
        person: NodeEntity,
        days: Int?,
    ) {
        relationshipCommands.setPersonFollowUpInDays(person, days)
    }

    fun setPersonImportantDate(
        person: NodeEntity,
        timestamp: Long?,
    ) {
        relationshipCommands.setPersonImportantDate(person, timestamp)
    }

    fun setPersonSocialEnergyNotes(
        person: NodeEntity,
        notes: String?,
    ) {
        relationshipCommands.setPersonSocialEnergyNotes(person, notes)
    }

    fun setPersonRelationshipContext(
        person: NodeEntity,
        context: String?,
    ) {
        relationshipCommands.setPersonRelationshipContext(person, context)
    }

    fun markImportantRelationship(
        person: NodeEntity,
        important: Boolean,
    ) {
        relationshipCommands.markImportantRelationship(person, important)
    }

    fun setPersonRelationshipType(
        person: NodeEntity,
        type: String?,
    ) {
        relationshipCommands.setPersonRelationshipType(person, type)
    }

    fun linkPersonToNode(
        personId: Long,
        nodeId: Long,
    ) {
        relationshipCommands.linkPersonToNode(personId, nodeId)
    }

    fun unlinkPersonFromNode(
        personId: Long,
        nodeId: Long,
    ) {
        relationshipCommands.unlinkPersonFromNode(personId, nodeId)
    }

    fun createReplyNeededForPerson(
        personId: Long,
        title: String,
        content: String = "",
    ) {
        relationshipCommands.createReplyNeededForPerson(personId, title, content)
    }

    fun createSharedPlanForPerson(
        personId: Long,
        title: String,
        content: String = "",
    ) {
        relationshipCommands.createSharedPlanForPerson(personId, title, content)
    }

    fun createAskAboutNextTimeNote(
        personId: Long,
        prompt: String,
    ) {
        relationshipCommands.createAskAboutNextTimeNote(personId, prompt)
    }

    fun addPlace(
        title: String,
        campus: Boolean = false,
        home: Boolean = false,
    ) {
        relationshipCommands.addPlace(title, campus, home)
    }

    fun linkNodeToPlace(
        nodeId: Long,
        placeId: Long,
    ) {
        relationshipCommands.linkNodeToPlace(nodeId, placeId)
    }

    fun unlinkNodeFromPlace(
        nodeId: Long,
        placeId: Long,
    ) {
        relationshipCommands.unlinkNodeFromPlace(nodeId, placeId)
    }

    fun createWhatToBringList(
        title: String,
        placeId: Long? = null,
    ) {
        relationshipCommands.createWhatToBringList(title, placeId)
    }

    fun createPackingList(title: String) {
        relationshipCommands.createPackingList(title)
    }

    fun createLeaveHomeChecklist(title: String = "Leave-home checklist") {
        relationshipCommands.createLeaveHomeChecklist(title)
    }

    fun createDontForgetSet(title: String) {
        relationshipCommands.createDontForgetSet(title)
    }

    fun createEventPreparationList(title: String) {
        relationshipCommands.createEventPreparationList(title)
    }

    fun createClassBringList(title: String) {
        relationshipCommands.createClassBringList(title)
    }

    fun ensureTravelPackTemplate() {
        relationshipCommands.ensureTravelPackTemplate()
    }

    fun addPhysicalLogisticsNote(
        title: String,
        content: String,
    ) {
        relationshipCommands.addPhysicalLogisticsNote(title, content)
    }

    fun addPersonalRule(
        title: String,
        content: String = "",
        categoryTag: String,
    ) {
        relationshipCommands.addPersonalRule(title, content, categoryTag)
    }

    fun pinOperatingPrinciple(
        node: NodeEntity,
        pinned: Boolean,
    ) {
        relationshipCommands.pinOperatingPrinciple(node, pinned)
    }

    fun linkPrincipleToPlaybook(
        principleId: Long,
        playbookNodeId: Long,
    ) {
        relationshipCommands.linkPrincipleToPlaybook(principleId, playbookNodeId)
    }

    fun unlinkPrincipleFromPlaybook(
        principleId: Long,
        playbookNodeId: Long,
    ) {
        relationshipCommands.unlinkPrincipleFromPlaybook(principleId, playbookNodeId)
    }

    fun addVaultEntry(
        categoryTag: String,
        title: String,
        content: String = "",
        asType: String = "note",
        dueAt: Long? = null,
    ) {
        relationshipCommands.addVaultEntry(categoryTag, title, content, asType, dueAt)
    }

    fun createApplicationStatusEntry(
        title: String,
        status: String,
        dueAt: Long? = null,
    ) {
        relationshipCommands.createApplicationStatusEntry(title, status, dueAt)
    }

    fun markMustFindLater(
        node: NodeEntity,
        enabled: Boolean,
    ) {
        relationshipCommands.markMustFindLater(node, enabled)
    }

    /**
     * Executes end-of-month system maintenance scripts via [nodeCommands].
     * This typically archives stale items, resets repeating structures, and generates monthly reflections.
     */
    fun runMonthlyReset() {
        nodeCommands.runMonthlyReset()
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
        anxiety: Int? = null,
        sleep: Float? = null,
        tookMeds: Boolean = false,
        note: String = "",
        energyPulse: Int? = null,
        affectivePulse: Int? = null,
        cognitivePulse: Int? = null,
        systemPulse: Int? = null,
        recoveryPulse: Float? = null,
        medicationIds: Set<Long> = emptySet(),
    ) {
        viewModelScope.launch {
            val entryId =
                repository.insertTrackEntry(
                    TrackEntryEntity(
                        date =
                            Clock.System
                                .now()
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                                .toString(),
                        moodScore = mood ?: affectivePulse,
                        energyScore = energy ?: energyPulse,
                        focusScore = focus ?: cognitivePulse,
                        anxietyScore = anxiety ?: systemPulse,
                        sleepScore = sleep ?: recoveryPulse,
                        tookMeds = tookMeds,
                        symptomNote = note,
                    ),
                )

            // Insert medication tracking
            medicationIds.forEach { medId ->
                repository.insertTrackMedication(
                    TrackMedicationJoinEntity(
                        trackEntryId = entryId,
                        medicationId = medId,
                        wasTaken = true,
                    ),
                )
            }
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val currentProfile = userProfile.value
            repository.saveUserProfile(
                currentProfile.copy(
                    nickname = name.trim().ifBlank { currentProfile.nickname },
                ),
            )
        }
    }

    /**
     * Persists profile changes captured by the user profile feature.
     */
    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun addMedication(
        substance: String,
        brandNames: String = "",
        dosage: String? = null,
        takeAtHour: Int? = null,
        isOptional: Boolean = false,
    ) {
        viewModelScope.launch {
            repository.insertMedication(
                MedicationEntity(
                    substance = substance,
                    brandNames = brandNames,
                    dosage = dosage,
                    takeAtHour = takeAtHour,
                    isOptional = isOptional,
                ),
            )
        }
    }

    fun updateMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.updateMedication(
                medication.copy(
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
        }
    }

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
        }
    }

    fun getMedicationsForEntry(trackEntryId: Long): Flow<List<TrackMedicationJoinEntity>> = repository.getTrackMedications(trackEntryId)

    /**
     * Initiates a tracked deep-work session for the specified node.
     * Creates and persists a new [FocusSessionEntity] to track duration and interruptions.
     *
     * @param nodeId The unique identifier of the node being focused on.
     */
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

    private val _searchStatusFilter =
        MutableStateFlow<String?>("active") // Default: active // NON-NLS
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

    private val _searchLocationContextFilter = MutableStateFlow<String?>(null)
    val searchLocationContextFilter: StateFlow<String?> = _searchLocationContextFilter.asStateFlow()

    private val _searchEnergyContextFilter = MutableStateFlow<String?>(null)
    val searchEnergyContextFilter: StateFlow<String?> = _searchEnergyContextFilter.asStateFlow()

    private val _searchDeviceContextFilter = MutableStateFlow<String?>(null)
    val searchDeviceContextFilter: StateFlow<String?> = _searchDeviceContextFilter.asStateFlow()

    private val _searchSocialContextFilter = MutableStateFlow<String?>(null)
    val searchSocialContextFilter: StateFlow<String?> = _searchSocialContextFilter.asStateFlow()

    private val _searchTimeWindowContextFilter = MutableStateFlow<String?>(null)
    val searchTimeWindowContextFilter: StateFlow<String?> =
        _searchTimeWindowContextFilter.asStateFlow()

    private val _searchTimeHorizonFilter = MutableStateFlow<String?>(null)
    val searchTimeHorizonFilter: StateFlow<String?> = _searchTimeHorizonFilter.asStateFlow()

    private val _searchSortMode = MutableStateFlow("relevance") // NON-NLS
    val searchSortMode: StateFlow<String> = _searchSortMode.asStateFlow()

    private val _recentSearchQueries = MutableStateFlow<List<String>>(emptyList())
    val recentSearchQueries: StateFlow<List<String>> = _recentSearchQueries.asStateFlow()

    private val searchPrimaryFilters: StateFlow<SearchPrimaryFilters> =
        combine(
            _searchQuery,
            _searchTypeFilter,
            _searchStatusFilter,
            _searchProjectFilter,
            _searchAreaFilter,
        ) { query, type, status, projectId, areaId ->
            SearchPrimaryFilters(
                query = query,
                type = type,
                status = status,
                projectId = projectId,
                areaId = areaId,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SearchPrimaryFilters(
                query = "",
                type = null,
                status = "active",
                projectId = null,
                areaId = null,
            ),
        )

    private val searchSecondaryFilters: StateFlow<SearchSecondaryFilters> =
        combine(
            _searchLinkedToFilter,
            _searchMaxMinutesFilter,
            _searchEnergyFilter,
            _searchFrictionFilter,
            _searchLocationContextFilter,
        ) { linkedToId, maxMins, energy, friction, locationContext ->
            SearchSecondaryFilters(
                linkedToId = linkedToId,
                maxMins = maxMins,
                energy = energy,
                friction = friction,
                locationContext = locationContext,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SearchSecondaryFilters(
                linkedToId = null,
                maxMins = null,
                energy = null,
                friction = null,
                locationContext = null,
            ),
        )

    private val searchTertiaryFilters: StateFlow<SearchTertiaryFilters> =
        combine(
            _searchEnergyContextFilter,
            _searchDeviceContextFilter,
            _searchSocialContextFilter,
            _searchTimeWindowContextFilter,
            _searchTimeHorizonFilter,
        ) { energyContext, deviceContext, socialContext, timeWindowContext, timeHorizon ->
            SearchTertiaryFilters(
                energyContext = energyContext,
                deviceContext = deviceContext,
                socialContext = socialContext,
                timeWindowContext = timeWindowContext,
                timeHorizon = timeHorizon,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SearchTertiaryFilters(
                energyContext = null,
                deviceContext = null,
                socialContext = null,
                timeWindowContext = null,
                timeHorizon = null,
            ),
        )

    private val searchFiltersState: StateFlow<SearchFiltersState> =
        combine(
            searchPrimaryFilters,
            searchSecondaryFilters,
            searchTertiaryFilters,
        ) { primary, secondary, tertiary ->
            SearchFiltersState(
                query = primary.query,
                type = primary.type,
                status = primary.status,
                projectId = primary.projectId,
                areaId = primary.areaId,
                linkedToId = secondary.linkedToId,
                maxMins = secondary.maxMins,
                energy = secondary.energy,
                friction = secondary.friction,
                locationContext = secondary.locationContext,
                energyContext = tertiary.energyContext,
                deviceContext = tertiary.deviceContext,
                socialContext = tertiary.socialContext,
                timeWindowContext = tertiary.timeWindowContext,
                timeHorizon = tertiary.timeHorizon,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchFiltersState())

    val searchResults: StateFlow<List<NodeWithPin>> =
        combine(
            allNodes,
            searchFiltersState,
            allRelations,
            _searchSortMode,
        ) { nodes, filters, relations, sortMode ->
            FilterHelper.filterAndSortNodes(
                nodes = nodes,
                query = filters.query,
                type = filters.type,
                status = filters.status,
                projectId = filters.projectId,
                areaId = filters.areaId,
                linkedToId = filters.linkedToId,
                maxMins = filters.maxMins,
                energy = filters.energy,
                friction = filters.friction,
                locationContext = filters.locationContext,
                energyContext = filters.energyContext,
                deviceContext = filters.deviceContext,
                socialContext = filters.socialContext,
                timeWindowContext = filters.timeWindowContext,
                timeHorizon = filters.timeHorizon,
                relations = relations,
                sortMode = sortMode,
            )
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        val normalized = query.trim()
        _searchQuery.value = query
        if (normalized.length >= 2) {
            _recentSearchQueries.value =
                (
                    _recentSearchQueries.value.filterNot {
                        it.equals(
                            normalized,
                            ignoreCase = true,
                        )
                    } +
                        normalized
                ).takeLast(8)
                    .reversed()
        }
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

    fun updateSearchLocationContextFilter(context: String?) {
        _searchLocationContextFilter.value = context
    }

    fun updateSearchEnergyContextFilter(context: String?) {
        _searchEnergyContextFilter.value = context
    }

    fun updateSearchDeviceContextFilter(context: String?) {
        _searchDeviceContextFilter.value = context
    }

    fun updateSearchSocialContextFilter(context: String?) {
        _searchSocialContextFilter.value = context
    }

    fun updateSearchTimeWindowContextFilter(context: String?) {
        _searchTimeWindowContextFilter.value = context
    }

    fun updateSearchTimeHorizonFilter(horizon: String?) {
        _searchTimeHorizonFilter.value = horizon
    }

    fun updateSearchSortMode(sortMode: String) {
        _searchSortMode.value = sortMode
    }

    fun clearSearchFilters() {
        _searchQuery.value = ""
        _searchTypeFilter.value = null
        _searchStatusFilter.value = "active" // NON-NLS
        _searchProjectFilter.value = null
        _searchAreaFilter.value = null
        _searchLinkedToFilter.value = null
        _searchMaxMinutesFilter.value = null
        _searchEnergyFilter.value = null
        _searchFrictionFilter.value = null
        _searchLocationContextFilter.value = null
        _searchEnergyContextFilter.value = null
        _searchDeviceContextFilter.value = null
        _searchSocialContextFilter.value = null
        _searchTimeWindowContextFilter.value = null
        _searchTimeHorizonFilter.value = null
    }

    fun applyContextPreset(contextKey: String?) {
        _searchLocationContextFilter.value = null
        _searchEnergyContextFilter.value = null
        _searchDeviceContextFilter.value = null
        _searchSocialContextFilter.value = null
        _searchTimeWindowContextFilter.value = null
        when (contextKey)
        {
            "at_home" -> {
                _searchLocationContextFilter.value = "at_home"
            }

            // NON-NLS
            "on_campus" -> {
                _searchLocationContextFilter.value = "on_campus"
            }

            // NON-NLS
            "out_of_home" -> {
                _searchLocationContextFilter.value = "out_of_home"
            }

            // NON-NLS
            "laptop_required" -> {
                _searchDeviceContextFilter.value = "laptop_required"
            }

            // NON-NLS
            "phone_okay" -> {
                _searchDeviceContextFilter.value = "phone_okay"
            }

            // NON-NLS
            "needs_internet" -> {
                _searchDeviceContextFilter.value = "needs_internet"
            }

            // NON-NLS
            "needs_privacy" -> {
                _searchSocialContextFilter.value = "needs_privacy"
            }

            // NON-NLS
            "low_energy" -> {
                _searchEnergyContextFilter.value = "low_energy"
            }

            // NON-NLS
            "high_focus" -> {
                _searchEnergyContextFilter.value = "high_focus"
            }

            // NON-NLS
            "brain_works" -> {
                _searchEnergyContextFilter.value = "brain_works"
            }

            // NON-NLS
            "emotionally_wrecked" -> {
                _searchEnergyContextFilter.value =
                    "emotionally_wrecked"
            }

            // NON-NLS
            "10_minute" -> {
                _searchTimeWindowContextFilter.value = "10_minute"
            }

            // NON-NLS
            "commute_friendly" -> {
                _searchSocialContextFilter.value =
                    "commute_friendly"
            }

            // NON-NLS
            "waiting_room" -> {
                _searchTimeWindowContextFilter.value =
                    "waiting_room"
            }

            // NON-NLS
            null -> {
                Unit
            }
        }
    }

    fun applyTimeHorizonFilter(horizon: String?) {
        _searchTimeHorizonFilter.value = horizon
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

            nodes
                .filter { other ->
                    other.node.id != nodeId &&
                            other.node.status != "archived" && // NON-NLS
                            other.node.type in
                            listOf(
                                "note",
                                "idea",
                                "resource",
                                "project",
                        ) && // NON-NLS
                        (
                            other.tags.any { it.id in currentTags } ||
                                other.node.title.split(" ").any { word ->
                                    word.length > 3 &&
                                        currentNode.title.contains(
                                            word,
                                            ignoreCase = true,
                                        )
                                }
                        )
                }.take(5)
        }

    fun getRelationsForNode(nodeId: Long): Flow<List<RelationEntity>> = repository.getRelationsForNode(nodeId)

    fun getSnapshotsForNode(nodeId: Long): Flow<List<NodeSnapshotEntity>> = repository.getSnapshotsForNode(nodeId)

    fun getLogsForNode(nodeId: Long): Flow<List<EventLogEntity>> = repository.getLogsForNode(nodeId)

    fun addRelation(
        fromNodeId: Long,
        toNodeId: Long,
        type: String,
    ) {
        viewModelScope.launch {
            repository.insertRelation(
                RelationEntity(
                    fromNodeId = fromNodeId,
                    toNodeId = toNodeId,
                    relationType = type,
                ),
            )
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
    )
    {
        viewModelScope.launch {
            repository.insertTag(
                TagEntity(
                    name = name,
                    normalizedName = name.lowercase().trim(),
                    color = color,
                ),
            )
        }
    }

    fun attachTagToNode(
        nodeId: Long,
        tagId: Long,
    )
    {
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

    fun startStudySession(nodeId: Long) {
        studentCommands.startStudySession(nodeId)
    }

    fun setReadingProgress(
        node: NodeEntity,
        progressPercent: Int,
    )
    {
        studentCommands.setReadingProgress(node, progressPercent)
    }

    fun setTopicMastery(
        node: NodeEntity,
        topic: String?,
        masteryPercent: Int,
    ) {
        studentCommands.setTopicMastery(node, topic, masteryPercent)
    }

    fun setStudentCourse(
        node: NodeEntity,
        courseId: String?,
        courseName: String?,
        semester: String?,
        assignmentType: String? = null,
    ) {
        studentCommands.setStudentCourse(node, courseId, courseName, semester, assignmentType)
    }

    fun addStudentNote(
        title: String,
        content: String,
        noteType: String,
        courseId: String? = null,
        courseName: String? = null,
        semester: String? = null,
        topic: String? = null,
    ) {
        studentCommands.addStudentNote(
            title,
            content,
            noteType,
            courseId,
            courseName,
            semester,
            topic,
        )
    }

    fun toggleFlashcardCandidate(
        node: NodeEntity,
        enabled: Boolean,
    )
    {
        studentCommands.toggleFlashcardCandidate(node, enabled)
    }

    fun toggleRevisitBeforeExam(
        node: NodeEntity,
        enabled: Boolean,
    ) {
        studentCommands.toggleRevisitBeforeExam(node, enabled)
    }

    fun linkTopicToNote(
        topicNodeId: Long,
        noteNodeId: Long,
    ) {
        studentCommands.linkTopicToNote(topicNodeId, noteNodeId)
    }

    fun linkPaperToNote(
        paperNodeId: Long,
        noteNodeId: Long,
    )
    {
        studentCommands.linkPaperToNote(paperNodeId, noteNodeId)
    }

    fun getAttachmentsForNode(nodeId: Long): Flow<List<AttachmentEntity>> = repository.getAttachmentsForNode(nodeId)

    fun addAttachment(
        nodeId: Long,
        type: String,
        uri: String,
        title: String? = null,
    ) {
        viewModelScope.launch {
            repository.insertAttachment(
                AttachmentEntity(
                    nodeId = nodeId,
                    assetType = type,
                    uriOrPath = uri,
                    title = title,
                ),
            )
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

    val studentBoardState: StateFlow<StudentBoardState> =
        combine(
            allNodes,
            allRelations,
            allSessions,
            allTemplates,
        ) { nodes, relations, sessions, templates ->
            calculateStudentBoardState(nodes, relations, sessions, templates)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentBoardState())

    fun addTemplate(
        name: String,
        type: String,
        title: String? = null,
        content: String? = null,
    )
    {
        val normalizedType =
            when (type.trim().lowercase())
            {
                "record"                                           -> "record"

                // NON-NLS
                "project"                                          -> "project"

                // NON-NLS
                    "area"                                             -> "area"

                // NON-NLS
                    "idea", "resource", "vault", "document" -> "note"

                    // NON-NLS
                    "maintenance", "open_loop", "decision", "protocol" -> "task"

                    // NON-NLS
                    else -> "task" // NON-NLS
                }
            viewModelScope.launch {
                repository.insertTemplate(
                    TemplateEntity(
                        name = name,
                        nodeType = normalizedType,
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
    )
    {
        viewModelScope.launch {
            val now = Clock.System.now()
            val dateStr = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

            val nodeId =
                repository.insertLifeItem(
                    kind = ItemKind.RECORD,
                        title = "${type.uppercase()} REVIEW - $dateStr",
                    content = content,
                        inboxState = false,
                        source = "review",
                        recordKind = RecordKind.REFLECTION,
                    )

                repository.insertReview(
                ReviewEntity(
                    type = type,
                        date = dateStr,
                        resultNodeId = nodeId,
                        moodScore = mood,
                    energyScore = energy,
                ),
            )

            if (mood != null || energy != null) {
                addTrackEntry(
                    mood = mood,
                    energy = energy,
                    note = "Linked to $type review",
                ) // NON-NLS
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

    suspend fun exportBundleJson(): String =
        withContext(Dispatchers.Default) {
            val enabledPacks = preferencesRepository.enabledPacks.first().enabledPackKeys
            val bundle = repository.buildExportBundle(enabledPacks = enabledPacks)
            Json.encodeToString(bundle)
        }

    suspend fun importDataJson(payload: String): String =
        withContext(Dispatchers.Default) {
            val content = payload.trim()
            if (content.isBlank()) return@withContext "Import failed: empty payload."

            val bundleResult = safeDecode { Json.decodeFromString<ExportBundle>(content) }
            if (bundleResult != null) {
                val report = repository.importBundle(bundleResult)
                return@withContext "Imported bundle: ${report.nodes} nodes, ${report.relations} relations, ${report.events} events."
            }

            val legacyResult = safeDecode { Json.decodeFromString<ExportData>(content) }
            if (legacyResult != null) {
                val count = repository.importLegacyNodes(legacyResult.nodes)
                return@withContext "Imported legacy export: $count nodes."
            }

            "Import failed: unrecognized JSON export format."
        }

    // Decisions
    val decisionInbox: StateFlow<List<NodeWithPin>> =
        allNodes
            .map { nodes ->
                nodes.filter { it.node.isDecisionSupportItem() && it.node.inboxState }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPendingDecisions: StateFlow<List<NodeWithPin>> =
        allNodes
            .map { nodes ->
                nodes.filter {
                    it.node.isDecisionSupportItem() &&
                        it.node.taskStateOrNull() == TaskState.ACTIVE &&
                        !it.node.inboxState &&
                        !it.node.isResolvedDecisionSupportItem()
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val decisionLog: StateFlow<List<NodeWithPin>> =
        allNodes
            .map { nodes ->
                nodes.filter { it.node.isResolvedDecisionSupportItem() }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Flow of active decisions that haven't been resolved for at least 7 days.
     * Uses optimized mapNotNull for filtering and mapping in a single pass without intermediate collections.
     */
    val stalePendingDecisions: StateFlow<List<DecisionStaleItem>> =
        allNodes
            .map { nodes ->
                val now = Clock.System.now().toEpochMilliseconds()
                nodes
                    .mapNotNull { decision ->
                        if (decision.node.isDecisionSupportItem() &&
                            decision.node.taskStateOrNull() == TaskState.ACTIVE &&
                            !decision.node.isResolvedDecisionSupportItem()) {
                            val ageDays =
                                ((now - decision.node.createdAt).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()
                            if (ageDays >= 7) DecisionStaleItem(node = decision, ageDays = ageDays) else null
                        } else null
                    }
                    .sortedByDescending { it.ageDays }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Emits a list of person nodes related to a specific decision.
     * Uses optimized mapNotNull to filter related links and extract person IDs efficiently.
     */
    fun getRelatedPeopleForDecision(decisionId: Long): Flow<List<NodeWithPin>> =
        combine(allNodes, allRelations) { nodes, relations ->
            val personIds =
                relations
                    .mapNotNull { relation ->
                        if (relation.relationType == "RELATED_PERSON" && // NON-NLS
                            (relation.fromNodeId == decisionId || relation.toNodeId == decisionId)
                        ) {
                            if (relation.fromNodeId == decisionId) relation.toNodeId else relation.fromNodeId
                        } else null
                    }.toSet()
            nodes.filter { it.node.id in personIds && it.node.type == "person" } // NON-NLS
        }

    fun linkDecisionToPerson(
        decisionId: Long,
        personId: Long,
    )
    {
        decisionCommands.linkDecisionToPerson(decisionId, personId)
        }

    fun unlinkDecisionFromPerson(
        decisionId: Long,
        personId: Long,
    )
    {
        decisionCommands.unlinkDecisionFromPerson(decisionId, personId)
        }

    fun setDecisionRevisit(
        node: NodeEntity,
        revisitAt: Long?,
    )
    {
        decisionCommands.setDecisionRevisit(node, revisitAt)
    }

    fun getOptionsForDecision(nodeId: Long) = repository.getOptionsForDecision(nodeId)

    fun addDecisionOption(
        nodeId: Long,
        title: String,
        description: String? = null,
    )
    {
        decisionCommands.addDecisionOption(nodeId, title, description)
    }

    fun updateDecisionOption(option: DecisionOptionEntity) = decisionCommands.updateDecisionOption(option)

    fun deleteDecisionOption(option: DecisionOptionEntity) = decisionCommands.deleteDecisionOption(option)

    fun decideOn(
        nodeId: Long,
        outcome: String,
        selectedOptionId: Long? = null,
    )
    {
        decisionCommands.decideOn(nodeId, outcome, selectedOptionId)
    }

    fun convertDecisionToProject(nodeId: Long) {
        decisionCommands.convertDecisionToProject(nodeId)
    }

    fun convertDecisionToTask(nodeId: Long) {
        decisionCommands.convertDecisionToTask(nodeId)
    }
}

private inline fun <T> safeDecode(block: () -> T): T? =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
