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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import com.tajemniktv.tajsos.data.FocusSessionEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TrackEntryEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.*
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Clock

@Composable
fun DashboardDesktopScreen(
    viewModel: MainViewModel,
    onNavigateTo: (Screen) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onNewEntry: () -> Unit,
    currentDestination: NavDestination? = null,
) {
    val allNodes by viewModel.allNodes.collectAsState()
    val todayNodes by viewModel.todayNodes.collectAsState()
    val trackEntries by viewModel.trackEntries.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val dashboardState by viewModel.dashboardUIState.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()

    val pinnedNodes = allNodes.filter { it.pin != null }
    val completedTodayCount = pinnedNodes.count { it.node.status == "done" }
    val totalTodayCount = pinnedNodes.size
    val dailyProgress =
        if (totalTodayCount > 0) completedTodayCount.toFloat() / totalTodayCount else 0f

    val now = Clock.System.now()
    val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())
    val todayDateStr = localNow.date.dayOfWeek.name.take(3).lowercase()
        .replaceFirstChar { it.uppercase() } + ", " +
            localNow.date.month.name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() } + " " + localNow.date.dayOfMonth

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0E))) {
        // 1. Sidebar (Navigation Rail)
        SidebarContent(
            currentDestination = currentDestination,
            onNavigate = onNavigateTo,
            onNewEntry = onNewEntry,
            modifier = Modifier.width(280.dp).background(Color(0xFF0A0A0E))
        )

        // 2. Main Dashboard Area
        Column(modifier = Modifier.weight(1f).padding(horizontal = 40.dp, vertical = 32.dp)) {
            // Header
            DesktopHeader(viewModel = viewModel, currentMode = currentMode)

            Spacer(Modifier.height(32.dp))

            // Bento Grid
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Top Row: Big Date (2/3) & Focus (1/3)
                    Row(
                        modifier = Modifier.weight(1.3f),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        DesktopDateProgressCard(
                            modifier = Modifier.weight(2f),
                            date = todayDateStr,
                            progress = dailyProgress,
                            completedTasks = completedTodayCount,
                            totalTasks = totalTodayCount,
                            onClick = { onNavigateTo(Screen.Today) }
                        )
                        DesktopFocusCard(
                            modifier = Modifier.weight(1f),
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

                    // Middle Row: Status (1/4), High Priority (1/2), Next Engagement (1/4)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        SystemStatusCard(
                            load = dashboardState.systemLoad,
                            fragmentation = dashboardState.fragmentation,
                            warning = dashboardState.capacityWarning,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTo(Screen.Insights) }
                        )
                        DesktopHighPriorityCard(
                            modifier = Modifier.weight(2f),
                            tasks = pinnedNodes.filter { it.node.status == "active" }.take(3),
                            onToggleTask = { nodeWithPin ->
                                val newStatus =
                                    if (nodeWithPin.node.status == "done") "active" else "done"
                                viewModel.updateNodeStatus(nodeWithPin.node, newStatus)
                            },
                            onTaskClick = onEditNode,
                            onViewAll = { onNavigateTo(Screen.Today) }
                        )
                        DesktopNextEngagementCard(
                            modifier = Modifier.weight(1.2f),
                            viewModel = viewModel,
                            onClick = { onNavigateTo(Screen.Calendar) }
                        )
                    }

                    // Bottom Row: Recently Edited (1/4), Active Project (1/2), Context (1/4)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        DesktopRecentlyEditedCard(
                            modifier = Modifier.weight(1f),
                            node = dashboardState.relevantNote,
                            onEditNode = onEditNode
                        )
                        DesktopActiveProjectCard(
                            modifier = Modifier.weight(1.5f),
                            project = allProjects.maxByOrNull { it.updatedAt },
                            onNavigateToProject = onNavigateToProject
                        )
                        // Area Health Desktop
                        Column(
                            modifier = Modifier.weight(1.5f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                stringResource(Res.string.dash_area_health),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                    // Operational Layer (Full Width)
                    if (dashboardState.openLoops.isNotEmpty() || dashboardState.pendingDecisions.isNotEmpty() || dashboardState.maintenanceQueue.isNotEmpty()) {
                        Row(
                            modifier = Modifier.weight(0.8f),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            if (dashboardState.openLoops.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = TactileTheme.Surface,
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        SuggestionGroup(
                                            title = stringResource(Res.string.dash_open_loops),
                                            icon = Icons.Default.AllInclusive,
                                            color = TactileTheme.Accent,
                                            nodes = dashboardState.openLoops,
                                            onEditNode = onEditNode
                                        )
                                    }
                                }
                            }
                            if (dashboardState.pendingDecisions.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = TactileTheme.Surface,
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        SuggestionGroup(
                                            title = stringResource(Res.string.dash_decisions),
                                            icon = Icons.Default.QuestionMark,
                                            color = TactileTheme.Primary,
                                            nodes = dashboardState.pendingDecisions,
                                            onEditNode = onEditNode
                                        )
                                    }
                                }
                            }
                            if (dashboardState.maintenanceQueue.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = TactileTheme.Surface,
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                ) {
                                    Column(modifier = Modifier.padding(24.dp)) {
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
                    }
                }
            }

            // Bottom Right Controls
            Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, TactileTheme.Border)
                    ) {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp).size(22.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                    Surface(
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, TactileTheme.Border)
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp).size(22.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DesktopHeader(viewModel: MainViewModel, currentMode: com.tajemniktv.tajsos.data.ModeEntity?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "TAJSOS // STATUS: OK",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 10.sp
            )
            if (currentMode != null) {
                Text(
                    "MODE: ${currentMode.name.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                )
            }
        }

        OutlinedTextField(
            value = "",
            onValueChange = { },
            modifier = Modifier.width(420.dp).height(44.dp),
            placeholder = {
                Text(
                    "SEARCH YOUR LIFE...",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 10.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(18.dp)
                )
            },
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                focusedBorderColor = TactileTheme.Primary,
                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.2f)
            ),
            singleLine = true
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
            Icon(
                Icons.Default.Sync,
                contentDescription = null,
                tint = TactileTheme.Primary,
                modifier = Modifier.size(20.dp)
            )
            Box(
                modifier = Modifier.size(32.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DesktopDateProgressCard(
    modifier: Modifier = Modifier,
    date: String,
    progress: Float,
    completedTasks: Int,
    totalTasks: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(40.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    date,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 64.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    "CURRENT PROTOCOL ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 12.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                ProgressRing(progress = progress, size = 100.dp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Daily Pulse",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Text(
                        "Cognitive load optimization at peak levels. 4 key milestones achieved today.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.width(220.dp),
                        lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = TactileTheme.Primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "HIGH PERFORMANCE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TactileTheme.Primary
                            )
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "STEADY STATE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DesktopFocusCard(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    activeSession: FocusSessionEntity?,
    onToggleFocus: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "FOCUS MODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Switch(
                    checked = activeSession != null,
                    onCheckedChange = { onToggleFocus() },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = TactileTheme.Primary,
                        checkedThumbColor = Color.White
                    )
                )
            }

            Spacer(Modifier.weight(1f))

            val timerText = if (activeSession != null) {
                val duration =
                    (Clock.System.now().toEpochMilliseconds() - activeSession.startedAt) / 1000
                val m = (duration / 60) % 60
                val s = duration % 60
                "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
            } else "25:00"

            Text(
                timerText,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 72.sp,
                color = Color.White,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                "DEEP WORK SESSION",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.2f),
                letterSpacing = 1.sp,
                fontSize = 10.sp
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onToggleFocus,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TactileTheme.Primary.copy(
                        alpha = 0.4f
                    ), contentColor = TactileTheme.Primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (activeSession != null) "TERMINATE FOCUS" else "INITIALIZE FOCUS",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun DesktopHighPriorityCard(
    modifier: Modifier = Modifier,
    tasks: List<NodeWithPin>,
    onToggleTask: (NodeWithPin) -> Unit,
    onTaskClick: (Long) -> Unit,
    onViewAll: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "3 High Priority",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
                TextButton(onClick = onViewAll) {
                    Text(
                        "VIEW ALL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tasks.forEach { task ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .clickable { onTaskClick(task.node.id) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(18.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.2f))
                        ) { }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            task.node.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold
                        )
                        if (task.node.status == "overdue") {
                            Text(
                                "OVERDUE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Error,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                "14:00",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.2f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No high priority tasks",
                            color = Color.White.copy(alpha = 0.1f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DesktopTrackStatsCard(
    modifier: Modifier = Modifier,
    trackEntries: List<TrackEntryEntity>,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Insights,
                contentDescription = null,
                tint = TactileTheme.Primary,
                modifier = Modifier.align(Alignment.Start).size(20.dp)
            )
            Spacer(Modifier.weight(1f))
            Box(contentAlignment = Alignment.Center) {
                ProgressRing(progress = 0.8f, size = 70.dp, color = TactileTheme.Accent)
                Text(
                    "4/5",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Hydration, Meditation, Reading, Mobility.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.3f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "STREAK: 12 DAYS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun DesktopRecentlyEditedCard(
    modifier: Modifier = Modifier,
    node: NodeWithPin?,
    onEditNode: (Long) -> Unit
) {
    Surface(
        onClick = { node?.let { onEditNode(it.node.id) } },
        modifier = modifier,
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "RECENTLY EDITED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.2f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                if (node != null) "\"${node.node.title}\"" else "No recent notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                "Modified 12 minutes ago",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 10.sp
            )
            Spacer(Modifier.weight(1f))
            LinearProgressIndicator(
                progress = { 0.6f },
                modifier = Modifier.fillMaxWidth().height(2.dp).clip(CircleShape),
                color = TactileTheme.Primary,
                trackColor = Color.White.copy(alpha = 0.05f)
            )
        }
    }
}

@Composable
fun DesktopActiveProjectCard(
    modifier: Modifier = Modifier,
    project: NodeEntity?,
    onNavigateToProject: (Long) -> Unit
) {
    Surface(
        onClick = { project?.let { onNavigateToProject(it.id) } },
        modifier = modifier,
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "ACTIVE PROJECT",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                project?.title ?: "No active projects",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 22.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).background(TactileTheme.Primary, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(
                    "In Progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 10.sp
                )
            }
            Spacer(Modifier.weight(1f))
            LinearProgressIndicator(
                progress = { 0.4f },
                modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
                color = TactileTheme.Primary,
                trackColor = Color.White.copy(alpha = 0.05f)
            )
        }
    }
}

@Composable
fun DesktopNextEngagementCard(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "NEXT ENGAGEMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(
                    "10:00 AM",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Team Sync",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 20.sp
            )
            Text(
                "IN 45 MINS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 10.sp
            )
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .offset(x = (-8 * i).dp)
                            .background(Color.White.copy(alpha = 0.1f + (i * 0.1f)), CircleShape)
                            .border(2.dp, TactileTheme.Surface, CircleShape)
                    )
                }
                Text(
                    "+2",
                    modifier = Modifier.offset(x = (-12).dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun DesktopContextCard(
    modifier: Modifier = Modifier,
    allAreas: List<NodeEntity>,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Mountain graphic
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(size.width * 0.4f, size.height * 0.6f)
                    lineTo(size.width * 0.7f, size.height * 0.8f)
                    lineTo(size.width, size.height * 0.5f)
                    lineTo(size.width, size.height)
                    close()
                }
                clipPath(path) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
                        )
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "Home Office",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                Text(
                    "CONTEXT: WORK FLOW",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 10.sp
                )
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "FOCUS INDEX",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.2f),
                        fontSize = 10.sp
                    )
                    Text(
                        "OPTIMAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Success,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    size: androidx.compose.ui.unit.Dp,
    color: Color = TactileTheme.Primary
) {
    val anim by animateFloatAsState(targetValue = progress)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            drawCircle(color = Color.White.copy(alpha = 0.05f), style = Stroke(width = 8.dp.toPx()))
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360 * anim,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = (size.value * 0.25f).sp
        )
    }
}
