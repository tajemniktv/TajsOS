/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.components.nodes.TaskRow
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 * Renders the "Today" screen showing up to three nodes scheduled for today and placeholder slots when fewer than three exist.
 *
 * Shows an empty state when there are no today nodes. For each shown node, the row supports swipe-to-complete (start-to-end) which marks the node done, toggling done status, unpinning, editing via click/long-click, and archiving through the provided callbacks; remaining slots are rendered as bordered placeholders labeled with their slot index.
 *
 * @param viewModel View model providing `todayNodes` and actions used to update node status, pinning, and archiving.
 * @param onEditNode Callback invoked with a node ID when the user requests to edit a node (click or long-click).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) TodayDashboardSurface.DESKTOP else TodayDashboardSurface.MOBILE
        val plan = remember(surface) { buildTodayDashboardPlan(surface) }
        val context =
            remember(viewModel, onEditNode) { TodayDashboardContext(viewModel, onEditNode) }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                TodayDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}
