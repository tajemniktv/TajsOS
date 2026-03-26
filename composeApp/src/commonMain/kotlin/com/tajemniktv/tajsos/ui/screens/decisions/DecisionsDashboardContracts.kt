/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.decisions

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class DecisionsDashboardSurface { MOBILE, DESKTOP }

data class DecisionsDashboardBlock(
    val id: String,
)

data class DecisionsDashboardPlan(
    val primary: List<DecisionsDashboardBlock>,
    val secondary: List<DecisionsDashboardBlock> = emptyList(),
)

data class DecisionsDashboardContext(
    val viewModel: MainViewModel,
    val onEditNode: (Long) -> Unit,
)

typealias DecisionsDashboardBlockRenderer = @Composable (DecisionsDashboardContext) -> Unit
