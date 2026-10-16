/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.places

@Suppress("UNUSED_PARAMETER")
fun buildPlacesDashboardPlan(surface: PlacesDashboardSurface): PlacesDashboardPlan =
    PlacesDashboardPlan(
        primary =
            listOf(
                PlacesDashboardBlock(
                    "places_main",
                ),
            ),
    )
