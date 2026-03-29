/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.InboxEntryEntity
import com.tajemniktv.tajsos.data.ItemKind
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.archive_empty
import tajsos.composeapp.generated.resources.tasks_all_title
import tajsos.composeapp.generated.resources.tasks_archive_action
import tajsos.composeapp.generated.resources.tasks_archive_subtitle
import tajsos.composeapp.generated.resources.tasks_archive_title
import tajsos.composeapp.generated.resources.tasks_column_context
import tajsos.composeapp.generated.resources.tasks_column_due
import tajsos.composeapp.generated.resources.tasks_column_status
import tajsos.composeapp.generated.resources.tasks_column_task
import tajsos.composeapp.generated.resources.tasks_command_subtitle
import tajsos.composeapp.generated.resources.tasks_command_title
import tajsos.composeapp.generated.resources.tasks_context_active
import tajsos.composeapp.generated.resources.tasks_context_blocked
import tajsos.composeapp.generated.resources.tasks_context_due_soon
import tajsos.composeapp.generated.resources.tasks_context_title
import tajsos.composeapp.generated.resources.tasks_current_priority
import tajsos.composeapp.generated.resources.tasks_delete_action
import tajsos.composeapp.generated.resources.tasks_detail_area
import tajsos.composeapp.generated.resources.tasks_detail_due
import tajsos.composeapp.generated.resources.tasks_detail_estimate
import tajsos.composeapp.generated.resources.tasks_detail_next_step
import tajsos.composeapp.generated.resources.tasks_detail_project
import tajsos.composeapp.generated.resources.tasks_detail_status
import tajsos.composeapp.generated.resources.tasks_detail_updated
import tajsos.composeapp.generated.resources.tasks_details_title
import tajsos.composeapp.generated.resources.tasks_do_now_action
import tajsos.composeapp.generated.resources.tasks_done_action
import tajsos.composeapp.generated.resources.tasks_empty
import tajsos.composeapp.generated.resources.tasks_inbox_capture_entries
import tajsos.composeapp.generated.resources.tasks_inbox_dismiss
import tajsos.composeapp.generated.resources.tasks_inbox_empty_entries
import tajsos.composeapp.generated.resources.tasks_inbox_empty_tasks
import tajsos.composeapp.generated.resources.tasks_inbox_mark_processed
import tajsos.composeapp.generated.resources.tasks_inbox_subtitle
import tajsos.composeapp.generated.resources.tasks_inbox_tasks_title
import tajsos.composeapp.generated.resources.tasks_inbox_title
import tajsos.composeapp.generated.resources.tasks_inbox_triage_task
import tajsos.composeapp.generated.resources.tasks_no_results
import tajsos.composeapp.generated.resources.tasks_open_action
import tajsos.composeapp.generated.resources.tasks_pin_today_action
import tajsos.composeapp.generated.resources.tasks_queue_title
import tajsos.composeapp.generated.resources.tasks_quick_add_action
import tajsos.composeapp.generated.resources.tasks_quick_add_hint
import tajsos.composeapp.generated.resources.tasks_quick_add_title
import tajsos.composeapp.generated.resources.tasks_quick_capture_action
import tajsos.composeapp.generated.resources.tasks_quick_capture_hint
import tajsos.composeapp.generated.resources.tasks_quick_capture_title
import tajsos.composeapp.generated.resources.tasks_restore_action
import tajsos.composeapp.generated.resources.tasks_scope_active
import tajsos.composeapp.generated.resources.tasks_scope_archived
import tajsos.composeapp.generated.resources.tasks_scope_completed
import tajsos.composeapp.generated.resources.tasks_search_placeholder
import tajsos.composeapp.generated.resources.tasks_sort_due
import tajsos.composeapp.generated.resources.tasks_sort_priority
import tajsos.composeapp.generated.resources.tasks_sort_title
import tajsos.composeapp.generated.resources.tasks_sort_updated
import tajsos.composeapp.generated.resources.tasks_start_focus_action
import tajsos.composeapp.generated.resources.tasks_tab_all_tasks
import tajsos.composeapp.generated.resources.tasks_tab_archive
import tajsos.composeapp.generated.resources.tasks_tab_command
import tajsos.composeapp.generated.resources.tasks_tab_inbox
import tajsos.composeapp.generated.resources.tasks_tab_today
import tajsos.composeapp.generated.resources.tasks_title
import tajsos.composeapp.generated.resources.tasks_today_due_soon
import tajsos.composeapp.generated.resources.tasks_today_empty
import tajsos.composeapp.generated.resources.tasks_today_overdue
import tajsos.composeapp.generated.resources.tasks_today_pinned
import tajsos.composeapp.generated.resources.tasks_today_subtitle
import tajsos.composeapp.generated.resources.tasks_today_title
import tajsos.composeapp.generated.resources.tasks_workspace_subtitle
import kotlin.time.Clock

private enum class TasksTab { COMMAND, INBOX, TODAY, ALL, ARCHIVE }

private enum class TaskScope { ACTIVE, COMPLETED, ARCHIVED }

private enum class TaskSort { PRIORITY, DUE, UPDATED, TITLE }

@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val activeNodes by viewModel.activeNodes.collectAsState()
    val todayNodes by viewModel.todayNodes.collectAsState()
    val archivedNodes by viewModel.archivedNodes.collectAsState()
    val inboxEntries by viewModel.inboxEntries.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()

    val activeTasks =
        remember(activeNodes) {
            activeNodes.map { it.node }.filter { it.isTaskItem() && it.status != "archived" }
        }
    val archivedTasks =
        remember(archivedNodes) { archivedNodes.map { it.node }.filter { it.isTaskItem() } }
    val todayTaskIds =
        remember(todayNodes) { todayNodes.filter { it.isTaskItem() }.map { it.id }.toSet() }
    val projectById = remember(allProjects) { allProjects.associate { it.id to it.title } }
    val areaById = remember(allAreas) { allAreas.associate { it.id to it.title } }

    var tab by remember { mutableStateOf(TasksTab.COMMAND) }

    Column(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(stringResource(Res.string.tasks_title), style = MaterialTheme.typography.displaySmall)
        Text(
            stringResource(Res.string.tasks_workspace_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            taskTabChip(
                tab == TasksTab.COMMAND,
                stringResource(Res.string.tasks_tab_command),
            ) { tab = TasksTab.COMMAND }
            taskTabChip(tab == TasksTab.INBOX, stringResource(Res.string.tasks_tab_inbox)) {
                tab = TasksTab.INBOX
            }
            taskTabChip(tab == TasksTab.TODAY, stringResource(Res.string.tasks_tab_today)) {
                tab = TasksTab.TODAY
            }
            taskTabChip(tab == TasksTab.ALL, stringResource(Res.string.tasks_tab_all_tasks)) {
                tab = TasksTab.ALL
            }
            taskTabChip(
                tab == TasksTab.ARCHIVE,
                stringResource(Res.string.tasks_tab_archive),
            ) { tab = TasksTab.ARCHIVE }
        }

        when (tab)
        {
            TasksTab.COMMAND -> {
                TasksCommandView(
                    tasks = activeTasks.filter { it.taskStateOrNull() != TaskState.DONE },
                    projectById = projectById,
                    areaById = areaById,
                    todayTaskIds = todayTaskIds,
                    onOpen = onEditNode,
                    onStartFocus = { viewModel.startFocusSession(it.id) },
                    onDone = { viewModel.updateNodeStatus(it, TaskState.DONE.storageKey) },
                    onPinToday = { viewModel.togglePin(it, true) },
                    onQuickAdd = {
                        viewModel.addNode(
                            title = it,
                            type = ItemKind.TASK.storageKey,
                            contextScreen = "tasks_command",
                            inboxState = false,
                        )
                    },
                    onQuickCapture = {
                        viewModel.captureInboxEntry(
                            rawText = it,
                            suggestedKind = ItemKind.TASK,
                            contextScreen = "tasks_command",
                        )
                    },
                )
            }

            TasksTab.INBOX -> {
                TasksInboxView(
                    inboxEntries = inboxEntries,
                    inboxTasks = activeTasks.filter { it.inboxState && it.taskStateOrNull() != TaskState.DONE },
                    projectById = projectById,
                    areaById = areaById,
                    onTriageTask = { viewModel.triageInboxEntry(it.id, ItemKind.TASK) },
                    onDismiss = viewModel::dismissInboxEntry,
                    onMarkProcessed = { viewModel.markAsProcessed(it.id) },
                    onOpen = onEditNode,
                )
            }

            TasksTab.TODAY -> {
                TasksTodayView(
                    tasks = activeTasks.filter { it.taskStateOrNull() != TaskState.DONE },
                    todayTaskIds = todayTaskIds,
                    projectById = projectById,
                    areaById = areaById,
                    onOpen = onEditNode,
                    onDone = { viewModel.updateNodeStatus(it, TaskState.DONE.storageKey) },
                    onDoNow = { viewModel.startFocusSession(it.id) },
                )
            }

            TasksTab.ALL -> {
                TasksAllView(
                    activeTasks = activeTasks,
                    archivedTasks = archivedTasks,
                    projectById = projectById,
                    areaById = areaById,
                    onOpen = onEditNode,
                    onDone = { viewModel.updateNodeStatus(it, TaskState.DONE.storageKey) },
                    onArchive = viewModel::archiveNode,
                    onRestore = { viewModel.updateNodeStatus(it, TaskState.ACTIVE.storageKey) },
                    onDelete = viewModel::deleteNodePermanently,
                )
            }

            TasksTab.ARCHIVE -> {
                TasksArchiveView(
                    archivedTasks = archivedTasks,
                    projectById = projectById,
                    areaById = areaById,
                    onOpen = onEditNode,
                    onRestore = { viewModel.updateNodeStatus(it, TaskState.ACTIVE.storageKey) },
                    onDelete = viewModel::deleteNodePermanently,
                )
            }
        }
    }
}

@Composable
private fun taskTabChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun TasksCommandView(
    tasks: List<NodeEntity>,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    todayTaskIds: Set<Long>,
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
    onQuickAddChanged: (String) -> Unit,
    onCaptureChanged: (String) -> Unit,
    onQuickAdd: () -> Unit,
    onCapture: () -> Unit,
) {
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
            contextRow(stringResource(Res.string.tasks_context_active), activeCount)
            contextRow(stringResource(Res.string.tasks_context_blocked), blockedCount)
            contextRow(stringResource(Res.string.tasks_context_due_soon), dueSoonCount)
        }
    }
}

@Composable
private fun contextRow(
    label: String,
    value: Int,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Muted)
        Text(
            value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = TactileTheme.Text,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TasksInboxView(
    inboxEntries: List<InboxEntryEntity>,
    inboxTasks: List<NodeEntity>,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onTriageTask: (InboxEntryEntity) -> Unit,
    onDismiss: (InboxEntryEntity) -> Unit,
    onMarkProcessed: (NodeEntity) -> Unit,
    onOpen: (Long) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            stringResource(Res.string.tasks_inbox_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(Res.string.tasks_inbox_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )

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
                    stringResource(Res.string.tasks_inbox_capture_entries),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (inboxEntries.isEmpty()) {
                    Text(
                        stringResource(Res.string.tasks_inbox_empty_entries),
                        color = TactileTheme.Muted,
                    )
                } else {
                    inboxEntries.take(20).forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    entry.rawText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TactileTheme.Text,
                                )
                                Text(
                                    shortDate(entry.capturedAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TactileTheme.Muted,
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                                OutlinedButton(onClick = { onTriageTask(entry) }) {
                                    Text(
                                        stringResource(Res.string.tasks_inbox_triage_task),
                                    )
                                }
                                IconButton(onClick = { onDismiss(entry) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        stringResource(Res.string.tasks_inbox_dismiss),
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = TactileTheme.Border)
                    }
                }
            }
        }

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
                    stringResource(Res.string.tasks_inbox_tasks_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (inboxTasks.isEmpty()) {
                    Text(
                        stringResource(Res.string.tasks_inbox_empty_tasks),
                        color = TactileTheme.Muted,
                    )
                } else {
                    inboxTasks.forEach { task ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                OutlinedButton(onClick = { onOpen(task.id) }) {
                                    Text(
                                        stringResource(
                                            Res.string.tasks_open_action,
                                        ),
                                    )
                                }
                                OutlinedButton(onClick = { onMarkProcessed(task) }) {
                                    Text(
                                        stringResource(Res.string.tasks_inbox_mark_processed),
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = TactileTheme.Border)
                    }
                }
            }
        }
    }
}

@Composable
private fun TasksTodayView(
    tasks: List<NodeEntity>,
    todayTaskIds: Set<Long>,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onOpen: (Long) -> Unit,
    onDone: (NodeEntity) -> Unit,
    onDoNow: (NodeEntity) -> Unit,
) {
    val now = Clock.System.now().toEpochMilliseconds()
    val tomorrow = now + 24L * 60 * 60 * 1000
    val overdue = remember(tasks) { tasks.filter { (it.dueAt ?: Long.MAX_VALUE) < now } }
    val dueSoon =
        remember(tasks) {
            tasks.filter {
                it.dueAt != null && (it.dueAt ?: Long.MAX_VALUE) in now..tomorrow
            }
        }
    val pinned = remember(tasks, todayTaskIds) { tasks.filter { it.id in todayTaskIds } }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            stringResource(Res.string.tasks_today_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(Res.string.tasks_today_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )
        if (overdue.isEmpty() && dueSoon.isEmpty() && pinned.isEmpty()) {
            EmptyState(message = stringResource(Res.string.tasks_today_empty))
            return@Column
        }
        todaySection(
            stringResource(Res.string.tasks_today_overdue),
            overdue,
            projectById,
            areaById,
            onOpen,
            onDone,
            onDoNow,
        )
        todaySection(
            stringResource(Res.string.tasks_today_due_soon),
            dueSoon,
            projectById,
            areaById,
            onOpen,
            onDone,
            onDoNow,
        )
        todaySection(
            stringResource(Res.string.tasks_today_pinned),
            pinned,
            projectById,
            areaById,
            onOpen,
            onDone,
            onDoNow,
        )
    }
}

@Composable
private fun todaySection(
    title: String,
    tasks: List<NodeEntity>,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onOpen: (Long) -> Unit,
    onDone: (NodeEntity) -> Unit,
    onDoNow: (NodeEntity) -> Unit,
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
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (tasks.isEmpty()) {
                Text(stringResource(Res.string.tasks_no_results), color = TactileTheme.Muted)
            } else {
                tasks.take(8).forEach { task ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                            IconButton(onClick = { onDone(task) }) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TasksAllView(
    activeTasks: List<NodeEntity>,
    archivedTasks: List<NodeEntity>,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onOpen: (Long) -> Unit,
    onDone: (NodeEntity) -> Unit,
    onArchive: (NodeEntity) -> Unit,
    onRestore: (NodeEntity) -> Unit,
    onDelete: (NodeEntity) -> Unit,
) {
    var scope by remember { mutableStateOf(TaskScope.ACTIVE) }
    var sort by remember { mutableStateOf(TaskSort.PRIORITY) }
    var query by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<Long?>(null) }

    val base =
        when (scope)
        {
            TaskScope.ACTIVE -> activeTasks.filter { it.taskStateOrNull() != TaskState.DONE }
            TaskScope.COMPLETED -> activeTasks.filter { it.taskStateOrNull() == TaskState.DONE }
            TaskScope.ARCHIVED -> archivedTasks
        }
    val filtered =
        base.filter {
            query.isBlank() ||
                it.title.contains(
                    query,
                    true,
                ) || it.content.contains(query, true)
        }
    val sorted =
        when (sort)
        {
            TaskSort.PRIORITY -> {
                filtered.sortedByDescending {
                    scoreTask(
                        it,
                        Clock.System.now().toEpochMilliseconds(),
                        emptySet(),
                    )
                }
            }

            TaskSort.DUE -> {
                filtered.sortedBy { it.dueAt ?: Long.MAX_VALUE }
            }

            TaskSort.UPDATED -> {
                filtered.sortedByDescending { it.updatedAt }
            }

            TaskSort.TITLE -> {
                filtered.sortedBy { it.title.lowercase() }
            }
        }
    val selected = sorted.find { it.id == selectedId } ?: sorted.firstOrNull()
    if (selected != null && selectedId == null) selectedId = selected.id

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            stringResource(Res.string.tasks_all_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(Res.string.tasks_search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
            )
            taskTabChip(
                scope == TaskScope.ACTIVE,
                stringResource(Res.string.tasks_scope_active),
            ) { scope = TaskScope.ACTIVE }
            taskTabChip(
                scope == TaskScope.COMPLETED,
                stringResource(Res.string.tasks_scope_completed),
            ) { scope = TaskScope.COMPLETED }
            taskTabChip(
                scope == TaskScope.ARCHIVED,
                stringResource(Res.string.tasks_scope_archived),
            ) { scope = TaskScope.ARCHIVED }
            taskTabChip(
                sort == TaskSort.PRIORITY,
                stringResource(Res.string.tasks_sort_priority),
            ) { sort = TaskSort.PRIORITY }
            taskTabChip(sort == TaskSort.DUE, stringResource(Res.string.tasks_sort_due)) {
                sort = TaskSort.DUE
            }
            taskTabChip(
                sort == TaskSort.UPDATED,
                stringResource(Res.string.tasks_sort_updated),
            ) { sort = TaskSort.UPDATED }
            taskTabChip(
                sort == TaskSort.TITLE,
                stringResource(Res.string.tasks_sort_title),
            ) { sort = TaskSort.TITLE }
        }
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (maxWidth > 1080.dp) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                ) {
                    Surface(
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(TactileTheme.RadiusMd),
                        color = TactileTheme.Surface,
                        border = BorderStroke(1.dp, TactileTheme.Border),
                    ) {
                        TaskTable(
                            sorted,
                            selected?.id,
                            projectById,
                            areaById,
                            onSelect = { selectedId = it },
                        )
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(TactileTheme.RadiusMd),
                        color = TactileTheme.Surface,
                        border = BorderStroke(1.dp, TactileTheme.Border),
                    ) {
                        TaskDetails(
                            selected,
                            projectById,
                            areaById,
                            onOpen,
                            onDone,
                            onArchive,
                            onRestore,
                            onDelete,
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                    color = TactileTheme.Surface,
                    border = BorderStroke(1.dp, TactileTheme.Border),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TaskTable(
                            sorted,
                            selected?.id,
                            projectById,
                            areaById,
                            onSelect = { selectedId = it },
                        )
                        HorizontalDivider(color = TactileTheme.Border)
                        TaskDetails(
                            selected,
                            projectById,
                            areaById,
                            onOpen,
                            onDone,
                            onArchive,
                            onRestore,
                            onDelete,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskTable(
    tasks: List<NodeEntity>,
    selectedId: Long?,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onSelect: (Long) -> Unit,
) {
    if (tasks.isEmpty()) {
        EmptyState(message = stringResource(Res.string.tasks_no_results))
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                stringResource(Res.string.tasks_column_task),
                style = MaterialTheme.typography.labelMedium,
                color = TactileTheme.Muted,
            )
            Text(
                stringResource(Res.string.tasks_column_context),
                style = MaterialTheme.typography.labelMedium,
                color = TactileTheme.Muted,
            )
            Text(
                stringResource(Res.string.tasks_column_due),
                style = MaterialTheme.typography.labelMedium,
                color = TactileTheme.Muted,
            )
            Text(
                stringResource(Res.string.tasks_column_status),
                style = MaterialTheme.typography.labelMedium,
                color = TactileTheme.Muted,
            )
        }
        HorizontalDivider(color = TactileTheme.Border)
        LazyColumn {
            items(tasks, key = { it.id }) { task ->
                val selected = task.id == selectedId
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(if (selected) TactileTheme.Primary.copy(alpha = 0.1f) else Color.Transparent)
                            .padding(TactileTheme.SpacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            task.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TactileTheme.Text,
                            fontWeight = FontWeight.SemiBold,
                        )
                        task.nextSmallestStep?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = TactileTheme.Muted,
                            )
                        }
                    }
                    Text(
                        listOfNotNull(
                            task.projectId?.let { projectById[it] },
                            task.areaId?.let { areaById[it] },
                        ).joinToString(" • ").ifBlank { "-" },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    Text(
                        task.dueAt?.let(::shortDate) ?: "-",
                        modifier = Modifier.weight(0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    statusPill(task.taskStateOrNull() ?: TaskState.ACTIVE)
                }
                OutlinedButton(
                    onClick = { onSelect(task.id) },
                    modifier = Modifier.padding(horizontal = TactileTheme.SpacingMd),
                ) { Text(stringResource(Res.string.tasks_open_action)) }
                HorizontalDivider(color = TactileTheme.Border)
            }
        }
    }
}

@Composable
private fun TaskDetails(
    task: NodeEntity?,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onOpen: (Long) -> Unit,
    onDone: (NodeEntity) -> Unit,
    onArchive: (NodeEntity) -> Unit,
    onRestore: (NodeEntity) -> Unit,
    onDelete: (NodeEntity) -> Unit,
) {
    if (task == null) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(Res.string.tasks_no_results), color = TactileTheme.Muted)
        }
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        Text(
            stringResource(Res.string.tasks_details_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            task.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TactileTheme.Text,
        )
        task.content.takeIf { it.isNotBlank() }?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = TactileTheme.Text,
            )
        }
        HorizontalDivider(color = TactileTheme.Border)
        detailRow(
            stringResource(Res.string.tasks_detail_status),
            (task.taskStateOrNull() ?: TaskState.ACTIVE).storageKey,
        )
        detailRow(stringResource(Res.string.tasks_detail_due), task.dueAt?.let(::shortDate) ?: "-")
        detailRow(
            stringResource(Res.string.tasks_detail_project),
            task.projectId?.let { projectById[it] } ?: "-",
        )
        detailRow(
            stringResource(Res.string.tasks_detail_area),
            task.areaId?.let { areaById[it] } ?: "-",
        )
        detailRow(stringResource(Res.string.tasks_detail_next_step), task.nextSmallestStep ?: "-")
        detailRow(
            stringResource(Res.string.tasks_detail_estimate),
            task.estimatedMinutes?.let { "${it}m" } ?: "-",
        )
        detailRow(stringResource(Res.string.tasks_detail_updated), shortDate(task.updatedAt))

        Row(
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(onClick = { onOpen(task.id) }, modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.tasks_open_action),
                )
            }
            if (task.taskStateOrNull() == TaskState.DONE) {
                OutlinedButton(onClick = { onRestore(task) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(Res.string.tasks_restore_action))
                }
            } else {
                OutlinedButton(onClick = { onDone(task) }, modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.tasks_done_action),
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(onClick = { onArchive(task) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Archive, null)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(Res.string.tasks_archive_action))
            }
            OutlinedButton(onClick = { onDelete(task) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Delete, null, tint = TactileTheme.Error)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(Res.string.tasks_delete_action), color = TactileTheme.Error)
            }
        }
    }
}

@Composable
private fun detailRow(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Muted)
        Text(value, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Text)
    }
}

@Composable
private fun TasksArchiveView(
    archivedTasks: List<NodeEntity>,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onOpen: (Long) -> Unit,
    onRestore: (NodeEntity) -> Unit,
    onDelete: (NodeEntity) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            stringResource(Res.string.tasks_archive_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(Res.string.tasks_archive_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )
        if (archivedTasks.isEmpty()) {
            EmptyState(message = stringResource(Res.string.archive_empty))
            return@Column
        }
        Surface(
            shape = RoundedCornerShape(TactileTheme.RadiusMd),
            color = TactileTheme.Surface,
            border = BorderStroke(1.dp, TactileTheme.Border),
        ) {
            Column {
                archivedTasks.forEach { task ->
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
                                    task.archivedAt?.let(::shortDate),
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
                            OutlinedButton(onClick = { onOpen(task.id) }) { Text(stringResource(Res.string.tasks_open_action)) }
                            OutlinedButton(onClick = { onRestore(task) }) { Text(stringResource(Res.string.tasks_restore_action)) }
                            IconButton(onClick = { onDelete(task) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = TactileTheme.Error,
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = TactileTheme.Border)
                }
            }
        }
    }
}

@Composable
private fun statusPill(state: TaskState) {
    val color =
        when (state)
        {
            TaskState.ACTIVE -> TactileTheme.Primary
            TaskState.DONE -> Color(0xFF2BAE66)
            TaskState.ON_HOLD -> Color(0xFFF5A623)
            TaskState.SOMEDAY -> TactileTheme.Muted
            TaskState.BLOCKED -> TactileTheme.Error
            TaskState.ARCHIVED -> TactileTheme.Muted
        }
    Box(
        modifier =
            Modifier
                .border(1.dp, color.copy(alpha = 0.55f), CircleShape)
                .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            state.storageKey.replace("_", " ").uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

private fun scoreTask(
    task: NodeEntity,
    now: Long,
    todayTaskIds: Set<Long>,
): Int {
    var score = 0
    if (task.taskStateOrNull() == TaskState.ACTIVE) score += 12
    if (task.id in todayTaskIds) score += 8
    if (task.nextSmallestStep?.isNotBlank() == true) score += 6
    task.dueAt?.let {
        val delta = it - now
        score +=
            when
                {
                    delta < 0 -> 12
                    delta <= 24L * 60 * 60 * 1000 -> 10
                    delta <= 72L * 60 * 60 * 1000 -> 7
                    else -> 2
                }
    }
    score +=
        when (task.energyLevel)
        {
            1 -> 4
            2 -> 2
            else -> 0
        }
    score +=
        when (task.friction)
        {
            "easy" -> 3
            "unclear" -> -2
            "mentally_heavy" -> -1
            else -> 0
        }
    return score
}

private fun shortDate(epochMillis: Long): String {
    val date =
        Instant
            .fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    return date.toString()
}
