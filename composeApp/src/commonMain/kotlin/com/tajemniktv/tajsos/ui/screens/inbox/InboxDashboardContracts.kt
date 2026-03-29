/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.inbox

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class InboxDashboardSurface { MOBILE, DESKTOP }

data class InboxDashboardBlock(
    val id: String,
)

data class InboxDashboardPlan(
    val primary: List<InboxDashboardBlock>,
    val secondary: List<InboxDashboardBlock> = emptyList(),
)

data class InboxDashboardContext(
    val viewModel: MainViewModel,
    val onEditNode: (Long) -> Unit,
)

typealias InboxDashboardBlockRenderer = @Composable (InboxDashboardContext) -> Unit
