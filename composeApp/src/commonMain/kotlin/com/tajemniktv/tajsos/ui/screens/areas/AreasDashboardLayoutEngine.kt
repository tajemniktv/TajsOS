/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.areas

fun buildAreasDashboardPlan(surface: AreasDashboardSurface): AreasDashboardPlan =
    when (surface)
    {
        AreasDashboardSurface.MOBILE -> AreasDashboardPlan(primary = listOf(AreasDashboardBlock("areas_main")))
        AreasDashboardSurface.DESKTOP -> AreasDashboardPlan(primary = listOf(AreasDashboardBlock("areas_main")))
    }
