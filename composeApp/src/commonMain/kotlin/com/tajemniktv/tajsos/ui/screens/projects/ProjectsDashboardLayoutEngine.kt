/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.projects

fun buildProjectsDashboardPlan(surface: ProjectsDashboardSurface): ProjectsDashboardPlan =
    when (surface)
    {
        ProjectsDashboardSurface.MOBILE -> {
            ProjectsDashboardPlan(
                primary =
                    listOf(
                        ProjectsDashboardBlock(
                            "projects_main",
                        ),
                    ),
            )
        }

        ProjectsDashboardSurface.DESKTOP -> {
            ProjectsDashboardPlan(
                primary =
                    listOf(
                        ProjectsDashboardBlock(
                            "projects_main",
                        ),
                    ),
            )
        }
    }
