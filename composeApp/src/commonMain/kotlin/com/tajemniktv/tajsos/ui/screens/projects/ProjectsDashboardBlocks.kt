/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.projects

import androidx.compose.runtime.Composable

object ProjectsDashboardBlockRegistry {
    private val renderers: Map<String, com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardBlockRenderer> =
        mapOf("projects_main" to ::renderProjectsMainBlock)

    fun resolve(id: String): com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderProjectsMainBlock(context: com.tajemniktv.tajsos.ui.screens.projects.ProjectsDashboardContext) {
    ProjectsMainBlock(viewModel = context.viewModel, onNavigateTo = context.onNavigateTo)
}
