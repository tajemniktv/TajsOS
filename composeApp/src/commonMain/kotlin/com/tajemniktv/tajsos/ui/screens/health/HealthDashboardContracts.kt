/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.health

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class HealthDashboardSurface { MOBILE, DESKTOP }

data class HealthDashboardBlock(
    val id: String,
)

data class HealthDashboardPlan(
    val primary: List<HealthDashboardBlock>,
    val secondary: List<HealthDashboardBlock> = emptyList(),
)

data class HealthDashboardContext(
    val viewModel: MainViewModel,
    val onEditNode: (Long) -> Unit,
)

typealias HealthDashboardBlockRenderer = @Composable (HealthDashboardContext) -> Unit
