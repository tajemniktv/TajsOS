/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.today

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
 * Central today entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of today state.
 * @param onEditNode Node edit callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun TodayRoute(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) TodayDashboardSurface.DESKTOP else TodayDashboardSurface.MOBILE
        val plan = remember(surface) { buildTodayDashboardPlan(surface) }
        val context =
            remember(viewModel, onEditNode) { TodayDashboardContext(viewModel, onEditNode) }

        TodayScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless today screen content.
 *
 * @param context Today dashboard context.
 * @param plan Today dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun TodayScreen(
    context: TodayDashboardContext,
    plan: TodayDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Today,
        onNavigate = onNavigate,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                TodayDashboardBlocks.resolve(block.id)?.invoke(context)
            }
        }
    }
}
