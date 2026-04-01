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
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.relationships_desc
import tajsos.composeapp.generated.resources.relationships_title

object RelationshipsDashboardBlocks {
    private val renderers: Map<String, RelationshipsDashboardBlockRenderer> =
        mapOf("relationships_main" to ::renderRelationshipsMainBlock) // NON-NLS

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
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        Text(
            text = stringResource(Res.string.relationships_title),
            style = MaterialTheme.typography.displaySmall,
            color = TajsOSTheme.Text,
        )
        Text(
            text = stringResource(Res.string.relationships_desc),
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted,
        )

        PeopleLayer(
            viewModel = viewModel,
            snapshot = relationshipSnapshot,
            onEditNode = onEditNode,
        )
    }
}
