/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun trackSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("tracking panel", "active metrics", "period filters"),
        workflow = listOf("log shortcuts", "metric upkeep", "tracking routines"),
        insights = listOf("trend shifts", "anomalies", "signal quality"),
    )