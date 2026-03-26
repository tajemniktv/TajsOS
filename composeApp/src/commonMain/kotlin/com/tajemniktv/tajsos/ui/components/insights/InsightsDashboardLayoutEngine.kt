/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.insights

fun buildInsightsDashboardPlan(surface: InsightsDashboardSurface): InsightsDashboardPlan =
    when (surface)
    {
        InsightsDashboardSurface.MOBILE -> {
            InsightsDashboardPlan(
                primary =
                    listOf(
                        InsightsDashboardBlock("insights_main"),
                    ),
            )
        }

        InsightsDashboardSurface.DESKTOP -> {
            InsightsDashboardPlan(
                primary =
                    listOf(
                        InsightsDashboardBlock("insights_main"),
                    ),
            )
        }
    }
