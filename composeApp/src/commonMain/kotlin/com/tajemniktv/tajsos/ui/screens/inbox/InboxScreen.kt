/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.inbox

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Displays the inbox UI with a quick-capture input, type filters, recent entries, and per-item actions.
 *
 * Delegates node operations (add, update status, pin/unpin, archive, mark processed) to the provided ViewModel.
 *
 * @param onEditNode Callback invoked with a node ID when the user requests to edit that node.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth >
                900.dp
            ) {
                InboxDashboardSurface.DESKTOP
            } else {
                InboxDashboardSurface.MOBILE
            }
        val plan =
            remember(surface) {
                buildInboxDashboardPlan(
                    surface,
                )
            }
        val context =
            remember(viewModel, onEditNode) {
                InboxDashboardContext(
                    viewModel,
                    onEditNode,
                )
            }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                InboxDashboardBlockRegistry
                    .resolve(block.id)
                    ?.invoke(context)
            }
        }
    }
}
