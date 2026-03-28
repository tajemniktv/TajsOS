/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.openloops

fun buildOpenLoopsDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardPlan =
    when (surface)
    {
        com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardSurface.MOBILE -> {
            com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardBlock(
                            "openloops_main",
                        ),
                    ),
            )
        }

        com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardSurface.DESKTOP -> {
            com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardBlock(
                            "openloops_main",
                        ),
                    ),
            )
        }
    }
