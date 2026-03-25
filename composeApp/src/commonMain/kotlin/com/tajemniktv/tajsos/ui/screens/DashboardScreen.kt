/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import com.tajemniktv.tajsos.data.FocusSessionEntity
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TrackEntryEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.dashboard.DashboardBlockRenderer
import com.tajemniktv.tajsos.ui.components.dashboard.ModuleCard
import com.tajemniktv.tajsos.ui.components.modes.ModeSuggestionBanner
import com.tajemniktv.tajsos.ui.components.modes.ModeSwitcherHeader
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Clock

/**
 * Displays the dashboard and selects a desktop or mobile layout based on available width.
 *
 * @param viewModel Provides dashboard state and actions.
 * @param onNavigateTo Invoked with a `Screen` when the UI requests navigation to that screen.
 * @param onEditNode Invoked with a node ID to open the node editor.
 * @param onNavigateToProject Invoked with a project ID to navigate to that project.
 * @param onNewEntry Invoked to create a new entry.
 * @param currentDestination Optional current navigation destination used to highlight or adapt UI state.
 */
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateTo: (Screen) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onNewEntry: () -> Unit,
    currentDestination: NavDestination? = null,
)
{
    BoxWithConstraints {
        if (maxWidth > 800.dp)
        {
            DashboardDesktopContent(
                viewModel = viewModel,
                onNavigateTo = onNavigateTo,
                onEditNode = onEditNode,
                onNavigateToProject = onNavigateToProject,
                onNewEntry = onNewEntry,
                currentDestination = currentDestination,
            )
        } else
        {
            DashboardMobileContent(
                viewModel = viewModel,
                onNavigateTo = onNavigateTo,
                onEditNode = onEditNode,
                onNavigateToProject = onNavigateToProject,
            )
        }
    }
}

/**
 * Renders the dashboard UI optimized for mobile widths, composing blocks, modules, and footer
 * based on state collected from the provided view model.
 *
 * Uses state flows from `viewModel` to build dashboard blocks, compute metrics (daily progress,
 * mood, weekly review need, pinned items), and wire navigation/edit callbacks into rendered blocks
 * and modules.
 *
 * @param viewModel Source of dashboard state and actions.
 * @param onNavigateTo Called with a `Screen` when the UI requests a top-level navigation.
 * @param onEditNode Called with a node ID to open the node editor.
 * @param onNavigateToProject Called with a project ID to navigate to a specific project.
 */
@Composable
private fun DashboardMobileContent(
    viewModel: MainViewModel,
    onNavigateTo: (Screen) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
)
{
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

    val blockList = remember(dashboardState.modePreferences) {
        try
        {
            val jsonStr = dashboardState.modePreferences?.dashboardBlocksJson
            if (jsonStr != null)
            {
                Json.decodeFromString<List<String>>(jsonStr)
            } else
            {
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
                    "protocols",
                )
            }
        } catch (e: Exception)
        {
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
                "protocols",
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TactileTheme.Background)
            .verticalScroll(scrollState)
            .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg),
    ) {
        ModeSwitcherHeader(
            currentMode = currentMode,
            allModes = allModes,
            onModeSelect = { viewModel.switchMode(it) },
        )

        dashboardState.modeSuggestion?.let { suggestion ->
            ModeSuggestionBanner(
                suggestion = suggestion,
                onAccept = {
                    val targetMode = allModes.find { it.key == suggestion }
                    if (targetMode != null) viewModel.switchMode(targetMode.id)
                },
                onDismiss = { /* Option to ignore until state changes */ },
            )
        }

        blockList.forEach { blockKey ->
            DashboardBlockRenderer(
                blockKey = blockKey,
                viewModel = viewModel,
                dashboardState = dashboardState,
                pinnedNodes = pinnedNodes,
                allProjects = allProjects,
                allAreas = allAreas,
                inboxNodes = inboxNodes,
                activeReminders = activeReminders,
                activeSession = activeSession,
                insights = insights,
                moodToday = moodToday,
                needsWeeklyReview = needsWeeklyReview,
                dailyProgress = dailyProgress,
                localNow = localNow,
                onNavigateTo = onNavigateTo,
                onEditNode = onEditNode,
                onNavigateToProject = onNavigateToProject,
            )
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
            viewModel = viewModel,
        )

        // 13. System Footer
        com.tajemniktv.tajsos.ui.components.dashboard.SystemFooter()

        Spacer(Modifier.height(80.dp))
    }
}

/**
 * Renders the "CORE MODULES" section containing cards for Today, Inbox, Projects, and Focus.
 *
 * Displays per-module status badges derived from the provided state and invokes navigation when a card is tapped.
 *
 * @param todayNodes Used to compute the Today module's item count.
 * @param activeSession If non-null, the Focus module shows "ACTIVE" and uses the primary color; otherwise it shows "READY".
 * @param allProjects Used to compute the Projects module's item count.
 * @param onNavigateTo Callback invoked with the destination `Screen` when a module card is tapped.
 */
@Composable
fun DashboardModules(
    todayNodes: List<NodeEntity>,
    activeSession: FocusSessionEntity?,
    moodToday: TrackEntryEntity?,
    tasksCount: Int,
    notesCount: Int,
    allProjects: List<NodeEntity>,
    allAreas: List<NodeEntity>,
    calendarEntries: List<com.tajemniktv.tajsos.ui.CalendarEntry>,
    allSessions: List<FocusSessionEntity>,
    onNavigateTo: (Screen) -> Unit,
    viewModel: MainViewModel,
)
{
    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
        Text(
            "CORE MODULES",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )

        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        ) {
            val itemModifier = Modifier.weight(1f).widthIn(min = 160.dp)

            ModuleCard(
                modifier = itemModifier,
                title = stringResource(Res.string.screen_today),
                icon = Icons.Default.Today,
                status = "${todayNodes.size}",
                onClick = { onNavigateTo(Screen.Today) },
            )

            ModuleCard(
                modifier = itemModifier,
                title = stringResource(Res.string.screen_inbox),
                icon = Icons.Default.Inbox,
                status = "12", // Placeholder
                onClick = { onNavigateTo(Screen.Inbox) },
                color = TactileTheme.Accent,
            )

            ModuleCard(
                modifier = itemModifier,
                title = stringResource(Res.string.screen_project),
                icon = Icons.Default.AccountTree,
                status = "${allProjects.size}",
                onClick = { onNavigateTo(Screen.Projects) },
                color = TactileTheme.Success,
            )

            ModuleCard(
                modifier = itemModifier,
                title = stringResource(Res.string.screen_focus),
                icon = Icons.Default.Timer,
                status = if (activeSession != null) "ACTIVE" else "READY",
                onClick = { onNavigateTo(Screen.Focus) },
                color = if (activeSession != null) TactileTheme.Primary else TactileTheme.Muted,
            )
        }
    }
}
