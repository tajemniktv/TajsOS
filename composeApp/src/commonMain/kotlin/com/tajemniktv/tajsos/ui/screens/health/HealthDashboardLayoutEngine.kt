/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.health

/**
 * Returns the block composition plan for the Health lens based on available surface width.
 */
fun buildHealthDashboardPlan(surface: HealthDashboardSurface): HealthDashboardPlan =
    when (surface)
    {
        HealthDashboardSurface.MOBILE -> HealthDashboardPlan(primary = listOf(HealthDashboardBlock("health_main")))
        HealthDashboardSurface.DESKTOP -> HealthDashboardPlan(primary = listOf(HealthDashboardBlock("health_main")))
    }
