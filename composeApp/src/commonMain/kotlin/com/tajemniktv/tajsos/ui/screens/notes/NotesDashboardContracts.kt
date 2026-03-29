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
    val primary: List<NotesDashboardBlock>,
    val secondary: List<NotesDashboardBlock> = emptyList(),
)

data class NotesDashboardContext(
    val viewModel: MainViewModel,
    val onNoteClick: (Long) -> Unit,
)

typealias NotesDashboardBlockRenderer = @Composable (NotesDashboardContext) -> Unit
