/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.health

/**
 * Returns the block composition plan for the Health lens based on available surface width.
 */
@Suppress("UNUSED_PARAMETER")
fun buildHealthDashboardPlan(surface: HealthDashboardSurface): HealthDashboardPlan =
    HealthDashboardPlan(
        primary =
            listOf(
                HealthDashboardBlock(
                    "health_main",
                ),
            ),
    )
