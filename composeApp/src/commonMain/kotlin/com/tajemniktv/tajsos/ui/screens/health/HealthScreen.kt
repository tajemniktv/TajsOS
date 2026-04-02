/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.health

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Minimal Health domain lens over shared system data.
 */
@Composable
fun HealthScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) {
                HealthDashboardSurface.DESKTOP
            } else {
                HealthDashboardSurface.MOBILE
            }
        val plan = remember(surface) { buildHealthDashboardPlan(surface) }
        val context =
            remember(viewModel, onEditNode) { HealthDashboardContext(viewModel, onEditNode) }
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp),
        ) {
            plan.primary.forEach { block ->
                HealthDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}
