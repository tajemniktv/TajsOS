/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.health

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
 * Central health entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of health state.
 * @param onEditNode Node edit callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun HealthRoute(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) HealthDashboardSurface.DESKTOP else HealthDashboardSurface.MOBILE
        val plan = remember(surface) { buildHealthDashboardPlan(surface) }
        val context =
            remember(viewModel, onEditNode) { HealthDashboardContext(viewModel, onEditNode) }

        HealthScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless health screen content.
 *
 * @param context Health dashboard context.
 * @param plan Health dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun HealthScreen(
    context: HealthDashboardContext,
    plan: HealthDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Health,
        onNavigate = onNavigate,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                HealthDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}
