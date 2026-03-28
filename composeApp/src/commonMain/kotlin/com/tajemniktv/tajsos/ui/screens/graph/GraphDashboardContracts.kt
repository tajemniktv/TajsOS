/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.graph

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class GraphDashboardSurface { MOBILE, DESKTOP }

data class GraphDashboardBlock(
    val id: String,
)

data class GraphDashboardPlan(
    val primary: List<GraphDashboardBlock>,
    val secondary: List<GraphDashboardBlock> = emptyList(),
)

data class GraphDashboardContext(
    val viewModel: MainViewModel,
    val onNodeClick: (Long) -> Unit,
)

typealias GraphDashboardBlockRenderer = @Composable (GraphDashboardContext) -> Unit
