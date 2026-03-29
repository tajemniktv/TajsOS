/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun dashboardSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("system overview", "quick launch", "daily priorities"),
        workflow = listOf("capture queue", "active lanes", "startup routines"),
        insights = listOf("health snapshot", "trend alerts", "recommended focus"),
    )