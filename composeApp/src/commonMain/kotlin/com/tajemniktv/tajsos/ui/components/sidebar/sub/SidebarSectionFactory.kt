/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection
import com.tajemniktv.tajsos.ui.components.sidebar.SidebarItem

/**
 * Builds the standard 3-section contextual sidebar model.
 */
internal fun sidebarSections(
    primary: List<String>,
    workflow: List<String>,
    insights: List<String>,
): List<SidebarSection> =
    listOf(
        SidebarSection(title = "PRIMARY", items = primary.map(::SidebarItem)),
        SidebarSection(title = "WORKFLOW", items = workflow.map(::SidebarItem)),
        SidebarSection(title = "INSIGHTS", items = insights.map(::SidebarItem)),
    )
