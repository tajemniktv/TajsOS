/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

/**
 * Builds a tasks dashboard layout plan based on the active surface and tab.
 */
fun buildTasksDashboardPlan(
    surface: TasksDashboardSurface,
    tab: TasksTab,
): TasksDashboardPlan {
    val primary = mutableListOf<TasksDashboardBlock>()

    primary.add(TasksDashboardBlock("tasks_header"))
    primary.add(TasksDashboardBlock("tasks_tabs"))

    when (tab) {
        TasksTab.COMMAND -> primary.add(TasksDashboardBlock("tasks_view_command"))
        TasksTab.INBOX -> primary.add(TasksDashboardBlock("tasks_view_inbox"))
        TasksTab.TODAY -> primary.add(TasksDashboardBlock("tasks_view_today"))
        TasksTab.ALL -> primary.add(TasksDashboardBlock("tasks_view_all"))
        TasksTab.ARCHIVE -> primary.add(TasksDashboardBlock("tasks_view_archive"))
    }

    return TasksDashboardPlan(primary = primary)
}
