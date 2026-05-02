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
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold

/**
 * Central graph entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of graph state.
 * @param onNodeClick Node click callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun GraphRoute(
    viewModel: MainViewModel,
    onNodeClick: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) GraphDashboardSurface.DESKTOP else GraphDashboardSurface.MOBILE
        val plan = remember(surface) { buildGraphDashboardPlan(surface) }
        val context =
            remember(viewModel, onNodeClick) { GraphDashboardContext(viewModel, onNodeClick) }

        GraphScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless graph screen content.
 *
 * @param context Graph dashboard context.
 * @param plan Graph dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun GraphScreen(
    context: GraphDashboardContext,
    plan: GraphDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Graph,
        onNavigate = onNavigate,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                GraphDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}
