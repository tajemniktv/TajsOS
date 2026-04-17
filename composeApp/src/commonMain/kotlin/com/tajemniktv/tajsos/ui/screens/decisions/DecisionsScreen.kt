/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.decisions

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Central decisions entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of decisions state.
 * @param onEditNode Node edit callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun DecisionsRoute(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) {
                DecisionsDashboardSurface.DESKTOP
            } else {
                DecisionsDashboardSurface.MOBILE
            }
        val plan = remember(surface) { buildDecisionsDashboardPlan(surface) }
        val context =
            remember(viewModel, onEditNode) { DecisionsDashboardContext(viewModel, onEditNode) }

        DecisionsScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless decisions screen content.
 *
 * @param context Decisions dashboard context.
 * @param plan Decisions dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun DecisionsScreen(
    context: DecisionsDashboardContext,
    plan: DecisionsDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Decisions,
        onNavigate = onNavigate,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                DecisionsDashboardBlocks.resolve(block.id)?.invoke(context)
            }
        }
    }
}

@Preview
@Composable
private fun DecisionsScreenPreview() {
    TajsOSTheme {
        // DecisionsScreen(...)
    }
}
