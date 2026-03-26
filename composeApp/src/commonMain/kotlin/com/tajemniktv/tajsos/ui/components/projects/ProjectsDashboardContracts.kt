/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.projects

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class ProjectsDashboardSurface { MOBILE, DESKTOP }

data class ProjectsDashboardBlock(
    val id: String,
)

data class ProjectsDashboardPlan(
    val primary: List<ProjectsDashboardBlock>,
    val secondary: List<ProjectsDashboardBlock> = emptyList(),
)

data class ProjectsDashboardContext(
    val viewModel: MainViewModel,
    val onNavigateTo: (String) -> Unit,
)

typealias ProjectsDashboardBlockRenderer = @Composable (ProjectsDashboardContext) -> Unit
