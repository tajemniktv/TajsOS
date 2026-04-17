/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.focus

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
 * Central focus entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of focus state.
 * @param onNavigate Navigation callback.
 */
@Composable
fun FocusRoute(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface = if (maxWidth > 900.dp) FocusDashboardSurface.DESKTOP else FocusDashboardSurface.MOBILE
        val plan = remember(surface) { buildFocusDashboardPlan(surface) }
        val context = remember(viewModel) { FocusDashboardContext(viewModel) }

        FocusScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless focus screen content.
 *
 * @param context Focus dashboard context.
 * @param plan Focus dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun FocusScreen(
    context: FocusDashboardContext,
    plan: FocusDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Focus,
        onNavigate = onNavigate,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                FocusDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}
