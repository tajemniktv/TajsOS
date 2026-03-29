/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.places

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class PlacesDashboardSurface { MOBILE, DESKTOP }

data class PlacesDashboardBlock(
    val id: String,
)

data class PlacesDashboardPlan(
    val primary: List<PlacesDashboardBlock>,
    val secondary: List<PlacesDashboardBlock> = emptyList(),
)

data class PlacesDashboardContext(
    val viewModel: MainViewModel,
    val onEditNode: (Long) -> Unit,
)

typealias PlacesDashboardBlockRenderer = @Composable (PlacesDashboardContext) -> Unit
