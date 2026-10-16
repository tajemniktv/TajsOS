/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.insights

@Suppress("UNUSED_PARAMETER")
fun buildInsightsDashboardPlan(surface: InsightsDashboardSurface): InsightsDashboardPlan =
    InsightsDashboardPlan(
        primary =
            listOf(
                InsightsDashboardBlock(
                    "insights_main",
                ),
            ),
    )
