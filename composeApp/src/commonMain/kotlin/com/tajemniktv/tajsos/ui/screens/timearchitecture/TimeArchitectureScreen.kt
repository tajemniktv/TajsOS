/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.timearchitecture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Central time architecture entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of time architecture state.
 * @param onEditNode Node edit callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun TimeArchitectureRoute(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    val timeArchitectureSnapshot by viewModel.timeArchitectureSnapshot.collectAsState()
    var selectedHorizon by remember { mutableStateOf(TimeArchitectureHorizon.MONTH) }

    val context =
        TimeArchitectureDashboardContext(
            viewModel = viewModel,
            snapshot = timeArchitectureSnapshot,
            selectedHorizon = selectedHorizon,
            onHorizonSelected = { horizon ->
                selectedHorizon = horizon
                viewModel.applyTimeHorizonFilter(horizon.key)
            },
            onEditNode = onEditNode,
        )

    val surface = TimeArchitectureDashboardSurface.MOBILE // Default for now
    val plan = remember(surface) { buildTimeArchitectureDashboardPlan(surface) }

    TimeArchitectureScreen(
        context = context,
        plan = plan,
        onNavigate = onNavigate,
    )
}

/**
 * Stateless time architecture screen content.
 *
 * @param context Time architecture dashboard context.
 * @param plan Time architecture dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun TimeArchitectureScreen(
    context: TimeArchitectureDashboardContext,
    plan: TimeArchitectureDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.TimeArchitecture,
        onNavigate = onNavigate,
        scrollBehavior = ScreenScrollBehavior.None,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
        ) {
            plan.primary.forEach { block ->
                item(key = block.id) {
                    TimeArchitectureDashboardBlocks.resolve(block.id)?.invoke(context)
                }
            }
        }
    }
}
