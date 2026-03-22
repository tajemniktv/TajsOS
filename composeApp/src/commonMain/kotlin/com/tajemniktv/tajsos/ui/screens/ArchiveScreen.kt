/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArchiveScreen(viewModel: MainViewModel, onEditNode: (Long) -> Unit) {
    val archivedNodes by viewModel.archivedNodes.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd)) {
        Text("ARCHIVE", style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(archivedNodes) { nodeWithPin ->
                ListItem(
                    headlineContent = { Text(nodeWithPin.node.title) },
                    supportingContent = { Text(nodeWithPin.node.type.uppercase()) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.updateNodeStatus(nodeWithPin.node, "active") }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restore")
                        }
                    },
                    modifier = Modifier.combinedClickable(
                        onClick = { onEditNode(nodeWithPin.node.id) },
                        onLongClick = { onEditNode(nodeWithPin.node.id) }
                    ),
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface)
                )
                HorizontalDivider(color = TactileTheme.Muted.copy(alpha = 0.5f))
            }
        }
    }
}
