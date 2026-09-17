/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.areas

import androidx.compose.runtime.Composable
import com.tajemniktv.tajos.ui.MainViewModel

enum class AreasDashboardSurface { MOBILE, DESKTOP }

data class AreasDashboardBlock(
    val id: String,
)

data class AreasDashboardPlan(
    val primary: List<AreasDashboardBlock>,
    val secondary: List<AreasDashboardBlock> = emptyList(),
)

data class AreasDashboardContext(
    val viewModel: MainViewModel,
    val onNavigateTo: (String) -> Unit,
)

typealias AreasDashboardBlockRenderer = @Composable (AreasDashboardContext) -> Unit
