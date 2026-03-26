/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.relationships

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.ui.screens.RelationshipsMainBlock

object RelationshipsDashboardBlockRegistry {
    private val renderers: Map<String, RelationshipsDashboardBlockRenderer> =
        mapOf("relationships_main" to ::renderRelationshipsMainBlock)

    fun resolve(id: String): RelationshipsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderRelationshipsMainBlock(context: RelationshipsDashboardContext) {
    RelationshipsMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}
