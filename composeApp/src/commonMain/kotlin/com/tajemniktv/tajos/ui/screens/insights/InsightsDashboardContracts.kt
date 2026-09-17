/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.insights

import androidx.compose.runtime.Composable
import com.tajemniktv.tajos.ui.MainViewModel

enum class InsightsDashboardSurface { MOBILE, DESKTOP }

data class InsightsDashboardBlock(
    val id: String,
)

data class InsightsDashboardPlan(
    val primary: List<InsightsDashboardBlock>,
    val secondary: List<InsightsDashboardBlock> = emptyList(),
)

data class InsightsDashboardContext(
    val viewModel: MainViewModel,
    val onNavigateToProject: (Long) -> Unit,
)

typealias InsightsDashboardBlockRenderer = @Composable (InsightsDashboardContext) -> Unit
