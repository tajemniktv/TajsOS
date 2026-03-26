/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.places

import androidx.compose.runtime.Composable

object PlacesDashboardBlockRegistry {
    private val renderers: Map<String, com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardBlockRenderer> =
        mapOf("places_main" to ::renderPlacesMainBlock)

    fun resolve(id: String): com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderPlacesMainBlock(context: com.tajemniktv.tajsos.ui.screens.places.PlacesDashboardContext) {
    PlacesMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}
