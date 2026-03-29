/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun projectsSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("project overview", "active projects", "quick filters"),
        workflow = listOf("milestones", "blocked lanes", "coordination actions"),
        insights = listOf("velocity", "delivery risk", "project recommendations"),
    )