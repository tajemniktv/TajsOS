/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.insights

fun buildInsightsDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardSurface,
): com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardPlan =
    when (surface)
    {
        com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardSurface.MOBILE -> {
            com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardBlock(
                            "insights_main",
                        ),
                    ),
            )
        }

        com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardSurface.DESKTOP -> {
            com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardPlan(
                primary =
                    listOf(
                        com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardBlock(
                            "insights_main",
                        ),
                    ),
            )
        }
    }
