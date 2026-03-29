/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun insightsSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("insight feed", "key findings", "priority signals"),
        workflow = listOf("analysis presets", "drilldown jumps", "report actions"),
        insights = listOf("confidence", "signal freshness", "prediction notes"),
    )