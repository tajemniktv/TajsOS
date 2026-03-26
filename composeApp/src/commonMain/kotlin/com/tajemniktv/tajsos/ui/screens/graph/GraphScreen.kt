/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.graph

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel

@Composable
fun GraphScreen(
    viewModel: MainViewModel,
    onNodeClick: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) {
                GraphDashboardSurface.DESKTOP
            } else {
                GraphDashboardSurface.MOBILE
            }
        val plan = remember(surface) { buildGraphDashboardPlan(surface) }
        val context =
            remember(viewModel, onNodeClick) { GraphDashboardContext(viewModel, onNodeClick) }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                GraphDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}
