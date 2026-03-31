/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.focus

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.ItemKind
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.focus_capture
import tajsos.composeapp.generated.resources.focus_capture_hint
import tajsos.composeapp.generated.resources.focus_capture_save
import tajsos.composeapp.generated.resources.focus_complete_task
import tajsos.composeapp.generated.resources.focus_current_task
import tajsos.composeapp.generated.resources.focus_end_session
import tajsos.composeapp.generated.resources.focus_focus_minutes_today
import tajsos.composeapp.generated.resources.focus_mode
import tajsos.composeapp.generated.resources.focus_next_tiny_step
import tajsos.composeapp.generated.resources.focus_no_active_task
import tajsos.composeapp.generated.resources.focus_open_task
import tajsos.composeapp.generated.resources.focus_pause
import tajsos.composeapp.generated.resources.focus_quick_wins
import tajsos.composeapp.generated.resources.focus_recent_sessions
import tajsos.composeapp.generated.resources.focus_replace_step
import tajsos.composeapp.generated.resources.focus_resume
import tajsos.composeapp.generated.resources.focus_session_duration
import tajsos.composeapp.generated.resources.focus_sessions_today
import tajsos.composeapp.generated.resources.focus_start
import tajsos.composeapp.generated.resources.focus_start_now
import tajsos.composeapp.generated.resources.focus_suggested_now
import tajsos.composeapp.generated.resources.focus_switch_target
import tajsos.composeapp.generated.resources.focus_unknown_task

object FocusDashboardBlockRegistry {
    private val renderers: Map<String, FocusDashboardBlockRenderer> =
        mapOf("focus_main" to ::renderFocusMainBlock)

    fun resolve(id: String): FocusDashboardBlockRenderer? = renderers[id]
}

@Composable private fun renderFocusMainBlock(context: FocusDashboardContext) = FocusMainBlock(context.viewModel)

@Composable
internal fun FocusMainBlock(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val todayNodes by viewModel.todayNodes.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val tasks =
        remember(todayNodes, allNodes) {
            (todayNodes + allNodes.map { it.node })
                .filter {
                    it.isTaskItem() && it.status != "archived" &&
                        it.taskStateOrNull() != TaskState.DONE
                }.distinctBy { it.id }
        }
    val current =
        remember(
            activeSession,
            tasks,
        ) { activeSession?.let { s -> tasks.find { it.id == s.nodeId } } ?: tasks.firstOrNull() }
    if (current == null) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) { Text(stringResource(Res.string.focus_no_active_task), color = TajsOSTheme.Muted) }
        return
    }
    val suggestions =
        remember(
            tasks,
            activeSession,
        ) {
            tasks
                .sortedByDescending {
                    (if (it.id == activeSession?.nodeId) 20 else 0) + (if (it.dueAt != null) 8 else 0) + (
                        if ((
                                it.estimatedMinutes
                                    ?: 999
                            ) <= 45
                        ) {
                            4
                        } else {
                            0
                        }
                    )
                }.take(5)
        }
    val quickWins =
        remember(tasks) {
            tasks
                .filter {
                    (
                        it.estimatedMinutes
                            ?: Int.MAX_VALUE
                    ) <= 15 || it.energyLevel == 1 || it.friction == "easy"
                }.take(4)
        }
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(activeSession) {
        if (activeSession != null) {
            while (true) {
                seconds =
                    (
                            (
                            kotlin.time.Clock.System
                                .now()
                                .toEpochMilliseconds() - (
                                activeSession?.startedAt
                                    ?: 0L
                            )
                        ) / 1000L
                    ).toInt()
                delay(1000L)
            }
        } else {
            seconds = 0
        }
    }
    var step by remember(current.id) { mutableStateOf(current.nextSmallestStep ?: current.content) }
    var capture by remember { mutableStateOf("") }
    val sessionsToday = allSessions.count()
    val minutesToday = allSessions.sumOf { it.durationSec } / 60
    val timer = "${(seconds / 60).toString().padStart(2, '0')}:${
        (seconds % 60).toString().padStart(2, '0')
    }"

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(TajsOSTheme.Background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            color = TajsOSTheme.Surface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            border = BorderStroke(1.dp, TajsOSTheme.GhostBorder)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(Res.string.focus_mode),
                    color = TajsOSTheme.Primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(Res.string.focus_current_task),
                    color = TajsOSTheme.Muted,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    current.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    timer,
                    style = MaterialTheme.typography.displayLarge.copy(letterSpacing = (-1).sp),
                    color = TajsOSTheme.Text
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("${stringResource(Res.string.focus_sessions_today)}: $sessionsToday") },
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("${stringResource(Res.string.focus_focus_minutes_today)}: $minutesToday") },
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(onClick = {
                        if (activeSession == null) {
                            viewModel.startFocusSession(
                                current.id,
                            )
                        } else {
                            viewModel.stopFocusSession(completed = false, interrupted = true)
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Icon(
                            if (activeSession == null) Icons.Default.PlayArrow else Icons.Default.Pause,
                            null,
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            if (activeSession == null) {
                                stringResource(
                                    Res.string.focus_start,
                                )
                            } else {
                                stringResource(Res.string.focus_pause)
                            },
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.resumeLastSession() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.size(6.dp))
                        Text(
                            stringResource(Res.string.focus_resume),
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.updateNodeStatus(current, "done") },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.size(6.dp))
                        Text(
                            stringResource(Res.string.focus_complete_task),
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.stopFocusSession() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Stop, null)
                        Spacer(Modifier.size(6.dp))
                        Text(
                            stringResource(Res.string.focus_end_session),
                        )
                    }
                }
            }
        }

        Surface(
            color = TajsOSTheme.Surface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            border = BorderStroke(1.dp, TajsOSTheme.GhostBorder)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(Res.string.focus_next_tiny_step),
                    style = MaterialTheme.typography.titleMedium,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = step,
                    onValueChange = { step = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TajsOSTheme.Primary,
                            unfocusedBorderColor = TajsOSTheme.Border
                        )
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedButton(
                        onClick = { viewModel.updateNode(current.copy(nextSmallestStep = step)) },
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(Res.string.focus_replace_step)) }
                    OutlinedButton(
                        onClick = { viewModel.updateNodeStatus(current, "done") },
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(Res.string.focus_complete_task)) }
                    OutlinedButton(
                        onClick = { viewModel.togglePin(current, true) },
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(Res.string.focus_open_task)) }
                    OutlinedButton(
                        onClick = {
                            if (activeSession == null) {
                                viewModel.startFocusSession(
                                    current.id,
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(Res.string.focus_start_now)) }
                }
            }
        }

        FocusListCard(stringResource(Res.string.focus_suggested_now), suggestions) { task ->
            if (activeSession != null) {
                viewModel.stopFocusSession(
                    completed = false,
                    interrupted = true,
                    note = "Switched target",
                )
            }
            viewModel.startFocusSession(task.id)
        }
        FocusListCard(stringResource(Res.string.focus_quick_wins), quickWins) { task ->
            if (activeSession != null) {
                viewModel.stopFocusSession(
                    completed = false,
                    interrupted = true,
                    note = "Switched target",
                )
            }
            viewModel.startFocusSession(task.id)
        }

        Surface(
            color = TajsOSTheme.Surface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            border = BorderStroke(1.dp, TajsOSTheme.GhostBorder)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(Res.string.focus_capture),
                    style = MaterialTheme.typography.titleMedium,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = capture,
                    onValueChange = { capture = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(Res.string.focus_capture_hint)) },
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TajsOSTheme.Primary,
                            unfocusedBorderColor = TajsOSTheme.Border
                        )
                )
                Button(onClick = {
                    if (capture.isNotBlank()) {
                        viewModel.captureInboxEntry(
                            capture,
                            current.areaId,
                            current.projectId,
                            ItemKind.TASK,
                            "focus",
                        )
                        capture = ""
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Inbox, null)
                    Spacer(Modifier.size(6.dp))
                    Text(
                        stringResource(Res.string.focus_capture_save),
                    )
                }
            }
        }

        Surface(
            color = TajsOSTheme.Surface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            border = BorderStroke(1.dp, TajsOSTheme.GhostBorder)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(Res.string.focus_recent_sessions),
                    style = MaterialTheme.typography.titleMedium,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.SemiBold,
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(220.dp),
                ) {
                    items(allSessions.take(8)) { s ->
                        val title =
                            allNodes.find { it.node.id == s.nodeId }?.node?.title ?: stringResource(
                                Res.string.focus_unknown_task,
                            )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TajsOSTheme.Text
                                )
                                Text(
                                    stringResource(
                                        Res.string.focus_session_duration,
                                        s.durationSec / 60,
                                        s.durationSec % 60,
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TajsOSTheme.Muted
                                )
                            }
                            Icon(Icons.Default.History, null, tint = TajsOSTheme.Muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusListCard(
    title: String,
    nodes: List<NodeEntity>,
    onPick: (NodeEntity) -> Unit,
) {
    Surface(
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.GhostBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.SemiBold,
            )
            if (nodes.isEmpty()) {
                Text(
                    stringResource(Res.string.focus_no_active_task),
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted
                )
            } else {
                nodes.forEach { task ->
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = TajsOSTheme.SurfaceLow
                        ),
                        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    task.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TajsOSTheme.Text
                                )
                                val tiny =
                                    listOfNotNull(
                                        task.nextSmallestStep,
                                        task.estimatedMinutes?.let { "${it}m" },
                                    ).joinToString(" • ")
                                        .ifBlank { "-" }
                                Text(
                                    tiny,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TajsOSTheme.Muted
                                )
                            }
                            OutlinedButton(onClick = { onPick(task) }) {
                                Icon(
                                    Icons.Default.SkipNext,
                                    null,
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(stringResource(Res.string.focus_switch_target))
                            }
                        }
                    }
                }
            }
        }
    }
}
