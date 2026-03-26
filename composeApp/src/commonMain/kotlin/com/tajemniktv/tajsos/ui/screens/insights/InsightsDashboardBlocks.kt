/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.insights

import androidx.compose.runtime.Composable

object InsightsDashboardBlockRegistry {
    private val renderers: Map<String, com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardBlockRenderer> =
        mapOf("insights_main" to ::renderInsightsMainBlock)

    fun resolve(id: String): com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderInsightsMainBlock(context: com.tajemniktv.tajsos.ui.screens.insights.InsightsDashboardContext) {
    InsightsMainBlock(
        viewModel = context.viewModel,
        onNavigateToProject = context.onNavigateToProject,
    )
}
