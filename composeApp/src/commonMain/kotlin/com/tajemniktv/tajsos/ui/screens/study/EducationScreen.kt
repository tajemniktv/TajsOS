/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.study

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

/**
 * Education domain screen entrypoint.
 *
 * Internally reuses the existing study dashboard implementation while the domain
 * surface transitions to Education naming.
 */
@Composable
fun EducationScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    StudyScreen(viewModel = viewModel, onEditNode = onEditNode)
}
