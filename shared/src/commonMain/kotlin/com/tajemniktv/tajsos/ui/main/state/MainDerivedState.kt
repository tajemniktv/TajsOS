/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeWithPin

/**
 * A data class pairing a physical location entity with its associated tasks and reminders.
 *
 * @param place The [NodeWithPin] representing the physical place.
 * @param relatedTasks A list of tasks strictly tied to this physical location.
 * @param remindersCount The number of active reminders or deadlines associated with this place.
 */
data class PlaceLogisticsItem(
    val place: NodeWithPin,
    val relatedTasks: List<NodeWithPin>,
    val remindersCount: Int,
)

/**
 * A snapshot mapping tasks, packing lists, and errands to specific physical locations.
 *
 * @param places A general list of tracked physical locations.
 * @param campusLocations A specific list of locations tagged as part of a university or school campus.
 * @param homeZones A specific list of locations tagged as distinct zones within the user's home.
 * @param placeBasedTasks A general list of all tasks requiring a physical location to complete.
 * @param outOfHomeTaskClusters A map grouping away-from-home tasks by their context tags or regions.
 * @param errandClusters A map grouping routine errands by proximity or location tags.
 * @param whatToBringLists Nodes structured as checklists for daily items to bring.
 * @param packingLists Nodes structured as checklists for travel or specific events.
 * @param leaveHomeChecklists Checklists of routines to complete before leaving the house.
 * @param dontForgetSets Lists of commonly forgotten items explicitly tracked to reduce mental load.
 * @param eventPreparationLists Checklists specifically tied to preparing for scheduled events.
 * @param classSpecificBringLists Checklists of materials required for specific academic classes.
 * @param physicalLogisticsNotes Freeform notes detailing logistics (e.g., bus routes, parking info).
 * @param travelPackTemplateReady True if the standard travel packing template is configured.
 * @param locationSpecificReminders Reminders specifically triggered or filtered by location context.
 */
data class PhysicalLogisticsSnapshot(
    val places: List<PlaceLogisticsItem> = emptyList(),
    val campusLocations: List<PlaceLogisticsItem> = emptyList(),
    val homeZones: List<PlaceLogisticsItem> = emptyList(),
    val placeBasedTasks: List<NodeWithPin> = emptyList(),
    val outOfHomeTaskClusters: Map<String, List<NodeWithPin>> = emptyMap(),
    val errandClusters: Map<String, List<NodeWithPin>> = emptyMap(),
    val whatToBringLists: List<NodeWithPin> = emptyList(),
    val packingLists: List<NodeWithPin> = emptyList(),
    val leaveHomeChecklists: List<NodeWithPin> = emptyList(),
    val dontForgetSets: List<NodeWithPin> = emptyList(),
    val eventPreparationLists: List<NodeWithPin> = emptyList(),
    val classSpecificBringLists: List<NodeWithPin> = emptyList(),
    val physicalLogisticsNotes: List<NodeWithPin> = emptyList(),
    val travelPackTemplateReady: Boolean = false,
    val locationSpecificReminders: List<NodeWithPin> = emptyList(),
)

/**
 * A snapshot containing the user's deeply personal operating principles, anti-goals, and behavioral rules.
 *
 * @param vault A general list of rule or reference nodes.
 * @param antiGoals A list of defined outcomes or behaviors the user explicitly wants to avoid.
 * @param redFlags A list of warning signs indicating the user is slipping into bad habits.
 * @param greenFlags A list of positive signs indicating the user is in a healthy, productive state.
 * @param priorities A heavily filtered list of the highest-level priorities across all areas.
 * @param tendToForget A list of recurring mistakes or lessons the user needs to be reminded of.
 * @param messesMeUp A list of specific triggers or environments that disrupt the user's focus or mood.
 * @param helpsOffBalance A list of immediate actions or interventions to take when feeling overwhelmed.
 * @param decisionPrinciples A list of heuristics used to simplify hard decisions.
 * @param constraints Imposed limitations designed to force creativity or prevent burnout (e.g., "No work after 8PM").
 * @param foundationalRules The absolute core rules driving the user's LifeOS.
 * @param recoveryReminders Notes specifically tailored to guide the user through a burnout recovery phase.
 * @param distrustBrainNotes Reminders for when the user is in an irrational state (e.g., anxiety spirals).
 * @param whatWorksNotes A historical log of strategies or tools that have proven highly effective.
 * @param pinnedPrinciples Principles pinned for constant visibility.
 * @param playbookLinksCount The number of links connecting these rules to actionable routine playbooks.
 */
data class PersonalRulesSnapshot(
    val vault: List<NodeWithPin> = emptyList(),
    val antiGoals: List<NodeWithPin> = emptyList(),
    val redFlags: List<NodeWithPin> = emptyList(),
    val greenFlags: List<NodeWithPin> = emptyList(),
    val priorities: List<NodeWithPin> = emptyList(),
    val tendToForget: List<NodeWithPin> = emptyList(),
    val messesMeUp: List<NodeWithPin> = emptyList(),
    val helpsOffBalance: List<NodeWithPin> = emptyList(),
    val decisionPrinciples: List<NodeWithPin> = emptyList(),
    val constraints: List<NodeWithPin> = emptyList(),
    val foundationalRules: List<NodeWithPin> = emptyList(),
    val recoveryReminders: List<NodeWithPin> = emptyList(),
    val distrustBrainNotes: List<NodeWithPin> = emptyList(),
    val whatWorksNotes: List<NodeWithPin> = emptyList(),
    val pinnedPrinciples: List<NodeWithPin> = emptyList(),
    val playbookLinksCount: Int = 0,
)

/**
 * A snapshot of durable reference material and retrieval-oriented storage across the system.
 *
 * @param referenceLibrary Durable reference notes, records, and document-like items worth keeping retrievable.
 * @param importantLinks Curated links and portal shortcuts that matter operationally.
 * @param healthReference Health-related records, provider info, prescriptions, and symptom context.
 * @param institutionalReference Admin, study, identity, and account reference material.
 * @param processTracking Tracked external processes such as applications, renewals, and approvals.
 * @param officialDeadlines Hard external deadlines tied to authorities, institutions, or financial obligations.
 * @param retrievalQueue Quick-captured items intentionally pinned for later retrieval or sorting.
 */
data class VaultsSnapshot(
    val referenceLibrary: List<NodeWithPin> = emptyList(),
    val importantLinks: List<NodeWithPin> = emptyList(),
    val healthReference: List<NodeWithPin> = emptyList(),
    val institutionalReference: List<NodeWithPin> = emptyList(),
    val processTracking: List<NodeWithPin> = emptyList(),
    val officialDeadlines: List<NodeWithPin> = emptyList(),
    val retrievalQueue: List<NodeWithPin> = emptyList(),
)

/**
 * A data class representing a specific point in a temporal trend of system load and fragmentation.
 *
 * @param label A string descriptor for the time period (e.g., "Week 42", "Oct 15").
 * @param load The calculated system load metric during this period.
 * @param fragmentation The calculated context fragmentation metric during this period.
 */
data class LoadTrendPoint(
    val label: String,
    val load: Int,
    val fragmentation: Int,
)

/**
 * A snapshot analyzing the user's total workload capacity against their current system commitments.
 *
 * @param loadScore A cumulative score representing the sheer volume of active tasks, projects, and deadlines.
 * @param fragmentationScore A metric indicating how scattered the user's focus is across different contexts.
 * @param tooManyActiveProjectsWarning An alert triggered if the user has breached their concurrent project limit.
 * @param adminDebtWarning An alert indicating that routine chores are piling up to an unmanageable degree.
 * @param openLoopsOverloadWarning An alert indicating that too many unresolved inputs are causing cognitive friction.
 * @param capacityMismatch An alert triggered if the estimated time to complete tasks vastly exceeds available focus hours.
 * @param unrealisticWeekSignal An alert suggesting that the tasks planned for the current week are probabilistically impossible to finish.
 * @param tooManyActiveFrontsIndicator An alert indicating focus is split across too many distinct Areas of Responsibility.
 * @param attentionFragmentedIndicator An alert indicating severe context-switching overhead.
 * @param weeklyStructuralOverloadWarning An alert combining multiple overload metrics into a severe system warning.
 * @param loadByArea A mapping of calculated load scores attributed to each Area ID.
 * @param loadByMode A mapping of calculated load scores attributed to each focus Mode.
 * @param loadTrend A historical list of [LoadTrendPoint] objects to visualize workload over time.
 * @param capacityAwareSuggestions Dynamically generated suggestions to defer tasks, archive projects, or switch modes based on load.
 */
data class CapacitySnapshot(
    val loadScore: Int = 0,
    val fragmentationScore: Int = 0,
    val tooManyActiveProjectsWarning: String? = null,
    val adminDebtWarning: String? = null,
    val openLoopsOverloadWarning: String? = null,
    val capacityMismatch: String? = null,
    val unrealisticWeekSignal: String? = null,
    val tooManyActiveFrontsIndicator: String? = null,
    val attentionFragmentedIndicator: String? = null,
    val weeklyStructuralOverloadWarning: String? = null,
    val loadByArea: Map<Long?, Int> = emptyMap(),
    val loadByMode: Map<String, Int> = emptyMap(),
    val loadTrend: List<LoadTrendPoint> = emptyList(),
    val capacityAwareSuggestions: List<String> = emptyList(),
)

/**
 * A snapshot assessing which core LifeOS subsystems and methodologies the user has actively enabled and configured.
 *
 * @param operatingModesEnabled True if the user is actively using context-based Modes (e.g., "Work Mode").
 * @param areaHealthEnabled True if the user is tracking tasks via distinct Areas of Responsibility.
 * @param openLoopsEnabled True if the user is systematically capturing and processing inbox inputs.
 * @param decisionSystemEnabled True if the user utilizes the explicit decision and options framework.
 * @param maintenanceEnabled True if the user has configured recurring chores or admin tasks.
 * @param contextAwareFilteringEnabled True if the user relies heavily on location or energy context tags.
 * @param transitionProtocolsEnabled True if the user has configured checklists for shifting between modes or states.
 * @param recoveryModeEnabled True if the user has configured a low-demand recovery state for burnout.
 * @param relationshipLayerEnabled True if the user tracks interactions or dependencies with specific People entities.
 * @param logisticsVaultEnabled True if the user utilizes the physical places and packing lists feature.
 * @param loadCapacityEnabled True if the user estimates task duration to monitor their workload capacity.
 * @param personalPrinciplesPlaybooksEnabled True if the user maintains explicit behavioral rules and playbooks.
 * @param modeOfLifeLabel A calculated descriptor of the user's current holistic system state, emitting labels like "stabilization", "firefighting", or "execution".
 * @param modeOfLifeReason The specific metric or trigger causing the [modeOfLifeLabel] classification.
 * @param workDateDueCoveragePercent The percentage of work/start-date coverage among tasks that already have due-dates.
 * @param workDateDueItems The list of items missing a work/start-date (the items that reduce coverage).
 */
data class LifeOSSignatureSnapshot(
    val operatingModesEnabled: Boolean = false,
    val areaHealthEnabled: Boolean = false,
    val openLoopsEnabled: Boolean = false,
    val decisionSystemEnabled: Boolean = false,
    val maintenanceEnabled: Boolean = false,
    val contextAwareFilteringEnabled: Boolean = false,
    val transitionProtocolsEnabled: Boolean = false,
    val recoveryModeEnabled: Boolean = false,
    val relationshipLayerEnabled: Boolean = false,
    val logisticsVaultEnabled: Boolean = false,
    val loadCapacityEnabled: Boolean = false,
    val personalPrinciplesPlaybooksEnabled: Boolean = false,
    val modeOfLifeLabel: String = "unknown",
    val modeOfLifeReason: String = "",
    val workDateDueCoveragePercent: Int = 0,
    val workDateDueItems: List<NodeWithPin> = emptyList(),
)

/**
 * A data class representing a self-reflection question to gauge system alignment.
 *
 * @param question The literal string of the reflective question.
 * @param answer The recorded string answer.
 * @param answered True if the question has been answered by the user.
 */
data class DistinctionQuestionState(
    val question: String,
    val answer: String,
    val answered: Boolean = true,
)

/**
 * A snapshot assessing the user's reliance on "Second Brain" knowledge management versus "LifeOS" action management.
 *
 * @param secondBrainQuestions A list of [DistinctionQuestionState] items assessing knowledge capture habits.
 * @param lifeOSQuestions A list of [DistinctionQuestionState] items assessing action and execution habits.
 * @param secondBrainCoveragePercent A calculated score indicating the depth of knowledge management integration.
 * @param lifeOSCoveragePercent A calculated score indicating the depth of action management integration.
 * @param postureLabel A descriptor categorizing the system's balance (e.g., "Heavy Execution", "Heavy Capture", "Balanced").
 */
data class LifeOSSecondBrainSnapshot(
    val secondBrainQuestions: List<DistinctionQuestionState> = emptyList(),
    val lifeOSQuestions: List<DistinctionQuestionState> = emptyList(),
    val secondBrainCoveragePercent: Int = 0,
    val lifeOSCoveragePercent: Int = 0,
    val postureLabel: String = "underconfigured",
)

/**
 * A data class representing a specific high-level commitment and its current validation status based on system data.
 *
 * @param commitment The defined string commitment (e.g., "Maintain Inbox Zero weekly").
 * @param satisfied True if the system metrics confirm the commitment is being upheld.
 * @param evidence The specific metric or data point validating the satisfaction state.
 */
data class DirectionCommitmentStatus(
    val commitment: String,
    val satisfied: Boolean,
    val evidence: String,
)

/**
 * A snapshot evaluating whether the user's daily actions align with their stated high-level commitments.
 *
 * @param commitments A list of [DirectionCommitmentStatus] items being tracked.
 * @param completionPercent The percentage of tracked commitments currently satisfied.
 * @param practicalitySignals A list of warning strings if commitments appear structurally impossible due to load.
 * @param postureLabel A descriptor of the alignment state (e.g., "Aligned", "Drifting", "Failing").
 */
data class CombinedDirectionSnapshot(
    val commitments: List<DirectionCommitmentStatus> = emptyList(),
    val completionPercent: Int = 0,
    val practicalitySignals: List<String> = emptyList(),
    val postureLabel: String = "underconfigured",
)

/**
 * A data class evaluating a specific shift or fundamental change in the user's LifeOS structure.
 *
 * @param criterion The specific criteria required to validate the shift (e.g., "Use Transition Protocols Daily").
 * @param satisfied True if the system metrics confirm the criterion is met.
 * @param evidence The specific metric or data point validating the satisfaction state.
 */
data class CoreLifeOSShiftItem(
    val criterion: String,
    val satisfied: Boolean,
    val evidence: String,
)

/**
 * A snapshot tracking the user's progress in adopting deeper or more advanced LifeOS paradigms.
 *
 * @param items A list of [CoreLifeOSShiftItem] elements tracking paradigm adoption.
 * @param completionPercent The overall progress percentage towards the core shift.
 * @param connectedProperly Derived from completionPercent; true when completionPercent meets or exceeds the configured threshold in calculateCoreLifeOSShiftSnapshot().
 * @param integrationWarning The fallback indicator shown when the threshold is not met.
 */
data class CoreLifeOSShiftSnapshot(
    val items: List<CoreLifeOSShiftItem> = emptyList(),
    val completionPercent: Int = 0,
    val connectedProperly: Boolean = false,
    val integrationWarning: String? = null,
)
