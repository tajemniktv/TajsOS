/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarItem
import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection
import com.tajemniktv.tajsos.ui.screens.tasks.TasksTab

internal fun tasksSubSidebarSections(): List<SidebarSection> =
    listOf(
        SidebarSection(
            title = "NAVIGATE",
            items =
                listOf(
                    SidebarItem("Command", TasksTab.COMMAND.toScreen()),
                    SidebarItem("Inbox", TasksTab.INBOX.toScreen()),
                    SidebarItem("Today", TasksTab.TODAY.toScreen()),
                    SidebarItem("All Tasks", TasksTab.ALL.toScreen()),
                    SidebarItem("Archive", TasksTab.ARCHIVE.toScreen()),
                ),
        ),
        SidebarSection(
            title = "PRIMARY",
            items =
                listOf(
                    SidebarItem("task board"),
                    SidebarItem("quick filters"),
                    SidebarItem("due soon"),
                ),
        ),
        SidebarSection(
            title = "WORKFLOW",
            items =
                listOf(
                    SidebarItem("bulk status actions"),
                    SidebarItem("assignment lanes"),
                    SidebarItem("dependency chains"),
                ),
        ),
        SidebarSection(
            title = "INSIGHTS",
            items =
                listOf(
                    SidebarItem("throughput"),
                    SidebarItem("blocked tasks"),
                    SidebarItem("completion pattern"),
                ),
        ),
    )
