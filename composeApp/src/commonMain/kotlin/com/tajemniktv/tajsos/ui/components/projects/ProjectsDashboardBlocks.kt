/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.projects

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.screens.ProjectsMainBlock

object ProjectsDashboardBlockRegistry {
    private val renderers: Map<String, ProjectsDashboardBlockRenderer> =
        mapOf("projects_main" to ::renderProjectsMainBlock)

    fun resolve(id: String): ProjectsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderProjectsMainBlock(context: ProjectsDashboardContext) {
    ProjectsMainBlock(viewModel = context.viewModel, onNavigateTo = context.onNavigateTo)
}
