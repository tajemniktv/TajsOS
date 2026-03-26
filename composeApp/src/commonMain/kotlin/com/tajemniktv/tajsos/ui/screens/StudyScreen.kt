/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.MainViewModel

@Composable
fun StudyScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    StudentBoardScreen(
        viewModel = viewModel,
        onEditNode = onEditNode,
    )
}
