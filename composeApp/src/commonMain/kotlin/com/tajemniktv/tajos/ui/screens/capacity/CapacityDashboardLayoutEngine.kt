/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.capacity

@Suppress("UNUSED_PARAMETER")
fun buildCapacityDashboardPlan(surface: CapacityDashboardSurface): CapacityDashboardPlan =
    CapacityDashboardPlan(primary = listOf(CapacityDashboardBlock("capacity_main")))
