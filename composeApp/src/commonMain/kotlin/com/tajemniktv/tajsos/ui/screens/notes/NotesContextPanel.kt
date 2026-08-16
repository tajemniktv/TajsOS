/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import com.tajemniktv.tajsos.ui.components.common.mouseClickable
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
import com.tajemniktv.tajsos.data.AttachmentEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.isNoteItem
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.note_select_inspect_context
import kotlin.time.Instant
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

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
                ContextSection(title = "State", icon = Icons.Default.Bookmark) {
                    Text("Pinned: ${yesNo(selectedNote.isPinned)}", color = TajsOSTheme.Text)
                    Text("Favorite: ${yesNo(selectedNote.isFavorite)}", color = TajsOSTheme.Text)
                }
            }
            item {
                ContextSection(title = "Tags", icon = Icons.Default.Sell) {
                    if (selectedNote.tags.isEmpty()) {
                        EmptyState(
                            message = "No tags",
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
                ContextSection(title = "Domain", icon = Icons.Default.Category) {
                    Text(
                        selectedNote.domain.displayName,
                        color = TajsOSTheme.Text,
                    )
                }
            }
            item {
                ContextSection(title = "Linked Tasks", icon = Icons.Default.Link) {
                    if (linkedTasks.isEmpty()) {
                        EmptyState(
                            message = "No linked tasks",
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
                ContextSection(title = "Related Notes", icon = Icons.Default.Link) {
                    if (relatedNotes.isEmpty()) {
                        EmptyState(
                            message = "No related notes",
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
                ContextSection(title = "Attachments", icon = Icons.Default.AttachFile) {
                    if (attachments.isEmpty()) {
                        EmptyState(
                            message = "No attachments",
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
                ContextSection(title = "History", icon = Icons.Default.History) {
                    Text("Created: ${formatDate(selectedNote.createdAt)}", color = TajsOSTheme.Text)
                    Text("Edited: ${formatDate(selectedNote.updatedAt)}", color = TajsOSTheme.Text)
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

private fun yesNo(value: Boolean): String = if (value) "Yes" else "No"

private fun formatDate(epochMillis: Long): String =
    Instant
        .fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toString()
        .replace("T", " ")
        .take(16)
