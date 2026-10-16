/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.decisions

@Suppress("UNUSED_PARAMETER")
fun buildDecisionsDashboardPlan(surface: DecisionsDashboardSurface): DecisionsDashboardPlan =
    DecisionsDashboardPlan(
        primary =
            listOf(
                DecisionsDashboardBlock("decisions_main"),
            ),
    )
