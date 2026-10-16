/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.timearchitecture

/**
 * Builds a time architecture dashboard layout plan based on the active surface.
 */
@Suppress("UNUSED_PARAMETER")
fun buildTimeArchitectureDashboardPlan(surface: TimeArchitectureDashboardSurface): TimeArchitectureDashboardPlan =
    TimeArchitectureDashboardPlan(
        primary =
            listOf(
                TimeArchitectureDashboardBlock("time_header"),
                TimeArchitectureDashboardBlock("time_horizon_switcher"),
                TimeArchitectureDashboardBlock("time_map"),
                TimeArchitectureDashboardBlock("time_cadence_anchors"),
                TimeArchitectureDashboardBlock("time_project_phases"),
                TimeArchitectureDashboardBlock("time_horizon_queue"),
            ),
    )
