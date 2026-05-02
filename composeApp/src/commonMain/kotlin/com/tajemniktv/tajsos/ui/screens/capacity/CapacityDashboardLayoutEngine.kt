/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.capacity

fun buildCapacityDashboardPlan(surface: CapacityDashboardSurface): CapacityDashboardPlan =
    when (surface) {
        CapacityDashboardSurface.MOBILE -> {
            CapacityDashboardPlan(primary = listOf(CapacityDashboardBlock("capacity_main")))
        }

        CapacityDashboardSurface.DESKTOP -> {
            CapacityDashboardPlan(primary = listOf(CapacityDashboardBlock("capacity_main")))
        }
    }
