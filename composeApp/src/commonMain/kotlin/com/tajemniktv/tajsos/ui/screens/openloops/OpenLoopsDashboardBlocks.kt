/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.openloops

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.lens.LensUiContract
import com.tajemniktv.tajsos.ui.screens.OpenLoopView
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource

object OpenLoopsDashboardBlockRegistry {
    private val renderers: Map<String, OpenLoopsDashboardBlockRenderer> =
        mapOf("openloops_main" to ::renderOpenLoopsMainBlock)

    fun resolve(id: String): OpenLoopsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderOpenLoopsMainBlock(context: OpenLoopsDashboardContext) {
    OpenLoopsMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun OpenLoopsMainBlock(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val openLoopsSnapshot by viewModel.openLoopsSnapshot.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    var openLoopView by remember { mutableStateOf(OpenLoopView.Inbox) }

    Column(
        modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)
    ) {
        Text(
            text = stringResource(LensUiContract.unresolvedLens.title),
            style = MaterialTheme.typography.displaySmall,
            color = TajsOSTheme.Text
        )
        Text(
            text = stringResource(LensUiContract.unresolvedLens.subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted
        )

        OpenLoopsLayer(
            viewModel = viewModel,
            snapshot = openLoopsSnapshot,
            allAreas = allAreas,
            allNodes = allNodes,
            openLoopView = openLoopView,
            onOpenLoopView = { openLoopView = it },
            onEditNode = onEditNode,
        )
    }
}
