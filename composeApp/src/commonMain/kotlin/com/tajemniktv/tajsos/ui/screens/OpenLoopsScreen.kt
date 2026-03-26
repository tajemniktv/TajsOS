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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun OpenLoopsScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val openLoopsSnapshot by viewModel.openLoopsSnapshot.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    var openLoopView by remember { mutableStateOf(OpenLoopView.Inbox) }

    Column(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            text = "OPEN LOOPS",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text,
        )
        Text(
            text = "Resolve inbox spillover, waiting-fors, pending decisions, and stale unresolved loops.",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
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
