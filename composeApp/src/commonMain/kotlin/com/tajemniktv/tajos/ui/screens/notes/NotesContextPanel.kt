/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.notes

import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.note_select_inspect_context
import tajsos.composeapp.generated.resources.notes_workspace_attachments
import tajsos.composeapp.generated.resources.notes_workspace_created_at
import tajsos.composeapp.generated.resources.notes_workspace_domain
import tajsos.composeapp.generated.resources.notes_workspace_edited_at
import tajsos.composeapp.generated.resources.notes_workspace_favorite_state
import tajsos.composeapp.generated.resources.notes_workspace_history
import tajsos.composeapp.generated.resources.notes_workspace_linked_tasks
import tajsos.composeapp.generated.resources.notes_workspace_no
import tajsos.composeapp.generated.resources.notes_workspace_no_attachments
import tajsos.composeapp.generated.resources.notes_workspace_no_linked_tasks
import tajsos.composeapp.generated.resources.notes_workspace_no_related_notes
import tajsos.composeapp.generated.resources.notes_workspace_no_tags
import tajsos.composeapp.generated.resources.notes_workspace_pin_state
import tajsos.composeapp.generated.resources.notes_workspace_related_notes
import tajsos.composeapp.generated.resources.notes_workspace_state
import tajsos.composeapp.generated.resources.notes_workspace_tags
import tajsos.composeapp.generated.resources.notes_workspace_yes

import com.tajemniktv.tajos.ui.components.common.mouseClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajos.data.AttachmentEntity
import com.tajemniktv.tajos.data.NodeEntity
import com.tajemniktv.tajos.data.isNoteItem
import com.tajemniktv.tajos.data.isTaskItem
import com.tajemniktv.tajos.ui.components.common.EmptyState
import com.tajemniktv.tajos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/**
 * Collapsible right rail with note metadata, graph context, and history sections.
 */
@Composable
fun NotesContextPanel(
    selectedNote: NotesWorkspaceItem?,
    relatedNodes: List<NodeEntity>,
    attachments: List<AttachmentEntity>,
    onOpenNode: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = TajsOSTheme.SurfaceLow,
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
    ) {
        if (selectedNote == null) {
            EmptyState(
                message = stringResource(Res.string.note_select_inspect_context),
                description = null,
                showContainer = false,
            )
            return@Surface
        }

        val linkedTasks = relatedNodes.filter { it.isTaskItem() }
        val relatedNotes = relatedNodes.filter { it.isNoteItem() }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            item {
                ContextSection(title = stringResource(Res.string.notes_workspace_state), icon = Icons.Default.Bookmark) {
                    Text(stringResource(Res.string.notes_workspace_pin_state, yesNo(selectedNote.isPinned)), color = TajsOSTheme.Text)
                    Text(stringResource(Res.string.notes_workspace_favorite_state, yesNo(selectedNote.isFavorite)), color = TajsOSTheme.Text)
                }
            }
            item {
                ContextSection(title = stringResource(Res.string.notes_workspace_tags), icon = Icons.Default.Sell) {
                    if (selectedNote.tags.isEmpty()) {
                        EmptyState(
                            message = stringResource(Res.string.notes_workspace_no_tags),
                            description = null,
                            fillParent = false,
                            showContainer = false,
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                        ) {
                            selectedNote.tags.forEach {
                                Text(
                                    "#$it",
                                    color = TajsOSTheme.Primary,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
            item {
                ContextSection(title = stringResource(Res.string.notes_workspace_domain), icon = Icons.Default.Category) {
                    Text(
                        notesDomainLabel(selectedNote.domain),
                        color = TajsOSTheme.Text,
                    )
                }
            }
            item {
                ContextSection(title = stringResource(Res.string.notes_workspace_linked_tasks), icon = Icons.Default.Link) {
                    if (linkedTasks.isEmpty()) {
                        EmptyState(
                            message = stringResource(Res.string.notes_workspace_no_linked_tasks),
                            description = null,
                            fillParent = false,
                            showContainer = false,
                        )
                    } else {
                        linkedTasks.forEach { task ->
                            Text(
                                text = task.title,
                                color = TajsOSTheme.Text,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .mouseClickable(onClick = { onOpenNode(task.id) })
                                        .padding(vertical = 2.dp),
                            )
                        }
                    }
                }
            }
            item {
                ContextSection(title = stringResource(Res.string.notes_workspace_related_notes), icon = Icons.Default.Link) {
                    if (relatedNotes.isEmpty()) {
                        EmptyState(
                            message = stringResource(Res.string.notes_workspace_no_related_notes),
                            description = null,
                            fillParent = false,
                            showContainer = false,
                        )
                    } else {
                        relatedNotes.forEach { note ->
                            Text(
                                text = note.title,
                                color = TajsOSTheme.Text,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .mouseClickable(onClick = { onOpenNode(note.id) })
                                        .padding(vertical = 2.dp),
                            )
                        }
                    }
                }
            }
            item {
                ContextSection(title = stringResource(Res.string.notes_workspace_attachments), icon = Icons.Default.AttachFile) {
                    if (attachments.isEmpty()) {
                        EmptyState(
                            message = stringResource(Res.string.notes_workspace_no_attachments),
                            description = null,
                            fillParent = false,
                            showContainer = false,
                        )
                    } else {
                        attachments.forEach {
                            Text(it.title ?: it.uriOrPath, color = TajsOSTheme.Text)
                        }
                    }
                }
            }
            item {
                ContextSection(title = stringResource(Res.string.notes_workspace_history), icon = Icons.Default.History) {
                    Text(stringResource(Res.string.notes_workspace_created_at, formatDate(selectedNote.createdAt)), color = TajsOSTheme.Text)
                    Text(stringResource(Res.string.notes_workspace_edited_at, formatDate(selectedNote.updatedAt)), color = TajsOSTheme.Text)
                }
            }
        }
    }
}

/**
 * Reusable section shell for right-rail context groups.
 */
@Composable
fun ContextSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = TajsOSTheme.SurfaceHighest.copy(alpha = 0.48f),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = TajsOSTheme.Primary)
                Text(
                    text = "  $title",
                    style = MaterialTheme.typography.titleSmall,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            content()
        }
    }
}

@Composable
private fun yesNo(value: Boolean): String = stringResource(if (value) Res.string.notes_workspace_yes else Res.string.notes_workspace_no)

private fun formatDate(epochMillis: Long): String =
    Instant
        .fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toString()
        .replace("T", " ")
        .take(16)
