/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.sidebar.sub

import com.tajemniktv.tajsos.ui.components.sidebar.SidebarSection

internal fun timeArchitectureSubSidebarSections(): List<SidebarSection> =
    sidebarSections(
        primary = listOf("time model", "horizon views", "cadence controls"),
        workflow = listOf("planning layers", "window handoffs", "time constraints"),
        insights = listOf("time debt", "slack health", "planning drift"),
    )