/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.dashboard

import com.tajemniktv.tajsos.ui.components.TactileOutlinedTextField
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import com.tajemniktv.tajsos.data.FocusSessionEntity
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TrackEntryEntity
import com.tajemniktv.tajsos.ui.DashboardUIState
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.cards.LifeSummaryCard
import com.tajemniktv.tajsos.ui.components.cards.ModuleCard
import com.tajemniktv.tajsos.ui.components.modes.ModeSuggestionBanner
import com.tajemniktv.tajsos.ui.components.modes.ModeSwitcherHeader
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.lens.LensUiContract
import com.tajemniktv.tajsos.ui.main.state.CalendarEntry
import com.tajemniktv.tajsos.ui.main.state.InsightsData
import com.tajemniktv.tajsos.ui.screens.GroupedOpenLoopSection
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.dash_placeholder_capture_shortcut
import tajsos.composeapp.generated.resources.screen_focus
import tajsos.composeapp.generated.resources.screen_inbox
import tajsos.composeapp.generated.resources.screen_project
import tajsos.composeapp.generated.resources.screen_today
import kotlin.time.Clock

/**
 * Central dashboard entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of dashboard state.
 * @param onNavigate Navigation callback.
 * @param onEditNode Node edit callback.
 * @param onNavigateToProject Project navigation callback.
 * @param onNewEntry Entry creation callback.
 * @param currentDestination Current navigation state.
 */
@Composable
fun DashboardRoute(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onNewEntry: () -> Unit,
    currentDestination: NavDestination? = null,
) {
    BoxWithConstraints {
        val surface = if (maxWidth > 800.dp) DashboardSurface.DESKTOP else DashboardSurface.MOBILE

        DashboardScreen(
            surface = surface,
            viewModel = viewModel,
            onNavigate = onNavigate,
            onEditNode = onEditNode,
            onNavigateToProject = onNavigateToProject,
            onNewEntry = onNewEntry,
            currentDestination = currentDestination,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DashboardScreen(
    surface: DashboardSurface,
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onNewEntry: () -> Unit,
    currentDestination: NavDestination?,
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

    val pinnedNodes = remember(allNodes) { allNodes.filter { it.pin != null } }
    val completedTodayCount =
        remember(pinnedNodes) { pinnedNodes.count { it.node.status == "done" } }
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

    val layoutPlan =
        remember(
            surface,
            dashboardState.modeQueryProfile,
            dashboardState.modePreferences,
            enabledPacks,
        ) {
            buildDashboardLayoutPlan(
                surface = surface,
                dashboardState = dashboardState,
                enabledPacks = enabledPacks,
            )
        }

    val context =
        remember(
            viewModel,
            dashboardState,
            pinnedNodes,
            allProjects,
            allAreas,
            inboxNodes,
            activeReminders,
            activeSession,
            insights,
            moodToday,
            needsWeeklyReview,
            dailyProgress,
            localNow,
            todayNodes,
            allSessions,
            calendarEntries,
            allModes,
            currentMode,
            onNavigate,
            onEditNode,
            onNavigateToProject,
            onNewEntry,
            currentDestination,
        ) {
            DashboardRenderContext(
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
                todayNodes = todayNodes,
                allSessions = allSessions,
                calendarEntries = calendarEntries,
                allModes = allModes,
                currentMode = currentMode,
                onNavigate = onNavigate,
                onEditNode = onEditNode,
                onNavigateToProject = onNavigateToProject,
                onNewEntry = onNewEntry,
                currentDestination = currentDestination,
            )
        }

    ScreenScaffold(
        screen = Screen.Dashboard,
        onNavigate = onNavigate,
        scrollBehavior = ScreenScrollBehavior.None,
        backgroundColor = TajsOSTheme.ScreenCanvas,
    ) {
        if (surface == DashboardSurface.MOBILE) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingLg),
            ) {
                layoutPlan.primary.forEach { block ->
                    item(key = block.id) {
                        RenderDashboardBlock(block = block, context = context)
                    }
                }
                layoutPlan.footer.forEach { block ->
                    item(key = block.id + "_footer") {
                        RenderDashboardBlock(block = block, context = context)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingLg),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1.5f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        layoutPlan.primary.forEach { block ->
                            RenderDashboardBlock(block = block, context = context)
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        layoutPlan.secondary.forEach { block ->
                            RenderDashboardBlock(block = block, context = context)
                        }
                    }
                }
                layoutPlan.bottomBar.forEach { block ->
                    RenderDashboardBlock(block = block, context = context)
                }
            }
        }
    }
}

/**
 * Captures dashboard dependencies and callbacks in one container for block rendering.
 */
private data class DashboardRenderContext(
    val viewModel: MainViewModel,
    val dashboardState: DashboardUIState,
    val pinnedNodes: List<NodeWithPin>,
    val allProjects: List<NodeEntity>,
    val allAreas: List<NodeEntity>,
    val inboxNodes: List<NodeWithPin>,
    val activeReminders: List<NodeEntity>,
    val activeSession: FocusSessionEntity?,
    val insights: InsightsData,
    val moodToday: TrackEntryEntity?,
    val needsWeeklyReview: Boolean,
    val dailyProgress: Float,
    val localNow: LocalDateTime,
    val todayNodes: List<NodeEntity>,
    val allSessions: List<FocusSessionEntity>,
    val calendarEntries: List<CalendarEntry>,
    val allModes: List<ModeEntity>,
    val currentMode: ModeEntity?,
    val onNavigate: (String) -> Unit,
    val onEditNode: (Long) -> Unit,
    val onNavigateToProject: (Long) -> Unit,
    val onNewEntry: () -> Unit,
    val currentDestination: NavDestination?,
)

/**
 * Renders one dashboard block from the engine plan.
 */
@Composable
private fun RenderDashboardBlock(
    block: DashboardBlockInstance,
    context: DashboardRenderContext,
) {
    when (block.id) {
        "mode_controls" -> {
            ModeSwitcherHeader(
                currentMode = context.currentMode,
                allModes = context.allModes,
                onModeSelect = { context.viewModel.switchMode(it) },
            )
            context.dashboardState.modeSuggestion?.let { suggestion ->
                ModeSuggestionBanner(
                    suggestion = suggestion,
                    onAccept = {
                        val targetMode = context.allModes.find { it.key == suggestion }
                        if (targetMode != null) context.viewModel.switchMode(targetMode.id)
                    },
                    onDismiss = {},
                )
            }
        }

        "search_capture" -> {
            TactileOutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(Res.string.dash_placeholder_capture_shortcut),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TajsOSTheme.Muted,
                    )
                },
                leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null) },
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            )
        }

        "insights_summary" -> {
            LifeSummaryCard(
                captures = context.insights.weeklyCaptures,
                completions = context.insights.weeklyCompletions,
                onClick = { context.onNavigate(Screen.Insights.route) },
            )
        }

        "modules" -> {
            DashboardModules(
                todayNodes = context.todayNodes,
                inboxCount = context.inboxNodes.size,
                activeSession = context.activeSession,
                moodToday = context.moodToday,
                tasksCount = context.dashboardState.tasksCount,
                notesCount = context.dashboardState.notesCount,
                allProjects = context.allProjects,
                allAreas = context.allAreas,
                calendarEntries = context.calendarEntries,
                allSessions = context.allSessions,
                onNavigate = context.onNavigate,
                viewModel = context.viewModel,
            )
        }

        "operations_overview" -> {
            DashboardOperationsOverview(
                viewModel = context.viewModel,
                onNavigate = context.onNavigate,
            )
        }

        "system_clock" -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TajsOSTheme.CardSurface,
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "SYSTEM CLOCK",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                    Text(
                        context.localNow.time
                            .toString()
                            .take(5),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        context.localNow.date
                            .toString()
                            .uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = TajsOSTheme.Primary,
                        letterSpacing = 2.sp,
                    )
                }
            }
        }

        "system_footer" -> {
            SystemFooter()
        }

        "command_bar" -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TajsOSTheme.CardSurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
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
                                .clickable { context.onNewEntry() }
                                .background(TajsOSTheme.Primary, RoundedCornerShape(TajsOSTheme.RadiusMd))
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

        else -> {
            DashboardBlockRenderer(
                blockKey = block.id,
                viewModel = context.viewModel,
                dashboardState = context.dashboardState,
                pinnedNodes = context.pinnedNodes,
                allProjects = context.allProjects,
                allAreas = context.allAreas,
                inboxNodes = context.inboxNodes,
                activeReminders = context.activeReminders,
                activeSession = context.activeSession,
                insights = context.insights,
                moodToday = context.moodToday,
                needsWeeklyReview = context.needsWeeklyReview,
                dailyProgress = context.dailyProgress,
                localNow = context.localNow,
                onNavigateTo = { context.onNavigate(it.route) },
                onEditNode = context.onEditNode,
                onNavigateToProject = context.onNavigateToProject,
            )
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
    calendarEntries: List<CalendarEntry>,
    allSessions: List<FocusSessionEntity>,
    onNavigate: (String) -> Unit,
    viewModel: MainViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        Text(
            "CORE MODULES",
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Muted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            val itemModifier = Modifier.weight(1f).widthIn(min = 160.dp)

            ModuleCard(
                modifier = itemModifier,
                title = stringResource(Res.string.screen_today),
                icon = Icons.Default.Today,
                status = "${todayNodes.size}",
                onClick = { onNavigate(Screen.Today.route) },
            )

            ModuleCard(
                modifier = itemModifier,
                title = stringResource(Res.string.screen_inbox),
                icon = Icons.Default.Inbox,
                status = "$inboxCount",
                onClick = { onNavigate(Screen.Inbox.route) },
                color = TajsOSTheme.Accent,
            )

            ModuleCard(
                modifier = itemModifier,
                title = stringResource(Res.string.screen_project),
                icon = Icons.Default.AccountTree,
                status = "${allProjects.size}",
                onClick = { onNavigate(Screen.Projects.route) },
                color = TajsOSTheme.Success,
            )

            ModuleCard(
                modifier = itemModifier,
                title = stringResource(Res.string.screen_focus),
                icon = Icons.Default.Timer,
                status = if (activeSession != null) "ACTIVE" else "READY",
                onClick = { onNavigate(Screen.Focus.route) },
                color = if (activeSession != null) TajsOSTheme.Primary else TajsOSTheme.Muted,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DashboardOperationsOverview(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
) {
    val capacitySnapshot by viewModel.capacitySnapshot.collectAsState()
    val lifeOSSignatureSnapshot by viewModel.lifeOSSignatureSnapshot.collectAsState()
    val lifeOSSecondBrainSnapshot by viewModel.lifeOSSecondBrainSnapshot.collectAsState()
    val combinedDirectionSnapshot by viewModel.combinedDirectionSnapshot.collectAsState()
    val relationshipSnapshot by viewModel.relationshipSnapshot.collectAsState()
    val coreLifeOSShiftSnapshot by viewModel.coreLifeOSShiftSnapshot.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        Text(
            "SYSTEMS OVERVIEW",
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Muted,
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
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            dashboardSystemsModules().forEach { module ->
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .widthIn(min = 180.dp)
                            .background(
                                TajsOSTheme.Surface,
                                RoundedCornerShape(TajsOSTheme.RadiusMd),
                            ).border(
                                1.dp,
                                TajsOSTheme.Border,
                                RoundedCornerShape(TajsOSTheme.RadiusMd),
                            ).clickable { onNavigate(module.screen.route) }
                            .padding(TajsOSTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(module.screen.label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(module.summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                    )
                }
            }
        }
    }
}

private fun dashboardSystemsModules() = LensUiContract.systemsModules

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
                    .background(TajsOSTheme.Border, RoundedCornerShape(TajsOSTheme.RadiusMd))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Text,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            action,
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Muted,
            fontWeight = FontWeight.Bold,
        )
    }
}
