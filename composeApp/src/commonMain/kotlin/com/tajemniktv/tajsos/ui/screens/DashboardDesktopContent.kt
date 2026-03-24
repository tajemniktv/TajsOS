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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.dashboard.DashboardBlockRenderer
import com.tajemniktv.tajsos.ui.theme.TactileTheme
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
    currentDestination: NavDestination?,
)
{
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
        modifier = Modifier
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
                val blocks = listOf(
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
                        needsWeeklyReview = false, // Desktop can handle this differently
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
                // Quick capture on desktop
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("CMD + K to capture anything...") },
                    leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null) },
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                )

                // Stats / Activity
                com.tajemniktv.tajsos.ui.components.dashboard.LifeSummaryCard(
                    captures = insights.weeklyCaptures,
                    completions = insights.weeklyCompletions,
                    onClick = { onNavigateTo(Screen.Insights) },
                )

                // Time / Context
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

        // Bottom command bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TactileTheme.Surface.copy(alpha = 0.5f),
            shape = RoundedCornerShape(TactileTheme.RadiusMd),
            border = BorderStroke(1.dp, TactileTheme.Border),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
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
                    modifier = Modifier
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
private fun CommandItem(key: String, action: String)
{
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            key,
            modifier = Modifier
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
