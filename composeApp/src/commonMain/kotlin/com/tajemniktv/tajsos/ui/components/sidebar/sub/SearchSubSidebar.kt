/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun searchSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("saved searches", "recent queries", "high-signal filters"),
        workflow = listOf("scope presets", "operator cheatsheet", "result pivots"),
        insights = listOf("query quality", "missed entities", "index coverage"),
    )