/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.relationships

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

object RelationshipsDashboardBlockRegistry {
    private val renderers: Map<String, RelationshipsDashboardBlockRenderer> =
        mapOf("relationships_main" to ::renderRelationshipsMainBlock)

    fun resolve(id: String): RelationshipsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderRelationshipsMainBlock(context: RelationshipsDashboardContext) {
    RelationshipsMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun RelationshipsMainBlock(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val relationshipSnapshot by viewModel.relationshipSnapshot.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)
    ) {
        Text(
            text = "RELATIONSHIPS LENS",
            style = MaterialTheme.typography.displaySmall,
            color = TajsOSTheme.Text
        )
        Text(
            text = "Track people, shared plans, follow-ups, and relationship continuity across life data.",
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted
        )

        PeopleLayer(
            viewModel = viewModel,
            snapshot = relationshipSnapshot,
            onEditNode = onEditNode,
        )
    }
}
