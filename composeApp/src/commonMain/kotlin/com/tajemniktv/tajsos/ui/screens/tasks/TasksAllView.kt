/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.tasks_all_title
import tajsos.composeapp.generated.resources.tasks_archive_action
import tajsos.composeapp.generated.resources.tasks_column_context
import tajsos.composeapp.generated.resources.tasks_column_due
import tajsos.composeapp.generated.resources.tasks_column_status
import tajsos.composeapp.generated.resources.tasks_column_task
import tajsos.composeapp.generated.resources.tasks_delete_action
import tajsos.composeapp.generated.resources.tasks_detail_area
import tajsos.composeapp.generated.resources.tasks_detail_due
import tajsos.composeapp.generated.resources.tasks_detail_estimate
import tajsos.composeapp.generated.resources.tasks_detail_next_step
import tajsos.composeapp.generated.resources.tasks_detail_project
import tajsos.composeapp.generated.resources.tasks_detail_status
import tajsos.composeapp.generated.resources.tasks_detail_updated
import tajsos.composeapp.generated.resources.tasks_details_title
import tajsos.composeapp.generated.resources.tasks_done_action
import tajsos.composeapp.generated.resources.tasks_no_results
import tajsos.composeapp.generated.resources.tasks_open_action
import tajsos.composeapp.generated.resources.tasks_restore_action
import tajsos.composeapp.generated.resources.tasks_scope_active
import tajsos.composeapp.generated.resources.tasks_scope_archived
import tajsos.composeapp.generated.resources.tasks_scope_completed
import tajsos.composeapp.generated.resources.tasks_search_placeholder
import tajsos.composeapp.generated.resources.tasks_sort_due
import tajsos.composeapp.generated.resources.tasks_sort_priority
import tajsos.composeapp.generated.resources.tasks_sort_title
import tajsos.composeapp.generated.resources.tasks_sort_updated
import kotlin.time.Clock

private enum class TaskScope { ACTIVE, COMPLETED, ARCHIVED }

private enum class TaskSort { PRIORITY, DUE, UPDATED, TITLE }

@Composable
internal fun TasksAllView(
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
        remember(scope, activeTasks, archivedTasks) {
            when (scope)
            {
                TaskScope.ACTIVE -> activeTasks.filter { it.taskStateOrNull() != TaskState.DONE }
                TaskScope.COMPLETED -> activeTasks.filter { it.taskStateOrNull() == TaskState.DONE }
                TaskScope.ARCHIVED -> archivedTasks
            }
        }
    val filtered =
        remember(base, query) {
            base.filter {
                query.isBlank() ||
                    it.title.contains(
                        query,
                        true,
                    ) || it.content.contains(query, true)
            }
        }
    val sorted =
        remember(filtered, sort) {
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
        }
    val selected = sorted.find { it.id == selectedId } ?: sorted.firstOrNull()
    if (selected != null && selectedId == null) selectedId = selected.id

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)
    ) {
        Text(
            stringResource(Res.string.tasks_all_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
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
            TaskTabChip(
                scope == TaskScope.ACTIVE,
                stringResource(Res.string.tasks_scope_active),
            ) { scope = TaskScope.ACTIVE }
            TaskTabChip(
                scope == TaskScope.COMPLETED,
                stringResource(Res.string.tasks_scope_completed),
            ) { scope = TaskScope.COMPLETED }
            TaskTabChip(
                scope == TaskScope.ARCHIVED,
                stringResource(Res.string.tasks_scope_archived),
            ) { scope = TaskScope.ARCHIVED }
            TaskTabChip(
                sort == TaskSort.PRIORITY,
                stringResource(Res.string.tasks_sort_priority),
            ) { sort = TaskSort.PRIORITY }
            TaskTabChip(sort == TaskSort.DUE, stringResource(Res.string.tasks_sort_due)) {
                sort = TaskSort.DUE
            }
            TaskTabChip(
                sort == TaskSort.UPDATED,
                stringResource(Res.string.tasks_sort_updated),
            ) { sort = TaskSort.UPDATED }
            TaskTabChip(
                sort == TaskSort.TITLE,
                stringResource(Res.string.tasks_sort_title),
            ) { sort = TaskSort.TITLE }
        }
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (maxWidth > 1080.dp) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)
                ) {
                    Surface(
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                        color = TajsOSTheme.Surface,
                        border = BorderStroke(1.dp, TajsOSTheme.Border)
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
                        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                        color = TajsOSTheme.Surface,
                        border = BorderStroke(1.dp, TajsOSTheme.Border)
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
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                    color = TajsOSTheme.Surface,
                    border = BorderStroke(1.dp, TajsOSTheme.Border)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TaskTable(
                            sorted,
                            selected?.id,
                            projectById,
                            areaById,
                            onSelect = { selectedId = it },
                        )
                        HorizontalDivider(color = TajsOSTheme.Border)
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
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                stringResource(Res.string.tasks_column_task),
                style = MaterialTheme.typography.labelMedium,
                color = TajsOSTheme.Muted
            )
            Text(
                stringResource(Res.string.tasks_column_context),
                style = MaterialTheme.typography.labelMedium,
                color = TajsOSTheme.Muted
            )
            Text(
                stringResource(Res.string.tasks_column_due),
                style = MaterialTheme.typography.labelMedium,
                color = TajsOSTheme.Muted
            )
            Text(
                stringResource(Res.string.tasks_column_status),
                style = MaterialTheme.typography.labelMedium,
                color = TajsOSTheme.Muted
            )
        }
        HorizontalDivider(color = TajsOSTheme.Border)
        LazyColumn {
            items(tasks, key = { it.id }) { task ->
                val selected = task.id == selectedId
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                if (selected) {
                                    TajsOSTheme.Primary.copy(
                                        alpha = 0.1f
                                    )
                                } else Color.Transparent
                            )
                            .padding(TajsOSTheme.SpacingMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            task.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TajsOSTheme.Text,
                            fontWeight = FontWeight.SemiBold,
                        )
                        task.nextSmallestStep?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = TajsOSTheme.Muted,
                            )
                        }
                    }
                    Text(
                        listOfNotNull(
                            task.projectId?.let { projectById[it] },
                            task.areaId?.let { areaById[it] }
                        ).joinToString(" • ").ifBlank { "-" },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                    )
                    Text(
                        task.dueAt?.let(::shortDate) ?: "-",
                        modifier = Modifier.weight(0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted
                    )
                    StatusPill(task.taskStateOrNull() ?: TaskState.ACTIVE)
                }
                OutlinedButton(
                    onClick = { onSelect(task.id) },
                    modifier = Modifier.padding(horizontal = TajsOSTheme.SpacingMd),
                ) { Text(stringResource(Res.string.tasks_open_action)) }
                HorizontalDivider(color = TajsOSTheme.Border)
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
    onDelete: (NodeEntity) -> Unit
) {
    if (task == null) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(Res.string.tasks_no_results), color = TajsOSTheme.Muted)
        }
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
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
            color = TajsOSTheme.Text
        )
        task.content.takeIf { it.isNotBlank() }?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = TajsOSTheme.Text,
            )
        }
        HorizontalDivider(color = TajsOSTheme.Border)
        DetailRow(
            stringResource(Res.string.tasks_detail_status),
            (task.taskStateOrNull() ?: TaskState.ACTIVE).storageKey,
        )
        DetailRow(stringResource(Res.string.tasks_detail_due), task.dueAt?.let(::shortDate) ?: "-")
        DetailRow(
            stringResource(Res.string.tasks_detail_project),
            task.projectId?.let { projectById[it] } ?: "-",
        )
        DetailRow(
            stringResource(Res.string.tasks_detail_area),
            task.areaId?.let { areaById[it] } ?: "-",
        )
        DetailRow(stringResource(Res.string.tasks_detail_next_step), task.nextSmallestStep ?: "-")
        DetailRow(
            stringResource(Res.string.tasks_detail_estimate),
            task.estimatedMinutes?.let { "${it}m" } ?: "-"
        )
        DetailRow(stringResource(Res.string.tasks_detail_updated), shortDate(task.updatedAt))

        Row(
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
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
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedButton(onClick = { onArchive(task) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Archive, null)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(Res.string.tasks_archive_action))
            }
            OutlinedButton(onClick = { onDelete(task) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Delete, null, tint = TajsOSTheme.Error)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(Res.string.tasks_delete_action), color = TajsOSTheme.Error)
            }
        }
    }
}
