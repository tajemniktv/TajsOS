/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.ModuleButton
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
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

    val today =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()
    val moodToday = trackEntries.find { it.date == today }
    val tasksCount = dashboardState.tasksCount
    val notesCount = dashboardState.notesCount
    val pinnedKnowledge = dashboardState.pinnedKnowledge
    val upcomingDeadlines = dashboardState.upcomingDeadlines
    val overdueNodes = dashboardState.overdueNodes
    val relevantNote = dashboardState.relevantNote

    val insights by viewModel.insights.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg),
    ) {
        Text(
            text = "COMMAND",
            style = MaterialTheme.typography.displayMedium,
            color = TactileTheme.Text,
        )

        // 0. Search Bar (Search-first approach)
        OutlinedTextField(
            value = "",
            onValueChange = {
                viewModel.updateSearchQuery(it)
                onNavigateTo(Screen.Search)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "SEARCH YOUR LIFE...",
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

        // 1. Life Summary
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TactileTheme.Surface,
            shape = RoundedCornerShape(TactileTheme.RadiusLg),
            border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Border),
        ) {
            Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
                Text(
                    text = "LIFE SUMMARY",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = TactileTheme.Primary,
                )
                Spacer(Modifier.height(TactileTheme.SpacingMd))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${insights.weeklyCaptures}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TactileTheme.Text
                        )
                        Text(
                            text = "CAPTURES / WEEK",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${insights.weeklyCompletions}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = TactileTheme.Success
                        )
                        Text(
                            text = "DONE / WEEK",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted
                        )
                    }
                }
                Spacer(Modifier.height(TactileTheme.SpacingMd))
                LinearProgressIndicator(
                    progress = {
                        if (insights.weeklyCaptures > 0)
                            (insights.weeklyCompletions.toFloat() / insights.weeklyCaptures.toFloat()).coerceIn(
                                0f,
                                1f
                            )
                        else 0f
                    },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = TactileTheme.Primary,
                    trackColor = TactileTheme.Border,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        // 2. Recovery Mode
        val lowEnergyTasks = dashboardState.lowEnergyTasks
        if (moodToday?.energyScore != null && moodToday.energyScore!! <= 2 && lowEnergyTasks.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Success.copy(alpha = 0.1f),
                shape = RoundedCornerShape(TactileTheme.RadiusLg),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    TactileTheme.Success.copy(alpha = 0.3f)
                ),
            ) {
                Column(modifier = Modifier.padding(TactileTheme.SpacingLg)) {
                    Text(
                        text = "RECOVERY MODE // LOW ENERGY DETECTED",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                        color = TactileTheme.Success,
                    )
                    Spacer(Modifier.height(TactileTheme.SpacingMd))
                    Text(
                        text = "Your energy is low. Try these easy wins:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted
                    )
                    Spacer(Modifier.height(TactileTheme.SpacingMd))
                    lowEnergyTasks.take(2).forEach { nodeWithPin ->
                        Surface(
                            onClick = { onEditNode(nodeWithPin.node.id) },
                            color = TactileTheme.Surface,
                            shape = RoundedCornerShape(TactileTheme.RadiusSm),
                            modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                        ) {
                            Row(
                                Modifier.padding(TactileTheme.SpacingMd),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.BatteryChargingFull,
                                    contentDescription = null,
                                    tint = TactileTheme.Success,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    nodeWithPin.node.title,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Operating / Next Context
        if (activeSession != null || todayNodes.isNotEmpty() || allSessions.isNotEmpty()) {
            val activeItem =
                activeSession?.let { session ->
                    todayNodes.find { it.id == session.nodeId }
                        ?: allNodes
                            .find { it.node.id == session.nodeId }
                            ?.node
                } ?: todayNodes.firstOrNull() ?: allSessions.firstOrNull()?.let { session ->
                    allNodes
                        .find { it.node.id == session.nodeId }
                        ?.node
                }

            activeItem?.let { item ->
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (activeSession != null) {
                                        onNavigateTo(Screen.Focus)
                                    } else {
                                        viewModel.startFocusSession(item.id)
                                    }
                                },
                                onLongClick = { onEditNode(item.id) },
                            ),
                    color = if (activeSession != null) TactileTheme.Primary.copy(alpha = 0.15f) else TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusLg),
                    border =
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (activeSession != null) TactileTheme.Primary else TactileTheme.Border,
                        ),
                ) {
                    Row(
                        modifier = Modifier.padding(TactileTheme.SpacingLg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (activeSession != null) "OPERATING" else "NEXT CONTEXT",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                                color = if (activeSession != null) TactileTheme.Accent else TactileTheme.Primary,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.title.uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = TactileTheme.Text,
                            )
                        }

                        Button(
                            onClick = { if (activeSession != null) onNavigateTo(Screen.Focus) else viewModel.startFocusSession(item.id) },
                            shape = RoundedCornerShape(TactileTheme.RadiusSm),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = if (activeSession != null) TactileTheme.Primary else TactileTheme.Surface,
                                    contentColor = if (activeSession != null) TactileTheme.Background else TactileTheme.Primary,
                                ),
                            border = if (activeSession == null) androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TactileTheme.Primary
                            ) else null,
                        ) {
                            Text(if (activeSession != null) "VIEW" else "ENGAGE")
                        }
                    }
                }
            }
        }

        // 4. Inbox Warning
        if (inboxNodes.isNotEmpty()) {
            Surface(
                onClick = { onNavigateTo(Screen.Inbox) },
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (inboxNodes.size > 10) TactileTheme.Error.copy(alpha = 0.5f) else TactileTheme.Accent.copy(
                        alpha = 0.5f
                    )
                ),
            ) {
                Row(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (inboxNodes.size > 10) Icons.Default.Warning else Icons.Default.MailOutline,
                        contentDescription = null,
                        tint = if (inboxNodes.size > 10) TactileTheme.Error else TactileTheme.Accent
                    )
                    Spacer(modifier = Modifier.width(TactileTheme.SpacingMd))
                    Text(
                        text = if (inboxNodes.size > 10) "${inboxNodes.size} ITEMS OVERFLOWING INBOX" else "${inboxNodes.size} NEW ITEMS TO PROCESS",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (inboxNodes.size > 10) TactileTheme.Error else TactileTheme.Accent,
                    )
                }
            }
        }

        // 5. Batch Suggestion
        val batchableTasks = dashboardState.batchableTasks
        if (batchableTasks.isNotEmpty()) {
            val firstBatch = batchableTasks.values.first()
            val areaName =
                allAreas.find { it.id == firstBatch.first().node.areaId }?.title ?: "GENERAL"
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = "BATCH SUGGESTION // $areaName",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Accent,
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Accent.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        TactileTheme.Accent.copy(alpha = 0.2f)
                    ),
                ) {
                    Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                        Text(
                            "You have ${firstBatch.size} tasks in $areaName. Batch them?",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(4.dp))
                        firstBatch.take(3).forEach {
                            Text(
                                "• ${it.node.title}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Muted
                            )
                        }
                    }
                }
            }
        }

        // 6. Quick Wins
        val quickWins = dashboardState.quickWins
        if (quickWins.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = "QUICK WINS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Success,
                )
                quickWins.take(2).forEach { nodeWithPin ->
                    Surface(
                        onClick = { onEditNode(nodeWithPin.node.id) },
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Success.copy(alpha = 0.2f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = TactileTheme.Success,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                nodeWithPin.node.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // 7. Deep Work
        val deepWork = dashboardState.deepWork
        if (deepWork.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = "DEEP WORK // HIGH ENERGY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
                deepWork.take(2).forEach { nodeWithPin ->
                    Surface(
                        onClick = { onEditNode(nodeWithPin.node.id) },
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Primary.copy(alpha = 0.2f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = TactileTheme.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                nodeWithPin.node.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // 8. Top 3 For Today
        if (todayNodes.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = "TOP 3 FOR TODAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
                todayNodes.take(3).forEach { node ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { onEditNode(node.id) },
                            onLongClick = { onEditNode(node.id) },
                        ),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Muted.copy(alpha = 0.2f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = false,
                                onCheckedChange = { viewModel.updateNodeStatus(node, "done") },
                                colors = CheckboxDefaults.colors(
                                    uncheckedColor = TactileTheme.Primary,
                                    checkmarkColor = TactileTheme.Background
                                )
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingSm))
                            Text(
                                node.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 9. Overdue
        if (overdueNodes.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = "OVERDUE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error
                )
                overdueNodes.take(3).forEach { nodeWithPin ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { onEditNode(nodeWithPin.node.id) },
                            onLongClick = { onEditNode(nodeWithPin.node.id) },
                        ),
                        color = TactileTheme.Error.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Error.copy(alpha = 0.2f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = TactileTheme.Error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                nodeWithPin.node.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 10. Active Reminders
        if (activeReminders.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = "ACTIVE REMINDERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error
                )
                activeReminders.forEach { node ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { onEditNode(node.id) },
                            onLongClick = { onEditNode(node.id) },
                        ),
                        color = TactileTheme.Error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Error.copy(alpha = 0.3f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = TactileTheme.Error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                node.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.updateNode(node.copy(reminderAt = null)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Dismiss",
                                    tint = TactileTheme.Error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 11. Pinned Knowledge
        if (pinnedKnowledge.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = "PINNED KNOWLEDGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                pinnedKnowledge.take(2).forEach { nodeWithPin ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = { onEditNode(nodeWithPin.node.id) },
                                onLongClick = { onEditNode(nodeWithPin.node.id) },
                            ),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Muted.copy(alpha = 0.2f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = TactileTheme.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                text = nodeWithPin.node.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text
                            )
                        }
                    }
                }
            }
        }

        // 12. Upcoming Deadlines
        if (upcomingDeadlines.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = "UPCOMING DEADLINES",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Accent
                )
                upcomingDeadlines.forEach { nodeWithPin ->
                    val due = nodeWithPin.node.dueAt?.let {
                        Instant.fromEpochMilliseconds(it)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { onEditNode(nodeWithPin.node.id) },
                            onLongClick = { onEditNode(nodeWithPin.node.id) },
                        ),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Accent.copy(alpha = 0.3f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = TactileTheme.Accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                nodeWithPin.node.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text,
                                modifier = Modifier.weight(1f)
                            )
                            if (due != null) {
                                Text(
                                    "${due.day}/${due.month.number}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TactileTheme.Accent
                                )
                            }
                        }
                    }
                }
            }
        }

        // 13. Resets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)
        ) {
            Surface(
                onClick = { /* Placeholder action */ },
                modifier = Modifier.weight(1f),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    TactileTheme.Primary.copy(alpha = 0.3f)
                ),
            ) {
                Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                    Icon(
                        Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = TactileTheme.Primary
                    )
                    Spacer(Modifier.height(TactileTheme.SpacingSm))
                    Text(
                        "MORNING RESET",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Text
                    )
                }
            }
            Surface(
                onClick = { /* Placeholder action */ },
                modifier = Modifier.weight(1f),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    TactileTheme.Accent.copy(alpha = 0.3f)
                ),
            ) {
                Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                    Icon(
                        Icons.Default.NightsStay,
                        contentDescription = null,
                        tint = TactileTheme.Accent
                    )
                    Spacer(Modifier.height(TactileTheme.SpacingSm))
                    Text(
                        "EVENING SHUTDOWN",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Text
                    )
                }
            }
        }

        // 14. Relevant Project
        if (allProjects.isNotEmpty()) {
            val mostRelevantProject = allProjects.sortedByDescending { it.updatedAt }.firstOrNull()
            mostRelevantProject?.let { project ->
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                    Text(
                        text = "MOST RELEVANT PROJECT",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary
                    )
                    Surface(
                        onClick = { onNavigateToProject(project.id) },
                        modifier = Modifier.fillMaxWidth(),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Primary.copy(alpha = 0.2f)
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                tint = TactileTheme.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(
                                text = project.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Text
                            )
                        }
                    }
                }
            }
        }

        // 15. Relevant Note
        if (relevantNote != null) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = "MOST RELEVANT NOTE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                Surface(
                    onClick = { onEditNode(relevantNote.node.id) },
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        TactileTheme.Primary.copy(alpha = 0.2f)
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(TactileTheme.SpacingMd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = TactileTheme.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(TactileTheme.SpacingMd))
                        Text(
                            text = relevantNote.node.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TactileTheme.Text
                        )
                    }
                }
            }
        }

        // 16. Modules Grid
        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "TODAY",
                    icon = Icons.Default.DateRange,
                    status = if (todayNodes.isNotEmpty()) "${todayNodes.size} TASKS" else "EMPTY",
                    onClick = { onNavigateTo(Screen.Today) })
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "FOCUS",
                    icon = Icons.Default.PlayArrow,
                    status = if (todayNodes.isNotEmpty()) "READY" else "WAITING",
                    onClick = { onNavigateTo(Screen.Focus) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "TRACK",
                    icon = Icons.Default.CheckCircle,
                    status = moodToday?.let { "LOGGED" } ?: "PENDING",
                    onClick = { onNavigateTo(Screen.Track) })
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "TASKS",
                    icon = Icons.AutoMirrored.Filled.List,
                    status = if (tasksCount > 0) "$tasksCount TOTAL" else "NONE",
                    onClick = { onNavigateTo(Screen.Tasks) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "NOTES",
                    icon = Icons.Default.Edit,
                    status = if (notesCount > 0) "$notesCount TOTAL" else "NONE",
                    onClick = { onNavigateTo(Screen.Notes) })
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "PROJ",
                    icon = Icons.AutoMirrored.Filled.List,
                    status = if (allProjects.isNotEmpty()) "${allProjects.size} ACTIVE" else "EMPTY",
                    onClick = { onNavigateTo(Screen.Projects) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "AREA",
                    icon = Icons.Default.LocationOn,
                    status = if (allAreas.isNotEmpty()) "${allAreas.size} TOTAL" else "EMPTY",
                    onClick = { onNavigateTo(Screen.Areas) })
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "CAL",
                    icon = Icons.Default.Event,
                    status = if (calendarEntries.isNotEmpty()) "${calendarEntries.size} ITEMS" else "EMPTY",
                    onClick = { onNavigateTo(Screen.Calendar) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "STATS",
                    icon = Icons.Default.Info,
                    status = "VIEW",
                    onClick = { onNavigateTo(Screen.Insights) })
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "HISTORY",
                    icon = Icons.Default.History,
                    status = if (allSessions.isNotEmpty()) {
                        "LAST: ${
                            Instant.fromEpochMilliseconds(allSessions.first().startedAt)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date
                        }"
                    } else {
                        "EMPTY"
                    },
                    onClick = { viewModel.resumeLastSession() },
                )
            }
        }
    }
}
