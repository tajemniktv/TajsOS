/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun todaySubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("today overview", "must-do tasks", "deadline radar"),
        workflow = listOf("time blocks", "sequence planner", "handoff staging"),
        insights = listOf("execution pace", "slip risks", "energy fit"),
    )
