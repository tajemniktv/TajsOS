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
 * Record detail route that reuses note detail architecture.
 *
 * @param viewModel Source of record state.
 * @param recordId ID of the record to display.
 * @param onBack Callback to go back.
 * @param onNavigateToNode Callback to navigate to another node.
 * @param onNavigateToSearch Callback to navigate to search.
 * @param isDesktop Whether the current environment is a desktop layout.
 * @param onNavigate Navigation callback.
 */
@Composable
fun RecordDetailRoute(
    viewModel: MainViewModel,
    recordId: Long,
    onBack: () -> Unit,
    onNavigateToNode: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    isDesktop: Boolean = false,
    onNavigate: (String) -> Unit,
) {
    RecordDetailScreen(
        viewModel = viewModel,
        recordId = recordId,
        onBack = onBack,
        onNavigateToNode = onNavigateToNode,
        onNavigateToSearch = onNavigateToSearch,
        isDesktop = isDesktop,
        onNavigate = onNavigate,
    )
}

/**
 * Stateless record detail screen content.
 *
 * @param viewModel Source of record state.
 * @param recordId ID of the record to display.
 * @param onBack Callback to go back.
 * @param onNavigateToNode Callback to navigate to another node.
 * @param onNavigateToSearch Callback to navigate to search.
 * @param isDesktop Whether the current environment is a desktop layout.
 * @param onNavigate Navigation callback.
 */
@Composable
fun RecordDetailScreen(
    viewModel: MainViewModel,
    recordId: Long,
    onBack: () -> Unit,
    onNavigateToNode: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    isDesktop: Boolean = false,
    onNavigate: (String) -> Unit,
) {
    NoteDetailScreen(
        viewModel = viewModel,
        noteId = recordId,
        onBack = onBack,
        onNavigateToNode = onNavigateToNode,
        onNavigateToSearch = onNavigateToSearch,
        isDesktop = isDesktop,
        onNavigate = onNavigate,
        headerScreen = Screen.RecordDetail,
    )
}
