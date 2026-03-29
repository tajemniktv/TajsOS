/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.timearchitecture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun TimeArchitectureScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
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

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        plan.primary.forEach { block ->
            item(key = block.id) {
                TimeArchitectureDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}
