/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Typed task detail entrypoint.
 *
 * The current implementation reuses the existing detail surface while task-specific content remains
 * under active decomposition.
 */
@Composable
fun TaskDetailScreen(
    viewModel: MainViewModel,
    taskId: Long,
    onBack: () -> Unit,
    onNavigateToNode: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    NoteDetailScreen(
        viewModel = viewModel,
        noteId = taskId,
        onBack = onBack,
        onNavigateToNode = onNavigateToNode,
        onNavigateToSearch = onNavigateToSearch,
    )
}
