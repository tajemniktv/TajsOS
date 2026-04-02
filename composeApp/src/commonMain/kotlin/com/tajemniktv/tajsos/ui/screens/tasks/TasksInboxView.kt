/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.tasks

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.InboxEntryEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.tasks_inbox_capture_entries
import tajsos.composeapp.generated.resources.tasks_inbox_dismiss
import tajsos.composeapp.generated.resources.tasks_inbox_empty_entries
import tajsos.composeapp.generated.resources.tasks_inbox_empty_tasks
import tajsos.composeapp.generated.resources.tasks_inbox_mark_processed
import tajsos.composeapp.generated.resources.tasks_inbox_subtitle
import tajsos.composeapp.generated.resources.tasks_inbox_tasks_title
import tajsos.composeapp.generated.resources.tasks_inbox_title
import tajsos.composeapp.generated.resources.tasks_inbox_triage_task
import tajsos.composeapp.generated.resources.tasks_open_action

@Composable
internal fun TasksInboxView(
    inboxEntries: List<InboxEntryEntity>,
    inboxTasks: List<NodeEntity>,
    projectById: Map<Long, String>,
    areaById: Map<Long, String>,
    onTriageTask: (InboxEntryEntity) -> Unit,
    onDismiss: (InboxEntryEntity) -> Unit,
    onMarkProcessed: (NodeEntity) -> Unit,
    onOpen: (Long) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        Text(
            stringResource(Res.string.tasks_inbox_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(Res.string.tasks_inbox_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted,
        )

        Surface(
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            color = TajsOSTheme.Surface,
            border = BorderStroke(1.dp, TajsOSTheme.Border),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                Text(
                    stringResource(Res.string.tasks_inbox_capture_entries),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (inboxEntries.isEmpty()) {
                    EmptyState(
                        message = stringResource(Res.string.tasks_inbox_empty_entries),
                        description = null,
                        fillParent = false,
                        showContainer = false,
                        modifier = Modifier.padding(vertical = TajsOSTheme.SpacingLg),
                    )
                } else {
                    inboxEntries.take(20).forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    entry.rawText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TajsOSTheme.Text,
                                )
                                Text(
                                    shortDate(entry.capturedAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TajsOSTheme.Muted,
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                            ) {
                                OutlinedButton(onClick = { onTriageTask(entry) }) {
                                    Text(
                                        stringResource(Res.string.tasks_inbox_triage_task),
                                    )
                                }
                                IconButton(onClick = { onDismiss(entry) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        stringResource(Res.string.tasks_inbox_dismiss),
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = TajsOSTheme.Border)
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            color = TajsOSTheme.Surface,
            border = BorderStroke(1.dp, TajsOSTheme.Border),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                Text(
                    stringResource(Res.string.tasks_inbox_tasks_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (inboxTasks.isEmpty()) {
                    EmptyState(
                        message = stringResource(Res.string.tasks_inbox_empty_tasks),
                        description = null,
                        fillParent = false,
                        showContainer = false,
                        modifier = Modifier.padding(vertical = TajsOSTheme.SpacingLg),
                    )
                } else {
                    inboxTasks.forEach { task ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                    ).joinToString(" • ")
                                if (context.isNotBlank()) {
                                    Text(
                                        context,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TajsOSTheme.Muted,
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                            ) {
                                OutlinedButton(onClick = { onOpen(task.id) }) {
                                    Text(
                                        stringResource(
                                            Res.string.tasks_open_action,
                                        ),
                                    )
                                }
                                OutlinedButton(onClick = { onMarkProcessed(task) }) {
                                    Text(
                                        stringResource(Res.string.tasks_inbox_mark_processed),
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = TajsOSTheme.Border)
                    }
                }
            }
        }
    }
}
