/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.NodeCard
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Legacy dashboard section header kept for compatibility with dashboard block renderers.
 */
@Composable
fun GroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = TajsOSTheme.Primary,
        modifier = Modifier.padding(top = TajsOSTheme.SpacingMd, bottom = TajsOSTheme.SpacingSm),
    )
}

/**
 * Legacy notes dashboard card renderer kept for compatibility while the new workspace route is active.
 */
@Composable
fun KnowledgeItem(
    node: NodeWithPin,
    viewModel: MainViewModel,
    onNoteClick: (Long) -> Unit,
) {
    NodeCard(
        nodeWithPin = node,
        onClick = { onNoteClick(node.node.id) },
        onToggleDone = { status ->
            viewModel.updateNodeStatus(node.node, status)
        },
        onTogglePin = { isPinned -> viewModel.togglePin(node.node, isPinned) },
        onArchive = { viewModel.archiveNode(node.node) },
    )
}
