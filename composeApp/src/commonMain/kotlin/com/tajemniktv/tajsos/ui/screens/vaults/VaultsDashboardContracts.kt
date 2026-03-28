/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.vaults

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class VaultsDashboardSurface { MOBILE, DESKTOP }

data class VaultsDashboardBlock(
    val id: String,
)

data class VaultsDashboardPlan(
    val primary: List<VaultsDashboardBlock>,
    val secondary: List<VaultsDashboardBlock> = emptyList(),
)

data class VaultsDashboardContext(
    val viewModel: MainViewModel,
    val onEditNode: (Long) -> Unit,
)

typealias VaultsDashboardBlockRenderer = @Composable (VaultsDashboardContext) -> Unit
