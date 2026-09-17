/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.protocols

import androidx.compose.runtime.Composable
import com.tajemniktv.tajos.ui.MainViewModel

enum class ProtocolsDashboardSurface { MOBILE, DESKTOP }

data class ProtocolsDashboardBlock(
    val id: String,
)

data class ProtocolsDashboardPlan(
    val primary: List<ProtocolsDashboardBlock>,
    val secondary: List<ProtocolsDashboardBlock> = emptyList(),
)

data class ProtocolsDashboardContext(
    val viewModel: MainViewModel,
    val onEditNode: (Long) -> Unit,
)

typealias ProtocolsDashboardBlockRenderer = @Composable (ProtocolsDashboardContext) -> Unit
