/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.places

fun buildPlacesDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardPlan =
    when (surface)
    {
        com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardSurface.MOBILE -> {
            com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardBlock(
                            "places_main",
                        ),
                    ),
            )
        }

        com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardSurface.DESKTOP -> {
            com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardBlock(
                            "places_main",
                        ),
                    ),
            )
        }
    }
