/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.projects

@Suppress("UNUSED_PARAMETER")
fun buildProjectsDashboardPlan(surface: ProjectsDashboardSurface): ProjectsDashboardPlan =
    ProjectsDashboardPlan(
        primary =
            listOf(
                ProjectsDashboardBlock(
                    "projects_main",
                ),
            ),
    )
