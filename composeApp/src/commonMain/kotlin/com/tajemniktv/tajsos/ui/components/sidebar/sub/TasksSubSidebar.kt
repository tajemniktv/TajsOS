/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun tasksSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("task board", "quick filters", "due soon"),
        workflow = listOf("bulk status actions", "assignment lanes", "dependency chains"),
        insights = listOf("throughput", "blocked tasks", "completion pattern"),
    )