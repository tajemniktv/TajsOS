/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.data.ItemKind
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.tasks_title
import tajsos.composeapp.generated.resources.tasks_workspace_subtitle

@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    currentTab: TasksTab = TasksTab.COMMAND,
    onTabChange: (TasksTab) -> Unit = {},
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
            TasksTab.entries.forEach { tab ->
                TaskTabChip(
                    selected = tab == currentTab,
                    label = stringResource(tab.label),
                    onClick = { onTabChange(tab) },
                )
            }
        }

        when (currentTab)
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
