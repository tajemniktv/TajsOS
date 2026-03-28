/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.focus

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class FocusDashboardSurface { MOBILE, DESKTOP }

data class FocusDashboardBlock(
    val id: String,
)

data class FocusDashboardPlan(
    val primary: List<FocusDashboardBlock>,
    val secondary: List<FocusDashboardBlock> = emptyList(),
)

data class FocusDashboardContext(
    val viewModel: MainViewModel,
)

typealias FocusDashboardBlockRenderer = @Composable (FocusDashboardContext) -> Unit
