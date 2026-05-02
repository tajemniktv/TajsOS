/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.archive_empty
import tajsos.composeapp.generated.resources.tasks_archive_subtitle
import tajsos.composeapp.generated.resources.tasks_archive_title
import tajsos.composeapp.generated.resources.tasks_open_action
import tajsos.composeapp.generated.resources.tasks_restore_action

@Composable
internal fun TasksArchiveView(
    archivedTasks: List<NodeEntity>,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onOpen: (Long) -> Unit,
    onRestore: (NodeEntity) -> Unit,
    onDelete: (NodeEntity) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        Text(
            stringResource(Res.string.tasks_archive_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(Res.string.tasks_archive_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted,
        )
        if (archivedTasks.isEmpty()) {
            EmptyState(message = stringResource(Res.string.archive_empty))
            return@Column
        }
        Surface(
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            color = TajsOSTheme.CardSurface,
        ) {
            Column {
                archivedTasks.forEach { task ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                task.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TajsOSTheme.Text,
                            )
                            val context =
                                listOfNotNull(
                                    task.projectId?.let { projectById[it] },
                                    task.areaId?.let { areaById[it] },
                                    task.archivedAt?.let(::shortDate),
                                ).joinToString(" • ")
                            if (context.isNotBlank()) {
                                Text(
                                    context,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TajsOSTheme.Muted,
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                            OutlinedButton(onClick = { onOpen(task.id) }) { Text(stringResource(Res.string.tasks_open_action)) }
                            OutlinedButton(onClick = { onRestore(task) }) { Text(stringResource(Res.string.tasks_restore_action)) }
                            IconButton(onClick = { onDelete(task) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = TajsOSTheme.Error,
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = TajsOSTheme.GhostBorder)
                }
            }
        }
    }
}
