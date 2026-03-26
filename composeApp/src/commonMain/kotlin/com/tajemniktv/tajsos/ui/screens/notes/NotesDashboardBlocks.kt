/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import androidx.compose.runtime.Composable

object NotesDashboardBlockRegistry {
    private val renderers: Map<String, com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardBlockRenderer> =
        mapOf("notes_main" to ::renderNotesMainBlock)

    fun resolve(id: String): com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderNotesMainBlock(context: com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardContext) {
    NotesMainBlock(viewModel = context.viewModel, onNoteClick = context.onNoteClick)
}
