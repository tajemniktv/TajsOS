/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.*
import com.tajemniktv.tajsos.ui.DashboardUIState
import com.tajemniktv.tajsos.ui.InsightsData
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.cards.AlertCard
import com.tajemniktv.tajsos.ui.components.cards.AreaHealthCard
import com.tajemniktv.tajsos.ui.components.cards.DashCard
import com.tajemniktv.tajsos.ui.components.cards.FocusCard
import com.tajemniktv.tajsos.ui.components.cards.LifeSummaryCard
import com.tajemniktv.tajsos.ui.components.cards.MetricCard
import com.tajemniktv.tajsos.ui.components.cards.StickyNoteCard
import com.tajemniktv.tajsos.ui.components.cards.SystemStatusCard
import com.tajemniktv.tajsos.ui.components.cards.TodayPulseCard
import com.tajemniktv.tajsos.ui.components.cards.VaultCard
import com.tajemniktv.tajsos.ui.components.layout.ProtocolTrigger
import com.tajemniktv.tajsos.ui.components.modes.RecoveryBasicsBlock
import com.tajemniktv.tajsos.ui.components.modes.StateAwareActionsGrid
import com.tajemniktv.tajsos.ui.components.nodes.*
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@Composable
fun DashboardBlockRenderer(
    blockKey: String,
    viewModel: MainViewModel,
    dashboardState: DashboardUIState,
    pinnedNodes: List<NodeWithPin>,
    allProjects: List<NodeEntity>,
    allAreas: List<NodeEntity>,
    inboxNodes: List<NodeWithPin>,
    activeReminders: List<NodeEntity>,
    activeSession: FocusSessionEntity?,
    insights: InsightsData,
    moodToday: TrackEntryEntity?,
    needsWeeklyReview: Boolean,
    dailyProgress: Float,
    localNow: LocalDateTime,
    onNavigateTo: (Screen) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
) {
    when (blockKey)
    {
        "today_pulse", "today_top_3" -> {
            TodayPulseCard(
                progress = dailyProgress,
                tasks = pinnedNodes,
                onToggleTask = { nodeWithPin ->
                    val newStatus =
                        if (nodeWithPin.node.status == "done") "active" else "done"
                    viewModel.updateNodeStatus(nodeWithPin.node, newStatus)
                },
                onTaskClick = { onEditNode(it) },
                onClick = { onNavigateTo(Screen.Today) },
            )
        }

        "load_capacity" -> {
            SystemStatusCard(
                load = dashboardState.systemLoad,
                fragmentation = dashboardState.fragmentation,
                warning = dashboardState.capacityWarning,
                onClick = { onNavigateTo(Screen.Insights) },
            )
        }

        "area_health" -> {
            if (allAreas.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                    Text(
                        stringResource(Res.string.dash_area_health),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                    if (dashboardState.areaImbalanceScore >= 30) {
                        Text(
                            "IMBALANCE ${dashboardState.areaImbalanceScore}% // ${dashboardState.areaImbalanceLabel.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (dashboardState.areaImbalanceScore >= 60) TactileTheme.Error else TactileTheme.Accent,
                        )
                    }
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        allAreas.forEach { area ->
                            AreaHealthCard(
                                area = area,
                                metrics = dashboardState.areaHealthMetrics[area.id],
                                onClick = {
                                    viewModel.clearSearchFilters()
                                    viewModel.updateSearchAreaFilter(area.id)
                                    onNavigateTo(Screen.Search)
                                },
                            )
                        }
                    }
                }
            }
        }

        "operational" -> {
            if (dashboardState.openLoops.isNotEmpty() || dashboardState.pendingDecisions.isNotEmpty() ||
                dashboardState.maintenanceQueue.isNotEmpty()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                    Text(
                        "LIFE OS // OPERATIONAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Accent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    )

                    TextButton(onClick = { onNavigateTo(Screen.OpenLoops) }) {
                        Text(
                            text = "OPEN OPEN LOOPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Primary,
                        )
                    }
                    dashboardState.openLoopsOverloadWarning?.let { warning ->
                        Text(
                            warning,
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Error,
                        )
                    }
                    dashboardState.maintenanceOverdueWarning?.let { warning ->
                        Text(
                            warning,
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Error,
                        )
                    }

                    if (dashboardState.openLoops.isNotEmpty()) {
                        SuggestionGroup(
                            title = stringResource(Res.string.dash_open_loops),
                            icon = Icons.Default.AllInclusive,
                            color = TactileTheme.Accent,
                            nodes = dashboardState.openLoops,
                            onEditNode = onEditNode,
                        )
                    }

                    if (dashboardState.pendingDecisions.isNotEmpty()) {
                        SuggestionGroup(
                            title = stringResource(Res.string.dash_decisions),
                            icon = Icons.Default.QuestionMark,
                            color = TactileTheme.Primary,
                            nodes = dashboardState.pendingDecisions,
                            onEditNode = onEditNode,
                        )
                    }

                    if (dashboardState.maintenanceQueue.isNotEmpty()) {
                        SuggestionGroup(
                            title = stringResource(Res.string.dash_maintenance),
                            icon = Icons.Default.Settings,
                            color = TactileTheme.Success,
                            nodes = dashboardState.maintenanceQueue,
                            onEditNode = onEditNode,
                        )
                    }
                }
            }
        }

        "time_architecture" -> {
            val timeSnapshot by viewModel.timeArchitectureSnapshot.collectAsState()
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                Text(
                    "TIME ARCHITECTURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Accent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                MetricCard(
                    label = "HORIZONS",
                    value = "TODAY ${timeSnapshot.todayLayer.size} • WEEK ${timeSnapshot.weekLayer.size} • MONTH ${timeSnapshot.monthLayer.size}",
                    secondaryLabel = "SEMESTER ${timeSnapshot.semesterLayer.size}",
                    icon = Icons.Default.Schedule,
                    iconColor = TactileTheme.Primary,
                    onClick = { onNavigateTo(Screen.TimeArchitecture) },
                )
                if (timeSnapshot.examPeriodMode) {
                    AlertCard(
                        title = "EXAM PERIOD MODE",
                        description = "Countdown detected in <= 30 days. Tighten weekly plan.",
                        icon = Icons.Default.School,
                        color = TactileTheme.Error,
                        onClick = { onNavigateTo(Screen.Study) },
                    )
                }
                if (timeSnapshot.countdowns.isNotEmpty()) {
                    SuggestionGroup(
                        title = "COUNTDOWNS",
                        icon = Icons.Default.HourglassBottom,
                        color = TactileTheme.Accent,
                        nodes = timeSnapshot.countdowns.map { it.node },
                        onEditNode = onEditNode,
                    )
                }
                if (timeSnapshot.shortHorizonTasks.isNotEmpty()) {
                    SuggestionGroup(
                        title = "SHORT HORIZON",
                        icon = Icons.Default.Bolt,
                        color = TactileTheme.Success,
                        nodes = timeSnapshot.shortHorizonTasks,
                        onEditNode = onEditNode,
                    )
                }
            }
        }

        "search" -> {
            OutlinedTextField(
                value = "",
                onValueChange = {
                    viewModel.updateSearchQuery(it)
                    onNavigateTo(Screen.Search)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(Res.string.dash_search_placeholder),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = TactileTheme.Primary,
                    )
                },
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = TactileTheme.Border,
                        focusedBorderColor = TactileTheme.Primary,
                        unfocusedContainerColor = TactileTheme.Surface,
                        focusedContainerColor = TactileTheme.Surface,
                    ),
            )
        }

        "alerts" -> {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                activeReminders.forEach { node ->
                    AlertCard(
                        title = "REMINDER: ${node.title}",
                        description = "Active notification threshold reached.",
                        icon = Icons.Default.NotificationsActive,
                        color = TactileTheme.Error,
                        action = {
                            IconButton(
                                onClick = {
                                    viewModel.updateNode(
                                        node.copy(
                                            reminderAt = null,
                                        ),
                                    )
                                },
                                modifier = Modifier.size(24.dp),
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = TactileTheme.Error,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        },
                        onClick = { onEditNode(node.id) },
                    )
                }

                if (needsWeeklyReview) {
                    AlertCard(
                        title = stringResource(Res.string.dash_review_pending),
                        description = stringResource(Res.string.dash_review_pending_desc),
                        icon = Icons.Default.EventRepeat,
                        color = TactileTheme.Primary,
                        onClick = { onNavigateTo(Screen.Review) },
                    )
                }

                if (inboxNodes.isNotEmpty()) {
                    AlertCard(
                        title =
                            if (inboxNodes.size > 10) {
                                stringResource(
                                    Res.string.dash_inbox_overflow,
                                    inboxNodes.size,
                                )
                            } else {
                                stringResource(Res.string.dash_inbox_new, inboxNodes.size)
                            },
                        description = "Process items to clear your mental buffer.",
                        icon = if (inboxNodes.size > 10) Icons.Default.Warning else Icons.Default.MailOutline,
                        color = if (inboxNodes.size > 10) TactileTheme.Error else TactileTheme.Accent,
                        onClick = { onNavigateTo(Screen.Inbox) },
                    )
                }

                if (dashboardState.overdueNodes.isNotEmpty()) {
                    AlertCard(
                        title = "${dashboardState.overdueNodes.size} OVERDUE ENTRIES",
                        description = "Deadlines exceeded. System integrity at risk.",
                        icon = Icons.Default.Warning,
                        color = TactileTheme.Error,
                        onClick = {
                            viewModel.clearSearchFilters()
                            viewModel.updateSearchStatusFilter("active")
                            onNavigateTo(Screen.Search)
                        },
                    )
                }

                moodToday?.let { mood ->
                    if ((mood.anxietyScore ?: 0) >= 4) {
                        AlertCard(
                            title = "STRESS DETECTED",
                            description = stringResource(Res.string.dash_suggestion_stress),
                            icon = Icons.Default.Psychology,
                            color = TactileTheme.Accent,
                            onClick = { onNavigateTo(Screen.Review) },
                        )
                    }
                    if ((mood.focusScore ?: 5) <= 2) {
                        AlertCard(
                            title = "LOW FOCUS PHASE",
                            description = stringResource(Res.string.dash_suggestion_low_focus),
                            icon = Icons.Default.Lightbulb,
                            color = TactileTheme.Accent,
                            onClick = {
                                viewModel.clearSearchFilters()
                                viewModel.updateSearchMaxMinutesFilter(5)
                                onNavigateTo(Screen.Search)
                            },
                        )
                    }
                    if (!mood.tookMeds && localNow.hour >= 10) {
                        AlertCard(
                            title = "MEDICATION LOG PENDING",
                            description = stringResource(Res.string.dash_suggestion_meds),
                            icon = Icons.Default.MedicalServices,
                            color = TactileTheme.Accent,
                            onClick = { onNavigateTo(Screen.Track) },
                        )
                    }
                }
            }
        }

        "sticky" -> {
            if (dashboardState.stickyNotes.isNotEmpty()) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                ) {
                    dashboardState.stickyNotes.forEach { note ->
                        StickyNoteCard(
                            title = note.node.title,
                            content = note.node.content,
                            onClick = { onEditNode(note.node.id) },
                        )
                    }
                }
            }
        }

        "focus", "current_task", "timer" -> {
            FocusCard(
                viewModel = viewModel,
                activeSession = activeSession,
                onToggleFocus = {
                    if (activeSession != null) {
                        viewModel.stopFocusSession()
                    } else {
                        pinnedNodes
                            .firstOrNull()
                            ?.let { viewModel.startFocusSession(it.node.id) }
                    }
                },
                onClick = { onNavigateTo(Screen.Focus) },
            )
        }

        "insights" -> {
            LifeSummaryCard(
                captures = insights.weeklyCaptures,
                completions = insights.weeklyCompletions,
                onClick = { onNavigateTo(Screen.Insights) },
            )
        }

        "actions" -> {
            StateAwareActionsGrid(viewModel = viewModel, onNavigateTo = onNavigateTo)
        }

        "suggestions", "easy_wins" -> {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg)) {
                val suggestedContextKey = dashboardState.suggestedContextKey
                if (dashboardState.suggestedContextTasks.isNotEmpty() && suggestedContextKey != null) {
                    SuggestionGroup(
                        title = "CONTEXT MATCH // ${
                            suggestedContextKey.replace("_", " ").uppercase()
                        }",
                        icon = Icons.Default.Explore,
                        color = TactileTheme.Primary,
                        nodes = dashboardState.suggestedContextTasks,
                        onEditNode = onEditNode,
                    )
                }

                if (dashboardState.lowEnergyTasks.isNotEmpty() && (
                        moodToday?.energyScore
                            ?: 5
                    ) <= 2
                ) {
                    SuggestionGroup(
                        title = "RECOVERY MODE // LOW ENERGY",
                        icon = Icons.Default.BatteryChargingFull,
                        color = TactileTheme.Success,
                        nodes = dashboardState.lowEnergyTasks,
                        onEditNode = onEditNode,
                    )
                }

                if (dashboardState.batchableTasks.isNotEmpty()) {
                    val firstBatch = dashboardState.batchableTasks.values.first()
                    val areaName =
                        allAreas.find { it.id == firstBatch.first().node.areaId }?.title
                            ?: "GENERAL"
                    SuggestionGroup(
                        title = "BATCH SUGGESTION // $areaName",
                        icon = Icons.Default.Layers,
                        color = TactileTheme.Accent,
                        nodes = firstBatch,
                        onEditNode = onEditNode,
                        description = "You have ${firstBatch.size} tasks in $areaName. Batch them?",
                    )
                }

                if (dashboardState.quickWins.isNotEmpty()) {
                    SuggestionGroup(
                        title = "QUICK WINS // EASY FRICTION",
                        icon = Icons.Default.Bolt,
                        color = TactileTheme.Success,
                        nodes = dashboardState.quickWins,
                        onEditNode = onEditNode,
                    )
                }

                if (dashboardState.deepWork.isNotEmpty()) {
                    SuggestionGroup(
                        title = "DEEP WORK // HIGH ENERGY",
                        icon = Icons.Default.Psychology,
                        color = TactileTheme.Primary,
                        nodes = dashboardState.deepWork,
                        onEditNode = onEditNode,
                    )
                }

                if (dashboardState.criticalProjects.isNotEmpty()) {
                    SuggestionGroup(
                        title = "NEEDS ATTENTION // CRITICAL PROJECTS",
                        icon = Icons.Default.AccountTree,
                        color = TactileTheme.Error,
                        nodes =
                            dashboardState.criticalProjects.map {
                                NodeWithPin(
                                    it,
                                    null,
                                )
                            },
                        onEditNode = { onNavigateToProject(it) },
                    )
                }

                if (dashboardState.deservesAttention.isNotEmpty()) {
                    SuggestionGroup(
                        title = "DESERVES ATTENTION // NEGLECTED",
                        icon = Icons.Default.NotificationImportant,
                        color = TactileTheme.Accent,
                        nodes = dashboardState.deservesAttention,
                        onEditNode = onEditNode,
                    )
                }

                if (dashboardState.upcomingDeadlines.isNotEmpty()) {
                    SuggestionGroup(
                        title = "UPCOMING DEADLINES",
                        icon = Icons.Default.DateRange,
                        color = TactileTheme.Accent,
                        nodes = dashboardState.upcomingDeadlines,
                        onEditNode = onEditNode,
                    )
                }
            }
        }

        "knowledge", "pinned_note" -> {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg)) {
                Text(
                    "KNOWLEDGE & CONTEXT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Accent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                ) {
                    VaultCard(
                        modifier = Modifier.weight(1f),
                        title = "READ LATER",
                        count = dashboardState.readLaterVault.size,
                        icon = Icons.Default.Bookmark,
                        onClick = {
                            viewModel.clearSearchFilters()
                            viewModel.updateSearchTypeFilter("note")
                            viewModel.updateSearchStatusFilter("active")
                            onNavigateTo(Screen.Search)
                        },
                    )
                    VaultCard(
                        modifier = Modifier.weight(1f),
                        title = "QUOTES",
                        count = dashboardState.quoteVault.size,
                        icon = Icons.Default.FormatQuote,
                        onClick = { onNavigateTo(Screen.Search) },
                    )
                    VaultCard(
                        modifier = Modifier.weight(1f),
                        title = "IDEAS",
                        count = dashboardState.ideaIncubator.size,
                        icon = Icons.Default.Lightbulb,
                        onClick = { onNavigateTo(Screen.Search) },
                    )
                }

                if (dashboardState.pinnedKnowledge.isNotEmpty()) {
                    SuggestionGroup(
                        title = "PINNED KNOWLEDGE",
                        icon = Icons.Default.Favorite,
                        color = TactileTheme.Primary,
                        nodes = dashboardState.pinnedKnowledge,
                        onEditNode = onEditNode,
                    )
                }

                if (dashboardState.foundationalNotes.isNotEmpty()) {
                    SuggestionGroup(
                        title = "FOUNDATIONAL PRINCIPLE",
                        icon = Icons.Default.AutoAwesome,
                        color = TactileTheme.Accent,
                        nodes = dashboardState.foundationalNotes,
                        onEditNode = onEditNode,
                    )
                }

                if (dashboardState.forgottenWisdom != null) {
                    DashCard(onClick = { onEditNode(dashboardState.forgottenWisdom!!.node.id) }) {
                        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                            Text(
                                "FORGOTTEN WISDOM",
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Muted,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                dashboardState.forgottenWisdom!!.node.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = TactileTheme.Text,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                dashboardState.forgottenWisdom!!
                                    .node.content
                                    .take(100) + "...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TactileTheme.Muted,
                            )
                        }
                    }
                }

                if (dashboardState.resourceHighlights.isNotEmpty()) {
                    SuggestionGroup(
                        title = "RESOURCE HIGHLIGHTS",
                        icon = Icons.AutoMirrored.Filled.LibraryBooks,
                        color = TactileTheme.Primary,
                        nodes = dashboardState.resourceHighlights,
                        onEditNode = onEditNode,
                    )
                }

                // Relevant Contexts
                allProjects.maxByOrNull { it.updatedAt }?.let { project ->
                    MetricCard(
                        label = "RELEVANT PROJECT",
                        value = project.title,
                        secondaryLabel = "LAST UPDATED",
                        icon = Icons.AutoMirrored.Filled.List,
                        iconColor = TactileTheme.Primary,
                        onClick = { onNavigateToProject(project.id) },
                    )
                }

                dashboardState.relevantNote?.let { nodeWithPin ->
                    MetricCard(
                        label = "RELEVANT NOTE",
                        value = nodeWithPin.node.title,
                        secondaryLabel = "RECENT ACTIVITY",
                        icon = Icons.Default.Edit,
                        iconColor = TactileTheme.Primary,
                        onClick = { onEditNode(nodeWithPin.node.id) },
                    )
                }
            }
        }

        "protocols" -> {
            val transitionSnapshot by viewModel.transitionProtocolsSnapshot.collectAsState()
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    stringResource(Res.string.dash_protocols),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                transitionSnapshot.recommendedLabel?.let { recommended ->
                    Text(
                        "SUGGESTED NOW // ${recommended.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Accent,
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                ) {
                    viewModel.transitionProtocolTemplates.forEach { template ->
                        val (icon, color, destination) =
                            when (template.key)
                            {
                                "morning_startup" -> {
                                    Triple(
                                        Icons.Default.WbSunny,
                                        TactileTheme.Primary,
                                        Screen.Review,
                                    )
                                }

                                "deep_work_entry" -> {
                                    Triple(
                                        Icons.Default.Psychology,
                                        TactileTheme.Accent,
                                        Screen.Focus,
                                    )
                                }

                                "shutdown_ritual" -> {
                                    Triple(
                                        Icons.Default.Brightness3,
                                        TactileTheme.Success,
                                        Screen.Review,
                                    )
                                }

                                "recovery_after_derailment" -> {
                                    Triple(
                                        Icons.Default.MedicalServices,
                                        TactileTheme.Error,
                                        Screen.Track,
                                    )
                                }

                                "exam_week" -> {
                                    Triple(
                                        Icons.Default.School,
                                        TactileTheme.Accent,
                                        Screen.Study,
                                    )
                                }

                                "travel_day" -> {
                                    Triple(
                                        Icons.Default.Flight,
                                        TactileTheme.Primary,
                                        Screen.Places,
                                    )
                                }

                                else -> {
                                    Triple(
                                        Icons.Default.RocketLaunch,
                                        TactileTheme.Primary,
                                        Screen.Protocols,
                                    )
                                }
                            }
                        ProtocolTrigger(
                            label = template.label.uppercase(),
                            icon = icon,
                            color = color,
                            onClick = {
                                viewModel.triggerProtocol(template.label)
                                onNavigateTo(destination)
                            },
                        )
                    }
                }

                if (dashboardState.activeProtocols.isNotEmpty()) {
                    SuggestionGroup(
                        title = "ACTIVE PROTOCOLS",
                        icon = Icons.Default.RocketLaunch,
                        color = TactileTheme.Primary,
                        nodes = dashboardState.activeProtocols,
                        onEditNode = onEditNode,
                    )
                }
            }
        }

        "basics", "survival_basics" -> {
            RecoveryBasicsBlock(
                onMedsClick = { onNavigateTo(Screen.Track) },
                onHydrationClick = { /* hydration track later */ },
                onFoodClick = { /* food track later */ },
            )
        }

        "shopping_list", "place_based_tasks", "errands" -> {
            ErrandListBlock(
                errands = dashboardState.shoppingList,
                onEdit = onEditNode,
            )
        }

        "tiny_wins", "tiny_victories" -> {
            TinyVictoriesBlock(
                victories = dashboardState.tinyVictories,
                onEdit = onEditNode,
            )
        }

        "current_focus" -> {
            CurrentTaskBlock(
                activeTask = pinnedNodes.firstOrNull(),
                onEdit = onEditNode,
            )
        }

        "classes", "assignments", "revision_targets" -> {
            val studentBoard by viewModel.studentBoardState.collectAsState()
            val studyNodes =
                when (blockKey)
                {
                    "classes" -> studentBoard.assignmentTracker
                    "assignments" -> studentBoard.assignmentDeadlines
                    else -> studentBoard.revisitBeforeExam
                }
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                SuggestionGroup(
                    title = "STUDY MODULE // ${blockKey.uppercase()}",
                    icon = Icons.Default.School,
                    color = Color(0xFFFF9800),
                    nodes = studyNodes.take(5),
                    onEditNode = onEditNode,
                )
                TextButton(onClick = { onNavigateTo(Screen.Study) }) {
                    Text("OPEN STUDY WORKSPACE")
                }
            }
        }

        "paperwork", "bills", "renewals", "subscriptions", "bureaucracy" -> {
            SuggestionGroup(
                title = "ADMIN // ${blockKey.uppercase()}",
                icon = Icons.Default.Gavel,
                color = Color(0xFF607D8B),
                nodes = dashboardState.unresolvedBureaucracy,
                onEditNode = onEditNode,
            )
        }
    }
}
