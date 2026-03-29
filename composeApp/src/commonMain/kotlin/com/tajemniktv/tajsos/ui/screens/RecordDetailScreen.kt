/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Typed record detail entrypoint.
 *
 * The current implementation reuses the existing detail surface while record-specific content remains
 * under active decomposition.
 */
@Composable
fun RecordDetailScreen(
    viewModel: MainViewModel,
    recordId: Long,
    onBack: () -> Unit,
    onNavigateToNode: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    isDesktop: Boolean = false,
) {
    NoteDetailScreen(
        viewModel = viewModel,
        noteId = recordId,
        onBack = onBack,
        onNavigateToNode = onNavigateToNode,
        onNavigateToSearch = onNavigateToSearch,
        isDesktop = isDesktop,
    )
}
