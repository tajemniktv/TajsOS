/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun reviewSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("review timeline", "weekly review", "open outcomes"),
        workflow = listOf("retrospective prompts", "closure actions", "plan handoff"),
        insights = listOf("outcome quality", "unfinished loops", "trend summary"),
    )
