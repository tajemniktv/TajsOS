/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.relationships

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class RelationshipsDashboardSurface { MOBILE, DESKTOP }

data class RelationshipsDashboardBlock(
    val id: String,
)

data class RelationshipsDashboardPlan(
    val primary: List<RelationshipsDashboardBlock>,
    val secondary: List<RelationshipsDashboardBlock> = emptyList(),
)

data class RelationshipsDashboardContext(
    val viewModel: MainViewModel,
    val onEditNode: (Long) -> Unit,
)

typealias RelationshipsDashboardBlockRenderer = @Composable (RelationshipsDashboardContext) -> Unit
