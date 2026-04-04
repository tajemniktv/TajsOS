/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.records.detail

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenHeaderController
import com.tajemniktv.tajsos.ui.screens.notes.detail.NoteDetailScreen

/**
 * Typed record detail entrypoint.
 *
 * Currently reuses the standard note detail architecture but is separated for later
 * specialized record layout engines.
 */
@Composable
fun RecordDetailScreen(
    viewModel: MainViewModel,
    recordId: Long,
    onBack: () -> Unit,
    onNavigateToNode: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    isDesktop: Boolean = false,
    screenHeaderController: ScreenHeaderController? = null,
) {
    NoteDetailScreen(
        viewModel = viewModel,
        noteId = recordId,
        onBack = onBack,
        onNavigateToNode = onNavigateToNode,
        onNavigateToSearch = onNavigateToSearch,
        isDesktop = isDesktop,
        screenHeaderController = screenHeaderController,
        headerScreen = Screen.RecordDetail,
    )
}
