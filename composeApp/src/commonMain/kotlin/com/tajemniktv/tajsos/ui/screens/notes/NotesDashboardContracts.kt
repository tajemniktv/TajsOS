/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

enum class NotesDashboardSurface { MOBILE, DESKTOP }

data class NotesDashboardBlock(
    val id: String,
)

data class NotesDashboardPlan(
    val primary: List<com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardBlock>,
    val secondary: List<com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardBlock> = emptyList(),
)

data class NotesDashboardContext(
    val viewModel: MainViewModel,
    val onNoteClick: (Long) -> Unit,
)

typealias NotesDashboardBlockRenderer = @Composable (com.tajemniktv.tajsos.ui.screens.notes.NotesDashboardContext) -> Unit
