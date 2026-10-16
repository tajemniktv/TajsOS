/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.notes

import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.notes_workspace_archive
import tajsos.composeapp.generated.resources.notes_workspace_breadcrumb
import tajsos.composeapp.generated.resources.notes_workspace_context
import tajsos.composeapp.generated.resources.notes_workspace_copy
import tajsos.composeapp.generated.resources.notes_workspace_delete
import tajsos.composeapp.generated.resources.notes_workspace_duplicate
import tajsos.composeapp.generated.resources.notes_workspace_favorite_add
import tajsos.composeapp.generated.resources.notes_workspace_favorite_remove
import tajsos.composeapp.generated.resources.notes_workspace_focus
import tajsos.composeapp.generated.resources.notes_workspace_item_meta
import tajsos.composeapp.generated.resources.notes_workspace_more
import tajsos.composeapp.generated.resources.notes_workspace_not_selected
import tajsos.composeapp.generated.resources.notes_workspace_select_hint
import tajsos.composeapp.generated.resources.notes_workspace_untitled
import tajsos.composeapp.generated.resources.notes_workspace_word_count

import com.tajemniktv.tajos.ui.components.TactileOutlinedTextField
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
import com.tajemniktv.tajos.ui.components.common.EmptyState
import com.tajemniktv.tajos.ui.platform.toClipEntry
import com.tajemniktv.tajos.ui.theme.TajsOSTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max
import kotlin.time.Instant

import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

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
    draft: com.tajemniktv.tajos.ui.main.state.NoteTextDraft? = null,
    onRetrySave: () -> Unit = {},
    contextToggleEnabled: Boolean = true,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
    ) {
        if (note == null) {
            EmptyState(
                message = stringResource(Res.string.notes_workspace_not_selected),
                description = stringResource(Res.string.notes_workspace_select_hint),
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
                        text = stringResource(Res.string.notes_workspace_breadcrumb, notesDomainLabel(note.domain)),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                    Spacer(Modifier.height(8.dp))
                    TactileOutlinedTextField(
                        value = note.title,
                        onValueChange = onTitleChange,
                        fieldModifier = Modifier.focusRequester(titleFocusRequester),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                        placeholder = { Text(stringResource(Res.string.notes_workspace_untitled)) },
                    )
                    Text(
                        text = stringResource(Res.string.notes_workspace_item_meta, absoluteDateLabel(note.updatedAt), pluralStringResource(Res.plurals.notes_workspace_word_count, wordCount(note.content), wordCount(note.content))),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    NoteSaveStatus(draft) { onRetrySave() }
                    NotesEditorHeaderActions(
                        favorite = note.isFavorite,
                        focusMode = focusMode,
                        contextPanelVisible = contextPanelVisible,
                        onToggleFavorite = onToggleFavorite,
                        onArchive = onArchive,
                        onToggleFocusMode = onToggleFocusMode,
                        onToggleContextPanel = onToggleContextPanel,
                        contextToggleEnabled = contextToggleEnabled,
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
    contextToggleEnabled: Boolean = true,
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
                    contentDescription = stringResource(if (favorite) Res.string.notes_workspace_favorite_remove else Res.string.notes_workspace_favorite_add),
                    tint = if (favorite) TajsOSTheme.Primary else TajsOSTheme.Text,
                )
            }
            IconButton(onClick = onArchive, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Archive, contentDescription = stringResource(Res.string.notes_workspace_archive), tint = TajsOSTheme.Text)
            }
            IconButton(onClick = onToggleFocusMode, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.Default.CenterFocusStrong,
                    contentDescription = stringResource(Res.string.notes_workspace_focus),
                    tint = if (focusMode) TajsOSTheme.Primary else TajsOSTheme.Text,
                )
            }
            IconButton(onClick = onToggleContextPanel, enabled = contextToggleEnabled, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = stringResource(Res.string.notes_workspace_context),
                    tint = if (contextPanelVisible) TajsOSTheme.Primary else TajsOSTheme.Text,
                )
            }
        }
        Box {
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(Res.string.notes_workspace_more))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.notes_workspace_duplicate)) },
                    onClick = {
                        menuOpen = false
                        onDuplicate()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.notes_workspace_copy)) },
                    onClick = {
                        menuOpen = false
                        onCopyContent()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.notes_workspace_delete)) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    },
                )
            }
        }
    }
}

/**
 * Cached regex for splitting text into words by whitespace to avoid repeated instantiations during typing.
 */
private val WHITESPACE_REGEX = Regex("\\s+")

private fun wordCount(content: String): Int =
    content
        .trim()
        .split(WHITESPACE_REGEX)
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
