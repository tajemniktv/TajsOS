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
fun ProtocolsScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val transitionProtocolsSnapshot by viewModel.transitionProtocolsSnapshot.collectAsState()
    val playbookSnapshot by viewModel.playbookSnapshot.collectAsState()
    val allModes by viewModel.allModes.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val protocolHistoryItems by viewModel.protocolHistoryItems.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            text = "PROTOCOLS",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text,
        )
        Text(
            text = "Trigger repeatable transition sequences and keep playbooks attached to real contexts.",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )

        ProtocolsLayer(
            viewModel = viewModel,
            snapshot = transitionProtocolsSnapshot,
            playbookSnapshot = playbookSnapshot,
            allModes = allModes,
            allAreas = allAreas,
            history = protocolHistoryItems,
            onEditNode = onEditNode,
        )
    }
}
