/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.areas

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
 * Central areas entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of areas state.
 * @param onNavigate Navigation callback.
 */
@Composable
fun AreasRoute(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) AreasDashboardSurface.DESKTOP else AreasDashboardSurface.MOBILE
        val plan = remember(surface) { buildAreasDashboardPlan(surface) }
        val context =
            remember(viewModel, onNavigate) { AreasDashboardContext(viewModel, onNavigate) }

        AreasScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless areas screen content.
 *
 * @param context Areas dashboard context.
 * @param plan Areas dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun AreasScreen(
    context: AreasDashboardContext,
    plan: AreasDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Areas,
        onNavigate = onNavigate,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                AreasDashboardBlocks.resolve(block.id)?.invoke(context)
            }
        }
    }
}
