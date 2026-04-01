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

/**
 * Renders the Archive dashboard shell and delegates block rendering using the archive plan.
 *
 * @param viewModel Source of archived node state and actions.
 * @param onEditNode Callback invoked with a node id to open the node editor.
 */
@Composable
fun ArchiveScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) {
                ArchiveDashboardSurface.DESKTOP
            } else {
                ArchiveDashboardSurface.MOBILE
            }
        val plan = remember(surface) { buildArchiveDashboardPlan(surface) }
        val context =
            remember(viewModel, onEditNode) { ArchiveDashboardContext(viewModel, onEditNode) }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
        ) {
            plan.primary.forEach { block ->
                item(key = block.id) {
                    ArchiveDashboardBlockRegistry.resolve(block.id)?.invoke(context)
                }
            }
        }
    }
}
