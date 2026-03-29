/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.tasks_command_subtitle
import tajsos.composeapp.generated.resources.tasks_command_title
import tajsos.composeapp.generated.resources.tasks_context_active
import tajsos.composeapp.generated.resources.tasks_context_blocked
import tajsos.composeapp.generated.resources.tasks_context_due_soon
import tajsos.composeapp.generated.resources.tasks_context_title
import tajsos.composeapp.generated.resources.tasks_current_priority
import tajsos.composeapp.generated.resources.tasks_do_now_action
import tajsos.composeapp.generated.resources.tasks_done_action
import tajsos.composeapp.generated.resources.tasks_empty
import tajsos.composeapp.generated.resources.tasks_open_action
import tajsos.composeapp.generated.resources.tasks_pin_today_action
import tajsos.composeapp.generated.resources.tasks_queue_title
import tajsos.composeapp.generated.resources.tasks_quick_add_action
import tajsos.composeapp.generated.resources.tasks_quick_add_hint
import tajsos.composeapp.generated.resources.tasks_quick_add_title
import tajsos.composeapp.generated.resources.tasks_quick_capture_action
import tajsos.composeapp.generated.resources.tasks_quick_capture_hint
import tajsos.composeapp.generated.resources.tasks_quick_capture_title
import tajsos.composeapp.generated.resources.tasks_start_focus_action
import kotlin.time.Clock

@Composable
internal fun TasksCommandView(
    tasks: List<NodeEntity>,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    todayTaskIds: Set<Long>,
    staleTasksCount: Int = 0,
    onSweepStaleTasks: () -> Unit = {},
    onOpen: (Long) -> Unit,
    onStartFocus: (NodeEntity) -> Unit,
    onDone: (NodeEntity) -> Unit,
    onPinToday: (NodeEntity) -> Unit,
    onQuickAdd: (String) -> Unit,
    onQuickCapture: (String) -> Unit,
) {
    val now = Clock.System.now().toEpochMilliseconds()
    val queue =
        remember(tasks, todayTaskIds) {
            tasks
                .sortedByDescending {
                    scoreTask(
                        it,
                        now,
                        todayTaskIds,
                    )
                }.take(8)
        }
    val current = queue.firstOrNull()
    var quickAdd by remember { mutableStateOf("") }
    var capture by remember { mutableStateOf("") }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val desktop = maxWidth > 1080.dp
        if (desktop) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
            ) {
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                ) {
                    Text(
                        stringResource(Res.string.tasks_command_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(Res.string.tasks_command_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    if (current == null) {
                        EmptyState(message = stringResource(Res.string.tasks_empty))
                    } else {
                        PriorityTaskCard(
                            current,
                            projectById,
                            areaById,
                            onOpen,
                            onStartFocus,
                            onDone,
                            onPinToday,
                        )
                        Text(
                            stringResource(Res.string.tasks_queue_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = TactileTheme.Text,
                        )
                        QueueList(queue, projectById, areaById, onOpen, onStartFocus, onDone)
                    }
                }
                CommandSidebar(
                    modifier = Modifier.weight(1f),
                    quickAdd = quickAdd,
                    capture = capture,
                    activeCount = tasks.count { it.taskStateOrNull() == TaskState.ACTIVE },
                    blockedCount = tasks.count { it.taskStateOrNull() == TaskState.BLOCKED },
                    dueSoonCount =
                        tasks.count {
                            it.dueAt != null && (
                                it.dueAt
                                    ?: Long.MAX_VALUE
                            ) <= now + 24L * 60 * 60 * 1000
                        },
                    staleTasksCount = staleTasksCount,
                    onSweepStaleTasks = onSweepStaleTasks,
                    onQuickAddChanged = { quickAdd = it },
                    onCaptureChanged = { capture = it },
                    onQuickAdd = {
                        val value = quickAdd.trim()
                        if (value.isNotBlank()) {
                            onQuickAdd(value)
                            quickAdd = ""
                        }
                    },
                    onCapture = {
                        val value = capture.trim()
                        if (value.isNotBlank()) {
                            onQuickCapture(value)
                            capture = ""
                        }
                    },
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
            ) {
                Text(
                    stringResource(Res.string.tasks_command_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                if (current == null) {
                    EmptyState(message = stringResource(Res.string.tasks_empty))
                } else {
                    PriorityTaskCard(
                        current,
                        projectById,
                        areaById,
                        onOpen,
                        onStartFocus,
                        onDone,
                        onPinToday,
                    )
                    QueueList(queue, projectById, areaById, onOpen, onStartFocus, onDone)
                }
                CommandSidebar(
                    modifier = Modifier.fillMaxWidth(),
                    quickAdd = quickAdd,
                    capture = capture,
                    activeCount = tasks.count { it.taskStateOrNull() == TaskState.ACTIVE },
                    blockedCount = tasks.count { it.taskStateOrNull() == TaskState.BLOCKED },
                    dueSoonCount =
                        tasks.count {
                            it.dueAt != null && (
                                it.dueAt
                                    ?: Long.MAX_VALUE
                            ) <= now + 24L * 60 * 60 * 1000
                        },
                    staleTasksCount = staleTasksCount,
                    onSweepStaleTasks = onSweepStaleTasks,
                    onQuickAddChanged = { quickAdd = it },
                    onCaptureChanged = { capture = it },
                    onQuickAdd = {
                        val value = quickAdd.trim()
                        if (value.isNotBlank()) {
                            onQuickAdd(value)
                            quickAdd = ""
                        }
                    },
                    onCapture = {
                        val value = capture.trim()
                        if (value.isNotBlank()) {
                            onQuickCapture(value)
                            capture = ""
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun PriorityTaskCard(
    task: NodeEntity,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onOpen: (Long) -> Unit,
    onStartFocus: (NodeEntity) -> Unit,
    onDone: (NodeEntity) -> Unit,
    onPinToday: (NodeEntity) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        color = TactileTheme.Surface,
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        ) {
            Text(
                stringResource(Res.string.tasks_current_priority),
                style = MaterialTheme.typography.labelMedium,
                color = TactileTheme.Primary,
            )
            Text(
                task.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TactileTheme.Text,
            )
            if (task.content.isNotBlank()) {
                Text(
                    task.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                    maxLines = 3,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                task.projectId
                    ?.let { projectById[it] }
                    ?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                task.areaId
                    ?.let { areaById[it] }
                    ?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                task.dueAt?.let { AssistChip(onClick = {}, label = { Text(shortDate(it)) }) }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(onClick = { onStartFocus(task) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.tasks_start_focus_action))
                }
                OutlinedButton(
                    onClick = { onOpen(task.id) },
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(Res.string.tasks_open_action)) }
                OutlinedButton(
                    onClick = { onPinToday(task) },
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(Res.string.tasks_pin_today_action)) }
                OutlinedButton(onClick = { onDone(task) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(Res.string.tasks_done_action))
                }
            }
        }
    }
}

@Composable
private fun QueueList(
    tasks: List<NodeEntity>,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onOpen: (Long) -> Unit,
    onDoNow: (NodeEntity) -> Unit,
    onDone: (NodeEntity) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        color = TactileTheme.Surface,
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column {
            tasks.forEach { task ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            task.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TactileTheme.Text,
                        )
                        val context =
                            listOfNotNull(
                                task.projectId?.let { projectById[it] },
                                task.areaId?.let { areaById[it] },
                                task.dueAt?.let(::shortDate),
                            ).joinToString(" • ")
                        if (context.isNotBlank()) {
                            Text(
                                context,
                                style = MaterialTheme.typography.bodySmall,
                                color = TactileTheme.Muted,
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                        OutlinedButton(onClick = { onDoNow(task) }) { Text(stringResource(Res.string.tasks_do_now_action)) }
                        OutlinedButton(onClick = { onOpen(task.id) }) { Text(stringResource(Res.string.tasks_open_action)) }
                        IconButton(onClick = { onDone(task) }) { Icon(Icons.Default.Check, null) }
                    }
                }
                HorizontalDivider(color = TactileTheme.Border)
            }
        }
    }
}

@Composable
private fun CommandSidebar(
    modifier: Modifier = Modifier,
    quickAdd: String,
    capture: String,
    activeCount: Int,
    blockedCount: Int,
    dueSoonCount: Int,
    staleTasksCount: Int = 0,
    onSweepStaleTasks: () -> Unit = {},
    onQuickAddChanged: (String) -> Unit,
    onCaptureChanged: (String) -> Unit,
    onQuickAdd: () -> Unit,
    onCapture: () -> Unit,
) {
    var showSweepDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        color = TactileTheme.Surface,
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
        ) {
            Text(
                stringResource(Res.string.tasks_quick_add_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = quickAdd,
                onValueChange = onQuickAddChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.tasks_quick_add_hint)) },
            )
            Button(onClick = onQuickAdd, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(
                        Res.string.tasks_quick_add_action,
                    ),
                )
            }
            HorizontalDivider(color = TactileTheme.Border)
            Text(
                stringResource(Res.string.tasks_quick_capture_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = capture,
                onValueChange = onCaptureChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.tasks_quick_capture_hint)) },
            )
            OutlinedButton(onClick = onCapture, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(Res.string.tasks_quick_capture_action),
                )
            }
            HorizontalDivider(color = TactileTheme.Border)
            Text(
                stringResource(Res.string.tasks_context_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            ContextRow(stringResource(Res.string.tasks_context_active), activeCount)
            ContextRow(stringResource(Res.string.tasks_context_blocked), blockedCount)
            ContextRow(stringResource(Res.string.tasks_context_due_soon), dueSoonCount)

            if (staleTasksCount > 0) {
                HorizontalDivider(color = TactileTheme.Border)
                OutlinedButton(
                    onClick = { showSweepDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, TactileTheme.Primary.copy(alpha = 0.5f))
                ) {
                    Text("Sweep $staleTasksCount Stale Tasks", color = TactileTheme.Primary)
                }
            }
        }
    }

    if (showSweepDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSweepDialog = false },
            title = {
                Text("Clear overdue backlog?")
            },
            text = {
                Text("You have $staleTasksCount stale overdue tasks. Want me to sweep them into Someday?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSweepStaleTasks()
                        showSweepDialog = false
                    }
                ) {
                    Text("Sweep to Someday")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSweepDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
