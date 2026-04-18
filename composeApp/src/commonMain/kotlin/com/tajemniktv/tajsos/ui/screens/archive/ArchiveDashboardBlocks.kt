/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.archive

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.archive_delete
import tajsos.composeapp.generated.resources.archive_empty
import tajsos.composeapp.generated.resources.archive_restore
import tajsos.composeapp.generated.resources.archive_subtitle
import tajsos.composeapp.generated.resources.archive_title
import tajsos.composeapp.generated.resources.type_area
import tajsos.composeapp.generated.resources.type_idea
import tajsos.composeapp.generated.resources.type_note
import tajsos.composeapp.generated.resources.type_project
import tajsos.composeapp.generated.resources.type_task

object ArchiveDashboardBlocks {
    private val renderers: Map<String, ArchiveDashboardBlockRenderer> =
        mapOf("archive_main" to ::renderArchiveMainBlock) // NON-NLS

    fun resolve(id: String): ArchiveDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderArchiveMainBlock(context: ArchiveDashboardContext) {
    ArchiveMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ArchiveMainBlock(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val archivedNodes by viewModel.archivedNodes.collectAsState()
    val isInitialLoadComplete by viewModel.isInitialLoadComplete.collectAsState()

    Column(modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd)) {
        Text(
            stringResource(Res.string.archive_title),
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            stringResource(Res.string.archive_subtitle),
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Muted,
        )
        Spacer(modifier = Modifier.height(TajsOSTheme.SpacingMd))

        if (archivedNodes.isEmpty() && isInitialLoadComplete) {
            EmptyState(message = stringResource(Res.string.archive_empty))
        } else {
            Column {
                archivedNodes.forEach { nodeWithPin ->
                    ListItem(
                        headlineContent = { Text(nodeWithPin.node.title) },
                        supportingContent = {
                            val typeLabel =
                                when (nodeWithPin.node.type)
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
                                        tint = TajsOSTheme.Primary,
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteNodePermanently(nodeWithPin.node) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(Res.string.archive_delete),
                                        tint = TajsOSTheme.Error,
                                    )
                                }
                            }
                        },
                        modifier =
                            Modifier.combinedClickable(
                                onClick = { onEditNode(nodeWithPin.node.id) },
                                onLongClick = { onEditNode(nodeWithPin.node.id) },
                            ),
                        colors = ListItemDefaults.colors(containerColor = TajsOSTheme.CardSurface),
                    )
                    HorizontalDivider(color = TajsOSTheme.Muted.copy(alpha = 0.5f))
                }
            }
        }
    }
}

