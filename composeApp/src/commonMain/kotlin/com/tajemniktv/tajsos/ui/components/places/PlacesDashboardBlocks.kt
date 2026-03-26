/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.places

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.screens.PlacesMainBlock

object PlacesDashboardBlockRegistry {
    private val renderers: Map<String, PlacesDashboardBlockRenderer> =
        mapOf("places_main" to ::renderPlacesMainBlock)

    fun resolve(id: String): PlacesDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderPlacesMainBlock(context: PlacesDashboardContext) {
    PlacesMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}
