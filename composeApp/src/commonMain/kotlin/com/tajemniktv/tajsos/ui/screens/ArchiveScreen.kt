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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArchiveScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
)
{
    val archivedNodes by viewModel.archivedNodes.collectAsState()
    val isInitialLoadComplete by viewModel.isInitialLoadComplete.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd)) {
        Text(
            stringResource(Res.string.archive_title),
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            stringResource(Res.string.archive_subtitle),
            style = MaterialTheme.typography.labelSmall,
            color = TactileTheme.Muted,
        )
        Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))

        if (archivedNodes.isEmpty() && isInitialLoadComplete)
        {
            EmptyState(message = stringResource(Res.string.archive_empty))
        } else
        {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(archivedNodes) { nodeWithPin ->
                    ListItem(
                        headlineContent = { Text(nodeWithPin.node.title) },
                        supportingContent = {
                            val typeLabel = when (nodeWithPin.node.type)
                            {
                                "task" -> stringResource(Res.string.type_task)
                                "note" -> stringResource(Res.string.type_note)
                                "idea" -> stringResource(Res.string.type_idea)
                                "project" -> stringResource(Res.string.type_project)
                                "area" -> stringResource(Res.string.type_area)
                                else -> nodeWithPin.node.type
                            }
                            Text(typeLabel.uppercase())
                        },
                        trailingContent = {
                            Row {
                                IconButton(
                                    onClick = {
                                        viewModel.updateNodeStatus(
                                            nodeWithPin.node,
                                            "active",
                                        )
                                    },
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = stringResource(Res.string.archive_restore),
                                        tint = TactileTheme.Primary,
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteNodePermanently(nodeWithPin.node) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(Res.string.archive_delete),
                                        tint = TactileTheme.Error,
                                    )
                                }
                            }
                        },
                        modifier =
                                Modifier.combinedClickable(
                                    onClick = { onEditNode(nodeWithPin.node.id) },
                                    onLongClick = { onEditNode(nodeWithPin.node.id) },
                                ),
                        colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                    )
                    HorizontalDivider(color = TactileTheme.Muted.copy(alpha = 0.5f))
                }
            }
        }
    }
}
