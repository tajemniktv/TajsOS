/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import com.tajemniktv.tajsos.data.AppPack
import com.tajemniktv.tajsos.data.FocusSessionEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TrackEntryEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.cards.LifeSummaryCard
import com.tajemniktv.tajsos.ui.components.cards.ModuleCard
import com.tajemniktv.tajsos.ui.components.dashboard.DashboardBlockRenderer
import com.tajemniktv.tajsos.ui.components.modes.ModeSuggestionBanner
import com.tajemniktv.tajsos.ui.components.modes.ModeSwitcherHeader
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.screen_focus
import tajsos.composeapp.generated.resources.screen_inbox
import tajsos.composeapp.generated.resources.screen_project
import tajsos.composeapp.generated.resources.screen_today
import kotlin.time.Clock

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateTo: (Screen) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onNewEntry: () -> Unit,
    currentDestination: NavDestination? = null,
) {
    BoxWithConstraints {
        if (maxWidth > 800.dp) {
            DashboardDesktopContent(
                viewModel = viewModel,
                onNavigateTo = onNavigateTo,
                onEditNode = onEditNode,
                onNavigateToProject = onNavigateToProject,
                onNewEntry = onNewEntry,
                currentDestination = currentDestination,
            )
        } else {
            DashboardMobileContent(
                viewModel = viewModel,
                onNavigateTo = onNavigateTo,
                onEditNode = onEditNode,
                onNavigateToProject = onNavigateToProject,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DashboardMobileContent(
    viewModel: MainViewModel,
    onNavigateTo: (Screen) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
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
    val enabledPacks by viewModel.enabledPacks.collectAsState()

    val rawBlockList =
        remember(dashboardState.modePreferences, dashboardState.modeQueryProfile) {
            dashboardState.modeQueryProfile
                ?.dashboardBlocks
                ?.takeIf { it.isNotEmpty() }
                ?.let { return@remember it }
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
                        "time_architecture",
                        "protocols",
                    )
                }
            } catch (_: Exception) {
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
                    "time_architecture",
                    "protocols",
                )
            }
        }

    val blockList =
        remember(rawBlockList, enabledPacks) {
            rawBlockList.filter { block ->
                when (block)
                {
                    "classes", "assignments", "revision_targets" -> {
                        enabledPacks.isEnabled(
                            AppPack.STUDENT,
                        )
                    }

                    "paperwork", "bills", "renewals", "subscriptions", "bureaucracy" -> {
                        enabledPacks.isEnabled(AppPack.FINANCE) || enabledPacks.isEnabled(AppPack.MAINTENANCE)
                    }

                    "protocols" -> {
                        enabledPacks.isEnabled(AppPack.PROTOCOLS) || enabledPacks.isEnabled(AppPack.MAINTENANCE)
                    }

                    else -> {
                        true
                    }
                }
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
    val moodToday = trackEntries.find { it.date == todayDateStr }

    val lastWeeklyReview = allReviews.find { it.type == "weekly" }
    val weekMillis = 7 * 24 * 60 * 60 * 1000L
    val needsWeeklyReview =
        lastWeeklyReview == null || (now.toEpochMilliseconds() - lastWeeklyReview.completedAt) > weekMillis

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(TactileTheme.Background)
                .verticalScroll(rememberScrollState())
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
                onDismiss = {},
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

        DashboardModules(
            todayNodes = todayNodes,
            inboxCount = inboxNodes.size,
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

        DashboardOperationsOverview(
            viewModel = viewModel,
            onNavigateTo = onNavigateTo,
        )

        com.tajemniktv.tajsos.ui.components.dashboard
            .SystemFooter()
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DashboardDesktopContent(
    viewModel: MainViewModel,
    onNavigateTo: (Screen) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onNewEntry: () -> Unit,
    currentDestination: NavDestination?,
) {
    val allNodes by viewModel.allNodes.collectAsState()
    val pinnedNodes = allNodes.filter { it.pin != null }
    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val inboxNodes by viewModel.inboxNodes.collectAsState()
    val activeReminders by viewModel.activeReminders.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val dashboardState by viewModel.dashboardUIState.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val trackEntries by viewModel.trackEntries.collectAsState()

    val completedTodayCount = pinnedNodes.count { it.node.status == "done" }
    val totalTodayCount = pinnedNodes.size
    val dailyProgress =
        if (totalTodayCount > 0) completedTodayCount.toFloat() / totalTodayCount else 0f

    val now = Clock.System.now()
    val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val todayDateStr = localNow.date.toString()
    val moodToday = trackEntries.find { it.date == todayDateStr }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(TactileTheme.Background)
                .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Column(
                modifier = Modifier.weight(1.5f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                val blocks =
                    listOf(
                        "today_pulse",
                        "load_capacity",
                        "operational",
                        "alerts",
                        "focus",
                        "suggestions",
                        "knowledge",
                    )
                blocks.forEach { blockKey ->
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
                        needsWeeklyReview = false,
                        dailyProgress = dailyProgress,
                        localNow = localNow,
                        onNavigateTo = onNavigateTo,
                        onEditNode = onEditNode,
                        onNavigateToProject = onNavigateToProject,
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("CMD + K to capture anything...") },
                    leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null) },
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                )

                LifeSummaryCard(
                    captures = insights.weeklyCaptures,
                    completions = insights.weeklyCompletions,
                    onClick = { onNavigateTo(Screen.Insights) },
                )

                DashboardOperationsOverview(
                    viewModel = viewModel,
                    onNavigateTo = onNavigateTo,
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    border = BorderStroke(1.dp, TactileTheme.Border),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "SYSTEM CLOCK",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                        )
                        Text(
                            localNow.time.toString().take(5),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            localNow.date.toString().uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = TactileTheme.Primary,
                            letterSpacing = 2.sp,
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TactileTheme.Surface.copy(alpha = 0.5f),
            shape = RoundedCornerShape(TactileTheme.RadiusMd),
            border = BorderStroke(1.dp, TactileTheme.Border),
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    CommandItem("F1", "SEARCH")
                    CommandItem("F2", "INBOX")
                    CommandItem("F3", "TODAY")
                    CommandItem("F4", "FOCUS")
                }

                Row(
                    modifier =
                        Modifier
                            .clickable { onNewEntry() }
                            .background(TactileTheme.Primary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp).size(22.dp),
                        tint = Color.White.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun DashboardModules(
    todayNodes: List<NodeEntity>,
    inboxCount: Int,
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
) {
    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
        Text(
            "CORE MODULES",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )

        FlowRow(
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
                status = "$inboxCount",
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

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DashboardOperationsOverview(
    viewModel: MainViewModel,
    onNavigateTo: (Screen) -> Unit,
) {
    val capacitySnapshot by viewModel.capacitySnapshot.collectAsState()
    val lifeOSSignatureSnapshot by viewModel.lifeOSSignatureSnapshot.collectAsState()
    val lifeOSSecondBrainSnapshot by viewModel.lifeOSSecondBrainSnapshot.collectAsState()
    val combinedDirectionSnapshot by viewModel.combinedDirectionSnapshot.collectAsState()
    val relationshipSnapshot by viewModel.relationshipSnapshot.collectAsState()
    val coreLifeOSShiftSnapshot by viewModel.coreLifeOSShiftSnapshot.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
        Text(
            "SYSTEMS OVERVIEW",
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )

        GroupedOpenLoopSection(
            title = "MODULE STATUS",
            items =
                listOf(
                    "Capacity load ${capacitySnapshot.loadScore}% • Fragmentation ${capacitySnapshot.fragmentationScore}%",
                    "Relationships ${relationshipSnapshot.people.size} • Follow-up ${relationshipSnapshot.followUpNeeded.size}",
                    "LifeOS signature ${lifeOSSignatureSnapshot.modeOfLifeLabel.uppercase()}",
                    "Second brain posture ${lifeOSSecondBrainSnapshot.postureLabel.uppercase()}",
                    "Direction ${combinedDirectionSnapshot.completionPercent}% • Core shift ${coreLifeOSShiftSnapshot.completionPercent}%",
                ),
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        ) {
            dashboardSystemsModules().forEach { (screen, summary) ->
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .widthIn(min = 180.dp)
                            .background(
                                TactileTheme.Surface,
                                RoundedCornerShape(TactileTheme.RadiusMd),
                            ).border(
                                1.dp,
                                TactileTheme.Border,
                                RoundedCornerShape(TactileTheme.RadiusMd),
                            ).clickable { onNavigateTo(screen) }
                            .padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = screen.route.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                }
            }
        }
    }
}

private fun dashboardSystemsModules(): List<Pair<Screen, String>> =
    listOf(
        Screen.OpenLoops to "Resolve active loops, inbox spillover, and review debt.",
        Screen.Protocols to "Run and maintain transition protocols and playbooks.",
        Screen.TimeArchitecture to "Manage horizons, countdowns, and focus periods.",
        Screen.Places to "Coordinate errands, travel packs, and place-based logistics.",
        Screen.Finances to "Keep bills, renewals, and subscriptions under control.",
        Screen.Relationships to "Maintain follow-ups, shared plans, and contact rhythm.",
        Screen.Study to "Keep coursework, revision, and study execution visible.",
        Screen.Rules to "Store and pin personal principles and decision rules.",
        Screen.Vaults to "Keep reference material, paperwork, and retrieval systems clean.",
        Screen.Capacity to "Track load, fragmentation, and realistic throughput.",
        Screen.Identity to "Review signature, distinction, direction, and core-shift state.",
    )

@Composable
private fun CommandItem(
    key: String,
    action: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            key,
            modifier =
                Modifier
                    .background(TactileTheme.Border, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Text,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            action,
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
            fontWeight = FontWeight.Bold,
        )
    }
}
