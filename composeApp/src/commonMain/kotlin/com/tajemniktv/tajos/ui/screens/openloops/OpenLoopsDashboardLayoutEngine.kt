/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.openloops

@Suppress("UNUSED_PARAMETER")
fun buildOpenLoopsDashboardPlan(surface: OpenLoopsDashboardSurface): OpenLoopsDashboardPlan =
    OpenLoopsDashboardPlan(
        primary =
            listOf(
                OpenLoopsDashboardBlock(
                    "openloops_main",
                ),
            ),
    )
