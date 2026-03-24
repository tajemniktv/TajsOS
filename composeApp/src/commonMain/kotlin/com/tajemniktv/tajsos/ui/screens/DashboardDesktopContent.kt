/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
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
import com.tajemniktv.tajsos.ui.design.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Clock

@Composable
fun DashboardDesktopContent(
    viewModel: MainViewModel,
    onNavigateTo: (Screen) -> Unit,
    onEditNode: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onNewEntry: () -> Unit,
    currentDestination: NavDestination? = null,
    currentMode: ModeEntity? = null,
    allModes: List<ModeEntity> = emptyList(),
    onModeSelect: (Long) -> Unit = {}
) {
    val allNodes by viewModel.allNodes.collectAsState()
    val todayNodes by viewModel.todayNodes.collectAsState()
    val trackEntries by viewModel.trackEntries.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val dashboardState by viewModel.dashboardUIState.collectAsState()
    val insights by viewModel.insights.collectAsState()

    val inboxNodes by viewModel.inboxNodes.collectAsState()
    val activeReminders by viewModel.activeReminders.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    val scope = rememberCoroutineScope()

    val blockList = remember(dashboardState.modePreferences) {
        try {
            val jsonStr = dashboardState.modePreferences?.dashboardBlocksJson
            if (jsonStr != null) {
                kotlinx.serialization.json.Json.decodeFromString<List<String>>(jsonStr)
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
    val todayDateStr = localNow.date.dayOfWeek.name.take(3).lowercase()
        .replaceFirstChar { it.uppercase() } + ", " +
            localNow.date.month.name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() } + " " + localNow.date.day
    val todayIsoDateStr = localNow.date.toString()
    val moodToday = trackEntries.find { it.date == todayIsoDateStr }

    val lastWeeklyReview = allReviews.find { it.type == "weekly" }
    val weekMillis = 7 * 24 * 60 * 60 * 1000L
    val needsWeeklyReview =
        lastWeeklyReview == null || (now.toEpochMilliseconds() - lastWeeklyReview.completedAt) > weekMillis

    // Main Dashboard Area
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 32.dp)) {
        // Header
        DesktopHeader(viewModel = viewModel, currentMode = currentMode)

        Spacer(Modifier.height(32.dp))

        // Bento Grid
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
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
                        onNavigateToProject = onNavigateToProject
                    )
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

@Composable
fun DesktopHeader(viewModel: MainViewModel, currentMode: ModeEntity?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "TAJSOS // STATUS: OK",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            if (currentMode != null) {
                Text(
                    "MODE: ${currentMode.name.uppercase()}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TactileTheme.Text,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = TactileTheme.Muted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "SEARCH YOUR LIFE...",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(TactileTheme.Surface, RoundedCornerShape(TactileTheme.RadiusMd))
                    .border(1.dp, TactileTheme.Border, RoundedCornerShape(TactileTheme.RadiusMd)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = TactileTheme.Text,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
