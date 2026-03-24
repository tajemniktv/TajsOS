/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.ModuleButton
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateTo: (Screen) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onOpenDrawer: () -> Unit,
) {
    val allNodes by viewModel.allNodes.collectAsState()
    val todayNodes by viewModel.todayNodes.collectAsState()
    val trackEntries by viewModel.trackEntries.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val inboxNodes by viewModel.inboxNodes.collectAsState()
    val activeReminders by viewModel.activeReminders.collectAsState()
    val calendarEntries by viewModel.calendarEntries.collectAsState()
    val dashboardState by viewModel.dashboardUIState.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()

    val pinnedNodes = allNodes.filter { it.pin != null }
    val completedTodayCount = pinnedNodes.count { it.node.status == "done" }
    val totalTodayCount = pinnedNodes.size
    val dailyProgress =
        if (totalTodayCount > 0) completedTodayCount.toFloat() / totalTodayCount else 0f

    val now = Clock.System.now()
    val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val todayDateStr = localNow.date.toString()
    val currentHour = localNow.hour
    val moodToday = trackEntries.find { it.date == todayDateStr }

    val lastWeeklyReview = allReviews.find { it.type == "weekly" }
    val weekMillis = 7 * 24 * 60 * 60 * 1000L
    val needsWeeklyReview =
        lastWeeklyReview == null || (now.toEpochMilliseconds() - lastWeeklyReview.completedAt) > weekMillis

    val scrollState = rememberScrollState()

    val vibeString = when (currentHour) {
        in 5..11 -> Res.string.dash_vibe_morning
        in 12..17 -> Res.string.dash_vibe_afternoon
        in 18..22 -> Res.string.dash_vibe_evening
        else -> Res.string.dash_vibe_night
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TactileTheme.Background)
            .verticalScroll(scrollState)
            .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg)
    ) {
        // 1. Header
        DashHeader(
            vibe = stringResource(vibeString),
            onMenuClick = onOpenDrawer,
            onSettingsClick = { onNavigateTo(Screen.Settings) }
        )

        // 2. Search & Quick Links
        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
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
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = TactileTheme.Primary
                    )
                },
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = TactileTheme.Border,
                    focusedBorderColor = TactileTheme.Primary,
                    unfocusedContainerColor = TactileTheme.Surface,
                    focusedContainerColor = TactileTheme.Surface
                )
            )

            if (allAreas.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
                ) {
                    allAreas.take(5).forEach { area ->
                        AssistChip(
                            onClick = {
                                viewModel.clearSearchFilters()
                                viewModel.updateSearchAreaFilter(area.id)
                                onNavigateTo(Screen.Search)
                            },
                            label = {
                                Text(
                                    area.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(TactileTheme.RadiusSm)
                        )
                    }
                }
            }
        }

        // 3. Alerts & Reminders (Critical Path)
        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            // Reminders with Dismiss
            activeReminders.forEach { node ->
                AlertCard(
                    title = "REMINDER: ${node.title}",
                    description = "Active notification threshold reached.",
                    icon = Icons.Default.NotificationsActive,
                    color = TactileTheme.Error,
                    action = {
                        IconButton(
                            onClick = { viewModel.updateNode(node.copy(reminderAt = null)) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = TactileTheme.Error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    onClick = { onEditNode(node.id) }
                )
            }

            if (needsWeeklyReview) {
                AlertCard(
                    title = stringResource(Res.string.dash_review_pending),
                    description = stringResource(Res.string.dash_review_pending_desc),
                    icon = Icons.Default.EventRepeat,
                    color = TactileTheme.Primary,
                    onClick = { onNavigateTo(Screen.Review) }
                )
            }

            if (inboxNodes.isNotEmpty()) {
                AlertCard(
                    title = if (inboxNodes.size > 10) stringResource(
                        Res.string.dash_inbox_overflow,
                        inboxNodes.size
                    )
                    else stringResource(Res.string.dash_inbox_new, inboxNodes.size),
                    description = "Process items to clear your mental buffer.",
                    icon = if (inboxNodes.size > 10) Icons.Default.Warning else Icons.Default.MailOutline,
                    color = if (inboxNodes.size > 10) TactileTheme.Error else TactileTheme.Accent,
                    onClick = { onNavigateTo(Screen.Inbox) }
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
                    }
                )
            }

            // Mood-based Suggestions
            moodToday?.let { mood ->
                if ((mood.anxietyScore ?: 0) >= 4) {
                    AlertCard(
                        title = "STRESS DETECTED",
                        description = stringResource(Res.string.dash_suggestion_stress),
                        icon = Icons.Default.Psychology,
                        color = TactileTheme.Accent,
                        onClick = { onNavigateTo(Screen.Review) }
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
                        }
                    )
                }
                if (!mood.tookMeds && localNow.hour >= 10) {
                    AlertCard(
                        title = "MEDICATION LOG PENDING",
                        description = stringResource(Res.string.dash_suggestion_meds),
                        icon = Icons.Default.MedicalServices,
                        color = TactileTheme.Accent,
                        onClick = { onNavigateTo(Screen.Track) }
                    )
                }
            }
        }

        // 4. Sticky Notes
        if (dashboardState.stickyNotes.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)
            ) {
                dashboardState.stickyNotes.forEach { note ->
                    StickyNoteCard(
                        title = note.node.title,
                        content = note.node.content,
                        onClick = { onEditNode(note.node.id) }
                    )
                }
            }
        }

        // 5. Daily Pulse & Today's Slots
        TodayPulseCard(
            progress = dailyProgress,
            tasks = pinnedNodes,
            onToggleTask = { nodeWithPin ->
                val newStatus = if (nodeWithPin.node.status == "done") "active" else "done"
                viewModel.updateNodeStatus(nodeWithPin.node, newStatus)
            },
            onTaskClick = { onEditNode(it) },
            onClick = { onNavigateTo(Screen.Today) }
        )

        // 6. Focus Engine
        FocusCard(
            viewModel = viewModel,
            activeSession = activeSession,
            onToggleFocus = {
                if (activeSession != null) {
                    viewModel.stopFocusSession()
                } else {
                    pinnedNodes.firstOrNull()?.let { viewModel.startFocusSession(it.node.id) }
                }
            },
            onClick = { onNavigateTo(Screen.Focus) }
        )

        // 7. Time-based Phase Cards
        if (currentHour in 5..11) {
            AlertCard(
                title = stringResource(Res.string.dash_morning_reset),
                description = stringResource(Res.string.dash_morning_reset_desc),
                icon = Icons.Default.WbSunny,
                color = TactileTheme.Primary,
                onClick = { onNavigateTo(Screen.Review) }
            )
        } else if (currentHour in 18..23) {
            AlertCard(
                title = stringResource(Res.string.dash_evening_shutdown),
                description = stringResource(Res.string.dash_evening_shutdown_desc),
                icon = Icons.Default.Brightness3,
                color = TactileTheme.Accent,
                onClick = { onNavigateTo(Screen.Review) }
            )
        }

        // 8. Performance Summary (Insights)
        LifeSummaryCard(
            captures = insights.weeklyCaptures,
            completions = insights.weeklyCompletions,
            onClick = { onNavigateTo(Screen.Insights) }
        )

        // 9. State-Aware Actions (Recovery, Overwhelmed, etc.)
        StateAwareActionsGrid(viewModel = viewModel, onNavigateTo = onNavigateTo)

        // 10. Operational Suggestions (Recovery, Batch, etc.)
        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg)) {
            if (dashboardState.lowEnergyTasks.isNotEmpty() && (moodToday?.energyScore ?: 5) <= 2) {
                SuggestionGroup(
                    title = "RECOVERY MODE // LOW ENERGY",
                    icon = Icons.Default.BatteryChargingFull,
                    color = TactileTheme.Success,
                    nodes = dashboardState.lowEnergyTasks,
                    onEditNode = onEditNode
                )
            }

            if (dashboardState.batchableTasks.isNotEmpty()) {
                val firstBatch = dashboardState.batchableTasks.values.first()
                val areaName =
                    allAreas.find { it.id == firstBatch.first().node.areaId }?.title ?: "GENERAL"
                SuggestionGroup(
                    title = "BATCH SUGGESTION // $areaName",
                    icon = Icons.Default.Layers,
                    color = TactileTheme.Accent,
                    nodes = firstBatch,
                    onEditNode = onEditNode,
                    description = "You have ${firstBatch.size} tasks in $areaName. Batch them?"
                )
            }

            if (dashboardState.quickWins.isNotEmpty()) {
                SuggestionGroup(
                    title = "QUICK WINS // EASY FRICTION",
                    icon = Icons.Default.Bolt,
                    color = TactileTheme.Success,
                    nodes = dashboardState.quickWins,
                    onEditNode = onEditNode
                )
            }

            if (dashboardState.deepWork.isNotEmpty()) {
                SuggestionGroup(
                    title = "DEEP WORK // HIGH ENERGY",
                    icon = Icons.Default.Psychology,
                    color = TactileTheme.Primary,
                    nodes = dashboardState.deepWork,
                    onEditNode = onEditNode
                )
            }

            if (dashboardState.criticalProjects.isNotEmpty()) {
                SuggestionGroup(
                    title = "NEEDS ATTENTION // CRITICAL PROJECTS",
                    icon = Icons.Default.AccountTree,
                    color = TactileTheme.Error,
                    nodes = dashboardState.criticalProjects.map {
                        com.tajemniktv.tajsos.data.NodeWithPin(
                            it,
                            null
                        )
                    },
                    onEditNode = { onNavigateToProject(it) }
                )
            }

            if (dashboardState.deservesAttention.isNotEmpty()) {
                SuggestionGroup(
                    title = "DESERVES ATTENTION // NEGLECTED",
                    icon = Icons.Default.NotificationImportant,
                    color = TactileTheme.Accent,
                    nodes = dashboardState.deservesAttention,
                    onEditNode = onEditNode
                )
            }

            if (dashboardState.upcomingDeadlines.isNotEmpty()) {
                SuggestionGroup(
                    title = "UPCOMING DEADLINES",
                    icon = Icons.Default.DateRange,
                    color = TactileTheme.Accent,
                    nodes = dashboardState.upcomingDeadlines,
                    onEditNode = onEditNode
                )
            }
        }

        // 11. Knowledge, Wisdom & Relevant Contexts
        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg)) {
            Text(
                "KNOWLEDGE & CONTEXT",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Accent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
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
                    }
                )
                VaultCard(
                    modifier = Modifier.weight(1f),
                    title = "QUOTES",
                    count = dashboardState.quoteVault.size,
                    icon = Icons.Default.FormatQuote,
                    onClick = { onNavigateTo(Screen.Search) }
                )
                VaultCard(
                    modifier = Modifier.weight(1f),
                    title = "IDEAS",
                    count = dashboardState.ideaIncubator.size,
                    icon = Icons.Default.Lightbulb,
                    onClick = { onNavigateTo(Screen.Search) }
                )
            }

            if (dashboardState.pinnedKnowledge.isNotEmpty()) {
                SuggestionGroup(
                    title = "PINNED KNOWLEDGE",
                    icon = Icons.Default.Favorite,
                    color = TactileTheme.Primary,
                    nodes = dashboardState.pinnedKnowledge,
                    onEditNode = onEditNode
                )
            }

            if (dashboardState.foundationalNotes.isNotEmpty()) {
                SuggestionGroup(
                    title = "FOUNDATIONAL PRINCIPLE",
                    icon = Icons.Default.AutoAwesome,
                    color = TactileTheme.Accent,
                    nodes = dashboardState.foundationalNotes,
                    onEditNode = onEditNode
                )
            }

            if (dashboardState.forgottenWisdom != null) {
                DashCard(onClick = { onEditNode(dashboardState.forgottenWisdom!!.node.id) }) {
                    Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                        Text(
                            "FORGOTTEN WISDOM",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            dashboardState.forgottenWisdom!!.node.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TactileTheme.Text
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            dashboardState.forgottenWisdom!!.node.content.take(100) + "...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Muted
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
                    onEditNode = onEditNode
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
                    onClick = { onNavigateToProject(project.id) }
                )
            }

            dashboardState.relevantNote?.let { nodeWithPin ->
                MetricCard(
                    label = "RELEVANT NOTE",
                    value = nodeWithPin.node.title,
                    secondaryLabel = "RECENT ACTIVITY",
                    icon = Icons.Default.Edit,
                    iconColor = TactileTheme.Primary,
                    onClick = { onEditNode(nodeWithPin.node.id) }
                )
            }
        }

        // 12. Modules Grid
        ModulesGrid(
            todayNodes = todayNodes,
            activeSession = activeSession,
            moodToday = moodToday,
            tasksCount = dashboardState.tasksCount,
            notesCount = dashboardState.notesCount,
            allProjects = allProjects,
            allAreas = allAreas,
            calendarEntries = calendarEntries,
            allSessions = allSessions,
            onNavigateTo = onNavigateTo,
            viewModel = viewModel
        )

        // 13. System Footer
        SystemFooter()

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun DashHeader(vibe: String, onMenuClick: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onMenuClick() }) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TactileTheme.Surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "T",
                    color = TactileTheme.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "TAJSOS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = TactileTheme.Text
                )
                Text(
                    vibe.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = CircleShape,
                border = BorderStroke(1.dp, TactileTheme.Border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(6.dp).clip(CircleShape)
                            .background(TactileTheme.Success)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "STATUS: OK",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TactileTheme.Text
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = TactileTheme.Muted)
            }
        }
    }
}

@Composable
fun StickyNoteCard(title: String, content: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        color = TactileTheme.Accent.copy(alpha = 0.1f),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Accent.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                color = TactileTheme.Accent,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                color = TactileTheme.Text
            )
        }
    }
}

@Composable
fun TodayPulseCard(
    progress: Float,
    tasks: List<com.tajemniktv.tajsos.data.NodeWithPin>,
    onToggleTask: (com.tajemniktv.tajsos.data.NodeWithPin) -> Unit,
    onTaskClick: (Long) -> Unit,
    onClick: () -> Unit
) {
    DashCard(onClick = onClick) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "Daily Pulse",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TactileTheme.Text
                    )
                }
                ProgressRing(progress = progress)
            }
            Spacer(Modifier.height(TactileTheme.SpacingLg))
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                tasks.filter { it.node.status == "active" }.take(3).forEach { nodeWithPin ->
                    TaskBrief(
                        title = nodeWithPin.node.title,
                        isDone = false,
                        onToggle = { onToggleTask(nodeWithPin) },
                        onClick = { onTaskClick(nodeWithPin.node.id) })
                }
                if (tasks.none { it.node.status == "active" }) {
                    Text(
                        "System clear for today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TactileTheme.Success,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FocusCard(
    viewModel: MainViewModel,
    activeSession: com.tajemniktv.tajsos.data.FocusSessionEntity?,
    onToggleFocus: () -> Unit,
    onClick: () -> Unit
) {
    val allNodes by viewModel.allNodes.collectAsState()
    val activeTask =
        activeSession?.let { session -> allNodes.find { it.node.id == session.nodeId }?.node }

    DashCard(onClick = onClick) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "FOCUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        if (activeSession != null) "Deep Work Phase" else "System Standby",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TactileTheme.Text
                    )
                }
                if (activeSession != null) {
                    val duration =
                        (Clock.System.now().toEpochMilliseconds() - activeSession.startedAt) / 1000
                    val h = duration / 3600;
                    val m = (duration % 3600) / 60;
                    val s = duration % 60
                    Text(
                        "${h.toString().padStart(2, '0')}:${
                            m.toString().padStart(2, '0')
                        }:${s.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(TactileTheme.SpacingMd))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (activeTask != null) "Active: ${activeTask.title}" else "Ready to engage",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = activeSession != null,
                    onCheckedChange = { onToggleFocus() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TactileTheme.Primary,
                        uncheckedThumbColor = TactileTheme.Muted,
                        uncheckedTrackColor = TactileTheme.Surface
                    )
                )
            }
        }
    }
}

@Composable
fun AlertCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    action: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.05f),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(Modifier.width(TactileTheme.SpacingMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Text
                )
            }
            action?.invoke()
        }
    }
}

@Composable
fun LifeSummaryCard(captures: Int, completions: Int, onClick: () -> Unit) {
    DashCard(onClick = onClick) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
            Text(
                "LIFE SUMMARY",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(TactileTheme.SpacingMd))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "$captures",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TactileTheme.Text
                    ); Text(
                    "CAPTURES / WEEK",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted
                )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$completions",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TactileTheme.Success
                    ); Text(
                    "DONE / WEEK",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted
                )
                }
            }
            Spacer(Modifier.height(TactileTheme.SpacingMd))
            LinearProgressIndicator(
                progress = {
                    if (captures > 0) (completions.toFloat() / captures.toFloat()).coerceIn(
                        0f,
                        1f
                    ) else 0f
                },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = TactileTheme.Primary,
                trackColor = TactileTheme.Border,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    secondaryLabel: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    DashCard(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    secondaryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = iconColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(TactileTheme.SpacingMd))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TactileTheme.Text,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StateAwareActionsGrid(viewModel: MainViewModel, onNavigateTo: (Screen) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
    ) {
        listOf(
            Triple(Icons.Default.Bolt, "OVERWHELMED", "Easy wins"),
            Triple(Icons.Default.BatteryChargingFull, "CANNOT THINK", "Low energy"),
            Triple(Icons.Default.Timer, "10 MINUTES", "Quick steps")
        ).forEach { (icon, title, desc) ->
            Surface(
                onClick = {
                    viewModel.clearSearchFilters(); when (title) {
                    "OVERWHELMED" -> {
                        viewModel.updateSearchFrictionFilter("easy"); viewModel.updateSearchEnergyFilter(
                            1
                        )
                    }; "CANNOT THINK" -> viewModel.updateSearchEnergyFilter(1); "10 MINUTES" -> viewModel.updateSearchMaxMinutesFilter(
                        10
                    )
                }; onNavigateTo(Screen.Search)
                },
                modifier = Modifier.weight(1f),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Border)
            ) {
                Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = TactileTheme.Primary,
                        modifier = Modifier.size(20.dp)
                    ); Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ); Text(
                    desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                    fontSize = 9.sp
                )
                }
            }
        }
    }
}

@Composable
fun SuggestionGroup(
    title: String,
    icon: ImageVector,
    color: Color,
    nodes: List<com.tajemniktv.tajsos.data.NodeWithPin>,
    onEditNode: (Long) -> Unit,
    description: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        if (description != null) Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted
        )
        nodes.take(2).forEach { nodeWithPin ->
            Surface(
                onClick = { onEditNode(nodeWithPin.node.id) },
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusSm),
                border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    ); Spacer(Modifier.width(TactileTheme.SpacingMd)); Text(
                    nodeWithPin.node.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                }
            }
        }
    }
}

@Composable
fun VaultCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border)
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Icon(
                icon,
                contentDescription = null,
                tint = TactileTheme.Accent,
                modifier = Modifier.size(20.dp)
            ); Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        ); Text(
            "$count ITEMS",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
            fontSize = 8.sp
        )
        }
    }
}

@Composable
fun ModulesGrid(
    todayNodes: List<com.tajemniktv.tajsos.data.NodeEntity>,
    activeSession: com.tajemniktv.tajsos.data.FocusSessionEntity?,
    moodToday: com.tajemniktv.tajsos.data.TrackEntryEntity?,
    tasksCount: Int,
    notesCount: Int,
    allProjects: List<com.tajemniktv.tajsos.data.NodeEntity>,
    allAreas: List<com.tajemniktv.tajsos.data.NodeEntity>,
    calendarEntries: List<com.tajemniktv.tajsos.ui.CalendarEntry>,
    allSessions: List<com.tajemniktv.tajsos.data.FocusSessionEntity>,
    onNavigateTo: (Screen) -> Unit,
    viewModel: MainViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
        Text(
            "MODULES",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        val rows = listOf(
            listOf(
                Quad(
                    stringResource(Res.string.screen_today),
                    Icons.Default.DateRange,
                    if (todayNodes.isNotEmpty()) "${todayNodes.size} TASKS" else "EMPTY",
                    { onNavigateTo(Screen.Today) }),
                Quad(
                    stringResource(Res.string.screen_focus),
                    Icons.Default.PlayArrow,
                    if (activeSession != null) "ACTIVE" else "READY",
                    { onNavigateTo(Screen.Focus) })
            ),
            listOf(
                Quad(
                    stringResource(Res.string.screen_track),
                    Icons.Default.CheckCircle,
                    if (moodToday != null) "LOGGED" else "PENDING",
                    { onNavigateTo(Screen.Track) }),
                Quad(
                    stringResource(Res.string.screen_tasks),
                    Icons.AutoMirrored.Filled.List,
                    "$tasksCount TOTAL",
                    { onNavigateTo(Screen.Tasks) })
            ),
            listOf(
                Quad(
                    stringResource(Res.string.screen_notes),
                    Icons.Default.Edit,
                    "$notesCount TOTAL",
                    { onNavigateTo(Screen.Notes) }),
                Quad(
                    stringResource(Res.string.screen_proj),
                    Icons.AutoMirrored.Filled.List,
                    "${allProjects.size} ACTIVE",
                    { onNavigateTo(Screen.Projects) })
            ),
            listOf(
                Quad(
                    stringResource(Res.string.screen_area),
                    Icons.Default.LocationOn,
                    "${allAreas.size} TOTAL",
                    { onNavigateTo(Screen.Areas) }),
                Quad(
                    stringResource(Res.string.screen_cal),
                    Icons.Default.Event,
                    "${calendarEntries.size} ITEMS",
                    { onNavigateTo(Screen.Calendar) })
            ),
            listOf(
                Quad(
                    stringResource(Res.string.screen_stats),
                    Icons.Default.Info,
                    "VIEW",
                    { onNavigateTo(Screen.Insights) }),
                Quad(
                    stringResource(Res.string.screen_history),
                    Icons.Default.History,
                    if (allSessions.isNotEmpty()) "LAST: ${
                        Instant.fromEpochMilliseconds(allSessions.first().startedAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    }" else "EMPTY",
                    { viewModel.resumeLastSession() })
            )
        )
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                row.forEach { (title, icon, status, action) ->
                    ModuleButton(
                        modifier = Modifier.weight(1f),
                        title = title,
                        icon = icon,
                        status = status,
                        onClick = action
                    )
                }
            }
        }
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun DashCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusLg),
        border = BorderStroke(1.dp, TactileTheme.Border)
    ) { content() }
}

@Composable
fun TaskBrief(title: String, isDone: Boolean, onToggle: () -> Unit, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Black.copy(alpha = 0.2f),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(20.dp).clip(CircleShape)
                    .background(if (isDone) TactileTheme.Primary else Color.Transparent).border(
                    1.dp,
                    if (isDone) TactileTheme.Primary else TactileTheme.Muted,
                    CircleShape
                ).clickable { onToggle() }, contentAlignment = Alignment.Center
            ) {
                if (isDone) Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(12.dp)); Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDone) TactileTheme.Muted else TactileTheme.Text,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        }
    }
}

@Composable
fun ProgressRing(progress: Float) {
    val anim by animateFloatAsState(targetValue = progress)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
        Canvas(modifier = Modifier.size(60.dp)) {
            drawCircle(color = Color.White.copy(alpha = 0.1f), style = Stroke(width = 4.dp.toPx()))
            drawArc(
                color = TactileTheme.Primary,
                startAngle = -90f,
                sweepAngle = 360 * anim,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TactileTheme.Text
        )
    }
}

@Composable
fun SystemFooter() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(
            "MEMORY USAGE: 42%  •  UPTIME: 14D 02H",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
