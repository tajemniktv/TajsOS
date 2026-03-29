/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun searchSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("ALL OBJECTS", "TASKS", "PROJECTS", "NOTES", "RECORDS"),
        workflow = listOf("CONTEXT", "TIME", "RECENT QUERIES"),
        insights = listOf("SEARCH STATUS", "SEARCH INSIGHTS", "ARCHIVE"),
    )
