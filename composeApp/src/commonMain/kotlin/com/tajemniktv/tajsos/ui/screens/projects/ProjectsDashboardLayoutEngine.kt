/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.projects

fun buildProjectsDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardPlan =
    when (surface)
    {
        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardSurface.MOBILE -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardBlock(
                            "projects_main",
                        ),
                    ),
            )
        }

        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardSurface.DESKTOP -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardBlock(
                            "projects_main",
                        ),
                    ),
            )
        }
    }
