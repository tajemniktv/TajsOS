/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.ModePreferenceEntity
import com.tajemniktv.tajsos.data.ModeQueryProfile
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin

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
    val areaHealth: Map<Long, String> = emptyMap(),
    val areaHealthMetrics: Map<Long, AreaHealthMetrics> = emptyMap(),
    val dominantAreaId: Long? = null,
    val disappearingAreaIds: Set<Long> = emptySet(),
    val areaImbalanceScore: Int = 0,
    val areaImbalanceLabel: String = "balanced",
    val openLoopsOverloadWarning: String? = null,
    val openLoopsDecayAverage: Int = 0,
    val maintenanceAdminDebtMeter: Int = 0,
    val maintenanceOverdueWarning: String? = null,
    val systemLoad: Int = 0,
    val fragmentation: Int = 0,
    val capacityWarning: String? = null,
    val openLoops: List<NodeWithPin> = emptyList(),
    val pendingDecisions: List<NodeWithPin> = emptyList(),
    val maintenanceQueue: List<NodeWithPin> = emptyList(),
    val activeProtocols: List<NodeWithPin> = emptyList(),
    val relationshipsToContact: List<NodeWithPin> = emptyList(),
    val contextClusteredTasks: Map<String, List<NodeWithPin>> = emptyMap(),
    val currentMode: ModeEntity? = null,
    val modePreferences: ModePreferenceEntity? = null,
    val modeQueryProfile: ModeQueryProfile? = null,
    val tinyVictories: List<NodeWithPin> = emptyList(),
    val shoppingList: List<NodeWithPin> = emptyList(),
    val unresolvedBureaucracy: List<NodeWithPin> = emptyList(),
    val modeSuggestion: String? = null,
    val suggestedContextKey: String? = null,
    val suggestedContextTasks: List<NodeWithPin> = emptyList(),
)

data class AreaHealthMetrics(
    val areaId: Long,
    val areaTitle: String,
    val status: String = "stable",
    val activeItems: Int = 0,
    val openLoops: Int = 0,
    val deadlines: Int = 0,
    val overdueDeadlines: Int = 0,
    val stressLoad: Int = 0,
    val recentActivity: Int = 0,
    val neglectedDays: Int = 0,
    val doneThisWeek: Int = 0,
    val lastActivityAt: Long? = null,
    val isDisappearing: Boolean = false,
)

data class AreaHealthSnapshot(
    val areas: List<AreaHealthMetrics> = emptyList(),
    val dominantAreaId: Long? = null,
    val disappearingAreaIds: Set<Long> = emptySet(),
    val imbalanceScore: Int = 0,
    val imbalanceLabel: String = "balanced",
)

data class OpenLoopStatusItem(
    val node: NodeWithPin,
    val urgency: String,
    val ageDays: Int,
    val stalenessDays: Int,
    val decayScore: Int,
    val relatedPersonId: Long? = null,
    val relatedPersonName: String? = null,
)

data class OpenLoopsSnapshot(
    val active: List<OpenLoopStatusItem> = emptyList(),
    val inbox: List<OpenLoopStatusItem> = emptyList(),
    val review: List<OpenLoopStatusItem> = emptyList(),
    val resolved: List<OpenLoopStatusItem> = emptyList(),
    val byArea: Map<Long?, List<OpenLoopStatusItem>> = emptyMap(),
    val byPerson: Map<Long, List<OpenLoopStatusItem>> = emptyMap(),
    val byUrgency: Map<String, List<OpenLoopStatusItem>> = emptyMap(),
    val overloadWarning: String? = null,
    val averageDecayScore: Int = 0,
)

data class MaintenanceStatusItem(
    val node: NodeWithPin,
    val urgency: String,
    val isRecurring: Boolean,
    val overdueDays: Int = 0,
    val dueInDays: Int? = null,
)

data class MaintenanceSnapshot(
    val active: List<MaintenanceStatusItem> = emptyList(),
    val recurring: List<MaintenanceStatusItem> = emptyList(),
    val overdue: List<MaintenanceStatusItem> = emptyList(),
    val byType: Map<String, List<MaintenanceStatusItem>> = emptyMap(),
    val byArea: Map<Long?, List<MaintenanceStatusItem>> = emptyMap(),
    val byUrgency: Map<String, List<MaintenanceStatusItem>> = emptyMap(),
    val expirationReminders: List<MaintenanceStatusItem> = emptyList(),
    val breakIfIgnored: List<MaintenanceStatusItem> = emptyList(),
    val adminDebtMeter: Int = 0,
    val overdueWarning: String? = null,
)

data class TimeCountdownItem(
    val node: NodeWithPin,
    val daysLeft: Long,
)

data class ProjectPhaseItem(
    val project: NodeEntity,
    val isActivePhase: Boolean,
    val phaseLabel: String,
)

data class TimeArchitectureSnapshot(
    val todayLayer: List<NodeWithPin> = emptyList(),
    val weekLayer: List<NodeWithPin> = emptyList(),
    val monthLayer: List<NodeWithPin> = emptyList(),
    val semesterLayer: List<NodeWithPin> = emptyList(),
    val examPeriodMode: Boolean = false,
    val projectPhases: List<ProjectPhaseItem> = emptyList(),
    val countdowns: List<TimeCountdownItem> = emptyList(),
    val monthlyResetDate: String = "",
    val weeklyMap: Map<String, Int> = emptyMap(),
    val seasonalGoals: List<NodeWithPin> = emptyList(),
    val temporaryFocusPeriods: List<NodeWithPin> = emptyList(),
    val shortHorizonTasks: List<NodeWithPin> = emptyList(),
    val longHorizonTasks: List<NodeWithPin> = emptyList(),
    val lifePeriodMarkers: List<NodeWithPin> = emptyList(),
)
