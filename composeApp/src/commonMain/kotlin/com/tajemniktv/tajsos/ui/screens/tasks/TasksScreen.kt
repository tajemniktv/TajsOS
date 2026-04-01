/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

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

    val context =
        TasksDashboardContext(
            viewModel = viewModel,
            currentTab = currentTab,
            activeTasks = activeTasks,
            archivedTasks = archivedTasks,
            todayTaskIds = todayTaskIds,
            inboxEntries = inboxEntries,
            projectById = projectById,
            areaById = areaById,
            onEditNode = onEditNode,
            onTabChange = onTabChange,
        )

    val surface = TasksDashboardSurface.MOBILE // Default for now
    val plan = remember(surface, currentTab) { buildTasksDashboardPlan(surface, currentTab) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(TajsOSTheme.SpacingMd)
                .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        plan.primary.forEach { block ->
            TasksDashboardBlockRegistry.resolve(block.id)?.invoke(context)
        }
    }
}
