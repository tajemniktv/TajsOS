/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.openloops

fun buildOpenLoopsDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardPlan =
    when (surface)
    {
        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardSurface.MOBILE -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardBlock(
                            "openloops_main",
                        ),
                    ),
            )
        }

        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardSurface.DESKTOP -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardBlock(
                            "openloops_main",
                        ),
                    ),
            )
        }
    }
