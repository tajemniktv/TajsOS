/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.today

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold

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
        ScreenScaffold {
            Column(modifier = Modifier.fillMaxSize()) {
                plan.primary.forEach { block ->
                    TodayDashboardBlocks.resolve(block.id)?.invoke(context)
                }
            }
        }
    }
}
