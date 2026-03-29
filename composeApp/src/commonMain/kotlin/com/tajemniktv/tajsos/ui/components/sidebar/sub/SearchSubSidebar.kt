/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun searchSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("all objects", "tasks", "projects", "notes", "records"),
        workflow = listOf("context scope", "time scope", "recent queries"),
        insights = listOf("search status", "relevance mapping", "archive"),
    )
