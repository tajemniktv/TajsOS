/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun archiveSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("archive index", "recently archived", "restore filters"),
        workflow = listOf("cleanup actions", "retention windows", "restore flow"),
        insights = listOf("archive growth", "recovery rate", "cold-data value"),
    )