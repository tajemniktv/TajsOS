/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.openloops

import androidx.compose.runtime.Composable

object OpenLoopsDashboardBlockRegistry {
    private val renderers: Map<String, com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardBlockRenderer> =
        mapOf("openloops_main" to ::renderOpenLoopsMainBlock)

    fun resolve(id: String): com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderOpenLoopsMainBlock(context: com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardContext) {
    OpenLoopsMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}
