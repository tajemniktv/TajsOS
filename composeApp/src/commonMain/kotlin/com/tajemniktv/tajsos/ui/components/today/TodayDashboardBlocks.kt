/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.today

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.screens.TodayMainBlock

object TodayDashboardBlockRegistry {
    private val renderers: Map<String, TodayDashboardBlockRenderer> =
        mapOf("today_main" to ::renderTodayMainBlock)

    fun resolve(id: String): TodayDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderTodayMainBlock(context: TodayDashboardContext) {
    TodayMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}
