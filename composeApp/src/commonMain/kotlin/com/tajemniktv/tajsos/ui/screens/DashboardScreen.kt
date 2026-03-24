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
import androidx.navigation.NavDestination
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.*
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
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
    onNewEntry: () -> Unit,
    currentDestination: NavDestination? = null,
    currentMode: com.tajemniktv.tajsos.data.ModeEntity? = null,
    allModes: List<com.tajemniktv.tajsos.data.ModeEntity> = emptyList(),
    onModeSelect: (Long) -> Unit = {}
) {
    BoxWithConstraints {
        if (maxWidth > 800.dp) {
            DashboardDesktopScreen(
                viewModel = viewModel,
                onNavigateTo = onNavigateTo,
                onEditNode = onEditNode,
                onNavigateToProject = onNavigateToProject,
                onNewEntry = onNewEntry,
                currentDestination = currentDestination,
                currentMode = currentMode,
                allModes = allModes,
                onModeSelect = onModeSelect
            )
        } else {
            DashboardMobileContent(
                viewModel = viewModel,
                onNavigateTo = onNavigateTo,
                onEditNode = onEditNode,
                onNavigateToProject = onNavigateToProject,
                onOpenDrawer = onOpenDrawer
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DashboardMobileContent(
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
    val currentMode by viewModel.currentMode.collectAsState()
    val allModes by viewModel.allModes.collectAsState()
    val scope = rememberCoroutineScope()

    val blockList = remember(dashboardState.modePreferences) {
        try {
            val jsonStr = dashboardState.modePreferences?.dashboardBlocksJson
            if (jsonStr != null) {
                Json.decodeFromString<List<String>>(jsonStr)
            } else {
                listOf(
                    "today_pulse",
                    "load_capacity",
                    "area_health",
                    "operational",
                    "search",
                    "alerts",
                    "sticky",
                    "focus",
                    "insights",
                    "actions",
                    "suggestions",
                    "knowledge",
                    "protocols"
                )
            }
        } catch (e: Exception) {
            listOf(
                "today_pulse",
                "load_capacity",
                "area_health",
                "operational",
                "search",
                "alerts",
                "sticky",
                "focus",
                "insights",
                "actions",
                "suggestions",
                "knowledge",
                "protocols"
            )
        }
    }

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

    val currentModeColor = currentMode?.themeColor?.let { Color(it) } ?: TactileTheme.Primary

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
            onSettingsClick = { onNavigateTo(Screen.Settings) },
            tintColor = currentModeColor
        )

        ModeSwitcherHeader(
            currentMode = currentMode,
            allModes = allModes,
            onModeSelect = { viewModel.switchMode(it) }
        )

        dashboardState.modeSuggestion?.let { suggestion ->
            ModeSuggestionBanner(
                suggestion = suggestion,
                onAccept = {
                    val targetMode = allModes.find { it.key == suggestion }
                    if (targetMode != null) viewModel.switchMode(targetMode.id)
                },
                onDismiss = { /* Option to ignore until state changes */ }
            )
        }

        blockList.forEach { blockKey ->
            when (blockKey) {
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
                        onClick = { onNavigateTo(Screen.Today) }
                    )
                }

                "load_capacity" -> {
                    SystemStatusCard(
                        load = dashboardState.systemLoad,
                        fragmentation = dashboardState.fragmentation,
                        warning = dashboardState.capacityWarning,
                        onClick = { onNavigateTo(Screen.Insights) }
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
                                letterSpacing = 1.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
                            ) {
                                allAreas.forEach { area ->
                                    AreaHealthCard(
                                        area = area,
                                        health = dashboardState.areaHealth[area.id] ?: "stable",
                                        onClick = {
                                            viewModel.clearSearchFilters()
                                            viewModel.updateSearchAreaFilter(area.id)
                                            onNavigateTo(Screen.Search)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                "operational" -> {
                    if (dashboardState.openLoops.isNotEmpty() || dashboardState.pendingDecisions.isNotEmpty() || dashboardState.maintenanceQueue.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                            Text(
                                "LIFE OS // OPERATIONAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Accent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )

                            if (dashboardState.openLoops.isNotEmpty()) {
                                SuggestionGroup(
                                    title = stringResource(Res.string.dash_open_loops),
                                    icon = Icons.Default.AllInclusive,
                                    color = TactileTheme.Accent,
                                    nodes = dashboardState.openLoops,
                                    onEditNode = onEditNode
                                )
                            }

                            if (dashboardState.pendingDecisions.isNotEmpty()) {
                                SuggestionGroup(
                                    title = stringResource(Res.string.dash_decisions),
                                    icon = Icons.Default.QuestionMark,
                                    color = TactileTheme.Primary,
                                    nodes = dashboardState.pendingDecisions,
                                    onEditNode = onEditNode
                                )
                            }

                            if (dashboardState.maintenanceQueue.isNotEmpty()) {
                                SuggestionGroup(
                                    title = stringResource(Res.string.dash_maintenance),
                                    icon = Icons.Default.Settings,
                                    color = TactileTheme.Success,
                                    nodes = dashboardState.maintenanceQueue,
                                    onEditNode = onEditNode
                                )
                            }
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
                                                    reminderAt = null
                                                )
                                            )
                                        },
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
                }

                "sticky" -> {
                    if (dashboardState.stickyNotes.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
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
                }

                "focus", "current_task", "timer" -> {
                    FocusCard(
                        viewModel = viewModel,
                        activeSession = activeSession,
                        onToggleFocus = {
                            if (activeSession != null) {
                                viewModel.stopFocusSession()
                            } else {
                                pinnedNodes.firstOrNull()
                                    ?.let { viewModel.startFocusSession(it.node.id) }
                            }
                        },
                        onClick = { onNavigateTo(Screen.Focus) }
                    )
                }

                "insights" -> {
                    LifeSummaryCard(
                        captures = insights.weeklyCaptures,
                        completions = insights.weeklyCompletions,
                        onClick = { onNavigateTo(Screen.Insights) }
                    )
                }

                "actions" -> {
                    StateAwareActionsGrid(viewModel = viewModel, onNavigateTo = onNavigateTo)
                }

                "suggestions", "easy_wins" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg)) {
                        if (dashboardState.lowEnergyTasks.isNotEmpty() && (moodToday?.energyScore
                                ?: 5) <= 2
                        ) {
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
                                allAreas.find { it.id == firstBatch.first().node.areaId }?.title
                                    ?: "GENERAL"
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
                }

                "knowledge", "pinned_note" -> {
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
                }

                "protocols" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                        Text(
                            stringResource(Res.string.dash_protocols),
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
                        ) {
                            ProtocolTrigger(
                                label = stringResource(Res.string.protocol_morning),
                                icon = Icons.Default.WbSunny,
                                color = TactileTheme.Primary,
                                onClick = { onNavigateTo(Screen.Review) }
                            )
                            ProtocolTrigger(
                                label = stringResource(Res.string.protocol_deep_work),
                                icon = Icons.Default.Psychology,
                                color = TactileTheme.Accent,
                                onClick = { onNavigateTo(Screen.Focus) }
                            )
                            ProtocolTrigger(
                                label = stringResource(Res.string.protocol_shutdown),
                                icon = Icons.Default.Brightness3,
                                color = TactileTheme.Success,
                                onClick = { onNavigateTo(Screen.Review) }
                            )
                            ProtocolTrigger(
                                label = stringResource(Res.string.protocol_recovery),
                                icon = Icons.Default.MedicalServices,
                                color = TactileTheme.Error,
                                onClick = { onNavigateTo(Screen.Track) }
                            )
                        }
                    }
                }

                "basics", "survival_basics" -> {
                    RecoveryBasicsBlock(
                        onMedsClick = { onNavigateTo(Screen.Track) },
                        onHydrationClick = { /* hydration track later */ },
                        onFoodClick = { /* food track later */ }
                    )
                }

                "shopping_list", "place_based_tasks", "errands" -> {
                    ErrandListBlock(
                        errands = dashboardState.shoppingList,
                        onEdit = onEditNode
                    )
                }

                "tiny_wins", "tiny_victories" -> {
                    TinyVictoriesBlock(
                        victories = dashboardState.tinyVictories,
                        onEdit = onEditNode
                    )
                }

                "current_focus" -> {
                    CurrentTaskBlock(
                        activeTask = pinnedNodes.firstOrNull(),
                        onEdit = onEditNode
                    )
                }

                "classes", "assignments", "revision_targets" -> {
                    SuggestionGroup(
                        title = "STUDY MODULE // ${blockKey.uppercase()}",
                        icon = Icons.Default.School,
                        color = Color(0xFFFF9800),
                        nodes = dashboardState.upcomingDeadlines, // Placeholder
                        onEditNode = onEditNode
                    )
                }

                "paperwork", "bills", "renewals", "subscriptions", "bureaucracy" -> {
                    SuggestionGroup(
                        title = "ADMIN // ${blockKey.uppercase()}",
                        icon = Icons.Default.Gavel,
                        color = Color(0xFF607D8B),
                        nodes = dashboardState.unresolvedBureaucracy,
                        onEditNode = onEditNode
                    )
                }
            }
        }

        // 12. Dashboard Modules
        DashboardModules(
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
fun DashHeader(
    vibe: String,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    tintColor: Color = TactileTheme.Primary
) {
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
                    color = tintColor,
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
                    color = tintColor,
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
                            .background(if (tintColor == TactileTheme.Primary) TactileTheme.Success else tintColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "SYSTEM: ONLINE",
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
fun DashboardModules(
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
            stringResource(Res.string.dash_modules),
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            ModuleCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.screen_today),
                icon = Icons.Default.DateRange,
                status = if (todayNodes.isNotEmpty()) stringResource(
                    Res.string.dash_module_tasks_count,
                    todayNodes.size
                ) else stringResource(Res.string.dash_module_empty),
                onClick = { onNavigateTo(Screen.Today) },
                color = Color(0xFF4CAF50)
            )
            ModuleCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.screen_focus),
                icon = Icons.Default.PlayArrow,
                status = if (activeSession != null) stringResource(Res.string.dash_module_active) else stringResource(
                    Res.string.dash_module_ready
                ),
                onClick = { onNavigateTo(Screen.Focus) },
                color = Color(0xFFF44336)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            ModuleCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.screen_track),
                icon = Icons.Default.CheckCircle,
                status = if (moodToday != null) stringResource(Res.string.dash_module_logged) else stringResource(
                    Res.string.dash_module_pending
                ),
                onClick = { onNavigateTo(Screen.Track) },
                color = Color(0xFF2196F3)
            )
            ModuleCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.screen_tasks),
                icon = Icons.AutoMirrored.Filled.List,
                status = stringResource(Res.string.dash_module_total_count, tasksCount),
                onClick = { onNavigateTo(Screen.Tasks) },
                color = Color(0xFF9C27B0)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            ModuleCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.screen_notes),
                icon = Icons.Default.Edit,
                status = stringResource(Res.string.dash_module_total_count, notesCount),
                onClick = { onNavigateTo(Screen.Notes) },
                color = Color(0xFFFF9800)
            )
            ModuleCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.screen_proj),
                icon = Icons.AutoMirrored.Filled.List,
                status = stringResource(Res.string.dash_module_active_count, allProjects.size),
                onClick = { onNavigateTo(Screen.Projects) },
                color = Color(0xFF00BCD4)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            ModuleCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.screen_area),
                icon = Icons.Default.LocationOn,
                status = stringResource(Res.string.dash_module_total_count, allAreas.size),
                onClick = { onNavigateTo(Screen.Areas) },
                color = Color(0xFF607D8B)
            )
            ModuleCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.screen_cal),
                icon = Icons.Default.Event,
                status = stringResource(Res.string.dash_module_items_count, calendarEntries.size),
                onClick = { onNavigateTo(Screen.Calendar) },
                color = Color(0xFF3F51B5)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            ModuleCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.screen_stats),
                icon = Icons.Default.Info,
                status = stringResource(Res.string.dash_module_view),
                onClick = { onNavigateTo(Screen.Insights) },
                color = Color(0xFF795548)
            )
            ModuleCard(
                modifier = Modifier.weight(1f),
                title = stringResource(Res.string.screen_history),
                icon = Icons.Default.History,
                status = if (allSessions.isNotEmpty()) {
                    val date = Instant.fromEpochMilliseconds(allSessions.first().startedAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    stringResource(Res.string.dash_history_last, date.toString())
                } else stringResource(Res.string.dash_module_empty),
                onClick = { viewModel.resumeLastSession() },
                color = Color(0xFFE91E63)
            )
        }
    }
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
