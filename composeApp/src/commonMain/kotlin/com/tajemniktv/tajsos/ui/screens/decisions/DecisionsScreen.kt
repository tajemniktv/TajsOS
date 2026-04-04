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
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DecisionsScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
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
        ScreenScaffold {
            Column(modifier = Modifier.fillMaxSize()) {
                plan.primary.forEach { block ->
                    DecisionsDashboardBlocks.resolve(block.id)?.invoke(context)
                }
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
