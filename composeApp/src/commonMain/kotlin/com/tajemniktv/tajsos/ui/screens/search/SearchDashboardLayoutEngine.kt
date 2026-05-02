/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.search

/**
 * Builds a search dashboard layout plan based on the active surface.
 */
fun buildSearchDashboardPlan(surface: SearchDashboardSurface): SearchDashboardPlan =
    when (surface) {
        SearchDashboardSurface.MOBILE -> {
            SearchDashboardPlan(
                primary =
                    listOf(
                        SearchDashboardBlock("search_input"),
                        SearchDashboardBlock("search_recent"),
                        SearchDashboardBlock("search_filters"),
                        SearchDashboardBlock("search_results_header"),
                        SearchDashboardBlock("search_results_list"),
                        SearchDashboardBlock("search_support"),
                    ),
            )
        }

        SearchDashboardSurface.DESKTOP -> {
            SearchDashboardPlan(
                primary =
                    listOf(
                        SearchDashboardBlock("search_input"),
                        SearchDashboardBlock("search_recent"),
                        SearchDashboardBlock("search_filters"),
                        SearchDashboardBlock("search_results_header"),
                        SearchDashboardBlock("search_results_list"),
                    ),
                secondary =
                    listOf(
                        SearchDashboardBlock("search_support"),
                    ),
            )
        }
    }
