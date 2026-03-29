/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.rules

/**
 * Builds a rules dashboard layout plan based on the active surface.
 */
fun buildRulesDashboardPlan(surface: RulesDashboardSurface): RulesDashboardPlan =
    when (surface)
    {
        RulesDashboardSurface.MOBILE -> {
            RulesDashboardPlan(
                primary =
                    listOf(
                        RulesDashboardBlock("rules_header"),
                        RulesDashboardBlock("rules_stats"),
                        RulesDashboardBlock("rules_input"),
                        RulesDashboardBlock("rules_grouped_sections"),
                        RulesDashboardBlock("rules_list"),
                    ),
            )
        }

        RulesDashboardSurface.DESKTOP -> {
            RulesDashboardPlan(
                primary =
                    listOf(
                        RulesDashboardBlock("rules_header"),
                        RulesDashboardBlock("rules_stats"),
                        RulesDashboardBlock("rules_input"),
                        RulesDashboardBlock("rules_grouped_sections"),
                        RulesDashboardBlock("rules_list"),
                    ),
            )
        }
    }
