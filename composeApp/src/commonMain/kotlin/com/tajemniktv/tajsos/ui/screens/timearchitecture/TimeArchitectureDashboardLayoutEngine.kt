/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.timearchitecture

/**
 * Builds a time architecture dashboard layout plan based on the active surface.
 */
fun buildTimeArchitectureDashboardPlan(surface: TimeArchitectureDashboardSurface): TimeArchitectureDashboardPlan =
    when (surface) {
        TimeArchitectureDashboardSurface.MOBILE -> {
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
        }

        TimeArchitectureDashboardSurface.DESKTOP -> {
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
        }
    }
