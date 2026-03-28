/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.NodeCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Clock

/**
 * Displays the Tasks screen: a header with a view-mode toggle, optional resurrection suggestions,
 * filter chips (status/project/area), and tasks presented either as a vertical list or a kanban-like board.
 *
 * The displayed tasks are derived from the viewModel's active nodes and are filtered by the selected
 * status, project, and area. If any active tasks have not been updated for 14 days, up to two are shown
 * as "resurrection suggestions" that open the editor when tapped.
 *
 * @param viewModel Provides the active nodes, projects, areas, and mutation functions used by the screen
 *                  (e.g., update status, toggle pin, archive).
 * @param onEditNode Callback invoked with a node id when the user requests to edit a task.
 */
@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val activeNodes by viewModel.activeNodes.collectAsState()
    val tasks = activeNodes.filter { it.node.isTaskItem() }
    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()

    var viewMode by remember { mutableStateOf("list") } // list, board
    var filterStatus by remember { mutableStateOf<TaskState?>(null) }
    var filterProject by remember { mutableStateOf<Long?>(null) }
    var filterArea by remember { mutableStateOf<Long?>(null) }

    val filteredTasks =
        tasks.filter {
            (filterStatus == null || it.node.taskStateOrNull() == filterStatus) &&
                (filterProject == null || it.node.projectId == filterProject) &&
                (filterArea == null || it.node.areaId == filterArea)
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TactileTheme.SpacingMd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.tasks_title),
                style = MaterialTheme.typography.displaySmall,
            )
            Row {
                IconButton(onClick = { viewMode = if (viewMode == "list") "board" else "list" }) {
                    Icon(
                        if (viewMode == "list") Icons.Default.ViewKanban else Icons.AutoMirrored.Filled.ViewList,
                        contentDescription = "Switch View",
                    )
                }
            }
        }

        // Resurrection / Suggestions
        val staleTime = Clock.System.now().toEpochMilliseconds() - (14 * 24 * 60 * 60 * 1000L)
        val resurrectionTasks =
            tasks.filter { it.node.taskStateOrNull() == TaskState.ACTIVE && it.node.updatedAt < staleTime }.take(2)

        if (resurrectionTasks.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = TactileTheme.SpacingSm),
                color = TactileTheme.Primary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border =
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        TactileTheme.Primary.copy(alpha = 0.2f),
                    ),
            ) {
                Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TactileTheme.Primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "RESURRECTION SUGGESTIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Primary,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    resurrectionTasks.forEach { task ->
                        TextButton(
                            onClick = { onEditNode(task.node.id) },
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text(
                                "• ${task.node.title}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TactileTheme.Text,
                            )
                        }
                    }
                }
            }
        }

        // Filters
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = TactileTheme.SpacingSm),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        ) {
            item {
                FilterChip(
                    selected = filterStatus == null && filterProject == null && filterArea == null,
                    onClick = {
                        filterStatus = null
                        filterProject = null
                        filterArea = null
                    },
                    label = { Text("ALL") },
                )
            }
            item {
                val statuses = listOf(TaskState.ACTIVE, TaskState.ON_HOLD, TaskState.SOMEDAY, TaskState.BLOCKED)
                statuses.forEach { status ->
                    FilterChip(
                        selected = filterStatus == status,
                        onClick = { filterStatus = if (filterStatus == status) null else status },
                        label = { Text(status.storageKey.uppercase().replace("_", " ")) },
                        modifier = Modifier.padding(end = 4.dp),
                    )
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            EmptyState(message = stringResource(Res.string.tasks_empty))
        } else if (viewMode == "list") {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(filteredTasks, key = { it.node.id }) { nodeWithPin ->
                    NodeCard(
                        nodeWithPin = nodeWithPin,
                        onToggleDone = { status ->
                            viewModel.updateNodeStatus(
                                nodeWithPin.node,
                                status,
                            )
                        },
                        onTogglePin = { isPinned ->
                            viewModel.togglePin(
                                nodeWithPin.node,
                                isPinned,
                            )
                        },
                        onClick = { onEditNode(nodeWithPin.node.id) },
                        onLongClick = { onEditNode(nodeWithPin.node.id) },
                        onArchive = { viewModel.archiveNode(nodeWithPin.node) },
                    )
                }
            }
        } else {
            // Board View
            val statuses = listOf(TaskState.ACTIVE, TaskState.ON_HOLD, TaskState.SOMEDAY, TaskState.BLOCKED)
            Row(
                modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
            ) {
                statuses.forEach { status ->
                    val columnTasks = filteredTasks.filter { it.node.taskStateOrNull() == status }
                    Column(
                        modifier = Modifier.width(280.dp).fillMaxHeight(),
                    ) {
                        Surface(
                            color = TactileTheme.Surface,
                            shape = RoundedCornerShape(TactileTheme.RadiusMd),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = TactileTheme.SpacingSm),
                        ) {
                            Text(
                                text = status.storageKey.uppercase().replace("_", " "),
                                modifier = Modifier.padding(TactileTheme.SpacingMd),
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Primary,
                            )
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                            items(columnTasks, key = { it.node.id }) { nodeWithPin ->
                                NodeCard(
                                    nodeWithPin = nodeWithPin,
                                    onToggleDone = { s ->
                                        viewModel.updateNodeStatus(
                                            nodeWithPin.node,
                                            s,
                                        )
                                    },
                                    onTogglePin = { isPinned ->
                                        viewModel.togglePin(
                                            nodeWithPin.node,
                                            isPinned,
                                        )
                                    },
                                    onClick = { onEditNode(nodeWithPin.node.id) },
                                    onLongClick = { onEditNode(nodeWithPin.node.id) },
                                    onArchive = { viewModel.archiveNode(nodeWithPin.node) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
