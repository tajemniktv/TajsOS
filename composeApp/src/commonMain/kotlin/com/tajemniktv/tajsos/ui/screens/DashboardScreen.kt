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
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel, 
    onNavigateTo: (Screen) -> Unit,
    onEditNode: (Long) -> Unit
) {
    val todayNodes by viewModel.todayNodes.collectAsState()
    val trackEntries by viewModel.trackEntries.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    val activeNodes by viewModel.activeNodes.collectAsState()
    val inboxNodes by viewModel.inboxNodes.collectAsState()
    val activeReminders by viewModel.activeReminders.collectAsState()
    val calendarEntries by viewModel.calendarEntries.collectAsState()

    val today = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    val moodToday = trackEntries.find { it.date == today }
    val tasksCount = activeNodes.count { it.node.type == "task" }
    val notesCount = activeNodes.count { it.node.type == "note" || it.node.type == "idea" }
    val pinnedKnowledge =
        activeNodes.filter { it.node.isPinned && (it.node.type == "note" || it.node.type == "idea") }
    val upcomingDeadlines =
        activeNodes.filter { it.node.dueAt != null && it.node.status == "active" }
        .sortedBy { it.node.dueAt }
        .take(3)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg)
    ) {
        Text(
            text = "COMMAND",
            style = MaterialTheme.typography.displayMedium,
            color = TactileTheme.Text
        )

        if (activeSession != null || todayNodes.isNotEmpty() || allSessions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
            val activeItem = activeSession?.let { session ->
                todayNodes.find { it.id == session.nodeId }
                    ?: viewModel.allNodes.value.find { it.node.id == session.nodeId }?.node
            } ?: todayNodes.firstOrNull() ?: allSessions.firstOrNull()?.let { session ->
                viewModel.allNodes.value.find { it.node.id == session.nodeId }?.node
            }

            activeItem?.let { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = TactileTheme.SpacingSm)
                        .combinedClickable(
                            onClick = { if (activeSession != null) onNavigateTo(Screen.Focus) else viewModel.startFocusSession(item.id) },
                            onLongClick = { onEditNode(item.id) }
                        ),
                    color = if (activeSession != null) TactileTheme.Primary.copy(alpha = 0.15f) else TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusLg),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (activeSession != null) TactileTheme.Primary else TactileTheme.Border
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(TactileTheme.SpacingLg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (activeSession != null) "OPERATING" else "NEXT CONTEXT",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                                color = if (activeSession != null) TactileTheme.Accent else TactileTheme.Primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.title.uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = TactileTheme.Text
                            )
                        }

                        Button(
                            onClick = { if (activeSession != null) onNavigateTo(Screen.Focus) else viewModel.startFocusSession(item.id) },
                            shape = RoundedCornerShape(TactileTheme.RadiusSm),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeSession != null) TactileTheme.Primary else TactileTheme.Surface,
                                contentColor = if (activeSession != null) TactileTheme.Background else TactileTheme.Primary
                            ),
                            border = if (activeSession == null) androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Primary) else null
                        ) {
                            Text(if (activeSession != null) "VIEW" else "ENGAGE")
                        }
                    }
                }
            }
        }

        if (inboxNodes.isNotEmpty()) {
            Surface(
                onClick = { onNavigateTo(Screen.Inbox) },
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Accent.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MailOutline, contentDescription = null, tint = TactileTheme.Accent)
                    Spacer(modifier = Modifier.width(TactileTheme.SpacingMd))
                    Text(
                        text = "${inboxNodes.size} NEW ITEMS TO PROCESS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Accent
                    )
                }
            }
        }

        if (activeReminders.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = "ACTIVE REMINDERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error
                )
                activeReminders.forEach { node ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onEditNode(node.id) },
                                onLongClick = { onEditNode(node.id) }
                            ),
                        color = TactileTheme.Error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TactileTheme.Error.copy(alpha = 0.3f)
                        )
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
                            IconButton(onClick = {
                                viewModel.updateNode(node.copy(reminderAt = null))
                            }, modifier = Modifier.size(24.dp)) {
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

        if (pinnedKnowledge.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = "PINNED KNOWLEDGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                pinnedKnowledge.take(2).forEach { nodeWithPin ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = { onEditNode(nodeWithPin.node.id) },
                                onLongClick = { onEditNode(nodeWithPin.node.id) }
                            ),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Muted.copy(alpha = 0.2f))
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
                            Spacer(modifier = Modifier.width(TactileTheme.SpacingMd))
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

        if (upcomingDeadlines.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Text(
                    text = "UPCOMING DEADLINES",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Accent
                )
                upcomingDeadlines.forEach { nodeWithPin ->
                    val due = nodeWithPin.node.dueAt?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()) }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onEditNode(nodeWithPin.node.id) },
                                onLongClick = { onEditNode(nodeWithPin.node.id) }
                            ),
                        color = TactileTheme.Surface,
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Accent.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = TactileTheme.Accent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(TactileTheme.SpacingMd))
                            Text(nodeWithPin.node.title, style = MaterialTheme.typography.bodyMedium, color = TactileTheme.Text, modifier = Modifier.weight(1f))
                            if (due != null) {
                                Text("${due.day}/${due.month.number}", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Accent)
                            }
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "TODAY",
                    icon = Icons.Default.DateRange,
                    status = if (todayNodes.isNotEmpty()) "${todayNodes.size} TASKS" else "EMPTY",
                    onClick = { onNavigateTo(Screen.Today) }
                )
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "FOCUS",
                    icon = Icons.Default.PlayArrow,
                    status = if (todayNodes.isNotEmpty()) "READY" else "WAITING",
                    onClick = { onNavigateTo(Screen.Focus) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "TRACK",
                    icon = Icons.Default.CheckCircle,
                    status = moodToday?.let { "LOGGED" } ?: "PENDING",
                    onClick = { onNavigateTo(Screen.Track) }
                )
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "TASKS",
                    icon = Icons.AutoMirrored.Filled.List,
                    status = if (tasksCount > 0) "$tasksCount TOTAL" else "NONE",
                    onClick = { onNavigateTo(Screen.Tasks) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "NOTES",
                    icon = Icons.Default.Edit,
                    status = if (notesCount > 0) "$notesCount TOTAL" else "NONE",
                    onClick = { onNavigateTo(Screen.Notes) }
                )
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "PROJ",
                    icon = Icons.AutoMirrored.Filled.List,
                    status = if (allProjects.isNotEmpty()) "${allProjects.size} ACTIVE" else "EMPTY",
                    onClick = { onNavigateTo(Screen.Projects) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "AREA",
                    icon = Icons.Default.LocationOn,
                    status = if (allAreas.isNotEmpty()) "${allAreas.size} TOTAL" else "EMPTY",
                    onClick = { onNavigateTo(Screen.Areas) }
                )
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "CAL",
                    icon = Icons.Default.Event,
                    status = if (calendarEntries.isNotEmpty()) "${calendarEntries.size} ITEMS" else "EMPTY",
                    onClick = { onNavigateTo(Screen.Calendar) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "STATS",
                    icon = Icons.Default.Info,
                    status = "VIEW",
                    onClick = { onNavigateTo(Screen.Insights) }
                )
                ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "HISTORY",
                    icon = Icons.Default.History,
                    status = if (allSessions.isNotEmpty()) "LAST: ${
                        Instant.fromEpochMilliseconds(allSessions.first().startedAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    }" else "EMPTY",
                    onClick = { viewModel.resumeLastSession() }
                )
            }
        }
    }
}
