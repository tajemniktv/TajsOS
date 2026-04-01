/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Entry screen for the Notes workspace.
 *
 * Delegates to [NotesRoute], which renders a responsive notes-specific workspace:
 * desktop/tablet uses a master-detail layout and narrow widths use list-to-detail navigation.
 */
@Composable
fun NotesScreen(
    viewModel: MainViewModel,
    onNoteClick: (Long) -> Unit,
    initialSelectedNoteId: Long? = null,
) {
    NotesRoute(
        viewModel = viewModel,
        onNavigateToNode = onNoteClick,
        initialSelectedNoteId = initialSelectedNoteId,
    )
}
