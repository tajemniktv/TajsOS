/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.places

fun buildPlacesDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardPlan =
    when (surface)
    {
        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardSurface.MOBILE -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardBlock(
                            "places_main",
                        ),
                    ),
            )
        }

        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardSurface.DESKTOP -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardBlock(
                            "places_main",
                        ),
                    ),
            )
        }
    }
