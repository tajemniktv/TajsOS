/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector
import com.tajemniktv.tajsos.ui.Screen
import org.jetbrains.compose.resources.StringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.tasks_tab_all_tasks
import tajsos.composeapp.generated.resources.tasks_tab_archive
import tajsos.composeapp.generated.resources.tasks_tab_command
import tajsos.composeapp.generated.resources.tasks_tab_inbox
import tajsos.composeapp.generated.resources.tasks_tab_today

/**
 * Tasks domain navigation metadata mapping.
 */
enum class TasksTab(
    val routeSegment: String,
    val label: StringResource,
    val icon: ImageVector,
) {
    COMMAND("command", Res.string.tasks_tab_command, Icons.Default.Terminal),
    INBOX("inbox", Res.string.tasks_tab_inbox, Icons.Default.Inbox),
    TODAY("today", Res.string.tasks_tab_today, Icons.Default.Today),
    ALL("all", Res.string.tasks_tab_all_tasks, Icons.AutoMirrored.Filled.List),
    ARCHIVE("archive", Res.string.tasks_tab_archive, Icons.Default.Archive),
    ;

    fun toScreen(): Screen = Screen.Sub(Screen.Tasks, routeSegment, label, icon)

    companion object {
        fun fromRouteSegment(segment: String?): TasksTab = entries.find { it.routeSegment == segment } ?: COMMAND
    }
}
