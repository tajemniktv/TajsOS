/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.projects

fun buildProjectsDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardPlan =
    when (surface)
    {
        com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardSurface.MOBILE -> {
            com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardBlock(
                            "projects_main",
                        ),
                    ),
            )
        }

        com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardSurface.DESKTOP -> {
            com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardBlock(
                            "projects_main",
                        ),
                    ),
            )
        }
    }
