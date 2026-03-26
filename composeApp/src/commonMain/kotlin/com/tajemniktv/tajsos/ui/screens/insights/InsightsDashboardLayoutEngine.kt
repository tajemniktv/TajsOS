/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.insights

fun buildInsightsDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardPlan =
    when (surface)
    {
        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardSurface.MOBILE -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardBlock(
                            "insights_main",
                        ),
                    ),
            )
        }

        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardSurface.DESKTOP -> {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardPlan(
                primary =
                    listOf(
                        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardBlock(
                            "insights_main",
                        ),
                    ),
            )
        }
    }
