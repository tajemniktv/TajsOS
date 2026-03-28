/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.today

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class TodayDashboardSurface { MOBILE, DESKTOP }

data class TodayDashboardBlock(
    val id: String,
)

data class TodayDashboardPlan(
    val primary: List<TodayDashboardBlock>,
    val secondary: List<TodayDashboardBlock> = emptyList(),
)

data class TodayDashboardContext(
    val viewModel: MainViewModel,
    val onEditNode: (Long) -> Unit,
)

typealias TodayDashboardBlockRenderer = @Composable (TodayDashboardContext) -> Unit
