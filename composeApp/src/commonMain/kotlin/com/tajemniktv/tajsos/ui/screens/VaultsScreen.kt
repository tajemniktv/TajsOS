/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun VaultsScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val vaultsSnapshot by viewModel.vaultsSnapshot.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            text = "VAULTS",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text,
        )
        Text(
            text = "Keep reference material, documents, paperwork, and must-find-later items easy to retrieve.",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )

        VaultsLayer(
            viewModel = viewModel,
            snapshot = vaultsSnapshot,
            onEditNode = onEditNode,
        )
    }
}
