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
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold

@Composable
fun CapacityScreen(viewModel: MainViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) {
                CapacityDashboardSurface.DESKTOP
            } else {
                CapacityDashboardSurface.MOBILE
            }
        val plan = remember(surface) { buildCapacityDashboardPlan(surface) }
        val context = remember(viewModel) { CapacityDashboardContext(viewModel) }
        ScreenScaffold {
            Column(modifier = Modifier.fillMaxSize()) {
                plan.primary.forEach { block ->
                    CapacityDashboardBlocks.resolve(block.id)?.invoke(context)
                }
            }
        }
    }
}
