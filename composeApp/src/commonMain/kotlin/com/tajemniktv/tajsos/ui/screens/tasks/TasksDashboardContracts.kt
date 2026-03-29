/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.data.InboxEntryEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Defines the supported surfaces for tasks dashboard layout planning.
 */
enum class TasksDashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical tasks dashboard block.
 */
data class TasksDashboardBlock(
    val id: String,
)

/**
 * Structured layout plan for the tasks dashboard screen.
 */
data class TasksDashboardPlan(
    val primary: List<TasksDashboardBlock> = emptyList(),
)

/**
 * Shared state and actions for tasks dashboard block renderers.
 */
data class TasksDashboardContext(
    val viewModel: MainViewModel,
    val currentTab: TasksTab,
    val activeTasks: List<NodeEntity>,
    val archivedTasks: List<NodeEntity>,
    val todayTaskIds: Set<Long>,
    val inboxEntries: List<InboxEntryEntity>,
    val projectById: Map<Long, String>,
    val areaById: Map<Long, String>,
    val onEditNode: (Long) -> Unit,
    val onTabChange: (TasksTab) -> Unit,
)

/**
 * Functional interface for rendering a tasks dashboard block.
 */
typealias TasksDashboardBlockRenderer = @Composable (TasksDashboardContext) -> Unit
