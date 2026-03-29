/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks.detail

/**
 * Builds a task detail layout plan based on the active surface.
 */
fun buildTaskDetailPlan(surface: TaskDetailSurface): TaskDetailPlan =
    when (surface)
    {
        TaskDetailSurface.MOBILE -> {
            TaskDetailPlan(
                primary =
                    listOf(
                        TaskDetailBlock("task_header"),
                        TaskDetailBlock("task_description"),
                        TaskDetailBlock("task_metadata"),
                        TaskDetailBlock("task_subtasks"),
                        TaskDetailBlock("task_attachments"),
                        TaskDetailBlock("task_history"),
                    ),
            )
        }

        TaskDetailSurface.DESKTOP -> {
            TaskDetailPlan(
                primary =
                    listOf(
                        TaskDetailBlock("task_header"),
                        TaskDetailBlock("task_description"),
                        TaskDetailBlock("task_subtasks"),
                        TaskDetailBlock("task_attachments"),
                        TaskDetailBlock("task_history"),
                    ),
                secondary =
                    listOf(
                        TaskDetailBlock("task_metadata"),
                    ),
            )
        }
    }
