/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.NodeWithPin
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
import com.tajemniktv.tajsos.ui.components.modes.StateAwareActionsGrid
import com.tajemniktv.tajsos.ui.components.nodes.SuggestionGroup
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.dash_area_health
import tajsos.composeapp.generated.resources.dash_decisions
import tajsos.composeapp.generated.resources.dash_inbox_new
import tajsos.composeapp.generated.resources.dash_inbox_overflow
import tajsos.composeapp.generated.resources.dash_maintenance
import tajsos.composeapp.generated.resources.dash_open_loops
import tajsos.composeapp.generated.resources.dash_protocols
import tajsos.composeapp.generated.resources.dash_review_pending
import tajsos.composeapp.generated.resources.dash_review_pending_desc
import tajsos.composeapp.generated.resources.dash_search_placeholder
import tajsos.composeapp.generated.resources.dash_suggestion_low_focus
import tajsos.composeapp.generated.resources.dash_suggestion_meds
import tajsos.composeapp.generated.resources.dash_suggestion_stress

@Composable
internal fun renderTodayPulseBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    TodayPulseCard(
        progress = context.dailyProgress,
        tasks = context.pinnedNodes,
        onToggleTask = { nodeWithPin ->
            val newStatus = if (nodeWithPin.node.status == "done") "active" else "done"
            context.viewModel.updateNodeStatus(nodeWithPin.node, newStatus)
        },
        onTaskClick = { context.onEditNode(it) },
        onClick = { context.onNavigateTo(Screen.Today) },
    )
}

@Composable
private fun renderForgottenWisdom(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    val forgottenWisdom = context.dashboardState.forgottenWisdom
    if (forgottenWisdom != null) {
        DashCard(onClick = {
            context.onEditNode(
                forgottenWisdom
                    .node.id,
            )
        }) {
            Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                Text(
                    "FORGOTTEN WISDOM",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    forgottenWisdom
                        .node.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TactileTheme.Text,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    forgottenWisdom
                        .node.content
                        .take(100) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                )
            }
        }
    }
}

@Composable
internal fun renderLoadCapacityBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    SystemStatusCard(
        load = context.dashboardState.systemLoad,
        fragmentation = context.dashboardState.fragmentation,
        warning = context.dashboardState.capacityWarning,
        onClick = { context.onNavigateTo(Screen.Insights) },
    )
}

@Composable
internal fun renderAreaHealthBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    if (context.allAreas.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            Text(
                stringResource(Res.string.dash_area_health),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            if (context.dashboardState.areaImbalanceScore >= 30) {
                Text(
                    "IMBALANCE ${context.dashboardState.areaImbalanceScore}% // ${context.dashboardState.areaImbalanceLabel.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (context.dashboardState.areaImbalanceScore >= 60) TactileTheme.Error else TactileTheme.Accent,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                context.allAreas.forEach { area ->
                    AreaHealthCard(
                        area = area,
                        metrics = context.dashboardState.areaHealthMetrics[area.id],
                        onClick = {
                            context.viewModel.clearSearchFilters()
                            context.viewModel.updateSearchAreaFilter(area.id)
                            context.onNavigateTo(Screen.Search)
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun renderOperationalBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    if (context.dashboardState.openLoops.isNotEmpty() ||
        context.dashboardState.pendingDecisions.isNotEmpty() ||
        context.dashboardState.maintenanceQueue.isNotEmpty()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            Text(
                "LIFE OS // OPERATIONAL",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Accent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )

            TextButton(onClick = { context.onNavigateTo(Screen.OpenLoops) }) {
                Text(
                    text = "OPEN OPEN LOOPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
            }

            context.dashboardState.openLoopsOverloadWarning?.let { warning ->
                Text(
                    warning,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error,
                )
            }
            context.dashboardState.maintenanceOverdueWarning?.let { warning ->
                Text(
                    warning,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error,
                )
            }

            if (context.dashboardState.openLoops.isNotEmpty()) {
                SuggestionGroup(
                    title = stringResource(Res.string.dash_open_loops),
                    icon = Icons.Default.AllInclusive,
                    color = TactileTheme.Accent,
                    nodes = context.dashboardState.openLoops,
                    onEditNode = context.onEditNode,
                )
            }

            if (context.dashboardState.pendingDecisions.isNotEmpty()) {
                SuggestionGroup(
                    title = stringResource(Res.string.dash_decisions),
                    icon = Icons.Default.QuestionMark,
                    color = TactileTheme.Primary,
                    nodes = context.dashboardState.pendingDecisions,
                    onEditNode = context.onEditNode,
                )
            }

            if (context.dashboardState.maintenanceQueue.isNotEmpty()) {
                SuggestionGroup(
                    title = stringResource(Res.string.dash_maintenance),
                    icon = Icons.Default.Settings,
                    color = TactileTheme.Success,
                    nodes = context.dashboardState.maintenanceQueue,
                    onEditNode = context.onEditNode,
                )
            }
        }
    }
}

@Composable
internal fun renderTimeArchitectureBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    val timeSnapshot by context.viewModel.timeArchitectureSnapshot.collectAsState()
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
            onClick = { context.onNavigateTo(Screen.TimeArchitecture) },
        )
        if (timeSnapshot.examPeriodMode) {
            AlertCard(
                title = "EXAM PERIOD MODE",
                description = "Countdown detected in <= 30 days. Tighten weekly plan.",
                icon = Icons.Default.School,
                color = TactileTheme.Error,
                onClick = { context.onNavigateTo(Screen.Study) },
            )
        }
        if (timeSnapshot.countdowns.isNotEmpty()) {
            SuggestionGroup(
                title = "COUNTDOWNS",
                icon = Icons.Default.HourglassBottom,
                color = TactileTheme.Accent,
                nodes = timeSnapshot.countdowns.map { it.node },
                onEditNode = context.onEditNode,
            )
        }
        if (timeSnapshot.shortHorizonTasks.isNotEmpty()) {
            SuggestionGroup(
                title = "SHORT HORIZON",
                icon = Icons.Default.Bolt,
                color = TactileTheme.Success,
                nodes = timeSnapshot.shortHorizonTasks,
                onEditNode = context.onEditNode,
            )
        }
    }
}

@Composable
internal fun renderSearchBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    OutlinedTextField(
        value = "",
        onValueChange = {
            context.viewModel.updateSearchQuery(it)
            context.onNavigateTo(Screen.Search)
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

@Composable
internal fun renderAlertsBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
        context.activeReminders.forEach { node ->
            AlertCard(
                title = "REMINDER: ${node.title}",
                description = "Active notification threshold reached.",
                icon = Icons.Default.NotificationsActive,
                color = TactileTheme.Error,
                action = {
                    IconButton(
                        onClick = { context.viewModel.updateNode(node.copy(reminderAt = null)) },
                        modifier = Modifier.padding(0.dp),
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = TactileTheme.Error,
                        )
                    }
                },
                onClick = { context.onEditNode(node.id) },
            )
        }

        if (context.needsWeeklyReview) {
            AlertCard(
                title = stringResource(Res.string.dash_review_pending),
                description = stringResource(Res.string.dash_review_pending_desc),
                icon = Icons.Default.EventRepeat,
                color = TactileTheme.Primary,
                onClick = { context.onNavigateTo(Screen.Review) },
            )
        }

        if (context.inboxNodes.isNotEmpty()) {
            AlertCard(
                title =
                    if (context.inboxNodes.size > 10) {
                        stringResource(Res.string.dash_inbox_overflow, context.inboxNodes.size)
                    } else {
                        stringResource(Res.string.dash_inbox_new, context.inboxNodes.size)
                    },
                description = "Process items to clear your mental buffer.",
                icon = if (context.inboxNodes.size > 10) Icons.Default.Warning else Icons.Default.MailOutline,
                color = if (context.inboxNodes.size > 10) TactileTheme.Error else TactileTheme.Accent,
                onClick = { context.onNavigateTo(Screen.Inbox) },
            )
        }

        if (context.dashboardState.overdueNodes.isNotEmpty()) {
            AlertCard(
                title = "${context.dashboardState.overdueNodes.size} OVERDUE ENTRIES",
                description = "Deadlines exceeded. System integrity at risk.",
                icon = Icons.Default.Warning,
                color = TactileTheme.Error,
                onClick = {
                    context.viewModel.clearSearchFilters()
                    context.viewModel.updateSearchStatusFilter("active")
                    context.onNavigateTo(Screen.Search)
                },
            )
        }

        context.moodToday?.let { mood ->
            if ((mood.anxietyScore ?: 0) >= 4) {
                AlertCard(
                    title = "STRESS DETECTED",
                    description = stringResource(Res.string.dash_suggestion_stress),
                    icon = Icons.Default.Psychology,
                    color = TactileTheme.Accent,
                    onClick = { context.onNavigateTo(Screen.Review) },
                )
            }
            if ((mood.focusScore ?: 5) <= 2) {
                AlertCard(
                    title = "LOW FOCUS PHASE",
                    description = stringResource(Res.string.dash_suggestion_low_focus),
                    icon = Icons.Default.Lightbulb,
                    color = TactileTheme.Accent,
                    onClick = {
                        context.viewModel.clearSearchFilters()
                        context.viewModel.updateSearchMaxMinutesFilter(5)
                        context.onNavigateTo(Screen.Search)
                    },
                )
            }
            if (!mood.tookMeds && context.localNow.hour >= 10) {
                AlertCard(
                    title = "MEDICATION LOG PENDING",
                    description = stringResource(Res.string.dash_suggestion_meds),
                    icon = Icons.Default.MedicalServices,
                    color = TactileTheme.Accent,
                    onClick = { context.onNavigateTo(Screen.Track) },
                )
            }
        }
    }
}

@Composable
internal fun renderStickyBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    if (context.dashboardState.stickyNotes.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
        ) {
            context.dashboardState.stickyNotes.forEach { note ->
                StickyNoteCard(
                    title = note.node.title,
                    content = note.node.content,
                    onClick = { context.onEditNode(note.node.id) },
                )
            }
        }
    }
}

@Composable
internal fun renderFocusBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    FocusCard(
        viewModel = context.viewModel,
        activeSession = context.activeSession,
        onToggleFocus = {
            if (context.activeSession != null) {
                context.viewModel.stopFocusSession()
            } else {
                context.pinnedNodes
                    .firstOrNull()
                    ?.let { context.viewModel.startFocusSession(it.node.id) }
            }
        },
        onClick = { context.onNavigateTo(Screen.Focus) },
    )
}

@Composable
internal fun renderInsightsBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    LifeSummaryCard(
        captures = context.insights.weeklyCaptures,
        completions = context.insights.weeklyCompletions,
        onClick = { context.onNavigateTo(Screen.Insights) },
    )
}

@Composable
internal fun renderActionsBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    StateAwareActionsGrid(viewModel = context.viewModel, onNavigateTo = context.onNavigateTo)
}

@Composable
internal fun renderSuggestionsBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg)) {
        val suggestedContextKey = context.dashboardState.suggestedContextKey
        if (context.dashboardState.suggestedContextTasks.isNotEmpty() && suggestedContextKey != null) {
            SuggestionGroup(
                title = "CONTEXT MATCH // ${suggestedContextKey.replace("_", " ").uppercase()}",
                icon = Icons.Default.Explore,
                color = TactileTheme.Primary,
                nodes = context.dashboardState.suggestedContextTasks,
                onEditNode = context.onEditNode,
            )
        }

        if (context.dashboardState.lowEnergyTasks.isNotEmpty() && (
                context.moodToday?.energyScore
                    ?: 5
            ) <= 2
        ) {
            SuggestionGroup(
                title = "RECOVERY MODE // LOW ENERGY",
                icon = Icons.Default.BatteryChargingFull,
                color = TactileTheme.Success,
                nodes = context.dashboardState.lowEnergyTasks,
                onEditNode = context.onEditNode,
            )
        }

        if (context.dashboardState.batchableTasks.isNotEmpty()) {
            val firstBatch =
                context.dashboardState.batchableTasks.values
                    .first()
            val areaName =
                context.allAreas.find { it.id == firstBatch.first().node.areaId }?.title
                    ?: "GENERAL"
            SuggestionGroup(
                title = "BATCH SUGGESTION // $areaName",
                icon = Icons.Default.Layers,
                color = TactileTheme.Accent,
                nodes = firstBatch,
                onEditNode = context.onEditNode,
                description = "You have ${firstBatch.size} tasks in $areaName. Batch them?",
            )
        }

        if (context.dashboardState.quickWins.isNotEmpty()) {
            SuggestionGroup(
                title = "QUICK WINS // EASY FRICTION",
                icon = Icons.Default.Bolt,
                color = TactileTheme.Success,
                nodes = context.dashboardState.quickWins,
                onEditNode = context.onEditNode,
            )
        }

        if (context.dashboardState.deepWork.isNotEmpty()) {
            SuggestionGroup(
                title = "DEEP WORK // HIGH ENERGY",
                icon = Icons.Default.Psychology,
                color = TactileTheme.Primary,
                nodes = context.dashboardState.deepWork,
                onEditNode = context.onEditNode,
            )
        }

        if (context.dashboardState.criticalProjects.isNotEmpty()) {
            SuggestionGroup(
                title = "NEEDS ATTENTION // CRITICAL PROJECTS",
                icon = Icons.Default.AccountTree,
                color = TactileTheme.Error,
                nodes = context.dashboardState.criticalProjects.map { NodeWithPin(it, null) },
                onEditNode = { context.onNavigateToProject(it) },
            )
        }

        if (context.dashboardState.deservesAttention.isNotEmpty()) {
            SuggestionGroup(
                title = "DESERVES ATTENTION // NEGLECTED",
                icon = Icons.Default.NotificationImportant,
                color = TactileTheme.Accent,
                nodes = context.dashboardState.deservesAttention,
                onEditNode = context.onEditNode,
            )
        }

        if (context.dashboardState.upcomingDeadlines.isNotEmpty()) {
            SuggestionGroup(
                title = "UPCOMING DEADLINES",
                icon = Icons.Default.DateRange,
                color = TactileTheme.Accent,
                nodes = context.dashboardState.upcomingDeadlines,
                onEditNode = context.onEditNode,
            )
        }
    }
}

@Composable
internal fun renderKnowledgeBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
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
                count = context.dashboardState.readLaterVault.size,
                icon = Icons.Default.Bookmark,
                onClick = {
                    context.viewModel.clearSearchFilters()
                    context.viewModel.updateSearchTypeFilter("note")
                    context.viewModel.updateSearchStatusFilter("active")
                    context.onNavigateTo(Screen.Search)
                },
            )
            VaultCard(
                modifier = Modifier.weight(1f),
                title = "QUOTES",
                count = context.dashboardState.quoteVault.size,
                icon = Icons.Default.FormatQuote,
                onClick = { context.onNavigateTo(Screen.Search) },
            )
            VaultCard(
                modifier = Modifier.weight(1f),
                title = "IDEAS",
                count = context.dashboardState.ideaIncubator.size,
                icon = Icons.Default.Lightbulb,
                onClick = { context.onNavigateTo(Screen.Search) },
            )
        }

        if (context.dashboardState.pinnedKnowledge.isNotEmpty()) {
            SuggestionGroup(
                title = "PINNED KNOWLEDGE",
                icon = Icons.Default.Favorite,
                color = TactileTheme.Primary,
                nodes = context.dashboardState.pinnedKnowledge,
                onEditNode = context.onEditNode,
            )
        }

        if (context.dashboardState.foundationalNotes.isNotEmpty()) {
            SuggestionGroup(
                title = "FOUNDATIONAL PRINCIPLE",
                icon = Icons.Default.AutoAwesome,
                color = TactileTheme.Accent,
                nodes = context.dashboardState.foundationalNotes,
                onEditNode = context.onEditNode,
            )
        }

        renderForgottenWisdom(context)

        if (context.dashboardState.resourceHighlights.isNotEmpty()) {
            SuggestionGroup(
                title = "RESOURCE HIGHLIGHTS",
                icon = Icons.AutoMirrored.Filled.LibraryBooks,
                color = TactileTheme.Primary,
                nodes = context.dashboardState.resourceHighlights,
                onEditNode = context.onEditNode,
            )
        }

        context.allProjects.maxByOrNull { it.updatedAt }?.let { project ->
            MetricCard(
                label = "RELEVANT PROJECT",
                value = project.title,
                secondaryLabel = "LAST UPDATED",
                icon = Icons.AutoMirrored.Filled.List,
                iconColor = TactileTheme.Primary,
                onClick = { context.onNavigateToProject(project.id) },
            )
        }

        context.dashboardState.relevantNote?.let { nodeWithPin ->
            MetricCard(
                label = "RELEVANT NOTE",
                value = nodeWithPin.node.title,
                secondaryLabel = "RECENT ACTIVITY",
                icon = Icons.Default.Edit,
                iconColor = TactileTheme.Primary,
                onClick = { context.onEditNode(nodeWithPin.node.id) },
            )
        }
    }
}

@Composable
internal fun renderProtocolsBlock(context: com.tajemniktv.tajsos.ui.screens.dashboard.DashboardBlockContext) {
    val transitionSnapshot by context.viewModel.transitionProtocolsSnapshot.collectAsState()
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
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        ) {
            context.viewModel.transitionProtocolTemplates.forEach { template ->
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
                        context.viewModel.triggerProtocol(template.label)
                        context.onNavigateTo(destination)
                    },
                )
            }
        }

        if (context.dashboardState.activeProtocols.isNotEmpty()) {
            SuggestionGroup(
                title = "ACTIVE PROTOCOLS",
                icon = Icons.Default.RocketLaunch,
                color = TactileTheme.Primary,
                nodes = context.dashboardState.activeProtocols,
                onEditNode = context.onEditNode,
            )
        }
    }
}
