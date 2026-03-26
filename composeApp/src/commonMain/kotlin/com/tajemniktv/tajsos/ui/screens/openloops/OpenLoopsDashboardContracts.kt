/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.openloops

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class OpenLoopsDashboardSurface { MOBILE, DESKTOP }

data class OpenLoopsDashboardBlock(
    val id: String,
)

data class OpenLoopsDashboardPlan(
    val primary: List<com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardBlock>,
    val secondary: List<com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardBlock> = emptyList(),
)

data class OpenLoopsDashboardContext(
    val viewModel: MainViewModel,
    val onEditNode: (Long) -> Unit,
)

typealias OpenLoopsDashboardBlockRenderer = @Composable (com.tajemniktv.tajsos.ui.screens.openloops.OpenLoopsDashboardContext) -> Unit
