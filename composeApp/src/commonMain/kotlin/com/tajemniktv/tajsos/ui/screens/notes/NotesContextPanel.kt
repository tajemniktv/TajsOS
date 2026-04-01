/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
            Column(
                modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Select a note to inspect context", color = TajsOSTheme.Muted)
            }
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
                        Text("No tags", color = TajsOSTheme.Muted)
                    } else {
                        selectedNote.tags.forEach {
                            Text("#$it", color = TajsOSTheme.Text)
                        }
                    }
                }
            }
            item {
                ContextSection(title = "Domain", icon = Icons.Default.Category) {
                    Text(selectedNote.domain.name.lowercase().replaceFirstChar(Char::titlecase), color = TajsOSTheme.Text)
                }
            }
            item {
                ContextSection(title = "Linked Tasks", icon = Icons.Default.Link) {
                    if (linkedTasks.isEmpty()) {
                        Text("No linked tasks", color = TajsOSTheme.Muted)
                    } else {
                        linkedTasks.forEach { task ->
                            Text(
                                text = task.title,
                                color = TajsOSTheme.Text,
                                modifier = Modifier.fillMaxWidth().clickable { onOpenNode(task.id) }.padding(vertical = 2.dp),
                            )
                        }
                    }
                }
            }
            item {
                ContextSection(title = "Related Notes", icon = Icons.Default.Link) {
                    if (relatedNotes.isEmpty()) {
                        Text("No related notes", color = TajsOSTheme.Muted)
                    } else {
                        relatedNotes.forEach { note ->
                            Text(
                                text = note.title,
                                color = TajsOSTheme.Text,
                                modifier = Modifier.fillMaxWidth().clickable { onOpenNode(note.id) }.padding(vertical = 2.dp),
                            )
                        }
                    }
                }
            }
            item {
                ContextSection(title = "Attachments", icon = Icons.Default.AttachFile) {
                    if (attachments.isEmpty()) {
                        Text("No attachments", color = TajsOSTheme.Muted)
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
