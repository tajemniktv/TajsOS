/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.places

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

object PlacesDashboardBlockRegistry {
    private val renderers: Map<String, PlacesDashboardBlockRenderer> =
        mapOf("places_main" to ::renderPlacesMainBlock)

    fun resolve(id: String): PlacesDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderPlacesMainBlock(context: PlacesDashboardContext) {
    PlacesMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun PlacesMainBlock(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val physicalLogisticsSnapshot by viewModel.physicalLogisticsSnapshot.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            text = "CONTEXT & LOGISTICS",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text,
        )
        Text(
            text = "Coordinate errands, packing, travel prep, and reminders tied to physical context anchors.",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )

        PlacesLayer(
            viewModel = viewModel,
            snapshot = physicalLogisticsSnapshot,
            allAreas = allAreas,
            onEditNode = onEditNode,
        )
    }
}
