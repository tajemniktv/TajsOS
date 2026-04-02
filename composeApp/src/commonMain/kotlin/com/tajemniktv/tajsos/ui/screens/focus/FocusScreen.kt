/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.focus

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

@Composable
fun FocusScreen(viewModel: MainViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) {
                FocusDashboardSurface.DESKTOP
            } else {
                FocusDashboardSurface.MOBILE
            }
        val plan = remember(surface) { buildFocusDashboardPlan(surface) }
        val context = remember(viewModel) { FocusDashboardContext(viewModel) }
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp),
        ) {
            plan.primary.forEach { block ->
                FocusDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}
