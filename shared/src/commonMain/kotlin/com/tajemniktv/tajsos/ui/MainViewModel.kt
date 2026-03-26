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
import kotlin.math.sqrt
import kotlin.time.Clock
import kotlin.time.Instant

data class DecisionStaleItem(
    val node: NodeWithPin,
    val ageDays: Int,
)

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
    // LifeOS Additions
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
    // Expanded for Modes
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

data class TransitionProtocolTemplate(
    val key: String,
    val label: String,
    val checklist: List<String>,
)

data class TransitionProtocolItem(
    val node: NodeWithPin,
    val checklistDone: Int,
    val checklistTotal: Int,
    val triggerCount: Int,
    val lastTriggeredAt: Long? = null,
)

data class ProtocolHistoryItem(
    val historyId: Long,
    val protocolNodeId: Long,
    val protocolLabel: String,
    val executedAt: Long,
    val notes: String? = null,
)

data class TransitionProtocolsSnapshot(
    val protocols: List<TransitionProtocolItem> = emptyList(),
    val templates: List<TransitionProtocolTemplate> = emptyList(),
    val recommendedLabel: String? = null,
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

data class RelationshipStatusItem(
    val person: NodeWithPin,
    val relationshipType: String? = null,
    val daysSinceLastContact: Int? = null,
    val followUpDueInDays: Int? = null,
    val isImportant: Boolean = false,
    val linkedItemsCount: Int = 0,
    val pendingReplyCount: Int = 0,
    val sharedPlansCount: Int = 0,
    val askAboutNextTimeCount: Int = 0,
)

data class RelationshipSnapshot(
    val people: List<RelationshipStatusItem> = emptyList(),
    val importantRelationships: List<RelationshipStatusItem> = emptyList(),
    val followUpNeeded: List<RelationshipStatusItem> = emptyList(),
    val upcomingImportantDates: List<RelationshipStatusItem> = emptyList(),
    val replyQueue: List<NodeWithPin> = emptyList(),
    val sharedPlans: List<NodeWithPin> = emptyList(),
    val professors: List<RelationshipStatusItem> = emptyList(),
    val friendsAndFamily: List<RelationshipStatusItem> = emptyList(),
    val gentlePrompt: String? = null,
)

data class PlaybookTemplate(
    val key: String,
    val label: String,
    val checklist: List<String>,
    val recommendedModeKey: String? = null,
)

data class PlaybookItem(
    val node: NodeWithPin,
    val checklistDone: Int,
    val checklistTotal: Int,
    val triggerCount: Int,
    val linkedModeKey: String? = null,
    val linkedAreaId: Long? = null,
    val isCustom: Boolean = false,
)

data class PlaybookSnapshot(
    val playbooks: List<PlaybookItem> = emptyList(),
    val templates: List<PlaybookTemplate> = emptyList(),
    val suggestedPlaybookLabel: String? = null,
)

data class PlaceLogisticsItem(
    val place: NodeWithPin,
    val relatedTasks: List<NodeWithPin>,
    val remindersCount: Int,
)

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

data class VaultsSnapshot(
    val documentVault: List<NodeWithPin> = emptyList(),
    val importantLinksVault: List<NodeWithPin> = emptyList(),
    val medicalInfoVault: List<NodeWithPin> = emptyList(),
    val universityInfoVault: List<NodeWithPin> = emptyList(),
    val idsAndFormsVault: List<NodeWithPin> = emptyList(),
    val applicationStatusTracking: List<NodeWithPin> = emptyList(),
    val receiptsPaperwork: List<NodeWithPin> = emptyList(),
    val accountReferenceVault: List<NodeWithPin> = emptyList(),
    val officialDeadlineReminders: List<NodeWithPin> = emptyList(),
    val mustFindLater: List<NodeWithPin> = emptyList(),
)

data class LoadTrendPoint(
    val label: String,
    val load: Int,
    val fragmentation: Int,
)

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

data class DistinctionQuestionState(
    val question: String,
    val answer: String,
    val answered: Boolean = true,
)

data class LifeOSSecondBrainSnapshot(
    val secondBrainQuestions: List<DistinctionQuestionState> = emptyList(),
    val lifeOSQuestions: List<DistinctionQuestionState> = emptyList(),
    val secondBrainCoveragePercent: Int = 0,
    val lifeOSCoveragePercent: Int = 0,
    val postureLabel: String = "underconfigured",
)

data class DirectionCommitmentStatus(
    val commitment: String,
    val satisfied: Boolean,
    val evidence: String,
)

data class CombinedDirectionSnapshot(
    val commitments: List<DirectionCommitmentStatus> = emptyList(),
    val completionPercent: Int = 0,
    val practicalitySignals: List<String> = emptyList(),
    val postureLabel: String = "underconfigured",
)

data class CoreLifeOSShiftItem(
    val criterion: String,
    val satisfied: Boolean,
    val evidence: String,
)

data class CoreLifeOSShiftSnapshot(
    val items: List<CoreLifeOSShiftItem> = emptyList(),
    val completionPercent: Int = 0,
    val connectedProperly: Boolean = false,
    val integrationWarning: String? = null,
)

private val suggestedAreaTitles =
    listOf(
        "Health",
        "Studies",
        "Admin",
        "Relationships",
        "Work / Projects",
        "Money",
        "Personal maintenance",
        "Learning",
        "Therapy / reflection",
        "Home / environment",
    )

private val defaultTransitionProtocolTemplates =
    listOf(
        TransitionProtocolTemplate(
            key = "morning_startup",
            label = "Morning startup",
            checklist =
                listOf(
                    "Drink water and take meds if needed",
                    "Review today's top 3 priorities",
                    "Clear inbox noise for 5 minutes",
                    "Start first focused task",
                ),
        ),
        TransitionProtocolTemplate(
            key = "before_class",
            label = "Before class",
            checklist =
                listOf(
                    "Open class materials and notes",
                    "Prepare one question to clarify",
                    "Set phone to focus mode",
                    "Confirm arrival buffer",
                ),
        ),
        TransitionProtocolTemplate(
            key = "before_leaving_home",
            label = "Before leaving home",
            checklist =
                listOf(
                    "Check keys, wallet, phone",
                    "Bring required documents/devices",
                    "Confirm destination and next stop",
                    "Capture any open loop before leaving",
                ),
        ),
        TransitionProtocolTemplate(
            key = "deep_work_entry",
            label = "Deep work entry",
            checklist =
                listOf(
                    "Choose one clear deliverable",
                    "Close distracting tabs and notifications",
                    "Set focus timer",
                    "Write the first concrete step",
                ),
        ),
        TransitionProtocolTemplate(
            key = "shutdown_ritual",
            label = "Shutdown ritual",
            checklist =
                listOf(
                    "Capture unfinished thoughts and loops",
                    "Reschedule or pin tomorrow's top tasks",
                    "Close active sessions",
                    "Set mode for next morning",
                ),
        ),
        TransitionProtocolTemplate(
            key = "recovery_after_derailment",
            label = "Recovery after derailment",
            checklist =
                listOf(
                    "Pause and run a 2-minute reset",
                    "Pick one tiny stabilizing task",
                    "Mark one open loop as next action",
                    "Restart with low-friction work",
                ),
        ),
        TransitionProtocolTemplate(
            key = "exam_week",
            label = "Exam week",
            checklist =
                listOf(
                    "Review exam schedule and deadlines",
                    "Select top revision targets",
                    "Timebox admin and social noise",
                    "Plan recovery blocks",
                ),
        ),
        TransitionProtocolTemplate(
            key = "travel_day",
            label = "Travel day",
            checklist =
                listOf(
                    "Confirm tickets, IDs, and timing",
                    "Prepare packing and chargers",
                    "Queue commute-friendly tasks",
                    "Set lightweight mode for transit",
                ),
        ),
        TransitionProtocolTemplate(
            key = "before_sleep",
            label = "Before sleep",
            checklist =
                listOf(
                    "Dump remaining mental load",
                    "Pin one first task for tomorrow",
                    "Set alarms and essentials",
                    "Close with low-stimulation routine",
                ),
        ),
        TransitionProtocolTemplate(
            key = "after_interruption",
            label = "After interruption",
            checklist =
                listOf(
                    "Re-open last task context",
                    "Restate next smallest step",
                    "Set 10-minute re-entry timer",
                    "Resume before checking messages",
                ),
        ),
        TransitionProtocolTemplate(
            key = "arriving_on_campus",
            label = "Arriving on campus",
            checklist =
                listOf(
                    "Check class location and timing",
                    "Open assignment/revision queue",
                    "Capture urgent follow-ups",
                    "Switch to study context",
                ),
        ),
        TransitionProtocolTemplate(
            key = "scrolling_to_working",
            label = "Switching from scrolling to working",
            checklist =
                listOf(
                    "Close social apps",
                    "Define one work target",
                    "Start 10-minute focus sprint",
                    "Block distractions",
                ),
        ),
        TransitionProtocolTemplate(
            key = "work_to_rest",
            label = "Switching from work to rest",
            checklist =
                listOf(
                    "Capture loose ends",
                    "Mark today's progress",
                    "Set first task for next session",
                    "Transition to rest intentionally",
                ),
        ),
    )

private val defaultPlaybookTemplates =
    listOf(
        PlaybookTemplate(
            key = "bad_day",
            label = "Bad day protocol",
            checklist =
                listOf(
                    "Drop scope to essentials",
                    "Hydrate + meds check",
                    "Choose one tiny win",
                    "Close one open loop",
                ),
            recommendedModeKey = "RECOVERY",
        ),
        PlaybookTemplate(
            key = "panic_ish_day",
            label = "Panic-ish day protocol",
            checklist =
                listOf(
                    "Box breathing 2 minutes",
                    "List immediate threats",
                    "Pick one stabilizing action",
                    "Silence non-critical channels",
                ),
            recommendedModeKey = "RECOVERY",
        ),
        PlaybookTemplate(
            key = "cant_start_studying",
            label = "Can't start studying protocol",
            checklist =
                listOf(
                    "Open material only",
                    "Set 10-minute timer",
                    "Write first question",
                    "Start with easiest section",
                ),
            recommendedModeKey = "STUDY",
        ),
        PlaybookTemplate(
            key = "need_to_leave_house",
            label = "Need to leave house protocol",
            checklist =
                listOf(
                    "Essentials check",
                    "Destination and route",
                    "Bring list verification",
                    "Queue out-of-home tasks",
                ),
            recommendedModeKey = "ERRAND",
        ),
        PlaybookTemplate(
            key = "weekly_reset",
            label = "Weekly reset protocol",
            checklist =
                listOf(
                    "Review completed work",
                    "Rebuild this-week map",
                    "Clear inbox noise",
                    "Set top priorities",
                ),
            recommendedModeKey = "COMMAND",
        ),
        PlaybookTemplate(
            key = "exam_prep",
            label = "Exam prep protocol",
            checklist =
                listOf(
                    "Identify exam targets",
                    "Plan revision sessions",
                    "Prepare question list",
                    "Protect recovery windows",
                ),
            recommendedModeKey = "EXAM_WEEK",
        ),
        PlaybookTemplate(
            key = "project_kickoff",
            label = "Project kickoff protocol",
            checklist =
                listOf(
                    "Define outcome",
                    "Break first milestone",
                    "Create first actions",
                    "Schedule review checkpoint",
                ),
            recommendedModeKey = "DEEP_WORK",
        ),
        PlaybookTemplate(
            key = "life_collapsing",
            label = "Life is collapsing protocol",
            checklist =
                listOf(
                    "Stabilize basics first",
                    "Pause new intake",
                    "Escalate only critical items",
                    "Ask for support where possible",
                ),
            recommendedModeKey = "RECOVERY",
        ),
        PlaybookTemplate(
            key = "low_energy_must_function",
            label = "Low energy but must function protocol",
            checklist =
                listOf(
                    "Switch to low-friction tasks",
                    "Use short sprints",
                    "Batch context",
                    "Minimize switching",
                ),
            recommendedModeKey = "LOW_BATTERY",
        ),
        PlaybookTemplate(
            key = "need_to_reply_everyone",
            label = "Need to reply to everyone protocol",
            checklist =
                listOf(
                    "Open reply queue",
                    "Sort by urgency",
                    "Send short acknowledgements first",
                    "Convert leftovers to follow-ups",
                ),
            recommendedModeKey = "SOCIAL",
        ),
        PlaybookTemplate(
            key = "back_on_track_after_derailment",
            label = "Get back on track after derailment protocol",
            checklist =
                listOf(
                    "Capture derailment fallout",
                    "Pick restart point",
                    "Run 15-minute reset",
                    "Resume with one concrete action",
                ),
            recommendedModeKey = "RECOVERY",
        ),
    )

data class StudentCourseSummary(
    val courseId: String,
    val courseName: String,
    val semester: String?,
    val openAssignments: Int,
    val upcomingExams: Int,
    val avgMasteryPercent: Int?,
)

data class StudentSemesterSummary(
    val semester: String,
    val courseCount: Int,
    val openAssignments: Int,
    val upcomingExams: Int,
    val dueSoon: Int,
)

data class StudentProgressItem(
    val node: NodeWithPin,
    val progressPercent: Int,
)

data class StudentMasteryItem(
    val node: NodeWithPin,
    val topic: String,
    val masteryPercent: Int,
)

data class StudentBoardState(
    val lectureTemplateReady: Boolean = false,
    val readingTemplateReady: Boolean = false,
    val paperSummaryTemplateReady: Boolean = false,
    val assignmentTracker: List<NodeWithPin> = emptyList(),
    val examPrepBoard: List<NodeWithPin> = emptyList(),
    val psychologyConceptMaps: List<NodeWithPin> = emptyList(),
    val glossaryCards: List<NodeWithPin> = emptyList(),
    val researchIdeaVault: List<NodeWithPin> = emptyList(),
    val quoteBank: List<NodeWithPin> = emptyList(),
    val caseReflectionNotes: List<NodeWithPin> = emptyList(),
    val readingBacklog: List<NodeWithPin> = emptyList(),
    val revisitBeforeExam: List<NodeWithPin> = emptyList(),
    val readingProgress: List<StudentProgressItem> = emptyList(),
    val assignmentDeadlines: List<NodeWithPin> = emptyList(),
    val topicMastery: List<StudentMasteryItem> = emptyList(),
    val courseDashboard: List<StudentCourseSummary> = emptyList(),
    val semesterDashboard: List<StudentSemesterSummary> = emptyList(),
    val examCountdownNode: NodeWithPin? = null,
    val examCountdownDays: Long? = null,
    val topicToNoteLinks: Int = 0,
    val paperToNoteLinks: Int = 0,
    val conceptGraphNodes: Int = 0,
    val conceptGraphEdges: Int = 0,
    val flashcardCandidates: List<NodeWithPin> = emptyList(),
    val studySessionsThisWeek: Int = 0,
    val studyMinutesThisWeek: Int = 0,
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

    private val allModesRaw: StateFlow<List<ModeEntity>> =
        repository
            .getAllModes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allModes: StateFlow<List<ModeEntity>> =
        combine(allModesRaw, preferencesRepository.enabledPacks) { modes, packs ->
            modes.filter { packs.canUseMode(it.key) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAreas: StateFlow<List<NodeEntity>> =
        repository
            .getNodesByType("area")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeNodes: StateFlow<List<NodeWithPin>> =
        allNodes
            .map { list ->
                list.filter { it.node.status != "archived" }
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
            val accessibleModes = modesList.filter { packs.canUseMode(it.key) }
            val mode = accessibleModes.find { it.id == activeId }
            val now = Clock.System.now().toEpochMilliseconds()
            val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
            val fourteenDaysAgo = now - (14 * 24 * 60 * 60 * 1000L)

            // 0. Mode Preferences & Initial Filtering
            val prefs =
                if (mode != null) repository.getPreferencesForMode(mode.id).first() else null

            // Apply Area Filters if any
            val areaFilters =
                if (mode != null && mode.key != "ALL") {
                    repository
                        .getAreaFiltersForMode(mode.id)
                        .first()
                } else {
                    emptyList()
                }
            val includedAreaIds = areaFilters.filter { it.include }.map { it.areaId }
            val excludedAreaIds = areaFilters.filter { !it.include }.map { it.areaId }

            var filteredNodes = nodes
            if (mode?.key != "ALL") {
                if (includedAreaIds.isNotEmpty()) {
                    filteredNodes =
                        filteredNodes.filter { it.node.areaId in includedAreaIds || it.node.type == "area" }
                }
                if (excludedAreaIds.isNotEmpty()) {
                    filteredNodes = filteredNodes.filter { it.node.areaId !in excludedAreaIds }
                }
            }

            // Apply Type Filters if any
            val typeFilters =
                if (mode != null && mode.key != "ALL") {
                    repository
                        .getTypeFiltersForMode(mode.id)
                        .first()
                } else {
                    emptyList()
                }
            val includedTypes = typeFilters.filter { it.include }.map { it.nodeType }
            val excludedTypes = typeFilters.filter { !it.include }.map { it.nodeType }

            if (mode?.key != "ALL") {
                if (includedTypes.isNotEmpty()) {
                    filteredNodes = filteredNodes.filter { it.node.type in includedTypes }
                }
                if (excludedTypes.isNotEmpty()) {
                    filteredNodes = filteredNodes.filter { it.node.type !in excludedTypes }
                }
            }

            // Recovery/Low Battery Special Logic: Filter by Energy/Friction
            if (mode?.key == "RECOVERY" || mode?.key == "LOW_BATTERY" || mode?.key == "CANT_THINK") {
                filteredNodes =
                    filteredNodes.filter {
                        it.node.type != "task" || (it.node.energyLevel == 1 && it.node.friction == "easy")
                    }
            }

            // 1. Basic counts and categories
            val activeTasks =
                filteredNodes.filter { it.node.type == "task" && it.node.status == "active" }
            val overdue =
                filteredNodes.filter { it.node.dueAt != null && it.node.dueAt < now && it.node.status == "active" }
            val pinnedK =
                filteredNodes.filter {
                    it.node.isPinned && (it.node.type == "note" || it.node.type == "idea" || it.node.type == "resource")
                }

            // 2. Open Loops, Decisions, Maintenance
            val openLoops =
                filteredNodes.filter { it.node.type == "open_loop" && it.node.status == "active" }
            val decisions =
                filteredNodes.filter { it.node.type == "decision" && it.node.status == "active" }
            val maintenance =
                filteredNodes.filter { it.node.type == "maintenance" && it.node.status == "active" }
            val maintenanceSnapshot = calculateMaintenanceSnapshot(nodes)
            val protocols =
                filteredNodes.filter { it.node.type == "protocol" && it.node.status == "active" }
            val people =
                filteredNodes.filter { it.node.type == "person" && it.node.status == "active" }
            val openLoopDecayScores = openLoops.map { openLoopDecayScore(it.node, now) }
            val openLoopsDecayAverage =
                if (openLoopDecayScores.isNotEmpty()) openLoopDecayScores.average().toInt() else 0
            val openLoopsOverloadWarning =
                when
                    {
                        openLoops.size >= 12 -> "OPEN LOOPS OVERLOAD // CLOSE LOOPS BEFORE NEW INTAKE"
                        openLoopsDecayAverage >= 60 -> "OPEN LOOPS DECAYING // RUN OPEN LOOP REVIEW"
                        else -> null
                    }

            // 3. Area Health Logic
            val areaSnapshot = calculateAreaHealthSnapshot(nodes, areasList)
            val areaHealthMap = areaSnapshot.areas.associate { it.areaId to it.status }
            val areaHealthMetrics = areaSnapshot.areas.associateBy { it.areaId }

            // 4. Load & Capacity
            val loadScore = (activeTasks.size * 2) + (openLoops.size * 3) + (overdue.size * 5)
            val fragmentation = activeTasks.groupBy { it.node.projectId }.size * 5
            val capWarning =
                when
                    {
                        loadScore > 100 -> "SYSTEM OVERLOADED // REDUCE INTAKE"
                        fragmentation > 40 -> "ATTENTION FRAGMENTED // FOCUS ON ONE AREA"
                        else -> null
                    }

            // 5. Context Clustered Tasks
            val contexts =
                filteredNodes
                    .filter { it.node.status == "active" && it.node.type == "task" }
                    .groupBy { it.node.locationContext ?: "general" }

            // 6. Mode Suggestions
            val localNow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val suggestion =
                when
                    {
                        localNow.hour >= 22 && mode?.key != "SHUTDOWN" && packs.canUseMode("SHUTDOWN") -> "SHUTDOWN"
                        loadScore > 80 && mode?.key != "RECOVERY" && mode?.key != "LOW_BATTERY" -> "RECOVERY"
                        else -> null
                    }
            val contextPriorityKeys =
                when (localNow.hour)
                {
                    in 22..23, in 0..5 -> listOf("at_home", "low_energy", "10_minute")
                    in 6..9, in 16..18 -> listOf("commute_friendly", "waiting_room", "phone_okay")
                    else -> listOf("on_campus", "laptop_required", "high_focus")
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
                    activeTasks.any { matchesContextKey(it.node, key) }
                }
            val suggestedContextTasks =
                if (suggestedContextKey != null) {
                    activeTasks.filter { matchesContextKey(it.node, suggestedContextKey) }.take(5)
                } else {
                    emptyList()
                }

            DashboardUIState(
                tasksCount = activeTasks.size,
                notesCount = filteredNodes.count { it.node.type == "note" || it.node.type == "idea" || it.node.type == "resource" },
                pinnedKnowledge = pinnedK,
                upcomingDeadlines =
                    filteredNodes
                        .filter { it.node.dueAt != null && it.node.status == "active" }
                        .sortedBy { it.node.dueAt }
                        .take(3),
                overdueNodes = overdue,
                relevantNote =
                    filteredNodes
                        .filter { (it.node.type == "note" || it.node.type == "idea") && it.node.status == "active" }
                        .sortedByDescending { it.node.updatedAt }
                        .firstOrNull(),
                lowEnergyTasks =
                    filteredNodes.filter {
                        it.node.type == "task" && it.node.status == "active" && it.node.energyLevel == 1
                    },
                batchableTasks =
                    activeTasks
                        .groupBy { it.node.areaId }
                        .filter { it.value.size >= 3 },
                quickWins =
                    filteredNodes.filter {
                        it.node.type == "task" && it.node.status == "active" && it.node.energyLevel == 1 &&
                            it.node.friction == "easy"
                    },
                deepWork =
                    filteredNodes.filter {
                        it.node.type == "task" && it.node.status == "active" && it.node.energyLevel == 3
                    },
                topTakeaways =
                    filteredNodes.filter {
                        (it.node.type == "note" || it.node.type == "idea") && it.node.noteState == "takeaway"
                    },
                readLaterVault = filteredNodes.filter { it.node.noteType == "read_later" && it.node.status == "active" },
                quoteVault = filteredNodes.filter { it.node.noteType == "quote" && it.node.status == "active" },
                ideaIncubator = filteredNodes.filter { it.node.type == "idea" && it.node.status == "active" && it.node.projectId == null },
                archivedThisWeek =
                    nodes.filter {
                        it.node.status == "archived" && (it.node.archivedAt ?: 0) >= sevenDaysAgo
                    },
                neglectedThisWeek =
                    filteredNodes.filter {
                        it.node.status == "active" && it.node.type == "task" &&
                            it.node.updatedAt < sevenDaysAgo
                    },
                foundationalNotes =
                    filteredNodes
                        .filter {
                            (it.node.type == "note" || it.node.type == "idea") &&
                                it.tags.any { tag ->
                                    tag.name.equals(
                                        "foundational",
                                        ignoreCase = true,
                                    )
                                }
                        }.take(1),
                resourceHighlights =
                    filteredNodes
                        .filter { it.node.type == "resource" && it.node.status == "active" }
                        .shuffled()
                        .take(2),
                stickyNotes = filteredNodes.filter { it.node.isSticky && it.node.status == "active" },
                criticalProjects =
                    filteredNodes
                        .filter { it.node.type == "project" && it.node.status == "active" }
                        .map { it.node }
                        .filter { proj ->
                            val projectNodes = nodes.filter { it.node.projectId == proj.id }
                            val hasCritical =
                                projectNodes.any {
                                    it.node.status == "active" && it.node.isHardDeadline && it.node.dueAt != null && it.node.dueAt < now
                                }
                            val isNeglected =
                                proj.status == "active" && !proj.isFrozen && projectNodes.none { it.node.updatedAt >= fourteenDaysAgo }
                            hasCritical || isNeglected
                        },
                forgottenWisdom =
                    filteredNodes
                        .filter {
                            (it.node.type == "note" || it.node.type == "idea") &&
                                it.node.status == "active" &&
                                (it.node.noteType == "evergreen" || it.node.updatedAt < (now - 30 * 24 * 60 * 60 * 1000L))
                        }.shuffled()
                        .firstOrNull(),
                deservesAttention =
                    filteredNodes
                        .filter {
                            it.node.status == "active" && it.node.type == "task" &&
                                !it.node.isPinned && it.node.dueAt == null &&
                                it.node.updatedAt < sevenDaysAgo
                        }.take(2),
                // LifeOS Specific
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
                relationshipsToContact =
                    people.filter {
                        (it.node.lastContactAt ?: 0) < fourteenDaysAgo
                    },
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
                        .filter { it.node.status == "done" && it.node.completedAt != null && it.node.completedAt >= sevenDaysAgo }
                        .take(5),
                shoppingList = nodes.filter { it.node.status == "active" && it.tags.any { t -> t.name.lowercase() == "shopping" } },
                unresolvedBureaucracy =
                    nodes.filter {
                        it.node.type == "maintenance" && it.node.status == "active" &&
                            it.node.createdAt < sevenDaysAgo
                    },
                modeSuggestion = suggestion,
                suggestedContextKey = suggestedContextKey,
                suggestedContextTasks = suggestedContextTasks,
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

    val medications: StateFlow<List<MedicationEntity>> =
        repository
            .getAllMedications()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProjects: StateFlow<List<NodeEntity>> =
        repository
            .getNodesByType("project")
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
            }.flowOn(Dispatchers.Default)
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

    val currentModeId: StateFlow<Long?> =
        preferencesRepository
            .activeModeId
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentMode: StateFlow<ModeEntity?> =
        combine(allModes, currentModeId) { modes, id ->
            modes.find { it.id == id } ?: modes.firstOrNull { it.key == "COMMAND" }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            preferencesRepository.ensureDefaultPackAccess()
            seedDefaultModes()
            seedStudentTemplates()
            seedLifeLogisticsTemplates()
            seedUserData()
            allNodes.filter { it.isNotEmpty() }.firstOrNull() ?: seedOnboardingData()
        }
        syncCalendars()
    }

    private suspend fun seedUserData() {
        if (user.first() == null) {
            repository.insertUser(UserEntity(name = "OPERATOR"))
        }
    }

    private suspend fun seedOnboardingData() {
        if (allNodes.value.isNotEmpty()) return

        seedDefaultModes()

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

        val areaId =
            repository.insertNode(
                NodeEntity(
                    title = "Personal",
                    type = "area",
                    inboxState = false,
                ),
            )

        // Seed some LifeOS sample data to ensure components are visible
        repository.insertNode(
            NodeEntity(
                title = "Reply to research email",
                type = "open_loop",
                openLoopType = "reply_needed",
                areaId = areaId,
                inboxState = false,
            ),
        )

        repository.insertNode(
            NodeEntity(
                title = "Choose between Hilt and Koin",
                type = "decision",
                decisionStatus = "pending",
                areaId = areaId,
                inboxState = false,
            ),
        )

        repository.insertNode(
            NodeEntity(
                title = "Monthly server backup",
                type = "maintenance",
                maintenanceType = "backup",
                areaId = areaId,
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

    private suspend fun seedStudentTemplates() {
        val existingNames =
            repository
                .getAllTemplates()
                .first()
                .map { it.name.trim().lowercase() }
                .toSet()
        val templates =
            listOf(
                TemplateEntity(
                    name = "Lecture Note Template",
                    nodeType = "note",
                    defaultTitle = "Lecture - [Course] - [Topic]",
                    defaultContent =
                        """
                        ## Key ideas
                        - 
                        
                        ## Definitions
                        - 
                        
                        ## Questions
                        - 
                        
                        ## Next actions
                        - 
                        """.trimIndent(),
                ),
                TemplateEntity(
                    name = "Reading Note Template",
                    nodeType = "note",
                    defaultTitle = "Reading - [Source] - [Chapter]",
                    defaultContent =
                        """
                        ## Source
                        - Author:
                        - Year:
                        - Link:
                        
                        ## Main argument
                        - 
                        
                        ## Evidence and methods
                        - 
                        
                        ## Quotes
                        - 
                        
                        ## Personal takeaways
                        - 
                        """.trimIndent(),
                ),
                TemplateEntity(
                    name = "Paper Summary Template",
                    nodeType = "note",
                    defaultTitle = "Paper Summary - [Title]",
                    defaultContent =
                        """
                        ## Citation
                        - 
                        
                        ## Research question
                        - 
                        
                        ## Method
                        - 
                        
                        ## Findings
                        - 
                        
                        ## Limitations
                        - 
                        
                        ## Relevance to exam
                        - 
                        """.trimIndent(),
                ),
            )

        templates.forEach { template ->
            if (!existingNames.contains(template.name.trim().lowercase())) {
                repository.insertTemplate(template)
            }
        }
    }

    private suspend fun seedDefaultModes() {
        val existingModes = repository.getAllModes().first()
        val existingKeys = existingModes.map { it.key }

        // Command Mode
        if ("COMMAND" !in existingKeys) {
            val commandId =
                repository.insertMode(
                    ModeEntity(
                        key = "COMMAND",
                        name = "Command",
                        description = "Default everyday overview mode. What matters right now?",
                        icon = "dashboard",
                        sortOrder = 0,
                        themeColor = 0xFF3F51B5.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = commandId,
                    showInbox = true,
                    showStats = true,
                    dashboardBlocksJson = "[\"today_top_3\", \"resume_context\", \"inbox_count\", \"deadlines\", \"overdue\", \"pinned_note\"]",
                ),
            )
            // Set initial mode only if none was active
            if (preferencesRepository.activeModeId.first() == null) {
                preferencesRepository.updateActiveModeId(commandId)
            }
        }

        // Focus Mode
        if ("FOCUS" !in existingKeys) {
            val focusId =
                repository.insertMode(
                    ModeEntity(
                        key = "FOCUS",
                        name = "Focus",
                        description = "Narrow the system to one thing. keep attention on this.",
                        icon = "center_focus_strong",
                        sortOrder = 1,
                        themeColor = 0xFFF44336.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = focusId,
                    showInbox = false,
                    showStats = false,
                    dashboardBlocksJson = "[\"current_task\", \"next_step\", \"timer\", \"blockers\", \"linked_resources\"]",
                ),
            )
        }

        // Recovery Mode
        if ("RECOVERY" !in existingKeys) {
            val recoveryId =
                repository.insertMode(
                    ModeEntity(
                        key = "RECOVERY",
                        name = "Recovery",
                        description = "Support low-capacity functioning. Smallest safe useful thing.",
                        icon = "medical_services",
                        sortOrder = 2,
                        themeColor = 0xFF4CAF50.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = recoveryId,
                    showInbox = false,
                    showStats = false,
                    dashboardBlocksJson = "[\"basics\", \"easy_wins\", \"urgent_only\", \"recovery_protocol\", \"check_in\"]",
                ),
            )
        }

        // Study Mode
        if ("STUDY" !in existingKeys) {
            val studyId =
                repository.insertMode(
                    ModeEntity(
                        key = "STUDY",
                        name = "Study",
                        description = "Focus on learning and academic performance.",
                        icon = "school",
                        sortOrder = 3,
                        themeColor = 0xFFFF9800.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = studyId,
                    dashboardBlocksJson = "[\"classes\", \"assignments\", \"deadlines\", \"notes\", \"revision_targets\"]",
                ),
            )
        }

        // Errand Mode
        if ("ERRAND" !in existingKeys) {
            val errandId =
                repository.insertMode(
                    ModeEntity(
                        key = "ERRAND",
                        name = "Errand",
                        description = "Out-of-home execution and logistical clustering.",
                        icon = "shopping_cart",
                        sortOrder = 4,
                        themeColor = 0xFF00BCD4.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = errandId,
                    dashboardBlocksJson = "[\"shopping_list\", \"place_based_tasks\", \"errands\", \"what_to_bring\"]",
                ),
            )
        }

        // Admin Mode
        if ("ADMIN" !in existingKeys) {
            val adminId =
                repository.insertMode(
                    ModeEntity(
                        key = "ADMIN",
                        name = "Admin",
                        description = "Handle the 'paperwork' of life. Subscriptions, bills, forms.",
                        icon = "gavel",
                        sortOrder = 5,
                        themeColor = 0xFF607D8B.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = adminId,
                    dashboardBlocksJson = "[\"paperwork\", \"bills\", \"renewals\", \"subscriptions\", \"bureaucracy\"]",
                ),
            )
        }

        // Shutdown Mode
        if ("SHUTDOWN" !in existingKeys) {
            val shutdownId =
                repository.insertMode(
                    ModeEntity(
                        key = "SHUTDOWN",
                        name = "Shutdown",
                        description = "Nightly reset and preparation for tomorrow.",
                        icon = "bedtime",
                        sortOrder = 6,
                        themeColor = 0xFF673AB7.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = shutdownId,
                    dashboardBlocksJson = "[\"tomorrow_prep\", \"mini_review\", \"dump_leftovers\", \"open_loops_reduction\"]",
                ),
            )
        }

        // Low Battery Mode
        if ("LOW_BATTERY" !in existingKeys) {
            val lowBatteryId =
                repository.insertMode(
                    ModeEntity(
                        key = "LOW_BATTERY",
                        name = "Low Battery",
                        description = "Minimal survival mode for when you are emotionally or physically drained.",
                        icon = "battery_alert",
                        sortOrder = 7,
                        themeColor = 0xFFE91E63.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = lowBatteryId,
                    showInbox = false,
                    dashboardBlocksJson = "[\"survival_basics\", \"tiny_wins\", \"passive_input\", \"comfort_notes\"]",
                ),
            )
        }

        // ALL Mode
        if ("ALL" !in existingKeys) {
            val allModeId =
                repository.insertMode(
                    ModeEntity(
                        key = "ALL",
                        name = "All",
                        description = "Unfiltered access to the entire system. No restrictions.",
                        icon = "all_inclusive",
                        sortOrder = 8,
                        themeColor = 0xFF9E9E9E.toInt(),
                    ),
                )
            repository.insertPreference(
                ModePreferenceEntity(
                    modeId = allModeId,
                    showInbox = true,
                    showStats = true,
                    dashboardBlocksJson = "[\"today_top_3\", \"search\", \"alerts\", \"focus\", \"insights\", \"knowledge\", \"operational\"]",
                ),
            )
        }
    }

    fun switchMode(modeId: Long) {
        viewModelScope.launch {
            val mode = allModesRaw.value.find { it.id == modeId } ?: return@launch
            val packs = enabledPacks.value
            if (!packs.canUseMode(mode.key)) return@launch
            preferencesRepository.updateActiveModeId(modeId)
            repository.insertModeUsageLog(
                ModeUsageLogEntity(
                    modeId = modeId,
                    activationSource = "manual",
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

    private fun calculateInsights(
        nodes: List<NodeWithPin>,
        sessions: List<FocusSessionEntity>,
        tracks: List<TrackEntryEntity>,
        projects: List<NodeEntity>,
    ): InsightsData {
        val now = Clock.System.now().toEpochMilliseconds()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)

        val recentNodes = nodes.filter { it.node.createdAt >= sevenDaysAgo }
        val recentCompletions =
            nodes.filter {
                it.node.status == "done" && (it.node.completedAt ?: 0) >= sevenDaysAgo
            }

        val recentSessions = sessions.filter { it.startedAt >= sevenDaysAgo && it.endedAt != null }
        val weeklyFocusSec = recentSessions.sumOf { it.durationSec.toLong() }
        val avgSessionMin =
            if (recentSessions.isNotEmpty()) {
                (recentSessions.map { it.durationSec }.average() / 60).toInt()
            } else {
                0
            }

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
            val hour =
                Instant
                    .fromEpochMilliseconds(it.node.completedAt ?: 0)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .hour
            completionHourlyDist[hour]++
        }
        val mostProductiveHour =
            completionHourlyDist.indices.maxByOrNull { completionHourlyDist[it] } ?: -1

        val sevenDaysAgoDate =
            Instant
                .fromEpochMilliseconds(sevenDaysAgo)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        val recentTracks = tracks.filter { it.date >= sevenDaysAgoDate.toString() }

        val avgMood =
            recentTracks.mapNotNull { it.moodScore }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
        val avgEnergy =
            recentTracks.mapNotNull { it.energyScore }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
        val avgFocus =
            recentTracks.mapNotNull { it.focusScore }.takeIf { it.isNotEmpty() }?.average() ?: 0.0

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

        val completionsByArea =
            recentCompletions
                .filter { it.node.areaId != null }
                .groupBy { it.node.areaId!! }
                .mapValues { it.value.size }
        val completionsByProject =
            recentCompletions
                .filter { it.node.projectId != null }
                .groupBy { it.node.projectId!! }
                .mapValues { it.value.size }

        val inboxGrowth = recentNodes.count { it.node.inboxState }
        val archivedCount =
            nodes.count {
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

        val uniqueContextsPerDay =
            recentSessions
                .groupBy {
                    Instant
                        .fromEpochMilliseconds(it.startedAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                }.mapValues {
                    it.value
                        .mapNotNull { s -> nodes.find { n -> n.node.id == s.nodeId }?.node?.projectId }
                        .distinct()
                        .size
                }
        val contextSwitchingRate =
            if (uniqueContextsPerDay.isNotEmpty()) uniqueContextsPerDay.values.average() else 0.0

        // Light Manual Statistics (Roadmap Section 7)
        // Correlating track entries with activity
        val dailyCompletions =
            recentCompletions
                .groupBy {
                    Instant
                        .fromEpochMilliseconds(it.node.completedAt ?: 0)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                        .toString()
                }.mapValues { it.value.size }

        val dailyCaptures =
            recentNodes
                .groupBy {
                    Instant
                        .fromEpochMilliseconds(it.node.createdAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                        .toString()
                }.mapValues { it.value.size }

        val dailyFocus =
            recentSessions
                .groupBy {
                    Instant
                        .fromEpochMilliseconds(it.startedAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                        .toString()
                }.mapValues { it.value.sumOf { s -> s.durationSec } / 3600.0 }

        val moodVsCompletions =
            if (recentTracks.isNotEmpty()) {
                val moodOnBusyDays =
                    recentTracks
                        .filter { (dailyCompletions[it.date] ?: 0) >= 3 }
                        .mapNotNull { it.moodScore }
                        .takeIf { it.isNotEmpty() }
                        ?.average() ?: Double.NaN
                val moodOnSlowDays =
                    recentTracks
                        .filter { (dailyCompletions[it.date] ?: 0) == 0 }
                        .mapNotNull { it.moodScore }
                        .takeIf { it.isNotEmpty() }
                        ?.average() ?: Double.NaN
                if (!moodOnBusyDays.isNaN() && !moodOnSlowDays.isNaN()) moodOnBusyDays - moodOnSlowDays else 0.0
            } else {
                0.0
            }

        val sleepVsFocus =
            if (recentTracks.isNotEmpty()) {
                val focusOnGoodSleep =
                    recentTracks
                        .filter { (it.sleepScore ?: 0f) >= 7f }
                        .map { dailyFocus[it.date] ?: 0.0 }
                        .takeIf { it.isNotEmpty() }
                        ?.average()
                        ?: Double.NaN
                val focusOnBadSleep =
                    recentTracks
                        .filter { (it.sleepScore ?: 0f) < 7f }
                        .map { dailyFocus[it.date] ?: 0.0 }
                        .takeIf { it.isNotEmpty() }
                        ?.average()
                        ?: Double.NaN
                if (!focusOnGoodSleep.isNaN() && !focusOnBadSleep.isNaN()) focusOnGoodSleep - focusOnBadSleep else 0.0
            } else {
                0.0
            }

        val energyVsCaptures =
            if (recentTracks.isNotEmpty()) {
                val capturesOnHighEnergy =
                    recentTracks
                        .filter { (it.energyScore ?: 0) >= 4 }
                        .map { dailyCaptures[it.date] ?: 0 }
                        .takeIf { it.isNotEmpty() }
                        ?.average()
                        ?: Double.NaN
                val capturesOnLowEnergy =
                    recentTracks
                        .filter { (it.energyScore ?: 0) <= 2 }
                        .map { dailyCaptures[it.date] ?: 0 }
                        .takeIf { it.isNotEmpty() }
                        ?.average()
                        ?: Double.NaN
                if (!capturesOnHighEnergy.isNaN() && !capturesOnLowEnergy.isNaN()) capturesOnHighEnergy - capturesOnLowEnergy else 0.0
            } else {
                0.0
            }

        val anxietyVsAvoidance =
            if (recentTracks.isNotEmpty()) {
                // Using low mood/energy as a proxy for high anxiety/stress if not explicitly tracked
                val postponesOnBadDays =
                    recentTracks.filter { (it.moodScore ?: 5) <= 2 }.sumOf { track ->
                        recentNodes
                            .filter {
                                val d =
                                    Instant
                                        .fromEpochMilliseconds(it.node.updatedAt)
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date
                                        .toString()
                                d == track.date && it.node.postponeCount > 0
                            }.size
                    }
                postponesOnBadDays.toDouble()
            } else {
                0.0
            }

        val medsEffectiveness =
            if (recentTracks.isNotEmpty()) {
                val focusWithMeds =
                    recentTracks
                        .filter { it.tookMeds }
                        .mapNotNull { it.focusScore }
                        .takeIf { it.isNotEmpty() }
                        ?.average() ?: Double.NaN
                val focusWithoutMeds =
                    recentTracks
                        .filter { !it.tookMeds }
                        .mapNotNull { it.focusScore }
                        .takeIf { it.isNotEmpty() }
                        ?.average() ?: Double.NaN
                if (!focusWithMeds.isNaN() && !focusWithoutMeds.isNaN()) focusWithMeds - focusWithoutMeds else 0.0
            } else {
                0.0
            }

        // Insight Cards Logic (Roadmap Section 7)
        val mostPostponedAreaId =
            nodes
                .filter { it.node.areaId != null && it.node.postponeCount > 0 }
                .groupBy { it.node.areaId!! }
                .maxByOrNull { entry -> entry.value.sumOf { it.node.postponeCount } }
                ?.key

        val ideaTimes =
            nodes
                .filter { it.node.type == "idea" && it.node.createdAt >= sevenDaysAgo }
                .map {
                    Instant
                        .fromEpochMilliseconds(it.node.createdAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .hour
                }

        val captureTimePattern =
            if (ideaTimes.isNotEmpty()) {
                val morning = ideaTimes.count { it in 6..11 }
                val afternoon = ideaTimes.count { it in 12..17 }
                val evening = ideaTimes.count { it in 18..23 }
                val night = ideaTimes.count { it in 0..5 }
                val max = listOf(morning, afternoon, evening, night).maxOrNull() ?: 0
                when (max)
                {
                    morning -> "Morning"
                    afternoon -> "Afternoon"
                    evening -> "Evening"
                    else -> "Night"
                }
            } else {
                null
            }

        val projectsWithoutTasks =
            projects.filter { project ->
                val projectNodes = nodes.filter { it.node.projectId == project.id }
                val hasNotes = projectNodes.any { it.node.type == "note" || it.node.type == "idea" }
                val hasTasks =
                    projectNodes.any { it.node.type == "task" && it.node.status == "active" }
                hasNotes && !hasTasks
            }

        val areas = nodes.filter { it.node.type == "area" }.map { it.node }
        val neglectedAreas =
            areas.filter { area ->
                val areaNodes = nodes.filter { it.node.areaId == area.id }
                val hasRecentActivity = areaNodes.any { it.node.updatedAt >= sevenDaysAgo }
                !hasRecentActivity
            }

        // Advanced Insight Concepts (Roadmap Section 7)
        val projectEntropy =
            projects.associate { project ->
                val projectNodes =
                    nodes.filter { it.node.projectId == project.id && it.node.status == "active" }
                if (projectNodes.isEmpty()) {
                    project.id to 0.0
                } else {
                    val messyNodes =
                        projectNodes.count {
                            it.node.dueAt == null || it.node.postponeCount > 2 || it.tags.isEmpty()
                        }
                    project.id to (messyNodes.toDouble() / projectNodes.size)
                }
            }

        val contextStability =
            if (contextSwitchingRate > 0) 1.0 / (1.0 + contextSwitchingRate) else 1.0

        val behaviorSummary =
            buildString {
                if (mostProductiveHour != -1) {
                    append("You typically finish tasks around $mostProductiveHour:00. ")
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

        val review =
            buildString {
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
            passiveBehaviorSummary = behaviorSummary,
        )
    }

    private fun calculateAreaHealthSnapshot(
        nodes: List<NodeWithPin>,
        areas: List<NodeEntity>,
    ): AreaHealthSnapshot {
        if (areas.isEmpty()) return AreaHealthSnapshot()

        val now = Clock.System.now().toEpochMilliseconds()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
        val dueSoonHorizon = now + (7 * 24 * 60 * 60 * 1000L)

        val computed =
            areas
                .map { area ->
                    val areaNodes =
                        nodes.filter { it.node.areaId == area.id && it.node.type != "area" }
                    val activeNodes = areaNodes.filter { it.node.status == "active" }
                    val openLoops = activeNodes.count { it.node.type == "open_loop" }
                    val deadlines = activeNodes.count { it.node.dueAt != null }
                    val overdueDeadlines =
                        activeNodes.count { (it.node.dueAt ?: Long.MAX_VALUE) < now }
                    val dueSoon =
                        activeNodes.count {
                            val due = it.node.dueAt
                            due != null && due in now..dueSoonHorizon
                        }
                    val recentActivity = areaNodes.count { it.node.updatedAt >= sevenDaysAgo }
                    val doneThisWeek =
                        areaNodes.count {
                            it.node.status == "done" && (it.node.completedAt ?: 0) >= sevenDaysAgo
                        }
                    val lastActivityAt =
                        (
                            areaNodes.maxOfOrNull { it.node.updatedAt }
                                ?: area.updatedAt
                        ).takeIf { it > 0 }
                    val neglectedDays =
                        (
                            (
                                (
                                    now - (
                                        lastActivityAt
                                            ?: now
                                    )
                                ).coerceAtLeast(0L)
                            ) / (24 * 60 * 60 * 1000L)
                        ).toInt()

                    val stressLoad =
                        (
                            (activeNodes.size * 2) +
                                (openLoops * 10) +
                                (deadlines * 4) +
                                (dueSoon * 8) +
                                (overdueDeadlines * 20) +
                                activeNodes.sumOf { (it.node.postponeCount.coerceAtMost(3) * 4) }
                        ).coerceIn(0, 100)

                    val status =
                        when
                            {
                                overdueDeadlines >= 3 || stressLoad >= 85 -> "on_fire"
                                stressLoad >= 70 || activeNodes.size >= 15 -> "overloaded"
                                neglectedDays >= 14 && activeNodes.isNotEmpty() -> "neglected"
                                activeNodes.isNotEmpty() || recentActivity > 0 -> "active"
                                else -> "stable"
                            }

                    val isDisappearing =
                        neglectedDays >= 10 &&
                            recentActivity == 0 &&
                            (activeNodes.isNotEmpty() || openLoops > 0 || deadlines > 0)

                    AreaHealthMetrics(
                        areaId = area.id,
                        areaTitle = area.title,
                        status = status,
                        activeItems = activeNodes.size,
                        openLoops = openLoops,
                        deadlines = deadlines,
                        overdueDeadlines = overdueDeadlines,
                        stressLoad = stressLoad,
                        recentActivity = recentActivity,
                        neglectedDays = neglectedDays,
                        doneThisWeek = doneThisWeek,
                        lastActivityAt = lastActivityAt,
                        isDisappearing = isDisappearing,
                    )
                }.sortedByDescending { it.stressLoad }

        val dominantAreaId =
            computed
                .maxByOrNull { area ->
                    (area.recentActivity * 2) + (area.doneThisWeek * 3) + area.activeItems
                }?.areaId

        val disappearingAreaIds =
            computed.filter { it.isDisappearing }.mapTo(mutableSetOf()) { it.areaId }

        val avgLoad = computed.map { it.stressLoad }.average()
        val variance =
            if (computed.size > 1) {
                computed.map { (it.stressLoad - avgLoad) * (it.stressLoad - avgLoad) }.average()
            } else {
                0.0
            }
        val imbalanceScore = (sqrt(variance) * 2).toInt().coerceIn(0, 100)
        val imbalanceLabel =
            when
                {
                    imbalanceScore >= 60 -> "critical"
                    imbalanceScore >= 30 -> "tilted"
                    else -> "balanced"
                }

        return AreaHealthSnapshot(
            areas = computed,
            dominantAreaId = dominantAreaId,
            disappearingAreaIds = disappearingAreaIds,
            imbalanceScore = imbalanceScore,
            imbalanceLabel = imbalanceLabel,
        )
    }

    private fun calculateOpenLoopsSnapshot(
        nodes: List<NodeWithPin>,
        relations: List<RelationEntity>,
    ): OpenLoopsSnapshot {
        val now = Clock.System.now().toEpochMilliseconds()
        val nodesById = nodes.associateBy { it.node.id }

        fun findRelatedPerson(openLoopId: Long): Pair<Long, String>? {
            val relatedPersonId =
                relations.firstNotNullOfOrNull { relation ->
                    val otherId =
                        when (openLoopId)
                            {
                                relation.fromNodeId -> relation.toNodeId
                                relation.toNodeId -> relation.fromNodeId
                                else -> null
                            } ?: return@firstNotNullOfOrNull null
                    val other = nodesById[otherId]?.node ?: return@firstNotNullOfOrNull null
                    if (other.type == "person") other.id else null
                } ?: return null
            val personName = nodesById[relatedPersonId]?.node?.title ?: "Unknown"
            return relatedPersonId to personName
        }

        val openLoopItems =
            nodes
                .filter { it.node.type == "open_loop" && it.node.status != "archived" }
                .map { openLoop ->
                    val person = findRelatedPerson(openLoop.node.id)
                    val urgency = openLoopUrgency(openLoop.node, now)
                    val ageDays =
                        ((now - openLoop.node.createdAt).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()
                    val stalenessAnchor =
                        openLoop.node.openLoopStalenessAt ?: openLoop.node.updatedAt
                    val stalenessDays =
                        ((now - stalenessAnchor).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()
                    OpenLoopStatusItem(
                        node = openLoop,
                        urgency = urgency,
                        ageDays = ageDays,
                        stalenessDays = stalenessDays,
                        decayScore = openLoopDecayScore(openLoop.node, now),
                        relatedPersonId = person?.first,
                        relatedPersonName = person?.second,
                    )
                }

        val active =
            openLoopItems
                .filter { it.node.node.status == "active" }
                .sortedByDescending { it.decayScore }
        val inbox = active.filter { it.node.node.inboxState }
        val review =
            active
                .filter { item ->
                    item.stalenessDays >= 5 ||
                        item.ageDays >= 7 ||
                        item.urgency == "critical" ||
                        item.urgency == "high"
                }.sortedByDescending { it.decayScore }
        val resolved =
            openLoopItems
                .filter { it.node.node.status == "done" }
                .sortedByDescending { it.node.node.completedAt ?: 0L }

        val byArea = active.groupBy { it.node.node.areaId }
        val byPerson = active.filter { it.relatedPersonId != null }.groupBy { it.relatedPersonId!! }
        val byUrgency =
            linkedMapOf(
                "critical" to active.filter { it.urgency == "critical" },
                "high" to active.filter { it.urgency == "high" },
                "medium" to active.filter { it.urgency == "medium" },
                "low" to active.filter { it.urgency == "low" },
            ).filterValues { it.isNotEmpty() }

        val averageDecayScore =
            if (active.isNotEmpty()) active.map { it.decayScore }.average().toInt() else 0
        val overloadWarning =
            when
                {
                    active.size >= 12 -> "TOO MANY OPEN LOOPS // REDUCE FRONTS"

                    (
                        byUrgency["critical"]?.size
                            ?: 0
                    ) >= 4 -> "MULTIPLE CRITICAL OPEN LOOPS // PRIORITIZE RESOLUTION"

                    averageDecayScore >= 60 -> "OPEN LOOPS DECAYING // RUN REVIEW"

                    else -> null
                }

        return OpenLoopsSnapshot(
            active = active,
            inbox = inbox,
            review = review,
            resolved = resolved,
            byArea = byArea,
            byPerson = byPerson,
            byUrgency = byUrgency,
            overloadWarning = overloadWarning,
            averageDecayScore = averageDecayScore,
        )
    }

    private fun openLoopUrgency(
        node: NodeEntity,
        now: Long,
    ): String {
        val dueAt = node.dueAt
        val stalenessAnchor = node.openLoopStalenessAt ?: node.updatedAt
        val stalenessDays =
            ((now - stalenessAnchor).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()

        return when
            {
                dueAt != null && dueAt < now -> "critical"
                dueAt != null && dueAt < now + (24 * 60 * 60 * 1000L) -> "critical"
                stalenessDays >= 14 -> "critical"
                dueAt != null && dueAt < now + (3 * 24 * 60 * 60 * 1000L) -> "high"
                stalenessDays >= 7 -> "high"
                dueAt != null && dueAt < now + (7 * 24 * 60 * 60 * 1000L) -> "medium"
                stalenessDays >= 3 -> "medium"
                else -> "low"
            }
    }

    private fun openLoopDecayScore(
        node: NodeEntity,
        now: Long,
    ): Int {
        val ageDays = ((now - node.createdAt).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()
        val stalenessAnchor = node.openLoopStalenessAt ?: node.updatedAt
        val stalenessDays =
            ((now - stalenessAnchor).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()
        val urgencyBoost =
            when (openLoopUrgency(node, now))
            {
                "critical" -> 30
                "high" -> 18
                "medium" -> 8
                else -> 0
            }
        return (
            (ageDays * 3) +
                (stalenessDays * 5) +
                (node.postponeCount.coerceAtMost(4) * 7) +
                urgencyBoost
        ).coerceIn(0, 100)
    }

    private fun calculateMaintenanceSnapshot(nodes: List<NodeWithPin>): MaintenanceSnapshot {
        val now = Clock.System.now().toEpochMilliseconds()
        val soonHorizon = now + (7 * 24 * 60 * 60 * 1000L)
        val criticalTypes =
            setOf("bill", "prescription", "renewal", "subscription", "form", "appointment")

        val activeItems =
            nodes
                .filter { it.node.type == "maintenance" && it.node.status == "active" }
                .map { item ->
                    val urgency = maintenanceUrgency(item.node, now)
                    val anchor = item.node.maintenanceOverdueAt ?: item.node.dueAt
                    val overdueDays =
                        if (anchor != null && anchor < now) {
                            ((now - anchor) / (24 * 60 * 60 * 1000L)).toInt()
                        } else {
                            0
                        }
                    val dueInDays =
                        if (anchor != null && anchor >= now) {
                            ((anchor - now) / (24 * 60 * 60 * 1000L)).toInt()
                        } else {
                            null
                        }
                    MaintenanceStatusItem(
                        node = item,
                        urgency = urgency,
                        isRecurring = item.node.isRecurring || item.node.maintenanceInterval != null,
                        overdueDays = overdueDays,
                        dueInDays = dueInDays,
                    )
                }.sortedByDescending {
                    when (it.urgency)
                        {
                            "critical" -> 4
                            "high" -> 3
                            "medium" -> 2
                            else -> 1
                        } * 100 + it.overdueDays
                }

        val recurring = activeItems.filter { it.isRecurring }
        val overdue = activeItems.filter { it.overdueDays > 0 || it.urgency == "critical" }
        val expirationReminders =
            activeItems
                .filter { item ->
                    val due = item.node.node.maintenanceOverdueAt ?: item.node.node.dueAt
                    due != null && due in now..soonHorizon
                }.sortedBy {
                    it.node.node.maintenanceOverdueAt ?: it.node.node.dueAt ?: Long.MAX_VALUE
                }

        val breakIfIgnored =
            activeItems
                .filter {
                    (
                        it.node.node.maintenanceType
                            ?: "manual"
                    ) in criticalTypes || it.urgency == "critical"
                }.take(6)

        val byType = activeItems.groupBy { it.node.node.maintenanceType ?: "manual" }
        val byArea = activeItems.groupBy { it.node.node.areaId }
        val byUrgency =
            linkedMapOf(
                "critical" to activeItems.filter { it.urgency == "critical" },
                "high" to activeItems.filter { it.urgency == "high" },
                "medium" to activeItems.filter { it.urgency == "medium" },
                "low" to activeItems.filter { it.urgency == "low" },
            ).filterValues { it.isNotEmpty() }

        val adminDebtMeter =
            (
                (activeItems.size * 4) +
                    (overdue.size * 12) +
                    ((byUrgency["critical"]?.size ?: 0) * 18)
            ).coerceIn(0, 100)
        val overdueWarning =
            when
                {
                    (
                        byUrgency["critical"]?.size
                            ?: 0
                    ) >= 3 -> "CRITICAL MAINTENANCE OVERDUE // ACT TODAY"

                    overdue.size >= 5 -> "MAINTENANCE DEBT SPIKING // RUN ADMIN BLOCK"

                    adminDebtMeter >= 70 -> "ADMIN DEBT HIGH // REDUCE RISK ITEMS"

                    else -> null
                }

        return MaintenanceSnapshot(
            active = activeItems,
            recurring = recurring,
            overdue = overdue,
            byType = byType,
            byArea = byArea,
            byUrgency = byUrgency,
            expirationReminders = expirationReminders,
            breakIfIgnored = breakIfIgnored,
            adminDebtMeter = adminDebtMeter,
            overdueWarning = overdueWarning,
        )
    }

    private fun maintenanceUrgency(
        node: NodeEntity,
        now: Long,
    ): String {
        val due = node.maintenanceOverdueAt ?: node.dueAt
        val type = node.maintenanceType ?: "manual"
        return when
            {
                due != null && due < now -> "critical"

                due != null && due < now + (24 * 60 * 60 * 1000L) -> "critical"

                type in
                    setOf(
                        "bill",
                        "prescription",
                        "renewal",
                    ) && due != null && due < now + (3 * 24 * 60 * 60 * 1000L) -> "high"

                due != null && due < now + (3 * 24 * 60 * 60 * 1000L) -> "high"

                due != null && due < now + (7 * 24 * 60 * 60 * 1000L) -> "medium"

                else -> "low"
            }
    }

    private suspend fun seedLifeLogisticsTemplates() {
        val existingNames =
            repository
                .getAllTemplates()
                .first()
                .map { it.name.trim().lowercase() }
                .toSet()
        if ("travel pack template" !in existingNames) {
            repository.insertTemplate(
                TemplateEntity(
                    name = "Travel Pack Template",
                    nodeType = "note",
                    defaultTitle = "Travel pack - [Trip]",
                    defaultContent =
                        """
                        - IDs / documents
                        - Wallet / cards / cash
                        - Phone / charger / powerbank
                        - Medications
                        - Clothes / hygiene
                        - Special gear
                        - Don't forget items
                        """.trimIndent(),
                ),
            )
        }
    }

    private fun calculateTimeArchitectureSnapshot(
        nodes: List<NodeWithPin>,
        todayLayerNodes: List<NodeEntity>,
        projects: List<NodeEntity>,
    ): TimeArchitectureSnapshot {
        val now = Clock.System.now().toEpochMilliseconds()
        val dayMs = 24 * 60 * 60 * 1000L
        val weekHorizon = now + (7 * dayMs)
        val monthHorizon = now + (30 * dayMs)
        val semesterHorizon = now + (120 * dayMs)
        val todayIds = todayLayerNodes.mapTo(mutableSetOf()) { it.id }

        val activeNodes = nodes.filter { it.node.status == "active" }
        val dueNodes =
            activeNodes
                .filter { it.node.dueAt != null }
                .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }
        val todayLayer = activeNodes.filter { it.node.id in todayIds }
        val weekLayer = dueNodes.filter { (it.node.dueAt ?: Long.MAX_VALUE) in now..weekHorizon }
        val monthLayer = dueNodes.filter { (it.node.dueAt ?: Long.MAX_VALUE) in now..monthHorizon }
        val semesterLayer =
            dueNodes.filter { (it.node.dueAt ?: Long.MAX_VALUE) in now..semesterHorizon }
        val shortHorizonTasks = weekLayer.filter { it.node.type == "task" }.take(8)
        val longHorizonTasks = dueNodes.filter { (it.node.dueAt ?: 0L) > monthHorizon }.take(8)
        val seasonalGoals =
            activeNodes.filter { item ->
                item.node.noteType == "goal_seasonal" ||
                    item.tags.any { tag -> tag.normalizedName == "seasonal_goal" }
            }
        val temporaryFocusPeriods =
            activeNodes
                .filter { item ->
                    val startAt = item.node.startAt ?: return@filter false
                    val dueAt = item.node.dueAt ?: return@filter false
                    dueAt > startAt && (dueAt - startAt) <= (14 * dayMs)
                }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }
        val lifePeriodMarkers =
            nodes
                .filter { item ->
                    item.node.noteType == "period_marker" ||
                        item.tags.any { tag -> tag.normalizedName == "life_period_marker" }
                }.sortedByDescending { it.node.updatedAt }
                .take(8)
        val countdowns =
            dueNodes
                .map { item ->
                    val due = item.node.dueAt ?: now
                    val daysLeft = ((due - now).coerceAtLeast(0L) / dayMs)
                    TimeCountdownItem(node = item, daysLeft = daysLeft)
                }.take(8)
        val examPeriodMode =
            countdowns.any { countdown ->
                countdown.daysLeft <= 30 &&
                    (
                        countdown.node.node.title
                            .contains("exam", ignoreCase = true) ||
                            countdown.node.tags.any { tag ->
                                tag.normalizedName.contains(
                                    "exam",
                                )
                            }
                    )
            }
        val weeklyMap =
            weekLayer
                .groupingBy { item ->
                    val due = item.node.dueAt ?: now
                    Instant
                        .fromEpochMilliseconds(due)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date.dayOfWeek.name
                }.eachCount()
                .toSortedMap()
        val projectPhases =
            projects
                .map { project ->
                    val phase = project.projectStatus ?: "active"
                    val isActive = phase in setOf("active", "exploratory")
                    ProjectPhaseItem(
                        project = project,
                        isActivePhase = isActive,
                        phaseLabel = if (isActive) "active_phase" else "inactive_phase",
                    )
                }.sortedBy { it.project.title.lowercase() }

        return TimeArchitectureSnapshot(
            todayLayer = todayLayer,
            weekLayer = weekLayer,
            monthLayer = monthLayer,
            semesterLayer = semesterLayer,
            examPeriodMode = examPeriodMode,
            projectPhases = projectPhases,
            countdowns = countdowns,
            monthlyResetDate = nextMonthlyResetDate(),
            weeklyMap = weeklyMap,
            seasonalGoals = seasonalGoals,
            temporaryFocusPeriods = temporaryFocusPeriods,
            shortHorizonTasks = shortHorizonTasks,
            longHorizonTasks = longHorizonTasks,
            lifePeriodMarkers = lifePeriodMarkers,
        )
    }

    private fun nextMonthlyResetDate(): String {
        val now = Clock.System.now()
        val zone = TimeZone.currentSystemDefault()
        val nextMonth = now.plus(1, DateTimeUnit.MONTH, zone).toLocalDateTime(zone)
        val nextReset = LocalDate(nextMonth.year, nextMonth.month, 1)
        return nextReset.toString()
    }

    private fun calculateRelationshipSnapshot(
        nodes: List<NodeWithPin>,
        relations: List<RelationEntity>,
    ): RelationshipSnapshot {
        val now = Clock.System.now().toEpochMilliseconds()
        val dayMs = 24 * 60 * 60 * 1000L
        val byId = nodes.associateBy { it.node.id }

        val people =
            nodes
                .filter { it.node.type == "person" && it.node.status == "active" }
                .sortedBy { it.node.title.lowercase() }

        fun relatedForPerson(personId: Long): List<NodeWithPin> =
            relations
                .mapNotNull { relation ->
                    when
                        {
                            relation.fromNodeId == personId -> byId[relation.toNodeId]
                            relation.toNodeId == personId -> byId[relation.fromNodeId]
                            else -> null
                        }
                }.filter { it.node.type != "person" }
                .distinctBy { it.node.id }

        val peopleItems =
            people.map { person ->
                val relatedNodes = relatedForPerson(person.node.id)
                val replyQueueCount =
                    relatedNodes.count {
                        it.node.type == "open_loop" &&
                            it.node.status == "active" &&
                            (it.node.openLoopType == "reply_needed" || it.node.openLoopType == "follow_up")
                    }
                val sharedPlansCount =
                    relatedNodes.count {
                        it.node.status == "active" &&
                            (
                                it.tags.any { tag -> tag.normalizedName == "shared_plan" } ||
                                    it.node.title.contains("shared", ignoreCase = true)
                            )
                    }
                val askAboutCount =
                    relatedNodes.count {
                        it.node.type == "note" &&
                            (
                                it.node.noteType == "ask_next_time" ||
                                    it.tags.any { tag -> tag.normalizedName == "ask_next_time" }
                            )
                    }
                val lastContact = person.node.lastContactAt
                val daysSince =
                    lastContact?.let { ((now - it).coerceAtLeast(0L) / dayMs).toInt() }
                val followUpAt = person.node.dueAt
                val followUpDueInDays =
                    followUpAt?.let { ((it - now) / dayMs).toInt() }
                val relationshipType =
                    when
                        {
                            person.tags.any { it.normalizedName == "professor" } -> "professor"
                            person.tags.any { it.normalizedName == "family" } -> "family"
                            person.tags.any { it.normalizedName == "friend" } -> "friend"
                            else -> null
                        }
                RelationshipStatusItem(
                    person = person,
                    relationshipType = relationshipType,
                    daysSinceLastContact = daysSince,
                    followUpDueInDays = followUpDueInDays,
                    isImportant =
                        person.tags.any { tag -> tag.normalizedName == "important_relationship" } ||
                            person.node.relationshipContext?.contains(
                                "important",
                                ignoreCase = true,
                            ) == true,
                    linkedItemsCount = relatedNodes.size,
                    pendingReplyCount = replyQueueCount,
                    sharedPlansCount = sharedPlansCount,
                    askAboutNextTimeCount = askAboutCount,
                )
            }

        val followUpNeeded =
            peopleItems
                .filter { item ->
                    val stale = (item.daysSinceLastContact ?: 0) >= 14
                    val followUpDue = (item.followUpDueInDays ?: Int.MAX_VALUE) <= 3
                    stale || followUpDue || item.pendingReplyCount > 0
                }.sortedWith(
                    compareByDescending<RelationshipStatusItem> { it.pendingReplyCount }
                        .thenByDescending { it.daysSinceLastContact ?: 0 },
                )

        val upcomingImportantDates =
            peopleItems
                .filter { item ->
                    val dueIn = item.followUpDueInDays ?: return@filter false
                    dueIn in 0..30
                }.sortedBy { it.followUpDueInDays }

        val allRelatedItemsByPerson =
            peopleItems.associate { item ->
                item.person.node.id to relatedForPerson(item.person.node.id)
            }
        val replyQueue =
            allRelatedItemsByPerson.values
                .flatten()
                .filter {
                    it.node.status == "active" &&
                        it.node.type == "open_loop" &&
                        (it.node.openLoopType == "reply_needed" || it.node.openLoopType == "follow_up")
                }.distinctBy { it.node.id }
                .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

        val sharedPlans =
            allRelatedItemsByPerson.values
                .flatten()
                .filter {
                    it.node.status == "active" &&
                        (
                            it.tags.any { tag -> tag.normalizedName == "shared_plan" } ||
                                it.node.title.contains("shared", ignoreCase = true)
                        )
                }.distinctBy { it.node.id }

        val importantRelationships =
            peopleItems
                .filter { it.isImportant }
                .sortedBy {
                    it.person.node.title
                        .lowercase()
                }

        val professors =
            peopleItems
                .filter { it.relationshipType == "professor" }
                .sortedBy {
                    it.person.node.title
                        .lowercase()
                }

        val friendsAndFamily =
            peopleItems
                .filter { it.relationshipType == "friend" || it.relationshipType == "family" }
                .sortedBy {
                    it.person.node.title
                        .lowercase()
                }

        val gentlePrompt =
            when
                {
                    followUpNeeded.size >= 8 -> "Several connections need a touchpoint. Pick 1-2 gentle follow-ups today."
                    followUpNeeded.isNotEmpty() -> "A small social maintenance pass could reduce open loops."
                    else -> null
                }

        return RelationshipSnapshot(
            people = peopleItems,
            importantRelationships = importantRelationships,
            followUpNeeded = followUpNeeded,
            upcomingImportantDates = upcomingImportantDates,
            replyQueue = replyQueue,
            sharedPlans = sharedPlans,
            professors = professors,
            friendsAndFamily = friendsAndFamily,
            gentlePrompt = gentlePrompt,
        )
    }

    private fun calculatePhysicalLogisticsSnapshot(
        nodes: List<NodeWithPin>,
        relations: List<RelationEntity>,
        templates: List<TemplateEntity>,
    ): PhysicalLogisticsSnapshot {
        val byId = nodes.associateBy { it.node.id }
        val activeNodes = nodes.filter { it.node.status == "active" }
        val activeTasks = activeNodes.filter { it.node.type == "task" }

        fun relatedTasksForPlace(placeId: Long): List<NodeWithPin> {
            val relationTaskIds =
                relations
                    .mapNotNull { relation ->
                        when (placeId)
                        {
                            relation.fromNodeId -> relation.toNodeId
                            relation.toNodeId -> relation.fromNodeId
                            else -> null
                        }
                    }.toSet()
            return activeTasks.filter { task ->
                task.node.id in relationTaskIds ||
                    (
                        task.node.locationContext == "on_campus" && (
                            byId[placeId]?.node?.title?.contains(
                                "campus",
                                ignoreCase = true,
                            ) == true
                        )
                    ) ||
                    (
                        task.node.locationContext == "at_home" && (
                            byId[placeId]?.node?.title?.contains(
                                "home",
                                ignoreCase = true,
                            ) == true
                        )
                    )
            }
        }

        val placeNodes = activeNodes.filter { it.node.type == "place" }
        val placeItems =
            placeNodes
                .map { place ->
                    val relatedTasks = relatedTasksForPlace(place.node.id)
                    PlaceLogisticsItem(
                        place = place,
                        relatedTasks = relatedTasks,
                        remindersCount = relatedTasks.count { it.node.reminderAt != null },
                    )
                }.sortedBy {
                    it.place.node.title
                        .lowercase()
                }

        val campusLocations =
            placeItems.filter {
                it.place.node.locationContext == "on_campus" ||
                    it.place.tags.any { tag -> tag.normalizedName == "campus" } ||
                    it.place.node.title
                        .contains("campus", ignoreCase = true)
            }
        val homeZones =
            placeItems.filter {
                it.place.node.locationContext == "at_home" ||
                    it.place.tags.any { tag -> tag.normalizedName == "home" } ||
                    it.place.node.title
                        .contains("home", ignoreCase = true)
            }

        val placeBasedTasks =
            activeTasks
                .filter { task ->
                    task.node.locationContext != null ||
                        relations.any { relation ->
                            relation.fromNodeId == task.node.id && byId[relation.toNodeId]?.node?.type == "place" ||
                                relation.toNodeId == task.node.id && byId[relation.fromNodeId]?.node?.type == "place"
                        }
                }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

        val outOfHomeTaskClusters =
            placeBasedTasks
                .filter { it.node.locationContext == "out_of_home" }
                .groupBy { task ->
                    val linkedPlaceName =
                        relations.firstNotNullOfOrNull { relation ->
                            val otherId =
                                when (task.node.id)
                                    {
                                        relation.fromNodeId -> relation.toNodeId
                                        relation.toNodeId -> relation.fromNodeId
                                        else -> null
                                    } ?: return@firstNotNullOfOrNull null
                            val other = byId[otherId]?.node ?: return@firstNotNullOfOrNull null
                            if (other.type == "place") other.title else null
                        }
                    linkedPlaceName ?: "GENERAL OUT-OF-HOME"
                }

        val errandClusters =
            activeTasks
                .filter {
                    it.node.locationContext == "out_of_home" ||
                        it.tags.any { tag -> tag.normalizedName in setOf("errand", "shopping") }
                }.groupBy { task ->
                    if (task.tags.any { it.normalizedName == "shopping" }) {
                        "SHOPPING"
                    } else {
                        (
                            task.node.areaId?.toString()
                                ?: "GENERAL"
                        )
                    }
                }

        fun hasLogisticsTag(
            node: NodeWithPin,
            vararg tags: String,
        ): Boolean = node.tags.any { it.normalizedName in tags.toSet() }

        val whatToBringLists =
            activeNodes.filter {
                (it.node.type == "note" || it.node.type == "task") &&
                    (
                        hasLogisticsTag(it, "what_to_bring") ||
                            it.node.title.contains("bring", ignoreCase = true)
                    )
            }
        val packingLists =
            activeNodes.filter {
                (it.node.type == "note" || it.node.type == "task") &&
                    (
                        hasLogisticsTag(it, "packing_list") ||
                            it.node.title.contains(
                                "pack",
                                ignoreCase = true,
                            )
                    )
            }
        val leaveHomeChecklists =
            activeNodes.filter {
                (it.node.type == "note" || it.node.type == "task" || it.node.type == "protocol") &&
                    (
                        hasLogisticsTag(
                            it,
                            "leave_home_checklist",
                        ) || it.node.title.contains("leave home", ignoreCase = true)
                    )
            }
        val dontForgetSets =
            activeNodes.filter {
                hasLogisticsTag(it, "dont_forget_set") ||
                    it.node.title.contains(
                        "don't forget",
                        ignoreCase = true,
                    )
            }
        val eventPreparationLists =
            activeNodes.filter {
                hasLogisticsTag(it, "event_prep") ||
                    it.node.title.contains(
                        "event prep",
                        ignoreCase = true,
                    )
            }
        val classSpecificBringLists =
            activeNodes.filter {
                hasLogisticsTag(it, "class_bring") ||
                    (
                        it.node.title.contains("class", ignoreCase = true) &&
                            it.node.title.contains("bring", ignoreCase = true)
                    )
            }
        val physicalLogisticsNotes =
            activeNodes.filter {
                it.node.type == "note" &&
                    (
                        it.node.noteType == "logistics" ||
                            hasLogisticsTag(it, "logistics")
                    )
            }

        val locationSpecificReminders =
            placeBasedTasks
                .filter { it.node.reminderAt != null }
                .sortedBy { it.node.reminderAt ?: Long.MAX_VALUE }

        val travelPackTemplateReady =
            templates.any { it.name.contains("travel pack", ignoreCase = true) }

        return PhysicalLogisticsSnapshot(
            places = placeItems,
            campusLocations = campusLocations,
            homeZones = homeZones,
            placeBasedTasks = placeBasedTasks,
            outOfHomeTaskClusters = outOfHomeTaskClusters,
            errandClusters = errandClusters,
            whatToBringLists = whatToBringLists,
            packingLists = packingLists,
            leaveHomeChecklists = leaveHomeChecklists,
            dontForgetSets = dontForgetSets,
            eventPreparationLists = eventPreparationLists,
            classSpecificBringLists = classSpecificBringLists,
            physicalLogisticsNotes = physicalLogisticsNotes,
            travelPackTemplateReady = travelPackTemplateReady,
            locationSpecificReminders = locationSpecificReminders,
        )
    }

    private fun calculatePersonalRulesSnapshot(
        nodes: List<NodeWithPin>,
        relations: List<RelationEntity>,
    ): PersonalRulesSnapshot {
        val activeRules =
            nodes
                .filter { it.node.status == "active" }
                .filter { item ->
                    item.node.type in setOf("rule", "principle", "note") &&
                        (
                            item.tags.any { it.normalizedName.startsWith("rule_") } ||
                                item.tags.any {
                                    it.normalizedName in
                                        setOf(
                                            "principle",
                                            "operating_principle",
                                        )
                                } ||
                                item.node.noteType in setOf("principle", "rule")
                        )
                }.sortedByDescending { it.node.updatedAt }

        fun byTag(tag: String): List<NodeWithPin> = activeRules.filter { item -> item.tags.any { it.normalizedName == tag } }

        val antiGoals = byTag("rule_anti_goal")
        val redFlags = byTag("rule_red_flag")
        val greenFlags = byTag("rule_green_flag")
        val priorities = byTag("rule_priority")
        val tendToForget = byTag("rule_tend_to_forget")
        val messesMeUp = byTag("rule_messes_me_up")
        val helpsOffBalance = byTag("rule_helps_off_balance")
        val decisionPrinciples = byTag("rule_decision_principle")
        val constraints = byTag("rule_constraint")
        val foundationalRules = byTag("rule_foundational")
        val recoveryReminders = byTag("rule_recovery_reminder")
        val distrustBrainNotes = byTag("rule_distrust_brain")
        val whatWorksNotes = byTag("rule_what_works")
        val pinnedPrinciples = activeRules.filter { it.node.isPinned }
        val playbookLinksCount =
            relations.count { relation ->
                relation.relationType == "PRINCIPLE_FOR_PLAYBOOK" ||
                    relation.relationType == "PLAYBOOK_SUPPORTS_PRINCIPLE"
            }

        return PersonalRulesSnapshot(
            vault = activeRules,
            antiGoals = antiGoals,
            redFlags = redFlags,
            greenFlags = greenFlags,
            priorities = priorities,
            tendToForget = tendToForget,
            messesMeUp = messesMeUp,
            helpsOffBalance = helpsOffBalance,
            decisionPrinciples = decisionPrinciples,
            constraints = constraints,
            foundationalRules = foundationalRules,
            recoveryReminders = recoveryReminders,
            distrustBrainNotes = distrustBrainNotes,
            whatWorksNotes = whatWorksNotes,
            pinnedPrinciples = pinnedPrinciples,
            playbookLinksCount = playbookLinksCount,
        )
    }

    private fun calculateVaultsSnapshot(nodes: List<NodeWithPin>): VaultsSnapshot {
        val active = nodes.filter { it.node.status == "active" }

        fun hasTag(
            node: NodeWithPin,
            tag: String,
        ): Boolean = node.tags.any { it.normalizedName == tag }

        val documentVault =
            active.filter {
                it.node.type in setOf("document", "vault", "note", "resource") &&
                    (hasTag(it, "vault_document") || it.node.type == "document")
            }
        val importantLinksVault =
            active.filter {
                it.node.type in setOf("resource", "note", "vault") &&
                    (hasTag(it, "vault_links") || it.node.mediaType == "link")
            }
        val medicalInfoVault =
            active.filter {
                hasTag(it, "vault_medical") || it.node.title.contains("medical", ignoreCase = true)
            }
        val universityInfoVault =
            active.filter {
                hasTag(it, "vault_university") ||
                    it.node.title.contains("university", ignoreCase = true) ||
                    it.node.title.contains("campus", ignoreCase = true)
            }
        val idsAndFormsVault =
            active.filter {
                hasTag(it, "vault_ids_forms") ||
                    it.node.title.contains("id", ignoreCase = true) ||
                    it.node.title.contains("form", ignoreCase = true)
            }
        val applicationStatusTracking =
            active.filter {
                hasTag(it, "vault_application_status") ||
                    it.node.title.contains("application status", ignoreCase = true)
            }
        val receiptsPaperwork =
            active.filter {
                hasTag(it, "vault_receipts_paperwork") ||
                    it.node.title.contains("receipt", ignoreCase = true) ||
                    it.node.title.contains("paperwork", ignoreCase = true)
            }
        val accountReferenceVault =
            active.filter {
                hasTag(it, "vault_account_reference") ||
                    it.node.title.contains("account", ignoreCase = true) ||
                    it.node.title.contains("reference", ignoreCase = true)
            }
        val officialDeadlineReminders =
            active
                .filter {
                    it.node.dueAt != null &&
                        (
                            hasTag(it, "vault_official_deadline") ||
                                it.node.type in setOf("document", "maintenance") ||
                                it.node.title.contains("deadline", ignoreCase = true)
                        )
                }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }
        val mustFindLater =
            active
                .filter {
                    hasTag(it, "must_find_later") || it.node.isPinned
                }.sortedByDescending { it.node.updatedAt }

        return VaultsSnapshot(
            documentVault = documentVault,
            importantLinksVault = importantLinksVault,
            medicalInfoVault = medicalInfoVault,
            universityInfoVault = universityInfoVault,
            idsAndFormsVault = idsAndFormsVault,
            applicationStatusTracking = applicationStatusTracking,
            receiptsPaperwork = receiptsPaperwork,
            accountReferenceVault = accountReferenceVault,
            officialDeadlineReminders = officialDeadlineReminders,
            mustFindLater = mustFindLater,
        )
    }

    private fun calculateCapacitySnapshot(
        nodes: List<NodeWithPin>,
        projects: List<NodeEntity>,
        areas: List<NodeEntity>,
        maintenance: MaintenanceSnapshot,
        openLoops: OpenLoopsSnapshot,
        trackEntries: List<TrackEntryEntity>,
        currentMode: ModeEntity?,
        allModes: List<ModeEntity>,
    ): CapacitySnapshot {
        val now = Clock.System.now().toEpochMilliseconds()
        val weekMs = 7L * 24 * 60 * 60 * 1000
        val activeTasks = nodes.filter { it.node.type == "task" && it.node.status == "active" }
        val activeProjects =
            projects.filter { it.status == "active" || it.projectStatus == "active" }
        val overdueCount =
            nodes.count { it.node.status == "active" && (it.node.dueAt ?: Long.MAX_VALUE) < now }
        val loadScore =
            (
                (activeTasks.size * 2) +
                    (openLoops.active.size * 3) +
                    (maintenance.overdue.size * 4) +
                    (overdueCount * 5)
            ).coerceIn(0, 100)
        val fragmentationScore =
            (
                activeTasks.groupBy { it.node.projectId }.size * 7 +
                    activeTasks.groupBy { it.node.areaId }.size * 4
            ).coerceIn(0, 100)

        val tooManyActiveProjectsWarning =
            if (activeProjects.size >= 7) "TOO MANY ACTIVE PROJECTS // REDUCE CONCURRENT FRONTS" else null
        val adminDebtWarning = maintenance.overdueWarning
        val openLoopsOverloadWarning = openLoops.overloadWarning
        val capacityMismatch =
            if (loadScore >= 75 && (currentMode?.key == "FOCUS" || currentMode?.key == "DEEP_WORK")) {
                "CAPACITY MISMATCH // CURRENT MODE TOO AMBITIOUS FOR LOAD"
            } else if (loadScore <= 35 && currentMode?.key == "RECOVERY") {
                "CAPACITY MISMATCH // YOU CAN SAFELY SHIFT TO EXECUTION MODE"
            } else {
                null
            }

        val weeklyCreatedActive =
            nodes.count { it.node.status == "active" && it.node.createdAt >= now - weekMs }
        val weeklyDone =
            nodes.count { it.node.status == "done" && (it.node.completedAt ?: 0L) >= now - weekMs }
        val unrealisticWeekSignal =
            if (weeklyCreatedActive > weeklyDone * 2 + 5) "THIS WEEK IS UNREALISTIC // INTAKE OUTPACES EXECUTION" else null
        val tooManyActiveFrontsIndicator =
            if (activeTasks.groupBy { it.node.areaId }.size >= 6) "TOO MANY ACTIVE FRONTS" else null
        val attentionFragmentedIndicator =
            if (fragmentationScore >= 55) "ATTENTION IS TOO FRAGMENTED" else null
        val weeklyStructuralOverloadWarning =
            if (loadScore >= 80 && fragmentationScore >= 60) "WEEKLY STRUCTURAL OVERLOAD DETECTED" else null

        val loadByArea =
            areas
                .associate { area ->
                    val areaTasks = activeTasks.count { it.node.areaId == area.id }
                    val areaOpenLoops = openLoops.active.count { it.node.node.areaId == area.id }
                    val areaOverdue =
                        nodes.count {
                            it.node.areaId == area.id &&
                                it.node.status == "active" &&
                                (it.node.dueAt ?: Long.MAX_VALUE) < now
                        }
                    area.id to
                        ((areaTasks * 2) + (areaOpenLoops * 3) + (areaOverdue * 5)).coerceIn(
                            0,
                            100,
                        )
                }.toMutableMap<Long?, Int>()
        val unassignedLoad =
            (
                activeTasks.count { it.node.areaId == null } * 2 +
                    openLoops.active.count { it.node.node.areaId == null } * 3
            ).coerceIn(0, 100)
        loadByArea[null] = unassignedLoad

        fun modeLoadForKey(key: String): Int =
            when (key)
            {
                "FOCUS", "DEEP_WORK" -> {
                    (loadScore + fragmentationScore / 2).coerceIn(
                        0,
                        100,
                    )
                }

                "RECOVERY", "LOW_BATTERY", "CANT_THINK" -> {
                    (loadScore - 15).coerceAtLeast(0)
                }

                "ADMIN" -> {
                    (maintenance.adminDebtMeter + loadScore / 4).coerceIn(
                        0,
                        100,
                    )
                }

                "SOCIAL" -> {
                    (openLoops.active.size * 6).coerceIn(
                        0,
                        100,
                    )
                }

                else -> {
                    loadScore
                }
            }

        val loadByMode =
            allModes.associate { mode ->
                mode.key to modeLoadForKey(mode.key)
            }

        val trendBuckets =
            (0..3)
                .map { index ->
                    val bucketEnd = now - (index * weekMs)
                    val bucketStart = bucketEnd - weekMs
                    val entry =
                        trackEntries
                            .filter { it.createdAt in bucketStart..bucketEnd }
                            .maxByOrNull { it.createdAt }
                    val fallbackLoad =
                        (
                            nodes.count { it.node.status == "active" && it.node.createdAt <= bucketEnd } * 2 +
                                nodes.count {
                                    it.node.status == "active" && (
                                        it.node.dueAt
                                            ?: Long.MAX_VALUE
                                    ) < bucketEnd
                                } * 3
                        ).coerceIn(0, 100)
                    val fallbackFrag =
                        nodes
                            .filter { it.node.status == "active" && it.node.createdAt <= bucketEnd }
                            .groupBy { it.node.projectId }
                            .size
                            .times(8)
                            .coerceIn(0, 100)
                    LoadTrendPoint(
                        label = "W-${index + 1}",
                        load = entry?.loadScore ?: fallbackLoad,
                        fragmentation = entry?.fragmentationScore ?: fallbackFrag,
                    )
                }.reversed()

        val suggestions =
            buildList {
                if (loadScore >= 75) add("Reduce new intake and close open loops before adding new projects.")
                if (fragmentationScore >= 55) add("Batch by area/context to reduce switching cost.")
                if (maintenance.adminDebtMeter >= 60) add("Run a focused admin block to cut maintenance debt.")
                if (openLoops.active.size >= 10) add("Schedule an open-loop review sweep today.")
                if (tooManyActiveProjectsWarning != null) add("Freeze or park at least one active project.")
            }

        return CapacitySnapshot(
            loadScore = loadScore,
            fragmentationScore = fragmentationScore,
            tooManyActiveProjectsWarning = tooManyActiveProjectsWarning,
            adminDebtWarning = adminDebtWarning,
            openLoopsOverloadWarning = openLoopsOverloadWarning,
            capacityMismatch = capacityMismatch,
            unrealisticWeekSignal = unrealisticWeekSignal,
            tooManyActiveFrontsIndicator = tooManyActiveFrontsIndicator,
            attentionFragmentedIndicator = attentionFragmentedIndicator,
            weeklyStructuralOverloadWarning = weeklyStructuralOverloadWarning,
            loadByArea = loadByArea,
            loadByMode = loadByMode,
            loadTrend = trendBuckets,
            capacityAwareSuggestions = suggestions,
        )
    }

    private fun calculateLifeOSSignatureSnapshot(
        modes: List<ModeEntity>,
        areaHealth: AreaHealthSnapshot,
        openLoops: OpenLoopsSnapshot,
        pendingDecisions: List<NodeWithPin>,
        maintenance: MaintenanceSnapshot,
        relationships: RelationshipSnapshot,
        vaults: VaultsSnapshot,
        capacity: CapacitySnapshot,
        playbooks: PlaybookSnapshot,
        currentMode: ModeEntity?,
        trackEntries: List<TrackEntryEntity>,
        nodes: List<NodeWithPin>,
    ): LifeOSSignatureSnapshot {
        val dueTasks =
            nodes.filter { it.node.type == "task" && it.node.status == "active" && it.node.dueAt != null }
        val withWorkDate = dueTasks.filter { it.node.startAt != null }
        val coverage =
            if (dueTasks.isEmpty()) 100 else ((withWorkDate.size * 100.0) / dueTasks.size).toInt()
        val workDateDueItems =
            dueTasks
                .filter { it.node.startAt == null }
                .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }
                .take(10)

        val today =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString()
        val latestEntry = trackEntries.filter { it.date == today }.maxByOrNull { it.createdAt }
        val energy = latestEntry?.energyScore ?: 3
        val anxiety = latestEntry?.anxietyScore ?: 2

        val modeOfLife =
            when
                {
                    anxiety >= 4 || energy <= 2 || currentMode?.key in
                        setOf(
                            "RECOVERY",
                            "LOW_BATTERY",
                            "CANT_THINK",
                        )
                    -> {
                        "stabilization"
                    }

                    capacity.loadScore >= 80 && capacity.fragmentationScore >= 60 -> {
                        "firefighting"
                    }

                    currentMode?.key in
                        setOf(
                            "FOCUS",
                            "DEEP_WORK",
                            "STUDY",
                        ) && capacity.loadScore < 75 -> {
                        "execution"
                    }

                    currentMode?.key == "SOCIAL" -> {
                        "relationship_maintenance"
                    }

                    currentMode?.key == "ADMIN" -> {
                        "admin_control"
                    }

                    else -> {
                        "navigation"
                    }
                }
        val modeReason =
            when (modeOfLife)
            {
                "stabilization" -> "Energy/anxiety profile points to recovery-first operations."
                "firefighting" -> "High load and fragmentation indicate active overload response."
                "execution" -> "Current mode and capacity suggest focused output window."
                "relationship_maintenance" -> "Social mode active; prioritize people and follow-ups."
                "admin_control" -> "Admin mode active; prioritize forms, renewals, and debt cleanup."
                else -> "System is in adaptive navigation mode."
            }

        return LifeOSSignatureSnapshot(
            operatingModesEnabled = modes.isNotEmpty(),
            areaHealthEnabled = areaHealth.areas.isNotEmpty(),
            openLoopsEnabled = openLoops.active.isNotEmpty() || openLoops.resolved.isNotEmpty(),
            decisionSystemEnabled = pendingDecisions.isNotEmpty() || nodes.any { it.node.type == "decision" },
            maintenanceEnabled = maintenance.active.isNotEmpty() || maintenance.overdue.isNotEmpty(),
            contextAwareFilteringEnabled =
                nodes.any {
                    it.node.locationContext != null ||
                        it.node.energyContext != null ||
                        it.node.deviceContext != null ||
                        it.node.socialContext != null ||
                        it.node.timeWindowContext != null
                },
            transitionProtocolsEnabled = playbooks.templates.isNotEmpty(),
            recoveryModeEnabled =
                modes.any {
                    it.key in
                        setOf(
                            "RECOVERY",
                            "LOW_BATTERY",
                            "CANT_THINK",
                        )
                },
            relationshipLayerEnabled = relationships.people.isNotEmpty() || relationships.replyQueue.isNotEmpty(),
            logisticsVaultEnabled =
                vaults.documentVault.isNotEmpty() ||
                    vaults.importantLinksVault.isNotEmpty() ||
                    vaults.mustFindLater.isNotEmpty(),
            loadCapacityEnabled = capacity.loadScore > 0 || capacity.fragmentationScore > 0,
            personalPrinciplesPlaybooksEnabled =
                nodes.any { it.node.type in setOf("rule", "principle") } &&
                    playbooks.playbooks.isNotEmpty(),
            modeOfLifeLabel = modeOfLife,
            modeOfLifeReason = modeReason,
            workDateDueCoveragePercent = coverage,
            workDateDueItems = workDateDueItems,
        )
    }

    private fun calculateLifeOSSecondBrainSnapshot(
        nodes: List<NodeWithPin>,
        relations: List<RelationEntity>,
        dashboard: DashboardUIState,
        areaHealth: AreaHealthSnapshot,
        openLoops: OpenLoopsSnapshot,
        maintenance: MaintenanceSnapshot,
        capacity: CapacitySnapshot,
        protocols: TransitionProtocolsSnapshot,
        playbooks: PlaybookSnapshot,
        currentMode: ModeEntity?,
        signature: LifeOSSignatureSnapshot,
        vaults: VaultsSnapshot,
    ): LifeOSSecondBrainSnapshot {
        val knowledgeCount =
            nodes.count {
                it.node.status == "active" &&
                    it.node.type in setOf("note", "idea", "resource")
            }
        val savedCount = nodes.size
        val connectedIds =
            relations
                .flatMap { listOf(it.fromNodeId, it.toNodeId) }
                .toSet()
                .intersect(nodes.map { it.node.id }.toSet())
        val findLaterCount =
            vaults.documentVault.size +
                vaults.importantLinksVault.size +
                vaults.mustFindLater.size

        val secondBrain =
            listOf(
                DistinctionQuestionState(
                    question = "What do I know?",
                    answer =
                        if (knowledgeCount > 0) {
                            "$knowledgeCount active knowledge nodes (notes, ideas, resources)."
                        } else {
                            "Knowledge layer is empty; capture notes/resources first."
                        },
                    answered = knowledgeCount > 0,
                ),
                DistinctionQuestionState(
                    question = "What did I save?",
                    answer =
                        if (savedCount > 0) {
                            "$savedCount nodes stored across memory and action layers."
                        } else {
                            "No saved nodes yet."
                        },
                    answered = savedCount > 0,
                ),
                DistinctionQuestionState(
                    question = "What is this connected to?",
                    answer =
                        if (relations.isNotEmpty()) {
                            "${relations.size} links across ${connectedIds.size} connected nodes."
                        } else {
                            "No explicit links yet; relation graph needs wiring."
                        },
                    answered = relations.isNotEmpty(),
                ),
                DistinctionQuestionState(
                    question = "Where can I find this later?",
                    answer =
                        if (findLaterCount > 0) {
                            "$findLaterCount entries in vault-focused find-later storage."
                        } else {
                            "Find-later vault is empty; add key docs/links for retrieval."
                        },
                    answered = findLaterCount > 0,
                ),
            )

        val pressureArea =
            areaHealth.areas
                .sortedByDescending { metrics ->
                    when (metrics.status)
                    {
                        "on_fire" -> 5
                        "overloaded" -> 4
                        "neglected" -> 3
                        "active" -> 2
                        else -> 1
                    }
                }.firstOrNull()

        val nowAction =
            when
                {
                    dashboard.overdueNodes.isNotEmpty() -> {
                        "Handle overdue first: ${dashboard.overdueNodes.first().node.title}."
                    }

                    dashboard.upcomingDeadlines.isNotEmpty() -> {
                        "Advance the nearest deadline: ${dashboard.upcomingDeadlines.first().node.title}."
                    }

                    dashboard.suggestedContextTasks.isNotEmpty() -> {
                        "Run a context batch (${dashboard.suggestedContextKey ?: "current context"})."
                    }

                    openLoops.review.isNotEmpty() -> {
                        "Review and close stale open loops."
                    }

                    else -> {
                        "Run a short maintenance sweep and pick one next task."
                    }
                }

        val decayingSignals =
            buildList {
                if (openLoops.review.isNotEmpty()) add("${openLoops.review.size} open loops waiting review")
                if (maintenance.overdue.isNotEmpty()) add("${maintenance.overdue.size} overdue maintenance items")
                if (areaHealth.disappearingAreaIds.isNotEmpty()) add("${areaHealth.disappearingAreaIds.size} areas fading from radar")
            }
        val parkedCount =
            nodes.count {
                it.node.status in setOf("on_hold", "someday")
            }

        val lifeOS =
            listOf(
                DistinctionQuestionState(
                    question = "What should happen now?",
                    answer = nowAction,
                    answered = true,
                ),
                DistinctionQuestionState(
                    question = "What part of life needs attention?",
                    answer =
                        pressureArea?.let {
                            "${it.areaTitle} is ${
                                it.status.replace(
                                    '_',
                                    ' ',
                                )
                            } (stress ${it.stressLoad})."
                        } ?: "No area pressure signal yet.",
                    answered = pressureArea != null,
                ),
                DistinctionQuestionState(
                    question = "What am I carrying?",
                    answer =
                        "Load ${capacity.loadScore}, fragmentation ${capacity.fragmentationScore}, open loops ${openLoops.active.size}, decisions ${dashboard.pendingDecisions.size}, maintenance ${maintenance.active.size}.",
                    answered = true,
                ),
                DistinctionQuestionState(
                    question = "What is decaying?",
                    answer =
                        if (decayingSignals.isNotEmpty()) {
                            decayingSignals.joinToString(" • ")
                        } else {
                            "No acute decay signal right now."
                        },
                    answered = decayingSignals.isNotEmpty(),
                ),
                DistinctionQuestionState(
                    question = "What mode am I in?",
                    answer =
                        "Mode ${(currentMode?.key ?: "UNSET")} • life posture ${signature.modeOfLifeLabel.uppercase()}.",
                    answered = currentMode != null,
                ),
                DistinctionQuestionState(
                    question = "What protocol helps here?",
                    answer =
                        protocols.recommendedLabel?.let { "Run protocol: $it." }
                            ?: playbooks.suggestedPlaybookLabel?.let { "Run playbook: $it." }
                            ?: "No protocol suggestion available; use a short reset protocol.",
                    answered = protocols.recommendedLabel != null || playbooks.suggestedPlaybookLabel != null,
                ),
                DistinctionQuestionState(
                    question = "What can I safely ignore?",
                    answer =
                        if (parkedCount > 0) {
                            "$parkedCount parked items (on_hold/someday) can stay deferred for now."
                        } else {
                            "No parked buffer; consider parking low-priority work."
                        },
                    answered = parkedCount > 0,
                ),
                DistinctionQuestionState(
                    question = "How do I move through today without dropping everything?",
                    answer =
                        "Use mode ${currentMode?.key ?: "NAVIGATION"}, execute one protocol, and keep focus on one overdue/deadline cluster.",
                    answered = true,
                ),
            )

        val secondCoverage =
            if (secondBrain.isEmpty()) 0 else ((secondBrain.count { it.answered } * 100.0) / secondBrain.size).toInt()
        val lifeCoverage =
            if (lifeOS.isEmpty()) 0 else ((lifeOS.count { it.answered } * 100.0) / lifeOS.size).toInt()
        val posture =
            when
                {
                    secondCoverage >= 75 && lifeCoverage >= 75 -> "balanced_hybrid"
                    secondCoverage > lifeCoverage -> "memory_heavy"
                    lifeCoverage > secondCoverage -> "operations_heavy"
                    else -> "underconfigured"
                }

        return LifeOSSecondBrainSnapshot(
            secondBrainQuestions = secondBrain,
            lifeOSQuestions = lifeOS,
            secondBrainCoveragePercent = secondCoverage,
            lifeOSCoveragePercent = lifeCoverage,
            postureLabel = posture,
        )
    }

    private fun calculateCombinedDirectionSnapshot(
        distinction: LifeOSSecondBrainSnapshot,
        signature: LifeOSSignatureSnapshot,
        dashboard: DashboardUIState,
        logistics: PhysicalLogisticsSnapshot,
        capacity: CapacitySnapshot,
        relationships: RelationshipSnapshot,
        protocols: TransitionProtocolsSnapshot,
        maintenance: MaintenanceSnapshot,
        openLoops: OpenLoopsSnapshot,
    ): CombinedDirectionSnapshot {
        val storageReady =
            distinction.secondBrainCoveragePercent >= 75 &&
                dashboard.notesCount > 0
        val lifeOsShellReady =
            signature.operatingModesEnabled &&
                signature.maintenanceEnabled &&
                signature.transitionProtocolsEnabled
        val rememberLifeReady =
            distinction.secondBrainCoveragePercent >= 75 &&
                relationships.people.isNotEmpty()
        val runLifeReady =
            distinction.lifeOSCoveragePercent >= 75 &&
                (dashboard.upcomingDeadlines.isNotEmpty() || dashboard.suggestedContextTasks.isNotEmpty())
        val recoveryReady =
            signature.recoveryModeEnabled &&
                (protocols.templates.any { it.key.contains("recovery") } || maintenance.overdue.isNotEmpty())
        val practicalMotionReady =
            signature.contextAwareFilteringEnabled &&
                (
                    logistics.placeBasedTasks.isNotEmpty() ||
                        logistics.whatToBringLists.isNotEmpty() ||
                        logistics.leaveHomeChecklists.isNotEmpty()
                )

        val commitments =
            listOf(
                DirectionCommitmentStatus(
                    commitment = "Keep the Second Brain layer for storage, notes, connections, and memory",
                    satisfied = storageReady,
                    evidence =
                        "Second Brain coverage ${distinction.secondBrainCoveragePercent}% • Notes ${dashboard.notesCount} • Relations context ${
                            if (distinction.secondBrainQuestions
                                    .any { it.question == "What is this connected to?" && it.answered }
                            )
                                {
                                    "present"
                                } else {
                                "missing"
                            }
                        }",
                ),
                DirectionCommitmentStatus(
                    commitment = "Wrap it in a LifeOS shell for modes, maintenance, transitions, and action",
                    satisfied = lifeOsShellReady,
                    evidence =
                        "Modes ${if (signature.operatingModesEnabled) "on" else "off"} • Maintenance ${if (signature.maintenanceEnabled) "on" else "off"} • Protocols ${if (signature.transitionProtocolsEnabled) "on" else "off"}",
                ),
                DirectionCommitmentStatus(
                    commitment = "Make TajOS remember life",
                    satisfied = rememberLifeReady,
                    evidence =
                        "Second Brain coverage ${distinction.secondBrainCoveragePercent}% • Relationship records ${relationships.people.size}",
                ),
                DirectionCommitmentStatus(
                    commitment = "Make TajOS help run life",
                    satisfied = runLifeReady,
                    evidence =
                        "LifeOS coverage ${distinction.lifeOSCoveragePercent}% • Next-action signals ${dashboard.upcomingDeadlines.size + dashboard.suggestedContextTasks.size}",
                ),
                DirectionCommitmentStatus(
                    commitment = "Make TajOS help recover from derailment",
                    satisfied = recoveryReady,
                    evidence =
                        "Recovery modes ${if (signature.recoveryModeEnabled) "available" else "missing"} • Recovery protocol ${
                            if (protocols.templates.any {
                                    it.key.contains(
                                        "recovery",
                                    )
                                }
                            )
                                {
                                    "available"
                                } else {
                                "not detected"
                            }
                        }",
                ),
                DirectionCommitmentStatus(
                    commitment = "Make TajOS practical in real-world motion, not only inside neat dashboards",
                    satisfied = practicalMotionReady,
                    evidence =
                        "Context filtering ${if (signature.contextAwareFilteringEnabled) "on" else "off"} • Place/bring signals ${logistics.placeBasedTasks.size + logistics.whatToBringLists.size + logistics.leaveHomeChecklists.size}",
                ),
            )

        val completion =
            if (commitments.isEmpty()) 0 else ((commitments.count { it.satisfied } * 100.0) / commitments.size).toInt()

        val practicalitySignals =
            buildList {
                if (logistics.placeBasedTasks.isNotEmpty()) add("${logistics.placeBasedTasks.size} place-based tasks")
                if (logistics.leaveHomeChecklists.isNotEmpty()) add("${logistics.leaveHomeChecklists.size} leave-home checklist items")
                if (dashboard.suggestedContextTasks.isNotEmpty()) add("${dashboard.suggestedContextTasks.size} context-suggested tasks")
                if (openLoops.review.isNotEmpty()) add("${openLoops.review.size} loops pending review")
                if (capacity.capacityAwareSuggestions.isNotEmpty()) add(capacity.capacityAwareSuggestions.first())
            }

        val posture =
            when
                {
                    completion >= 85 -> "ready_to_ship"
                    completion >= 60 -> "mostly_operational"
                    else -> "underconfigured"
                }

        return CombinedDirectionSnapshot(
            commitments = commitments,
            completionPercent = completion,
            practicalitySignals = practicalitySignals,
            postureLabel = posture,
        )
    }

    private fun calculateCoreLifeOSShiftSnapshot(
        distinction: LifeOSSecondBrainSnapshot,
        signature: LifeOSSignatureSnapshot,
        direction: CombinedDirectionSnapshot,
        dashboard: DashboardUIState,
        time: TimeArchitectureSnapshot,
        areaHealth: AreaHealthSnapshot,
        openLoops: OpenLoopsSnapshot,
        maintenance: MaintenanceSnapshot,
        protocols: TransitionProtocolsSnapshot,
        capacity: CapacitySnapshot,
        currentMode: ModeEntity?,
    ): CoreLifeOSShiftSnapshot {
        val operatingLayerReady =
            direction.commitments.any {
                it.commitment == "Keep the Second Brain layer for storage, notes, connections, and memory" && it.satisfied
            } &&
                direction.commitments.any {
                    it.commitment == "Make TajOS help run life" && it.satisfied
                } &&
                distinction.postureLabel != "memory_heavy"

        val lifeInMotionReady =
            time.todayLayer.isNotEmpty() ||
                time.weekLayer.isNotEmpty() ||
                dashboard.suggestedContextTasks.isNotEmpty()

        val stateContextModeReady =
            signature.operatingModesEnabled &&
                signature.contextAwareFilteringEnabled &&
                (currentMode != null || dashboard.modeSuggestion != null)

        val transitionsReady =
            signature.transitionProtocolsEnabled &&
                (protocols.protocols.isNotEmpty() || protocols.templates.isNotEmpty())

        val decayOverloadTrackingReady =
            areaHealth.areas.any { it.status in setOf("neglected", "overloaded", "on_fire") } ||
                openLoops.review.isNotEmpty() ||
                maintenance.overdue.isNotEmpty() ||
                capacity.openLoopsOverloadWarning != null ||
                capacity.adminDebtWarning != null

        val moveThroughTimeReady =
            time.todayLayer.isNotEmpty() &&
                (time.weekLayer.isNotEmpty() || time.monthLayer.isNotEmpty() || time.countdowns.isNotEmpty())

        val items =
            listOf(
                CoreLifeOSShiftItem(
                    criterion = "Treat TajOS as a personal operating layer, not only a storage system",
                    satisfied = operatingLayerReady,
                    evidence =
                        "Direction ${direction.completionPercent}% • posture ${distinction.postureLabel} • LifeOS coverage ${distinction.lifeOSCoveragePercent}%",
                ),
                CoreLifeOSShiftItem(
                    criterion = "Build TajOS to understand life in motion, not just static information",
                    satisfied = lifeInMotionReady,
                    evidence =
                        "Today ${time.todayLayer.size} • Week ${time.weekLayer.size} • Context suggestions ${dashboard.suggestedContextTasks.size}",
                ),
                CoreLifeOSShiftItem(
                    criterion = "Make TajOS state-aware, context-aware, and mode-aware",
                    satisfied = stateContextModeReady,
                    evidence =
                        "Mode ${currentMode?.key ?: "unset"} • Context filter ${if (signature.contextAwareFilteringEnabled) "on" else "off"} • Mode suggestion ${dashboard.modeSuggestion ?: "none"}",
                ),
                CoreLifeOSShiftItem(
                    criterion = "Make TajOS support real-life transitions, not just pages and tasks",
                    satisfied = transitionsReady,
                    evidence =
                        "Protocols active ${protocols.protocols.size} • templates ${protocols.templates.size}",
                ),
                CoreLifeOSShiftItem(
                    criterion = "Make TajOS track what is decaying, neglected, or overloaded",
                    satisfied = decayOverloadTrackingReady,
                    evidence =
                        "Area alerts ${
                            areaHealth.areas.count {
                                it.status in
                                    setOf(
                                        "neglected",
                                        "overloaded",
                                        "on_fire",
                                    )
                            }
                        } • Loop review ${openLoops.review.size} • Overdue maintenance ${maintenance.overdue.size}",
                ),
                CoreLifeOSShiftItem(
                    criterion = "Make TajOS help the user move through time, not just save information in place",
                    satisfied = moveThroughTimeReady,
                    evidence =
                        "Today ${time.todayLayer.size} • Week ${time.weekLayer.size} • Month ${time.monthLayer.size} • Countdowns ${time.countdowns.size}",
                ),
            )

        val completion =
            if (items.isEmpty()) 0 else ((items.count { it.satisfied } * 100.0) / items.size).toInt()
        val connectedProperly = completion >= 100 && direction.completionPercent >= 100
        val warning =
            if (connectedProperly) {
                null
            } else {
                "Some Core LifeOS Shift criteria are not fully satisfied or not fully integrated yet."
            }

        return CoreLifeOSShiftSnapshot(
            items = items,
            completionPercent = completion,
            connectedProperly = connectedProperly,
            integrationWarning = warning,
        )
    }

    private fun calculateStudentBoardState(
        nodes: List<NodeWithPin>,
        relations: List<RelationEntity>,
        sessions: List<FocusSessionEntity>,
        templates: List<TemplateEntity>,
    ): StudentBoardState {
        val now = Clock.System.now().toEpochMilliseconds()
        val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)
        val activeNodes = nodes.filter { it.node.status == "active" }
        val activeById = activeNodes.associateBy { it.node.id }

        fun NodeWithPin.hasTag(tag: String): Boolean = tags.any { it.normalizedName == tag.lowercase() }

        fun NodeWithPin.student(): StudentMetadata? = node.metadataEnvelopeOrNull()?.student

        val assignmentTracker =
            activeNodes
                .filter { item ->
                    item.node.type == "task" && (
                        item.student()?.assignmentType != null ||
                            item.hasTag("assignment")
                    )
                }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

        val examNodes =
            activeNodes
                .filter { item ->
                    item.node.dueAt != null && (
                        item.hasTag("exam") ||
                            item.student()?.assignmentType.equals(
                                "exam",
                                ignoreCase = true,
                            ) ||
                            item.node.title.contains("exam", ignoreCase = true)
                    )
                }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

        val examCountdownNode = examNodes.firstOrNull()
        val examCountdownDays =
            examCountdownNode?.node?.dueAt?.let { due ->
                (((due - now).coerceAtLeast(0L)) / (24 * 60 * 60 * 1000L))
            }

        val examPrepBoard =
            activeNodes
                .filter {
                    it.hasTag("exam_prep") ||
                        it.hasTag("revisit_before_exam") ||
                        it.student()?.revisitBeforeExam == true
                }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

        val psychologyConceptMaps =
            activeNodes.filter {
                it.node.type == "note" &&
                    it.node.noteType == "concept" &&
                    it.hasTag("psychology")
            }

        val glossaryCards =
            activeNodes.filter {
                it.node.type == "note" &&
                    (it.hasTag("glossary") || it.hasTag("knowledge_card") || it.node.noteType == "concept")
            }

        val researchIdeaVault =
            activeNodes.filter {
                it.node.type == "idea" &&
                    (it.hasTag("research") || it.hasTag("research_idea") || it.node.noteType == "research")
            }

        val quoteBank =
            activeNodes.filter {
                it.node.type == "note" && it.node.noteType == "quote"
            }

        val caseReflectionNotes =
            activeNodes.filter {
                it.node.type == "note" &&
                    (it.node.noteType == "reflection" || it.hasTag("case_study") || it.hasTag("reflection"))
            }

        val readingBacklog =
            activeNodes
                .filter {
                    it.node.type == "note" &&
                        (it.node.noteType == "reading" || it.hasTag("reading"))
                }.sortedByDescending { it.node.updatedAt }

        val readingProgress =
            readingBacklog
                .mapNotNull { note ->
                    note.student()?.readingProgressPercent?.let { progress ->
                        StudentProgressItem(
                            node = note,
                            progressPercent = progress.coerceIn(0, 100),
                        )
                    }
                }.sortedByDescending { it.progressPercent }

        val assignmentDeadlines =
            assignmentTracker
                .filter { it.node.dueAt != null }
                .sortedBy { it.node.dueAt ?: Long.MAX_VALUE }
                .take(8)

        val revisitBeforeExam =
            activeNodes
                .filter {
                    it.hasTag("revisit_before_exam") || it.student()?.revisitBeforeExam == true
                }.sortedBy { it.node.dueAt ?: Long.MAX_VALUE }

        val topicMastery =
            activeNodes
                .mapNotNull { item ->
                    val student = item.student() ?: return@mapNotNull null
                    val mastery = student.masteryPercent ?: return@mapNotNull null
                    val topic = student.topic ?: item.node.title
                    StudentMasteryItem(
                        node = item,
                        topic = topic,
                        masteryPercent = mastery.coerceIn(0, 100),
                    )
                }.sortedByDescending { it.masteryPercent }

        val byCourse =
            activeNodes
                .mapNotNull { item ->
                    val student = item.student() ?: return@mapNotNull null
                    val courseId = student.courseId ?: return@mapNotNull null
                    Triple(courseId, student, item)
                }.groupBy { it.first }

        val courseDashboard =
            byCourse
                .map { (courseId, entries) ->
                    val courseName =
                        entries.firstNotNullOfOrNull { it.second.courseName }
                            ?: courseId
                    val openAssignments =
                        entries.count {
                            it.third.node.type == "task" &&
                                it.third.node.status == "active" &&
                                it.second.assignmentType != null
                        }
                    val upcomingExams =
                        entries.count {
                            (
                                it.third.hasTag("exam") ||
                                    it.second.assignmentType.equals(
                                        "exam",
                                        ignoreCase = true,
                                    )
                            ) &&
                                (it.third.node.dueAt ?: Long.MAX_VALUE) >= now
                        }
                    val masteryValues =
                        entries.mapNotNull { it.second.masteryPercent }.map { it.coerceIn(0, 100) }
                    StudentCourseSummary(
                        courseId = courseId,
                        courseName = courseName,
                        semester = entries.firstNotNullOfOrNull { it.second.semester },
                        openAssignments = openAssignments,
                        upcomingExams = upcomingExams,
                        avgMasteryPercent =
                            if (masteryValues.isNotEmpty()) {
                                masteryValues
                                    .average()
                                    .toInt()
                            } else {
                                null
                            },
                    )
                }.sortedBy { it.courseName.lowercase() }

        val bySemester =
            activeNodes
                .mapNotNull { item ->
                    val student = item.student() ?: return@mapNotNull null
                    val semester = student.semester ?: return@mapNotNull null
                    semester to item
                }.groupBy { it.first }

        val semesterDashboard =
            bySemester
                .map { (semester, entries) ->
                    val semesterNodes = entries.map { it.second }
                    val courseCount =
                        semesterNodes.mapNotNull { it.student()?.courseId }.distinct().size
                    val openAssignments =
                        semesterNodes.count { it.node.type == "task" && it.student()?.assignmentType != null }
                    val upcomingExams =
                        semesterNodes.count {
                            it.node.dueAt != null &&
                                (
                                    it.hasTag("exam") ||
                                        it.student()?.assignmentType.equals(
                                            "exam",
                                            ignoreCase = true,
                                        )
                                )
                        }
                    val dueSoon =
                        semesterNodes.count {
                            val due = it.node.dueAt ?: return@count false
                            due in now..(now + 7 * 24 * 60 * 60 * 1000L)
                        }
                    StudentSemesterSummary(
                        semester = semester,
                        courseCount = courseCount,
                        openAssignments = openAssignments,
                        upcomingExams = upcomingExams,
                        dueSoon = dueSoon,
                    )
                }.sortedBy { it.semester.lowercase() }

        val topicToNoteLinks =
            relations.count {
                it.relationType.equals("TOPIC_LINK", ignoreCase = true)
            }

        val paperToNoteLinks =
            relations.count {
                it.relationType.equals("PAPER_REFERENCE", ignoreCase = true)
            }

        val conceptNodeIds =
            activeNodes
                .filter {
                    it.node.type == "note" && (it.node.noteType == "concept" || it.hasTag("psychology"))
                }.map { it.node.id }
                .toSet()

        val conceptEdges =
            relations.count { relation ->
                conceptNodeIds.contains(relation.fromNodeId) && conceptNodeIds.contains(relation.toNodeId)
            }

        val flashcardCandidates =
            activeNodes
                .filter {
                    it.hasTag("flashcard") ||
                        it.hasTag("flashcard_candidate") ||
                        it.student()?.flashcardCandidate == true
                }.sortedByDescending { it.node.updatedAt }

        val studentNodeIds = activeNodes.map { it.node.id }.toSet()
        val studySessionsThisWeek =
            sessions.count {
                it.startedAt >= sevenDaysAgo && studentNodeIds.contains(it.nodeId)
            }
        val studyMinutesThisWeek =
            sessions
                .filter { it.startedAt >= sevenDaysAgo && studentNodeIds.contains(it.nodeId) }
                .sumOf { if (it.durationSec > 0) it.durationSec else ((now - it.startedAt) / 1000).toInt() }
                .div(60)

        val templateNames = templates.map { it.name.trim().lowercase() }.toSet()
        return StudentBoardState(
            lectureTemplateReady = templateNames.contains("lecture note template"),
            readingTemplateReady = templateNames.contains("reading note template"),
            paperSummaryTemplateReady = templateNames.contains("paper summary template"),
            assignmentTracker = assignmentTracker,
            examPrepBoard = examPrepBoard,
            psychologyConceptMaps = psychologyConceptMaps,
            glossaryCards = glossaryCards,
            researchIdeaVault = researchIdeaVault,
            quoteBank = quoteBank,
            caseReflectionNotes = caseReflectionNotes,
            readingBacklog = readingBacklog,
            revisitBeforeExam = revisitBeforeExam,
            readingProgress = readingProgress,
            assignmentDeadlines = assignmentDeadlines,
            topicMastery = topicMastery,
            courseDashboard = courseDashboard,
            semesterDashboard = semesterDashboard,
            examCountdownNode = examCountdownNode,
            examCountdownDays = examCountdownDays,
            topicToNoteLinks = topicToNoteLinks,
            paperToNoteLinks = paperToNoteLinks,
            conceptGraphNodes = conceptNodeIds.size,
            conceptGraphEdges = conceptEdges,
            flashcardCandidates = flashcardCandidates,
            studySessionsThisWeek = studySessionsThisWeek,
            studyMinutesThisWeek = studyMinutesThisWeek,
        )
    }

    val isBiometricEnabled: StateFlow<Boolean?> =
        preferencesRepository.isBiometricEnabled
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

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
                    .filter { it.node.type == "protocol" && it.node.status != "archived" }
                    .sortedByDescending { it.node.updatedAt }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val protocolHistoryItems: StateFlow<List<ProtocolHistoryItem>> =
        combine(protocolHistory, allNodes) { history, nodes ->
            val byId = nodes.associateBy { it.node.id }
            history.map { item ->
                ProtocolHistoryItem(
                    historyId = item.id,
                    protocolNodeId = item.protocolNodeId,
                    protocolLabel = byId[item.protocolNodeId]?.node?.title ?: "Unknown protocol",
                    executedAt = item.executedAt,
                    notes = item.notes,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transitionProtocolsSnapshot: StateFlow<TransitionProtocolsSnapshot> =
        combine(transitionProtocolNodes, protocolHistoryItems) { protocolNodes, historyItems ->
            val usageByLabel =
                historyItems.groupBy { normalizeProtocolLabel(it.protocolLabel) }
            val protocolItems =
                protocolNodes
                    .map { protocol ->
                        val (done, total) = protocolChecklistProgress(protocol.node.content)
                        val usage =
                            usageByLabel[normalizeProtocolLabel(protocol.node.title)].orEmpty()
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
            TransitionProtocolsSnapshot(
                protocols = protocolItems,
                templates = transitionProtocolTemplates,
                recommendedLabel = recommendProtocolLabel(),
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
            val playbookNodes =
                protocolNodes.filter { node ->
                    val normalized = normalizeProtocolLabel(node.node.title)
                    playbookTemplates.any { normalizeProtocolLabel(it.label) == normalized } ||
                        node.tags.any { it.normalizedName == "playbook" } ||
                        node.node.relationshipContext?.contains(
                            "playbook",
                            ignoreCase = true,
                        ) == true
                }
            val usageByLabel = historyItems.groupBy { normalizeProtocolLabel(it.protocolLabel) }
            val playbooks =
                playbookNodes
                    .map { playbook ->
                        val (done, total) = protocolChecklistProgress(playbook.node.content)
                        val usage =
                            usageByLabel[normalizeProtocolLabel(playbook.node.title)].orEmpty()
                        val linkedMode = parsePlaybookModeKey(playbook.node.relationshipContext)
                        PlaybookItem(
                            node = playbook,
                            checklistDone = done,
                            checklistTotal = total,
                            triggerCount = usage.size,
                            linkedModeKey = linkedMode,
                            linkedAreaId = playbook.node.areaId,
                            isCustom =
                                playbookTemplates.none {
                                    normalizeProtocolLabel(it.label) ==
                                        normalizeProtocolLabel(
                                            playbook.node.title,
                                        )
                                },
                        )
                    }.sortedWith(
                        compareByDescending<PlaybookItem> { it.triggerCount }
                            .thenBy {
                                it.node.node.title
                                    .lowercase()
                            },
                    )
            PlaybookSnapshot(
                playbooks = playbooks,
                templates = playbookTemplates,
                suggestedPlaybookLabel = suggestPlaybookLabel(mode, entries),
            )
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

    val capacitySnapshot: StateFlow<CapacitySnapshot> =
        combine(
            activeNodes,
            allProjects,
            allAreas,
            maintenanceSnapshot,
            openLoopsSnapshot,
            trackEntries,
            currentMode,
            allModes,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val nodes = args[0] as List<NodeWithPin>

            @Suppress("UNCHECKED_CAST")
            val projects = args[1] as List<NodeEntity>

            @Suppress("UNCHECKED_CAST")
            val areas = args[2] as List<NodeEntity>
            val maintenance = args[3] as MaintenanceSnapshot
            val openLoops = args[4] as OpenLoopsSnapshot

            @Suppress("UNCHECKED_CAST")
            val entries = args[5] as List<TrackEntryEntity>
            val currentMode = args[6] as ModeEntity?

            @Suppress("UNCHECKED_CAST")
            val allModes = args[7] as List<ModeEntity>
            calculateCapacitySnapshot(
                nodes = nodes,
                projects = projects,
                areas = areas,
                maintenance = maintenance,
                openLoops = openLoops,
                trackEntries = entries,
                currentMode = currentMode,
                allModes = allModes,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CapacitySnapshot())

    val lifeOSSignatureSnapshot: StateFlow<LifeOSSignatureSnapshot> =
        combine(
            allModes,
            areaHealthSnapshot,
            openLoopsSnapshot,
            maintenanceSnapshot,
            relationshipSnapshot,
            vaultsSnapshot,
            capacitySnapshot,
            playbookSnapshot,
            currentMode,
            trackEntries,
            activeNodes,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val modes = args[0] as List<ModeEntity>
            val areaHealth = args[1] as AreaHealthSnapshot
            val openLoops = args[2] as OpenLoopsSnapshot
            val maintenance = args[3] as MaintenanceSnapshot
            val relationships = args[4] as RelationshipSnapshot
            val vaults = args[5] as VaultsSnapshot
            val capacity = args[6] as CapacitySnapshot
            val playbooks = args[7] as PlaybookSnapshot
            val currentMode = args[8] as ModeEntity?

            @Suppress("UNCHECKED_CAST")
            val entries = args[9] as List<TrackEntryEntity>

            @Suppress("UNCHECKED_CAST")
            val nodes = args[10] as List<NodeWithPin>
            val pendingDecisions =
                nodes.filter {
                    it.node.type == "decision" &&
                        it.node.status == "active" &&
                        !it.node.inboxState
                }
            calculateLifeOSSignatureSnapshot(
                modes = modes,
                areaHealth = areaHealth,
                openLoops = openLoops,
                pendingDecisions = pendingDecisions,
                maintenance = maintenance,
                relationships = relationships,
                vaults = vaults,
                capacity = capacity,
                playbooks = playbooks,
                currentMode = currentMode,
                trackEntries = entries,
                nodes = nodes,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LifeOSSignatureSnapshot())

    val lifeOSSecondBrainSnapshot: StateFlow<LifeOSSecondBrainSnapshot> =
        combine(
            activeNodes,
            allRelations,
            dashboardUIState,
            areaHealthSnapshot,
            openLoopsSnapshot,
            maintenanceSnapshot,
            capacitySnapshot,
            transitionProtocolsSnapshot,
            playbookSnapshot,
            currentMode,
            lifeOSSignatureSnapshot,
            vaultsSnapshot,
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val nodes = args[0] as List<NodeWithPin>

            @Suppress("UNCHECKED_CAST")
            val relations = args[1] as List<RelationEntity>
            val dashboard = args[2] as DashboardUIState
            val areaHealth = args[3] as AreaHealthSnapshot
            val openLoops = args[4] as OpenLoopsSnapshot
            val maintenance = args[5] as MaintenanceSnapshot
            val capacity = args[6] as CapacitySnapshot
            val protocols = args[7] as TransitionProtocolsSnapshot
            val playbooks = args[8] as PlaybookSnapshot
            val mode = args[9] as ModeEntity?
            val signature = args[10] as LifeOSSignatureSnapshot
            val vaults = args[11] as VaultsSnapshot
            calculateLifeOSSecondBrainSnapshot(
                nodes = nodes,
                relations = relations,
                dashboard = dashboard,
                areaHealth = areaHealth,
                openLoops = openLoops,
                maintenance = maintenance,
                capacity = capacity,
                protocols = protocols,
                playbooks = playbooks,
                currentMode = mode,
                signature = signature,
                vaults = vaults,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LifeOSSecondBrainSnapshot())

    val combinedDirectionSnapshot: StateFlow<CombinedDirectionSnapshot> =
        combine(
            lifeOSSecondBrainSnapshot,
            lifeOSSignatureSnapshot,
            dashboardUIState,
            physicalLogisticsSnapshot,
            capacitySnapshot,
            relationshipSnapshot,
            transitionProtocolsSnapshot,
            maintenanceSnapshot,
            openLoopsSnapshot,
        ) { args ->
            val distinction = args[0] as LifeOSSecondBrainSnapshot
            val signature = args[1] as LifeOSSignatureSnapshot
            val dashboard = args[2] as DashboardUIState
            val logistics = args[3] as PhysicalLogisticsSnapshot
            val capacity = args[4] as CapacitySnapshot
            val relationships = args[5] as RelationshipSnapshot
            val protocols = args[6] as TransitionProtocolsSnapshot
            val maintenance = args[7] as MaintenanceSnapshot
            val openLoops = args[8] as OpenLoopsSnapshot
            calculateCombinedDirectionSnapshot(
                distinction = distinction,
                signature = signature,
                dashboard = dashboard,
                logistics = logistics,
                capacity = capacity,
                relationships = relationships,
                protocols = protocols,
                maintenance = maintenance,
                openLoops = openLoops,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CombinedDirectionSnapshot())

    val coreLifeOSShiftSnapshot: StateFlow<CoreLifeOSShiftSnapshot> =
        combine(
            lifeOSSecondBrainSnapshot,
            lifeOSSignatureSnapshot,
            combinedDirectionSnapshot,
            dashboardUIState,
            timeArchitectureSnapshot,
            areaHealthSnapshot,
            openLoopsSnapshot,
            maintenanceSnapshot,
            transitionProtocolsSnapshot,
            capacitySnapshot,
            currentMode,
        ) { args ->
            val distinction = args[0] as LifeOSSecondBrainSnapshot
            val signature = args[1] as LifeOSSignatureSnapshot
            val direction = args[2] as CombinedDirectionSnapshot
            val dashboard = args[3] as DashboardUIState
            val time = args[4] as TimeArchitectureSnapshot
            val areaHealth = args[5] as AreaHealthSnapshot
            val openLoops = args[6] as OpenLoopsSnapshot
            val maintenance = args[7] as MaintenanceSnapshot
            val protocols = args[8] as TransitionProtocolsSnapshot
            val capacity = args[9] as CapacitySnapshot
            val mode = args[10] as ModeEntity?
            calculateCoreLifeOSShiftSnapshot(
                distinction = distinction,
                signature = signature,
                direction = direction,
                dashboard = dashboard,
                time = time,
                areaHealth = areaHealth,
                openLoops = openLoops,
                maintenance = maintenance,
                protocols = protocols,
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

    fun triggerProtocol(
        protocolLabel: String,
        source: String = "dashboard",
    ) {
        viewModelScope.launch {
            val normalized = protocolLabel.trim()
            val template = findProtocolTemplate(normalized)
            val existing =
                allNodes.value
                    .firstOrNull {
                        it.node.type == "protocol" && normalizeProtocolLabel(it.node.title) ==
                            normalizeProtocolLabel(
                                normalized,
                            )
                    }?.node

            val protocolNodeId =
                if (existing != null) {
                    if (existing.content.isBlank() && template != null) {
                        repository.updateNode(
                            existing.copy(
                                content = buildProtocolChecklistContent(template),
                                updatedAt = Clock.System.now().toEpochMilliseconds(),
                            ),
                        )
                    }
                    existing.id
                } else {
                    repository.insertNode(
                        NodeEntity(
                            type = "protocol",
                            title = template?.label ?: normalized,
                            content =
                                template?.let { buildProtocolChecklistContent(it) }
                                    ?: "Operational protocol trigger: $normalized",
                            inboxState = false,
                            status = "active",
                        ),
                    )
                }

            repository.insertProtocolHistory(
                ProtocolHistoryEntity(
                    protocolNodeId = protocolNodeId,
                    notes = "Triggered from $source",
                    completed = true,
                ),
            )
        }
    }

    fun applyProtocolTemplate(protocolLabel: String) {
        viewModelScope.launch {
            val template = findProtocolTemplate(protocolLabel) ?: return@launch
            val existing =
                allNodes.value
                    .firstOrNull {
                        it.node.type == "protocol" && normalizeProtocolLabel(it.node.title) ==
                            normalizeProtocolLabel(
                                template.label,
                            )
                    }?.node
            if (existing != null) {
                repository.updateNode(
                    existing.copy(
                        title = template.label,
                        content = buildProtocolChecklistContent(template),
                        status = "active",
                        inboxState = false,
                        updatedAt = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            } else {
                repository.insertNode(
                    NodeEntity(
                        type = "protocol",
                        title = template.label,
                        content = buildProtocolChecklistContent(template),
                        status = "active",
                        inboxState = false,
                    ),
                )
            }
        }
    }

    fun applyPlaybookTemplate(
        playbookLabel: String,
        modeKey: String? = null,
        areaId: Long? = null,
    ) {
        viewModelScope.launch {
            val template = findPlaybookTemplate(playbookLabel) ?: return@launch
            val resolvedModeKey = modeKey ?: template.recommendedModeKey
            val existing =
                allNodes.value
                    .firstOrNull {
                        it.node.type == "protocol" &&
                            normalizeProtocolLabel(it.node.title) ==
                            normalizeProtocolLabel(
                                template.label,
                            )
                    }?.node
            val relationshipContext = buildPlaybookRelationshipContext(resolvedModeKey)
            if (existing != null) {
                repository.updateNode(
                    existing.copy(
                        title = template.label,
                        content =
                            buildProtocolChecklistContent(
                                TransitionProtocolTemplate(
                                    template.key,
                                    template.label,
                                    template.checklist,
                                ),
                            ),
                        areaId = areaId ?: existing.areaId,
                        relationshipContext = relationshipContext,
                        status = "active",
                        inboxState = false,
                        updatedAt = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
                setTagOnNode(existing.id, "playbook", true)
            } else {
                val playbookId =
                    repository.insertNode(
                        NodeEntity(
                            type = "protocol",
                            title = template.label,
                            content =
                                buildProtocolChecklistContent(
                                    TransitionProtocolTemplate(
                                        template.key,
                                        template.label,
                                        template.checklist,
                                    ),
                                ),
                            areaId = areaId,
                            relationshipContext = relationshipContext,
                            status = "active",
                            inboxState = false,
                        ),
                    )
                setTagOnNode(playbookId, "playbook", true)
            }
        }
    }

    fun saveCustomPlaybook(
        label: String,
        checklistLines: List<String>,
        modeKey: String? = null,
        areaId: Long? = null,
    ) {
        val cleanLabel = label.trim().ifBlank { return }
        val cleanChecklist = checklistLines.map { it.trim() }.filter { it.isNotBlank() }
        if (cleanChecklist.isEmpty()) return
        viewModelScope.launch {
            val nodeId =
                repository.insertNode(
                    NodeEntity(
                        type = "protocol",
                        title = cleanLabel,
                        content =
                            buildProtocolChecklistContent(
                                TransitionProtocolTemplate(
                                    key =
                                        cleanLabel
                                            .lowercase()
                                            .replace(Regex("[^a-z0-9]+"), "_")
                                            .trim('_'),
                                    label = cleanLabel,
                                    checklist = cleanChecklist,
                                ),
                            ),
                        areaId = areaId,
                        relationshipContext = buildPlaybookRelationshipContext(modeKey),
                        status = "active",
                        inboxState = false,
                    ),
                )
            setTagOnNode(nodeId, "playbook", true)
            if (modeKey != null) setTagOnNode(nodeId, "mode_${modeKey.lowercase()}", true)
        }
    }

    fun setPlaybookModeLink(
        playbookNode: NodeEntity,
        modeKey: String?,
    ) {
        if (playbookNode.type != "protocol") return
        updateNode(playbookNode.copy(relationshipContext = buildPlaybookRelationshipContext(modeKey)))
    }

    fun setPlaybookAreaLink(
        playbookNode: NodeEntity,
        areaId: Long?,
    ) {
        if (playbookNode.type != "protocol") return
        updateNode(playbookNode.copy(areaId = areaId))
    }

    fun toggleProtocolChecklistStep(
        protocolNode: NodeEntity,
        checklistIndex: Int,
        checked: Boolean,
    ) {
        if (protocolNode.type != "protocol") return
        viewModelScope.launch {
            val lines = protocolNode.content.lines().toMutableList()
            val checklistLineIndexes =
                lines
                    .withIndex()
                    .filter { (_, line) ->
                        line.trimStart().startsWith("- [ ] ") ||
                            line
                                .trimStart()
                                .startsWith("- [x] ")
                    }.map { it.index }
            val targetLine = checklistLineIndexes.getOrNull(checklistIndex) ?: return@launch
            val original = lines[targetLine].trimStart()
            val replacement =
                when
                    {
                        checked && original.startsWith("- [ ] ") -> {
                            original.replaceFirst(
                                "- [ ] ",
                                "- [x] ",
                            )
                        }

                        !checked && original.startsWith("- [x] ") -> {
                            original.replaceFirst(
                                "- [x] ",
                                "- [ ] ",
                            )
                        }

                        else -> {
                            original
                        }
                    }
            lines[targetLine] = replacement
            repository.updateNode(
                protocolNode.copy(
                    content = lines.joinToString("\n"),
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
        }
    }

    private fun normalizeProtocolLabel(label: String): String =
        label
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()

    private fun findProtocolTemplate(label: String): TransitionProtocolTemplate? {
        val normalized = normalizeProtocolLabel(label)
        return transitionProtocolTemplates.firstOrNull {
            normalizeProtocolLabel(it.label) == normalized || it.key == normalized.replace(" ", "_")
        }
    }

    private fun findPlaybookTemplate(label: String): PlaybookTemplate? {
        val normalized = normalizeProtocolLabel(label)
        return playbookTemplates.firstOrNull {
            normalizeProtocolLabel(it.label) == normalized || it.key == normalized.replace(" ", "_")
        }
    }

    private fun buildPlaybookRelationshipContext(modeKey: String?): String? =
        buildString {
            append("playbook")
            if (!modeKey.isNullOrBlank()) append("|mode=").append(modeKey.trim().uppercase())
        }.ifBlank { null }

    private fun parsePlaybookModeKey(context: String?): String? =
        context?.split("|")?.firstNotNullOfOrNull { token ->
            if (token.startsWith("mode=", ignoreCase = true)) {
                token
                    .substringAfter("=")
                    .trim()
                    .uppercase()
                    .ifBlank { null }
            } else {
                null
            }
        }

    private fun buildProtocolChecklistContent(template: TransitionProtocolTemplate): String =
        buildString {
            appendLine("## TRANSITION CHECKLIST")
            template.checklist.forEach { step ->
                appendLine("- [ ] $step")
            }
        }.trimEnd()

    private fun protocolChecklistProgress(content: String): Pair<Int, Int> {
        val checklistLines =
            content.lines().map { it.trimStart() }.filter {
                it.startsWith("- [ ] ") || it.startsWith("- [x] ")
            }
        val total = checklistLines.size
        val done = checklistLines.count { it.startsWith("- [x] ") }
        return done to total
    }

    private fun suggestPlaybookLabel(
        mode: ModeEntity?,
        entries: List<TrackEntryEntity>,
    ): String? {
        val today =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString()
        val latest = entries.filter { it.date == today }.maxByOrNull { it.createdAt }
        if ((latest?.anxietyScore ?: 0) >= 4) {
            return "Panic-ish day protocol"
        }
        if ((latest?.energyScore ?: 5) <= 2) {
            return "Low energy but must function protocol"
        }
        return when (mode?.key)
        {
            "STUDY" -> "Can't start studying protocol"
            "ERRAND" -> "Need to leave house protocol"
            "RECOVERY", "LOW_BATTERY", "CANT_THINK" -> "Bad day protocol"
            "SOCIAL" -> "Need to reply to everyone protocol"
            else -> null
        }
    }

    private fun recommendProtocolLabel(): String? {
        val localNow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val key =
            when (localNow.hour)
            {
                in 5..9 -> "morning_startup"
                in 10..14 -> "before_class"
                in 15..18 -> "deep_work_entry"
                in 19..21 -> "work_to_rest"
                else -> "before_sleep"
            }
        return transitionProtocolTemplates.firstOrNull { it.key == key }?.label
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
        decisionCategory: String? = null,
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
                    decisionStatus = if (autoType == "decision") "pending" else null,
                    decisionCategory =
                        if (autoType == "decision") {
                            decisionCategory
                                ?: "major"
                        } else {
                            null
                        },
                    openLoopType = if (autoType == "open_loop") "unresolved_problem" else null,
                    openLoopStalenessAt =
                        if (autoType == "open_loop") {
                            Clock.System
                                .now()
                                .plus(3, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                                .toEpochMilliseconds()
                        } else {
                            null
                        },
                    maintenanceType = if (autoType == "maintenance") "form" else null,
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
                    openLoopType = if (type == "open_loop") "unresolved_problem" else null,
                    openLoopStalenessAt =
                        if (type == "open_loop") {
                            Clock.System
                                .now()
                                .plus(3, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                                .toEpochMilliseconds()
                        } else {
                            null
                        },
                    maintenanceType = if (type == "maintenance") "form" else null,
                ),
            )
        }

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
                val matches =
                    regex
                        .findAll(node.content)
                        .map { match ->
                            val fullMatch = match.groupValues[1]
                            if (fullMatch.contains("|")) fullMatch.split("|")[0] else fullMatch
                        }.toList()

                if (matches.isNotEmpty()) {
                    val nodes = allNodes.value
                    for (match in matches) {
                        nodes
                            .find { it.node.title.equals(match.trim(), ignoreCase = true) }
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
                            lines
                                .first()
                                .trim()
                                .removePrefix("-")
                                .removePrefix("*")
                                .trim()
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
        viewModelScope.launch {
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
                                    type = "task",
                                    projectId = node.projectId,
                                    areaId = node.areaId,
                                    parentNodeId = node.id,
                                ),
                            )
                        repository.insertRelation(
                            RelationEntity(
                                fromNodeId = node.id,
                                toNodeId = subtaskId,
                                relationType = "DEPENDS_ON",
                            ),
                        )
                    }
                    // Optionally clear content or prefix it with "SPLIT"
                    repository.updateNode(
                        node.copy(
                            content = "// SPLIT INTO SUBTASKS\n" + node.content,
                            updatedAt = Clock.System.now().toEpochMilliseconds(),
                        ),
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
                        content = node.content,
                    ),
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
                        updatedAt = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            }
        }
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
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
        }
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
        viewModelScope.launch {
            repository.getNodeById(nodeId)?.let { node ->
                val sections =
                    node.content
                        .split(Regex("(?=^# )", RegexOption.MULTILINE))
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
                                areaId = node.areaId,
                            ),
                        )
                    }
                    archiveNode(node)
                }
            }
        }
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
            when (interval.uppercase())
            {
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

    fun addSuggestedAreas() {
        viewModelScope.launch {
            val existing = allAreas.value.map { it.title.trim().lowercase() }.toSet()
            suggestedAreaTitles
                .filterNot { existing.contains(it.trim().lowercase()) }
                .forEach { addArea(it) }
        }
    }

    fun updateOpenLoopType(
        node: NodeEntity,
        openLoopType: String,
    ) {
        if (node.type != "open_loop") return
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

    fun convertOpenLoopToTask(nodeId: Long) {
        convertOpenLoop(nodeId, "task")
    }

    fun convertOpenLoopToDecision(nodeId: Long) {
        convertOpenLoop(nodeId, "decision")
    }

    fun convertOpenLoopToNote(nodeId: Long) {
        convertOpenLoop(nodeId, "note")
    }

    fun resolveOpenLoop(
        nodeId: Long,
        resolutionNote: String? = null,
    ) {
        viewModelScope.launch {
            val node = repository.getNodeById(nodeId) ?: return@launch
            if (node.type != "open_loop") return@launch
            val now = Clock.System.now().toEpochMilliseconds()
            repository.updateNode(
                node.copy(
                    status = "done",
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

    fun archiveResolvedOpenLoops() {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            allNodes.value
                .map { it.node }
                .filter { it.type == "open_loop" && it.status == "done" }
                .forEach { loop ->
                    repository.updateNode(
                        loop.copy(
                            status = "archived",
                            archivedAt = now,
                            updatedAt = now,
                        ),
                    )
                }
        }
    }

    fun updateMaintenanceType(
        node: NodeEntity,
        maintenanceType: String,
    ) {
        if (node.type != "maintenance") return
        updateNode(node.copy(maintenanceType = maintenanceType))
    }

    fun setMaintenanceOverdueAt(
        node: NodeEntity,
        timestamp: Long?,
    ) {
        if (node.type != "maintenance") return
        updateNode(node.copy(maintenanceOverdueAt = timestamp))
    }

    fun setMaintenanceRecurring(
        node: NodeEntity,
        interval: String?,
    ) {
        if (node.type != "maintenance") return
        updateNode(
            node.copy(
                isRecurring = interval != null,
                recurringInterval = interval,
                maintenanceInterval = interval,
            ),
        )
    }

    fun setProjectActivePhase(
        project: NodeEntity,
        active: Boolean,
    ) {
        if (project.type != "project") return
        updateNode(
            project.copy(
                projectStatus = if (active) "active" else "on_hold",
            ),
        )
    }

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
                status = "active",
            ),
        )
    }

    fun clearTemporaryFocusPeriod(node: NodeEntity) {
        updateNode(
            node.copy(
                startAt = null,
            ),
        )
    }

    fun setWorkDate(
        node: NodeEntity,
        workAt: Long?,
    ) {
        if (node.type != "task") return
        updateNode(node.copy(startAt = workAt))
    }

    fun toggleSeasonalGoal(
        node: NodeEntity,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            setTagOnNode(node.id, "seasonal_goal", enabled)
            updateNode(node.copy(noteType = if (enabled) "goal_seasonal" else node.noteType))
        }
    }

    fun addLifePeriodMarker(
        title: String,
        content: String = "",
    ) {
        viewModelScope.launch {
            val markerId =
                addNodeForResult(
                    title = title,
                    content = content,
                    type = "note",
                    inboxState = false,
                )
            val markerNode = repository.getNodeById(markerId) ?: return@launch
            repository.updateNode(
                markerNode.copy(
                    noteType = "period_marker",
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            setTagOnNode(markerId, "life_period_marker", true)
        }
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
        if (person.type != "person") return
        updateNode(person.copy(lastContactAt = Clock.System.now().toEpochMilliseconds()))
    }

    fun setPersonFollowUpInDays(
        person: NodeEntity,
        days: Int?,
    ) {
        if (person.type != "person") return
        val followUpAt =
            if (days == null) {
                null
            } else {
                Clock.System
                    .now()
                    .plus(days.coerceIn(1, 365), DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                    .toEpochMilliseconds()
            }
        updateNode(person.copy(dueAt = followUpAt))
    }

    fun setPersonImportantDate(
        person: NodeEntity,
        timestamp: Long?,
    ) {
        if (person.type != "person") return
        updateNode(person.copy(dueAt = timestamp))
    }

    fun setPersonSocialEnergyNotes(
        person: NodeEntity,
        notes: String?,
    ) {
        if (person.type != "person") return
        updateNode(person.copy(socialEnergyNotes = notes?.trim()?.ifBlank { null }))
    }

    fun setPersonRelationshipContext(
        person: NodeEntity,
        context: String?,
    ) {
        if (person.type != "person") return
        updateNode(person.copy(relationshipContext = context?.trim()?.ifBlank { null }))
    }

    fun markImportantRelationship(
        person: NodeEntity,
        important: Boolean,
    ) {
        if (person.type != "person") return
        viewModelScope.launch {
            setTagOnNode(person.id, "important_relationship", important)
        }
    }

    fun setPersonRelationshipType(
        person: NodeEntity,
        type: String?,
    ) {
        if (person.type != "person") return
        val supported = setOf("professor", "friend", "family")
        val normalized = type?.trim()?.lowercase()?.takeIf { it in supported }
        viewModelScope.launch {
            supported.forEach { tag -> setTagOnNode(person.id, tag, false) }
            if (normalized != null) setTagOnNode(person.id, normalized, true)
        }
    }

    fun linkPersonToNode(
        personId: Long,
        nodeId: Long,
    ) {
        addRelation(personId, nodeId, "RELATED_PERSON")
    }

    fun unlinkPersonFromNode(
        personId: Long,
        nodeId: Long,
    ) {
        viewModelScope.launch {
            val relation =
                repository.getRelationsForNode(personId).first().firstOrNull {
                    it.relationType == "RELATED_PERSON" &&
                        (
                            (it.fromNodeId == personId && it.toNodeId == nodeId) ||
                                (it.toNodeId == personId && it.fromNodeId == nodeId)
                        )
                } ?: return@launch
            repository.deleteRelation(relation)
        }
    }

    fun createReplyNeededForPerson(
        personId: Long,
        title: String,
        content: String = "",
    ) {
        viewModelScope.launch {
            val nodeId =
                addNodeForResult(
                    title = title.ifBlank { "Reply needed" },
                    content = content,
                    type = "open_loop",
                    inboxState = false,
                )
            val node = repository.getNodeById(nodeId) ?: return@launch
            repository.updateNode(
                node.copy(
                    openLoopType = "reply_needed",
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            addRelation(personId, nodeId, "RELATED_PERSON")
        }
    }

    fun createSharedPlanForPerson(
        personId: Long,
        title: String,
        content: String = "",
    ) {
        viewModelScope.launch {
            val nodeId =
                addNodeForResult(
                    title = title.ifBlank { "Shared plan" },
                    content = content,
                    type = "task",
                    inboxState = false,
                )
            setTagOnNode(nodeId, "shared_plan", true)
            addRelation(personId, nodeId, "RELATED_PERSON")
        }
    }

    fun createAskAboutNextTimeNote(
        personId: Long,
        prompt: String,
    ) {
        viewModelScope.launch {
            val nodeId =
                addNodeForResult(
                    title = "Ask next time: ${prompt.ifBlank { "Topic" }}",
                    content = prompt,
                    type = "note",
                    inboxState = false,
                )
            val note = repository.getNodeById(nodeId) ?: return@launch
            repository.updateNode(
                note.copy(
                    noteType = "ask_next_time",
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            setTagOnNode(nodeId, "ask_next_time", true)
            addRelation(personId, nodeId, "RELATED_PERSON")
        }
    }

    fun addPlace(
        title: String,
        campus: Boolean = false,
        home: Boolean = false,
    ) {
        viewModelScope.launch {
            val placeId =
                addNodeForResult(
                    title = title.ifBlank { "Place" },
                    type = "place",
                    inboxState = false,
                )
            val placeNode = repository.getNodeById(placeId) ?: return@launch
            repository.updateNode(
                placeNode.copy(
                    locationContext =
                        when
                            {
                                campus -> "on_campus"
                                home -> "at_home"
                                else -> "out_of_home"
                            },
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            if (campus) setTagOnNode(placeId, "campus", true)
            if (home) setTagOnNode(placeId, "home", true)
        }
    }

    fun linkNodeToPlace(
        nodeId: Long,
        placeId: Long,
    ) {
        addRelation(placeId, nodeId, "PLACE_CONTEXT")
    }

    fun unlinkNodeFromPlace(
        nodeId: Long,
        placeId: Long,
    ) {
        viewModelScope.launch {
            val relation =
                repository.getRelationsForNode(placeId).first().firstOrNull {
                    it.relationType == "PLACE_CONTEXT" &&
                        (
                            (it.fromNodeId == placeId && it.toNodeId == nodeId) ||
                                (it.fromNodeId == nodeId && it.toNodeId == placeId)
                        )
                } ?: return@launch
            repository.deleteRelation(relation)
        }
    }

    fun createWhatToBringList(
        title: String,
        placeId: Long? = null,
    ) {
        createLogisticsList(
            title = title.ifBlank { "What to bring" },
            tag = "what_to_bring",
            noteType = "logistics",
            placeId = placeId,
        )
    }

    fun createPackingList(title: String) {
        createLogisticsList(
            title = title.ifBlank { "Packing list" },
            tag = "packing_list",
            noteType = "logistics",
        )
    }

    fun createLeaveHomeChecklist(title: String = "Leave-home checklist") {
        createLogisticsList(
            title = title,
            tag = "leave_home_checklist",
            noteType = "logistics",
            type = "protocol",
        )
    }

    fun createDontForgetSet(title: String) {
        createLogisticsList(
            title = title.ifBlank { "Don't forget set" },
            tag = "dont_forget_set",
            noteType = "logistics",
        )
    }

    fun createEventPreparationList(title: String) {
        createLogisticsList(
            title = title.ifBlank { "Event prep list" },
            tag = "event_prep",
            noteType = "logistics",
        )
    }

    fun createClassBringList(title: String) {
        createLogisticsList(
            title = title.ifBlank { "Class bring list" },
            tag = "class_bring",
            noteType = "logistics",
        )
    }

    fun ensureTravelPackTemplate() {
        viewModelScope.launch {
            val exists =
                allTemplates.value.any { it.name.contains("travel pack", ignoreCase = true) }
            if (!exists) {
                repository.insertTemplate(
                    TemplateEntity(
                        name = "Travel Pack Template",
                        nodeType = "note",
                        defaultTitle = "Travel pack - [Trip]",
                        defaultContent =
                            """
                            - IDs / documents
                            - Wallet / cards / cash
                            - Phone / charger / powerbank
                            - Medications
                            - Clothes / hygiene
                            - Special gear
                            - Don't forget items
                            """.trimIndent(),
                    ),
                )
            }
        }
    }

    fun addPhysicalLogisticsNote(
        title: String,
        content: String,
    ) {
        viewModelScope.launch {
            val noteId =
                addNodeForResult(
                    title = title.ifBlank { "Physical logistics note" },
                    content = content,
                    type = "note",
                    inboxState = false,
                )
            val note = repository.getNodeById(noteId) ?: return@launch
            repository.updateNode(
                note.copy(
                    noteType = "logistics",
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            setTagOnNode(noteId, "logistics", true)
        }
    }

    fun addPersonalRule(
        title: String,
        content: String = "",
        categoryTag: String,
    ) {
        val validPrefix = categoryTag.trim().lowercase()
        if (!validPrefix.startsWith("rule_")) return
        viewModelScope.launch {
            val nodeId =
                addNodeForResult(
                    title = title.ifBlank { validPrefix.removePrefix("rule_").replace("_", " ") },
                    content = content,
                    type = "rule",
                    inboxState = false,
                )
            val node = repository.getNodeById(nodeId) ?: return@launch
            repository.updateNode(
                node.copy(
                    noteType = "rule",
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            setTagOnNode(nodeId, validPrefix, true)
            setTagOnNode(nodeId, "operating_principle", true)
        }
    }

    fun pinOperatingPrinciple(
        node: NodeEntity,
        pinned: Boolean,
    ) {
        if (node.type !in setOf("rule", "principle", "note")) return
        updateNode(node.copy(isPinned = pinned))
    }

    fun linkPrincipleToPlaybook(
        principleId: Long,
        playbookNodeId: Long,
    ) {
        addRelation(principleId, playbookNodeId, "PRINCIPLE_FOR_PLAYBOOK")
    }

    fun unlinkPrincipleFromPlaybook(
        principleId: Long,
        playbookNodeId: Long,
    ) {
        viewModelScope.launch {
            val relation =
                repository.getRelationsForNode(principleId).first().firstOrNull {
                    it.relationType in
                        setOf(
                            "PRINCIPLE_FOR_PLAYBOOK",
                            "PLAYBOOK_SUPPORTS_PRINCIPLE",
                        ) &&
                        (
                            (it.fromNodeId == principleId && it.toNodeId == playbookNodeId) ||
                                (it.fromNodeId == playbookNodeId && it.toNodeId == principleId)
                        )
                } ?: return@launch
            repository.deleteRelation(relation)
        }
    }

    fun addVaultEntry(
        categoryTag: String,
        title: String,
        content: String = "",
        asType: String = "note",
        dueAt: Long? = null,
    ) {
        viewModelScope.launch {
            val cleanTag = categoryTag.trim().lowercase()
            val type =
                when (asType)
                {
                    "document", "vault", "resource", "note", "maintenance" -> asType
                    else -> "note"
                }
            val nodeId =
                addNodeForResult(
                    title = title.ifBlank { "Vault entry" },
                    content = content,
                    type = type,
                    inboxState = false,
                )
            val node = repository.getNodeById(nodeId) ?: return@launch
            repository.updateNode(
                node.copy(
                    dueAt = dueAt ?: node.dueAt,
                    noteType = if (type == "note") "reference" else node.noteType,
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            setTagOnNode(nodeId, cleanTag, true)
        }
    }

    fun createApplicationStatusEntry(
        title: String,
        status: String,
        dueAt: Long? = null,
    ) {
        val normalizedStatus = status.trim().ifBlank { "pending" }
        addVaultEntry(
            categoryTag = "vault_application_status",
            title = title.ifBlank { "Application status" },
            content = "Status: $normalizedStatus",
            asType = "document",
            dueAt = dueAt,
        )
    }

    fun markMustFindLater(
        node: NodeEntity,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            setTagOnNode(node.id, "must_find_later", enabled)
            updateNode(node.copy(isPinned = enabled || node.isPinned))
        }
    }

    private fun createLogisticsList(
        title: String,
        tag: String,
        noteType: String,
        placeId: Long? = null,
        type: String = "note",
    ) {
        viewModelScope.launch {
            val nodeId =
                addNodeForResult(
                    title = title,
                    type = type,
                    inboxState = false,
                )
            val node = repository.getNodeById(nodeId) ?: return@launch
            repository.updateNode(
                node.copy(
                    noteType = noteType,
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            setTagOnNode(nodeId, tag, true)
            if (placeId != null) addRelation(placeId, nodeId, "PLACE_CONTEXT")
        }
    }

    fun runMonthlyReset() {
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val monthAgo = now - (30L * 24 * 60 * 60 * 1000)
            todayNodes.value.forEach { node ->
                repository.unpinFromToday(node.id)
            }
            allNodes.value
                .map { it.node }
                .filter {
                    it.status == "done" && (it.completedAt ?: 0L) < monthAgo && it.type == "task"
                }.forEach { doneTask ->
                    repository.updateNode(
                        doneTask.copy(
                            status = "archived",
                            archivedAt = now,
                            updatedAt = now,
                        ),
                    )
                }
            repository.insertNode(
                NodeEntity(
                    type = "note",
                    title = "Monthly reset ${
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    }",
                    content = "Auto-generated monthly reset summary and cleanup marker.",
                    noteType = "reflection",
                    inboxState = false,
                    status = "active",
                ),
            )
        }
    }

    private fun convertOpenLoop(
        nodeId: Long,
        targetType: String,
    ) {
        viewModelScope.launch {
            val source = repository.getNodeById(nodeId) ?: return@launch
            if (source.type != "open_loop") return@launch
            val now = Clock.System.now().toEpochMilliseconds()

            val createdId =
                repository.insertNode(
                    NodeEntity(
                        title =
                            when (targetType)
                            {
                                "task" -> "Follow-up: ${source.title}"
                                "decision" -> "Decision: ${source.title}"
                                "note" -> "Open loop note: ${source.title}"
                                else -> source.title
                            },
                        content =
                            buildString {
                                append(source.content)
                                if (source.content.isNotBlank()) append("\n\n")
                                append("Converted from open loop (${source.openLoopType ?: "untyped"}).")
                            },
                        type = targetType,
                        projectId = source.projectId,
                        areaId = source.areaId,
                        inboxState = true,
                        decisionStatus = if (targetType == "decision") "pending" else null,
                        decisionCategory = if (targetType == "decision") "major" else null,
                    ),
                )

            repository.insertRelation(
                RelationEntity(
                    fromNodeId = source.id,
                    toNodeId = createdId,
                    relationType = "DERIVED_FROM",
                ),
            )

            repository.updateNode(
                source.copy(
                    status = "done",
                    inboxState = false,
                    completedAt = now,
                    updatedAt = now,
                    completionNote = "Converted to ${targetType.uppercase()}",
                ),
            )
        }
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
            val currentUser = user.value ?: UserEntity(name = name)
            repository.insertUser(
                currentUser.copy(
                    name = name,
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
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
            _searchLocationContextFilter,
            _searchEnergyContextFilter,
            _searchDeviceContextFilter,
            _searchSocialContextFilter,
            _searchTimeWindowContextFilter,
            _searchTimeHorizonFilter,
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
            val locationContext = args[10] as String?
            val energyContext = args[11] as String?
            val deviceContext = args[12] as String?
            val socialContext = args[13] as String?
            val timeWindowContext = args[14] as String?
            val timeHorizon = args[15] as String?

            @Suppress("UNCHECKED_CAST")
            val relations = args[16] as List<RelationEntity>

            FilterHelper.filterAndSortNodes(
                nodes = nodes,
                query = query,
                type = type,
                status = status,
                projectId = projectId,
                areaId = areaId,
                linkedToId = linkedToId,
                maxMins = maxMins,
                energy = energy,
                friction = friction,
                locationContext = locationContext,
                energyContext = energyContext,
                deviceContext = deviceContext,
                socialContext = socialContext,
                timeWindowContext = timeWindowContext,
                timeHorizon = timeHorizon,
                relations = relations,
            )
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun matchesQuery(
        nodeWithPin: NodeWithPin,
        query: String,
    ): Boolean = FilterHelper.matchesQuery(nodeWithPin, query)

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
            "at_home" -> _searchLocationContextFilter.value = "at_home"
            "on_campus" -> _searchLocationContextFilter.value = "on_campus"
            "out_of_home" -> _searchLocationContextFilter.value = "out_of_home"
            "laptop_required" -> _searchDeviceContextFilter.value = "laptop_required"
            "phone_okay" -> _searchDeviceContextFilter.value = "phone_okay"
            "needs_internet" -> _searchDeviceContextFilter.value = "needs_internet"
            "needs_privacy" -> _searchSocialContextFilter.value = "needs_privacy"
            "low_energy" -> _searchEnergyContextFilter.value = "low_energy"
            "high_focus" -> _searchEnergyContextFilter.value = "high_focus"
            "brain_works" -> _searchEnergyContextFilter.value = "brain_works"
            "emotionally_wrecked" -> _searchEnergyContextFilter.value = "emotionally_wrecked"
            "10_minute" -> _searchTimeWindowContextFilter.value = "10_minute"
            "commute_friendly" -> _searchSocialContextFilter.value = "commute_friendly"
            "waiting_room" -> _searchTimeWindowContextFilter.value = "waiting_room"
            null -> Unit
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
                        other.node.status != "archived" &&
                        other.node.type in listOf("note", "idea", "resource", "project") &&
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
    ) {
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

    fun startStudySession(nodeId: Long) {
        startFocusSession(nodeId)
    }

    fun setReadingProgress(
        node: NodeEntity,
        progressPercent: Int,
    ) {
        updateStudentMetadata(node) { student ->
            student.copy(readingProgressPercent = progressPercent.coerceIn(0, 100))
        }
    }

    fun setTopicMastery(
        node: NodeEntity,
        topic: String?,
        masteryPercent: Int,
    ) {
        updateStudentMetadata(node) { student ->
            student.copy(
                topic = topic?.trim()?.ifBlank { null },
                masteryPercent = masteryPercent.coerceIn(0, 100),
            )
        }
    }

    fun setStudentCourse(
        node: NodeEntity,
        courseId: String?,
        courseName: String?,
        semester: String?,
        assignmentType: String? = null,
    ) {
        updateStudentMetadata(node) { student ->
            student.copy(
                courseId = courseId?.trim()?.ifBlank { null },
                courseName = courseName?.trim()?.ifBlank { null },
                semester = semester?.trim()?.ifBlank { null },
                assignmentType = assignmentType?.trim()?.ifBlank { null } ?: student.assignmentType,
            )
        }
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
        viewModelScope.launch {
            val id =
                addNodeForResult(
                    title = title,
                    content = content,
                    type = "note",
                    inboxState = false,
                )
            val node = repository.getNodeById(id) ?: return@launch
            val envelope =
                NodeMetadataEnvelope(
                    student =
                        StudentMetadata(
                            courseId = courseId?.trim()?.ifBlank { null },
                            courseName = courseName?.trim()?.ifBlank { null },
                            semester = semester?.trim()?.ifBlank { null },
                            topic = topic?.trim()?.ifBlank { null },
                        ),
                )
            repository.updateNode(
                node.copy(
                    noteType = noteType,
                    metadataJson = node.withMetadataEnvelope(envelope).metadataJson,
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
        }
    }

    fun toggleFlashcardCandidate(
        node: NodeEntity,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            setTagOnNode(node.id, "flashcard_candidate", enabled)
            updateStudentMetadataInternal(node) { student ->
                student.copy(flashcardCandidate = enabled)
            }
        }
    }

    fun toggleRevisitBeforeExam(
        node: NodeEntity,
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            setTagOnNode(node.id, "revisit_before_exam", enabled)
            updateStudentMetadataInternal(node) { student ->
                student.copy(revisitBeforeExam = enabled)
            }
        }
    }

    fun linkTopicToNote(
        topicNodeId: Long,
        noteNodeId: Long,
    ) {
        addRelation(topicNodeId, noteNodeId, "TOPIC_LINK")
    }

    fun linkPaperToNote(
        paperNodeId: Long,
        noteNodeId: Long,
    ) {
        addRelation(paperNodeId, noteNodeId, "PAPER_REFERENCE")
    }

    private fun updateStudentMetadata(
        node: NodeEntity,
        update: (StudentMetadata) -> StudentMetadata,
    ) {
        viewModelScope.launch {
            updateStudentMetadataInternal(node, update)
        }
    }

    private suspend fun updateStudentMetadataInternal(
        node: NodeEntity,
        update: (StudentMetadata) -> StudentMetadata,
    ) {
        val envelope = node.metadataEnvelopeOrNull() ?: NodeMetadataEnvelope()
        val current = envelope.student ?: StudentMetadata()
        val updated = node.withMetadataEnvelope(envelope.copy(student = update(current)))
        repository.updateNode(updated.copy(updatedAt = Clock.System.now().toEpochMilliseconds()))
    }

    private suspend fun setTagOnNode(
        nodeId: Long,
        tagName: String,
        enabled: Boolean,
    ) {
        val normalized = tagName.trim().lowercase()
        val existingTag = allTags.value.firstOrNull { it.normalizedName == normalized }
        val tagId =
            existingTag?.id
                ?: repository.insertTag(
                    TagEntity(
                        name = tagName.trim(),
                        normalizedName = normalized,
                    ),
                )
        if (enabled) {
            repository.attachTagToNode(nodeId, tagId)
        } else {
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

            val nodeId =
                repository.insertNode(
                    NodeEntity(
                        title = "${type.uppercase()} REVIEW - $dateStr",
                        content = content,
                        type = "note",
                        noteType = "reflection",
                        inboxState = false,
                    ),
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

            val bundleResult =
                runCatching {
                    Json.decodeFromString<ExportBundle>(content)
                }.getOrNull()
            if (bundleResult != null) {
                val report = repository.importBundle(bundleResult)
                return@withContext "Imported bundle: ${report.nodes} nodes, ${report.relations} relations, ${report.events} events."
            }

            val legacyResult =
                runCatching {
                    Json.decodeFromString<ExportData>(content)
                }.getOrNull()
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
                nodes.filter { it.node.type == "decision" && it.node.inboxState }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPendingDecisions: StateFlow<List<NodeWithPin>> =
        allNodes
            .map { nodes ->
                nodes.filter { it.node.type == "decision" && it.node.status == "active" && !it.node.inboxState }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val decisionLog: StateFlow<List<NodeWithPin>> =
        allNodes
            .map { nodes ->
                nodes.filter { it.node.type == "decision" && (it.node.decisionStatus == "decided" || it.node.decisionStatus == "expired") }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stalePendingDecisions: StateFlow<List<DecisionStaleItem>> =
        allNodes
            .map { nodes ->
                val now = Clock.System.now().toEpochMilliseconds()
                nodes
                    .filter {
                        it.node.type == "decision" &&
                            it.node.status == "active" &&
                            (it.node.decisionStatus == null || it.node.decisionStatus == "pending" || it.node.decisionStatus == "parked")
                    }.map { decision ->
                        val ageDays =
                            ((now - decision.node.createdAt).coerceAtLeast(0L) / (24 * 60 * 60 * 1000L)).toInt()
                        DecisionStaleItem(node = decision, ageDays = ageDays)
                    }.filter { it.ageDays >= 7 }
                    .sortedByDescending { it.ageDays }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getRelatedPeopleForDecision(decisionId: Long): Flow<List<NodeWithPin>> =
        combine(allNodes, allRelations) { nodes, relations ->
            val personIds =
                relations
                    .filter { relation ->
                        relation.relationType == "RELATED_PERSON" &&
                            (relation.fromNodeId == decisionId || relation.toNodeId == decisionId)
                    }.map { relation ->
                        if (relation.fromNodeId == decisionId) relation.toNodeId else relation.fromNodeId
                    }.toSet()
            nodes.filter { it.node.id in personIds && it.node.type == "person" }
        }

    fun linkDecisionToPerson(
        decisionId: Long,
        personId: Long,
    ) {
        addRelation(decisionId, personId, "RELATED_PERSON")
    }

    fun unlinkDecisionFromPerson(
        decisionId: Long,
        personId: Long,
    ) {
        viewModelScope.launch {
            val relation =
                repository.getRelationsForNode(decisionId).first().firstOrNull {
                    it.relationType == "RELATED_PERSON" &&
                        (
                            (it.fromNodeId == decisionId && it.toNodeId == personId) ||
                                (it.fromNodeId == personId && it.toNodeId == decisionId)
                        )
                } ?: return@launch
            repository.deleteRelation(relation)
        }
    }

    fun setDecisionRevisit(
        node: NodeEntity,
        revisitAt: Long?,
    ) {
        if (node.type != "decision") return
        updateNode(node.copy(decisionRevisitAt = revisitAt))
    }

    fun getOptionsForDecision(nodeId: Long) = repository.getOptionsForDecision(nodeId)

    fun addDecisionOption(
        nodeId: Long,
        title: String,
        description: String? = null,
    ) {
        viewModelScope.launch {
            repository.insertDecisionOption(
                DecisionOptionEntity(
                    decisionNodeId = nodeId,
                    title = title,
                    description = description,
                ),
            )
        }
    }

    fun updateDecisionOption(option: DecisionOptionEntity) {
        viewModelScope.launch {
            repository.updateDecisionOption(option)
        }
    }

    fun deleteDecisionOption(option: DecisionOptionEntity) {
        viewModelScope.launch {
            repository.deleteDecisionOption(option)
        }
    }

    fun decideOn(
        nodeId: Long,
        outcome: String,
        selectedOptionId: Long? = null,
    ) {
        viewModelScope.launch {
            repository.decideOn(nodeId, outcome, selectedOptionId)
        }
    }

    fun convertDecisionToProject(nodeId: Long) {
        viewModelScope.launch {
            repository.convertDecisionToProject(nodeId)
        }
    }

    fun convertDecisionToTask(nodeId: Long) {
        viewModelScope.launch {
            repository.convertDecisionToTask(nodeId)
        }
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
