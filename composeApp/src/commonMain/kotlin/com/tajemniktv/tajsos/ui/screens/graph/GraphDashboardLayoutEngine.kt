/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.graph

fun buildGraphDashboardPlan(surface: GraphDashboardSurface): GraphDashboardPlan =
    when (surface) {
        GraphDashboardSurface.MOBILE -> GraphDashboardPlan(primary = listOf(GraphDashboardBlock("graph_main")))
        GraphDashboardSurface.DESKTOP -> GraphDashboardPlan(primary = listOf(GraphDashboardBlock("graph_main")))
    }
