/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.places

fun buildPlacesDashboardPlan(surface: PlacesDashboardSurface): PlacesDashboardPlan =
    when (surface)
    {
        PlacesDashboardSurface.MOBILE -> {
            PlacesDashboardPlan(
                primary =
                    listOf(
                        PlacesDashboardBlock(
                            "places_main",
                        ),
                    ),
            )
        }

        PlacesDashboardSurface.DESKTOP -> {
            PlacesDashboardPlan(
                primary =
                    listOf(
                        PlacesDashboardBlock(
                            "places_main",
                        ),
                    ),
            )
        }
    }
