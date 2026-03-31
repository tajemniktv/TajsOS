/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.protocols

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
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

object ProtocolsDashboardBlockRegistry {
    private val renderers: Map<String, ProtocolsDashboardBlockRenderer> =
        mapOf("protocols_main" to ::renderProtocolsMainBlock)

    fun resolve(id: String): ProtocolsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderProtocolsMainBlock(context: ProtocolsDashboardContext) {
    ProtocolsMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun ProtocolsMainBlock(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val transitionProtocolsSnapshot by viewModel.transitionProtocolsSnapshot.collectAsState()
    val playbookSnapshot by viewModel.playbookSnapshot.collectAsState()
    val allModes by viewModel.allModes.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val protocolHistoryItems by viewModel.protocolHistoryItems.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)
    ) {
        Text(
            text = "PROTOCOLS",
            style = MaterialTheme.typography.displaySmall,
            color = TajsOSTheme.Text
        )
        Text(
            text = "Browse reusable routines in Library and run them step-by-step in Active mode.",
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted
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
