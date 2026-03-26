/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.notes

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.screens.NotesMainBlock

object NotesDashboardBlockRegistry {
    private val renderers: Map<String, NotesDashboardBlockRenderer> =
        mapOf("notes_main" to ::renderNotesMainBlock)

    fun resolve(id: String): NotesDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderNotesMainBlock(context: NotesDashboardContext) {
    NotesMainBlock(viewModel = context.viewModel, onNoteClick = context.onNoteClick)
}
