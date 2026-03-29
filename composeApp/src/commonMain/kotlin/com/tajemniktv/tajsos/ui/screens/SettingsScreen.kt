/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Renders the "Preferences" settings surface.
 *
 * This route is kept as the root settings destination for compatibility with existing
 * navigation calls and sidebar context behavior.
 */
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToCalendarSettings: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    SettingsPreferencesScreen(viewModel = viewModel)
}
