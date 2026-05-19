/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import com.tajemniktv.tajsos.ui.components.TactileOutlinedTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.platform.toClipEntry
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max
import kotlin.time.Instant

/**
 * Primary note viewer/editor pane with metadata and action row.
 */
@Composable
fun NotesEditorPane(
    note: NotesWorkspaceItem?,
    focusMode: Boolean,
    contextPanelVisible: Boolean,
    focusTitleSignal: Int,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onArchive: () -> Unit,
    onToggleFocusMode: () -> Unit,
    onToggleContextPanel: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
    ) {
        if (note == null) {
            EmptyState(
                message = "No note selected",
                description = "Pick a note from the list to start reading or editing.",
            )
            return@Surface
        }

        val titleFocusRequester = remember(note.id) { FocusRequester() }
        val clipboard = LocalClipboard.current
        val scope = rememberCoroutineScope()
        LaunchedEffect(note.id, focusTitleSignal) {
            if (focusTitleSignal > 0) {
                titleFocusRequester.requestFocus()
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.fillMaxWidth().widthIn(max = 880.dp)) {
                    Text(
                        text = "NOTES > ${
                            note.domain.name.lowercase().replaceFirstChar(Char::titlecase)
                        }",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                    Spacer(Modifier.height(8.dp))
                    TactileOutlinedTextField(
                        value = note.title,
                        onValueChange = onTitleChange,
                        modifier = Modifier.focusRequester(titleFocusRequester),
                        containerModifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                        placeholder = { Text("Untitled note") },
                    )
                    Text(
                        text = "${absoluteDateLabel(note.updatedAt)} • ${wordCount(note.content)} words • Markdown",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    NotesEditorHeaderActions(
                        favorite = note.isFavorite,
                        focusMode = focusMode,
                        contextPanelVisible = contextPanelVisible,
                        onToggleFavorite = onToggleFavorite,
                        onArchive = onArchive,
                        onToggleFocusMode = onToggleFocusMode,
                        onToggleContextPanel = onToggleContextPanel,
                        onDuplicate = onDuplicate,
                        onCopyContent = {
                            scope.launch {
                                clipboard.setClipEntry(AnnotatedString(note.content).toClipEntry())
                            }
                        },
                        onDelete = onDelete,
                    )
                    Spacer(Modifier.height(8.dp))
                    BasicTextField(
                        value = note.content,
                        onValueChange = onContentChange,
                        cursorBrush = SolidColor(TajsOSTheme.Primary),
                        textStyle =
                            MaterialTheme.typography.bodyLarge.merge(
                                TextStyle(
                                    color = TajsOSTheme.Text,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.35f,
                                ),
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = true)
                                .background(
                                    color = TajsOSTheme.SurfaceHigh.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                                ).padding(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * Reusable action row for the note editor header.
 */
@Composable
fun NotesEditorHeaderActions(
    favorite: Boolean,
    focusMode: Boolean,
    contextPanelVisible: Boolean,
    onToggleFavorite: () -> Unit,
    onArchive: () -> Unit,
    onToggleFocusMode: () -> Unit,
    onToggleContextPanel: () -> Unit,
    onDuplicate: () -> Unit,
    onCopyContent: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row {
            IconButton(onClick = onToggleFavorite, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = if (favorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = if (favorite) TajsOSTheme.Primary else TajsOSTheme.Text,
                )
            }
            IconButton(onClick = onArchive, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Archive, contentDescription = null, tint = TajsOSTheme.Text)
            }
            IconButton(onClick = onToggleFocusMode, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.Default.CenterFocusStrong,
                    contentDescription = null,
                    tint = if (focusMode) TajsOSTheme.Primary else TajsOSTheme.Text,
                )
            }
            IconButton(onClick = onToggleContextPanel, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = if (contextPanelVisible) TajsOSTheme.Primary else TajsOSTheme.Text,
                )
            }
        }
        Box {
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Duplicate note") },
                    onClick = {
                        menuOpen = false
                        onDuplicate()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Copy content") },
                    onClick = {
                        menuOpen = false
                        onCopyContent()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete note") },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    },
                )
            }
        }
    }
}

private fun wordCount(content: String): Int =
    content
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .size
        .let { max(0, it) }

private fun absoluteDateLabel(epochMillis: Long): String =
    Instant
        .fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toString()
        .replace("T", " ")
        .take(16)
