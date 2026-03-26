/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.archive

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class ArchiveDashboardSurface { MOBILE, DESKTOP }

data class ArchiveDashboardBlock(
    val id: String,
)

data class ArchiveDashboardPlan(
    val primary: List<ArchiveDashboardBlock>,
    val secondary: List<ArchiveDashboardBlock> = emptyList(),
)

data class ArchiveDashboardContext(
    val viewModel: MainViewModel,
    val onEditNode: (Long) -> Unit,
)

typealias ArchiveDashboardBlockRenderer = @Composable (ArchiveDashboardContext) -> Unit
