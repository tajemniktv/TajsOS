/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tajemniktv.tajsos.data.ItemKind
import com.tajemniktv.tajsos.data.TaskState
import com.tajemniktv.tajsos.data.taskStateOrNull
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.tasks_title
import tajsos.composeapp.generated.resources.tasks_workspace_subtitle

object TasksDashboardBlocks {
    private val renderers: Map<String, TasksDashboardBlockRenderer> =
        mapOf(
            "tasks_header" to ::renderTasksHeader,
            "tasks_tabs" to ::renderTasksTabs,
            "tasks_view_command" to ::renderTasksViewCommand,
            "tasks_view_inbox" to ::renderTasksViewInbox,
            "tasks_view_today" to ::renderTasksViewToday,
            "tasks_view_all" to ::renderTasksViewAll,
            "tasks_view_archive" to ::renderTasksViewArchive,
        )

    fun resolve(id: String): TasksDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderTasksHeader(context: TasksDashboardContext) {
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        Text(stringResource(Res.string.tasks_title), style = MaterialTheme.typography.displaySmall)
        Text(
            stringResource(Res.string.tasks_workspace_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted,
        )
    }
}

@Composable
private fun renderTasksTabs(context: TasksDashboardContext) {
    Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        TasksTab.entries.forEach { tab ->
            TaskTabChip(
                selected = tab == context.currentTab,
                label = stringResource(tab.label),
                onClick = { context.onTabChange(tab) },
            )
        }
    }
}

@Composable
private fun renderTasksViewCommand(context: TasksDashboardContext) {
    val viewModel = context.viewModel
    val dashboardUIState by viewModel.dashboardUIState.collectAsState()

    /** Cache filtered incomplete tasks to avoid redundant O(N) traversals during recomposition */
    val incompleteTasks = remember(context.activeTasks) { context.activeTasks.filter { it.taskStateOrNull() != TaskState.DONE } }

    TasksCommandView(
        tasks = incompleteTasks,
        projectById = context.projectById,
        areaById = context.areaById,
        todayTaskIds = context.todayTaskIds,
        staleTasksCount = dashboardUIState.staleTasksCount,
        onSweepStaleTasks = { viewModel.sweepStaleTasks() },
        onOpen = context.onEditNode,
        onStartFocus = { viewModel.startFocusSession(it.id) },
        onDone = { viewModel.updateNodeStatus(it, TaskState.DONE.storageKey) },
        onSetTodayPayload = { node, included -> viewModel.setTodayPayload(node, included) },
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

@Composable
private fun renderTasksViewInbox(context: TasksDashboardContext) {
    val viewModel = context.viewModel
    /** Cache filtered inbox tasks to avoid redundant O(N) traversals during recomposition */
    val inboxTasks = remember(context.activeTasks) { context.activeTasks.filter { it.inboxState && it.taskStateOrNull() != TaskState.DONE } }

    TasksInboxView(
        inboxEntries = context.inboxEntries,
        inboxTasks = inboxTasks,
        projectById = context.projectById,
        areaById = context.areaById,
        onTriageTask = { viewModel.triageInboxEntry(it.id, ItemKind.TASK) },
        onDismiss = viewModel::dismissInboxEntry,
        onMarkProcessed = { viewModel.markAsProcessed(it.id) },
        onOpen = context.onEditNode,
    )
}

@Composable
private fun renderTasksViewToday(context: TasksDashboardContext) {
    val viewModel = context.viewModel
    /** Cache filtered incomplete tasks to avoid redundant O(N) traversals during recomposition */
    val incompleteTasks = remember(context.activeTasks) { context.activeTasks.filter { it.taskStateOrNull() != TaskState.DONE } }

    TasksTodayView(
        tasks = incompleteTasks,
        todayTaskIds = context.todayTaskIds,
        projectById = context.projectById,
        areaById = context.areaById,
        onOpen = context.onEditNode,
        onDone = { viewModel.updateNodeStatus(it, TaskState.DONE.storageKey) },
        onStartFocus = { viewModel.startFocusSession(it.id) },
        onSetTodayPayload = { node, included -> viewModel.setTodayPayload(node, included) },
    )
}

@Composable
private fun renderTasksViewAll(context: TasksDashboardContext) {
    val viewModel = context.viewModel
    TasksAllView(
        activeTasks = context.activeTasks,
        archivedTasks = context.archivedTasks,
        projectById = context.projectById,
        areaById = context.areaById,
        onOpen = context.onEditNode,
        onDone = { viewModel.updateNodeStatus(it, TaskState.DONE.storageKey) },
        onArchive = viewModel::archiveNode,
        onRestore = { viewModel.updateNodeStatus(it, TaskState.ACTIVE.storageKey) },
        onDelete = viewModel::deleteNodePermanently,
    )
}

@Composable
private fun renderTasksViewArchive(context: TasksDashboardContext) {
    val viewModel = context.viewModel
    TasksArchiveView(
        archivedTasks = context.archivedTasks,
        projectById = context.projectById,
        areaById = context.areaById,
        onOpen = context.onEditNode,
        onRestore = { viewModel.updateNodeStatus(it, TaskState.ACTIVE.storageKey) },
        onDelete = viewModel::deleteNodePermanently,
    )
}
