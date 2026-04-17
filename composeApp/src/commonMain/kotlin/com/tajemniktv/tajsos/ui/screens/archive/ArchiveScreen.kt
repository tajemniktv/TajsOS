/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.archive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold

/**
 * Central archive entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of archive state.
 * @param onEditNode Node edit callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun ArchiveRoute(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface = if (maxWidth > 900.dp) ArchiveDashboardSurface.DESKTOP else ArchiveDashboardSurface.MOBILE
        val plan = remember(surface) { buildArchiveDashboardPlan(surface) }
        val context = remember(viewModel, onEditNode) { ArchiveDashboardContext(viewModel, onEditNode) }

        ArchiveScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless archive screen content.
 *
 * @param context Archive dashboard context.
 * @param plan Archive dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun ArchiveScreen(
    context: ArchiveDashboardContext,
    plan: ArchiveDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Archive,
        onNavigate = onNavigate,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            plan.primary.forEach { block ->
                item(key = block.id) {
                    ArchiveDashboardBlocks.resolve(block.id)?.invoke(context)
                }
            }
        }
    }
}
