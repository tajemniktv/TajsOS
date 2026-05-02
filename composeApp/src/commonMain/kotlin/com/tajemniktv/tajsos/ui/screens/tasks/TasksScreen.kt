/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Central tasks entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of tasks state.
 * @param onEditNode Node edit callback.
 * @param onNavigate Navigation callback.
 * @param currentTab Currently active tasks tab.
 * @param onTabChange Callback when tab is changed.
 */
@Composable
fun TasksRoute(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    onNavigate: (String) -> Unit,
    currentTab: TasksTab = TasksTab.COMMAND,
    onTabChange: (TasksTab) -> Unit = {},
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
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

        val surface =
            if (maxWidth > 980.dp) TasksDashboardSurface.DESKTOP else TasksDashboardSurface.MOBILE
        val plan = remember(surface, currentTab) { buildTasksDashboardPlan(surface, currentTab) }

        TasksScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless tasks screen content.
 *
 * @param context Tasks dashboard context.
 * @param plan Tasks dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun TasksScreen(
    context: TasksDashboardContext,
    plan: TasksDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = context.currentTab.toScreen(),
        onNavigate = onNavigate,
        scrollBehavior = ScreenScrollBehavior.None,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
        ) {
            plan.primary.forEach { block ->
                TasksDashboardBlocks.resolve(block.id)?.invoke(context)
            }
        }
    }
}
