/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.insights

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.screens.InsightsMainBlock

object InsightsDashboardBlockRegistry {
    private val renderers: Map<String, InsightsDashboardBlockRenderer> =
        mapOf("insights_main" to ::renderInsightsMainBlock)

    fun resolve(id: String): InsightsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderInsightsMainBlock(context: InsightsDashboardContext) {
    InsightsMainBlock(
        viewModel = context.viewModel,
        onNavigateToProject = context.onNavigateToProject,
    )
}
