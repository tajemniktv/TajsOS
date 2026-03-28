/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.capacity

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class CapacityDashboardSurface { MOBILE, DESKTOP }

data class CapacityDashboardBlock(
    val id: String,
)

data class CapacityDashboardPlan(
    val primary: List<CapacityDashboardBlock>,
    val secondary: List<CapacityDashboardBlock> = emptyList(),
)

data class CapacityDashboardContext(
    val viewModel: MainViewModel,
)

typealias CapacityDashboardBlockRenderer = @Composable (CapacityDashboardContext) -> Unit
