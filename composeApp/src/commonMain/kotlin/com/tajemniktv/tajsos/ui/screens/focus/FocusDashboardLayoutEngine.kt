/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.focus

fun buildFocusDashboardPlan(surface: FocusDashboardSurface): FocusDashboardPlan =
    when (surface)
    {
        FocusDashboardSurface.MOBILE -> FocusDashboardPlan(primary = listOf(FocusDashboardBlock("focus_main")))
        FocusDashboardSurface.DESKTOP -> FocusDashboardPlan(primary = listOf(FocusDashboardBlock("focus_main")))
    }
