/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.ModePreferenceEntity
import com.tajemniktv.tajsos.data.ModeQueryProfile
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin

/**
 * A central UI state wrapper containing all pre-calculated lists, metrics, and warnings for the main dashboard.
 *
 * @param tasksCount The total number of active task nodes.
 * @param notesCount The total number of active note nodes.
 * @param pinnedKnowledge A list of nodes (notes/knowledge) explicitly pinned by the user.
 * @param upcomingDeadlines A list of nodes with impending due dates.
 * @param overdueNodes A list of nodes where the due date has passed.
 * @param relevantNote A single dynamically surfaced note deemed highly relevant to the current context or time.
 * @param lowEnergyTasks A list of active tasks tagged with a low energy requirement.
 * @param batchableTasks A map grouping active tasks by their assigned context or location to facilitate batching.
 * @param quickWins A list of short, low-friction tasks that can be completed quickly.
 * @param deepWork A list of high-focus, high-energy tasks requiring sustained attention.
 * @param topTakeaways A curated list of nodes containing key summaries or learnings.
 * @param readLaterVault A curated list of notes or reference items tagged to read later.
 * @param quoteVault A list of nodes containing saved quotes or philosophical excerpts.
 * @param ideaIncubator A list of rough notes or underdeveloped concepts waiting for review.
 * @param archivedThisWeek A list of nodes that were completed or archived within the current week.
 * @param neglectedThisWeek A list of active nodes that have seen no updates or interaction this week.
 * @param foundationalNotes A list of core knowledge or heavily linked reference notes.
 * @param resourceHighlights A list of reference items marked as highly valuable.
 * @param stickyNotes A list of temporary or fleeting notes meant for immediate visibility.
 * @param criticalProjects A list of project entities flagged as urgent, failing, or critical path.
 * @param forgottenWisdom A randomly surfaced older note to encourage serendipitous review.
 * @param deservesAttention A list of nodes prioritized by a combination of age, links, and urgency.
 * @param areaHealth A simple mapping of Area IDs to their string health status (e.g., "stable", "neglected").
 * @param areaHealthMetrics Detailed metrics for every active Area of Responsibility.
 * @param dominantAreaId The ID of the Area currently consuming the most time, energy, or tasks.
 * @param disappearingAreaIds A set of Area IDs that are severely neglected and falling out of focus.
 * @param areaImbalanceScore A calculated metric (0-100) indicating how skewed the user's focus is across Areas.
 * @param areaImbalanceLabel A string descriptor of the imbalance score (e.g., "balanced", "hyperfocused").
 * @param openLoopsOverloadWarning An optional warning message triggered if the user has too many unresolved inputs.
 * @param openLoopsDecayAverage The average age or "decay" score of unresolved work.
 * @param maintenanceAdminDebtMeter A metric (0-100) indicating the backlog of personal admin or maintenance tasks.
 * @param maintenanceOverdueWarning An optional warning message if critical maintenance tasks are overdue.
 * @param systemLoad A holistic metric combining task volume, deadlines, and active projects.
 * @param fragmentation A metric evaluating how context-switching or scattered the user's attention currently is.
 * @param capacityWarning An optional warning message if the calculated workload exceeds the user's estimated capacity.
 * @param openLoops A raw list of nodes classified as unresolved work.
 * @param pendingDecisions A list of nodes explicitly marked as decisions requiring a choice.
 * @param maintenanceQueue A list of active tasks tagged as routine maintenance or chores.
 * @param activeProtocols A list of instantiated routine/playbook nodes (List<NodeWithPin>) rather than template definitions.
 * @param relationshipsToContact A list of relationship anchors scheduled for follow-up.
 * @param contextClusteredTasks A map of tasks grouped dynamically by their string context (e.g., "errands").
 * @param currentMode The currently active focus Mode (e.g., "Work", "Relax").
 * @param modePreferences The UI and functional preferences associated with the [currentMode].
 * @param modeQueryProfile The underlying query constraints for the [currentMode].
 * @param tinyVictories A list of extremely small, recently completed tasks to boost morale.
 * @param shoppingList A curated list of active nodes tagged as items to purchase.
 * @param unresolvedBureaucracy A list of tasks relating to paperwork, taxes, or administrative hurdles.
 * @param modeSuggestion An optional system suggestion to switch to a different Mode based on time or context.
 * @param suggestedContextKey An optional suggested context string (e.g., "laptop") based on current patterns.
 * @param suggestedContextTasks A list of tasks matching the [suggestedContextKey].
 */
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

/**
 * Detailed analytics and health status for a specific Area of Responsibility.
 *
 * @param areaId The unique ID of the Area node.
 * @param areaTitle The title of the Area node.
 * @param status A string descriptor of the area's health using real tokens (e.g., "on_fire", "overloaded", "stable").
 * @param activeItems The count of all active non-area nodes in this area (not only tasks).
 * @param openLoops The count of unresolved work items in this area.
 * @param deadlines The count of impending deadlines in this area.
 * @param overdueDeadlines The count of missed deadlines in this area.
 * @param stressLoad A calculated numeric load indicating how heavy this area currently is.
 * @param recentActivity The number of interactions or completed items in this area recently.
 * @param neglectedDays The number of days since this area saw meaningful activity.
 * @param doneThisWeek The number of tasks completed within this area this week.
 * @param lastActivityAt The epoch timestamp of the last activity in this area.
 * @param isDisappearing A flag indicating if this area has been neglected long enough to be at risk of failing.
 */
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

/**
 * A snapshot encompassing the health metrics for all Areas of Responsibility across the system.
 *
 * @param areas A list of individual [AreaHealthMetrics] for each active Area.
 * @param dominantAreaId The ID of the Area with the highest stress load or activity.
 * @param disappearingAreaIds A set of Area IDs classified as heavily neglected.
 * @param imbalanceScore A computed variance score (0-100) representing how unevenly attention is distributed.
 * @param imbalanceLabel A qualitative descriptor of the [imbalanceScore].
 */
data class AreaHealthSnapshot(
    val areas: List<AreaHealthMetrics> = emptyList(),
    val dominantAreaId: Long? = null,
    val disappearingAreaIds: Set<Long> = emptySet(),
    val imbalanceScore: Int = 0,
    val imbalanceLabel: String = "balanced",
)

/**
 * A data class representing a specific unresolved work item and its calculated urgency.
 *
 * @param node The [NodeWithPin] wrapper for the unresolved item.
 * @param urgency A calculated string representing how critical the loop is (e.g., "high", "low").
 * @param ageDays The number of days since the loop was captured.
 * @param stalenessDays The number of days since the loop was last modified or reviewed.
 * @param decayScore A severity score escalating as the loop ages without resolution.
 * @param relatedPersonId Optional ID of a person entity this loop depends on (e.g., waiting for reply).
 * @param relatedPersonName Optional name of the person entity this loop depends on.
 */
data class OpenLoopStatusItem(
    val node: NodeWithPin,
    val urgency: String,
    val ageDays: Int,
    val stalenessDays: Int,
    val decayScore: Int,
    val relatedPersonId: Long? = null,
    val relatedPersonName: String? = null,
)

/**
 * A snapshot encompassing all unresolved work across the system, categorized by state, area, and urgency.
 *
 * @param active A list of all currently active unresolved items.
 * @param inbox A list of unprocessed unresolved items still residing in the inbox.
 * @param review A list of older unresolved items specifically flagged for review.
 * @param resolved A list of unresolved items closed recently.
 * @param byArea A mapping of unresolved items to their respective Area IDs.
 * @param byPerson A mapping of unresolved items to the relationship anchor ID they are waiting on.
 * @param byUrgency A mapping of unresolved items grouped by their calculated urgency string.
 * @param overloadWarning An optional warning if the system detects too many active unresolved items.
 * @param averageDecayScore The calculated average of all active loop decay scores.
 */
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

/**
 * A data class representing a routine or maintenance task, tracking its recurrence and overdue status.
 *
 * @param node The [NodeWithPin] wrapper for the maintenance task.
 * @param urgency A string representing how critical it is to complete the task (e.g., "critical").
 * @param isRecurring True if the task has a defined repeating schedule.
 * @param overdueDays The number of days past its intended due date.
 * @param dueInDays The number of days remaining until the task is due.
 */
data class MaintenanceStatusItem(
    val node: NodeWithPin,
    val urgency: String,
    val isRecurring: Boolean,
    val overdueDays: Int = 0,
    val dueInDays: Int? = null,
)

/**
 * A snapshot aggregating all routine, chore, and admin tasks across the system.
 *
 * @param active A list of all active maintenance tasks.
 * @param recurring A list of maintenance tasks explicitly marked as repeating.
 * @param overdue A list of maintenance tasks whose due date has passed.
 * @param byType A map of maintenance tasks grouped by specific chore types (e.g., "cleaning", "finance").
 * @param byArea A map of maintenance tasks grouped by their parent Area ID.
 * @param byUrgency A map of maintenance tasks grouped by their urgency level.
 * @param expirationReminders Tasks linked to expiring documents or warranties (e.g., passport renewal).
 * @param breakIfIgnored Critical maintenance tasks that carry severe consequences if neglected.
 * @param adminDebtMeter A calculated metric (0-100) showing how heavily the user is procrastinating on admin work.
 * @param overdueWarning An optional alert if a critical maintenance task is severely overdue.
 */
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

/**
 * A data class representing a node (often an event or deadline) mapped to a specific future countdown.
 *
 * @param node The [NodeWithPin] wrapper.
 * @param daysLeft The calculated number of days remaining until the node's target date.
 */
data class TimeCountdownItem(
    val node: NodeWithPin,
    val daysLeft: Long,
)

/**
 * A data class describing the current phase of an active project.
 *
 * @param project The parent [NodeEntity] representing the project.
 * @param isActivePhase True if the project is currently in an active execution phase.
 * @param phaseLabel A descriptive string of the current phase (e.g., "Planning", "Execution", "Review").
 */
data class ProjectPhaseItem(
    val project: NodeEntity,
    val isActivePhase: Boolean,
    val phaseLabel: String,
)

/**
 * A comprehensive snapshot breaking down tasks and projects across distinct temporal horizons.
 *
 * @param todayLayer A list of tasks strictly scheduled or intended for today.
 * @param weekLayer A list of tasks scheduled for the current week but not necessarily today.
 * @param monthLayer A list of tasks or milestones targeted for the current month.
 * @param semesterLayer A list of academic or long-term goals spanning the current semester or quarter.
 * @param examPeriodMode True if the system detects an impending heavy exam period.
 * @param projectPhases A list of active projects and their corresponding [ProjectPhaseItem] states.
 * @param countdowns A list of significant upcoming events tracked via [TimeCountdownItem].
 * @param monthlyResetDate A string representing the next major monthly planning or reset date.
 * @param weeklyMap A mapping grouping upcoming tasks by specific days of the week.
 * @param seasonalGoals A list of nodes tagged as broad goals for the current season.
 * @param temporaryFocusPeriods A list of nodes indicating short-term intensive focus themes (e.g., "Launch week").
 * @param shortHorizonTasks A list of all tasks due within the near future (e.g., next 7 days).
 * @param longHorizonTasks A list of all tasks scheduled significantly far into the future.
 * @param lifePeriodMarkers Major milestones or distinct life events tracked in the system.
 */
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
