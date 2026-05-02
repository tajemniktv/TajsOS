/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.openloops

fun buildOpenLoopsDashboardPlan(surface: OpenLoopsDashboardSurface): OpenLoopsDashboardPlan =
    when (surface) {
        OpenLoopsDashboardSurface.MOBILE -> {
            OpenLoopsDashboardPlan(
                primary =
                    listOf(
                        OpenLoopsDashboardBlock(
                            "openloops_main",
                        ),
                    ),
            )
        }

        OpenLoopsDashboardSurface.DESKTOP -> {
            OpenLoopsDashboardPlan(
                primary =
                    listOf(
                        OpenLoopsDashboardBlock(
                            "openloops_main",
                        ),
                    ),
            )
        }
    }
