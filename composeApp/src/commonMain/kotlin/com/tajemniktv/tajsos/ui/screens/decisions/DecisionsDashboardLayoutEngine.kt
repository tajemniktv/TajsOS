/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.decisions

fun buildDecisionsDashboardPlan(surface: DecisionsDashboardSurface): DecisionsDashboardPlan =
    when (surface)
    {
        DecisionsDashboardSurface.MOBILE -> {
            DecisionsDashboardPlan(
                primary =
                    listOf(
                        DecisionsDashboardBlock("decisions_main"),
                    ),
            )
        }

        DecisionsDashboardSurface.DESKTOP -> {
            DecisionsDashboardPlan(
                primary =
                    listOf(
                        DecisionsDashboardBlock("decisions_main"),
                    ),
            )
        }
    }
