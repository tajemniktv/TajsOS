/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.capacity

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
 * Central capacity entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of capacity state.
 * @param onNavigate Navigation callback.
 */
@Composable
fun CapacityRoute(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface = if (maxWidth > 900.dp) CapacityDashboardSurface.DESKTOP else CapacityDashboardSurface.MOBILE
        val plan = remember(surface) { buildCapacityDashboardPlan(surface) }
        val context = remember(viewModel) { CapacityDashboardContext(viewModel) }

        CapacityScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless capacity screen content.
 *
 * @param context Capacity dashboard context.
 * @param plan Capacity dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun CapacityScreen(
    context: CapacityDashboardContext,
    plan: CapacityDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Capacity,
        onNavigate = onNavigate,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                CapacityDashboardBlocks.resolve(block.id)?.invoke(context)
            }
        }
    }
}
