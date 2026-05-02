/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.isNoteItem
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.archive_restore
import tajsos.composeapp.generated.resources.common_area
import tajsos.composeapp.generated.resources.common_edit
import tajsos.composeapp.generated.resources.common_project
import tajsos.composeapp.generated.resources.common_save
import tajsos.composeapp.generated.resources.common_updated
import tajsos.composeapp.generated.resources.detail_attachments
import tajsos.composeapp.generated.resources.detail_content
import tajsos.composeapp.generated.resources.detail_no_attachments_simple
import tajsos.composeapp.generated.resources.detail_no_linked_items
import tajsos.composeapp.generated.resources.detail_no_snapshots
import tajsos.composeapp.generated.resources.detail_relations
import tajsos.composeapp.generated.resources.detail_title
import tajsos.composeapp.generated.resources.notes_action_find_links
import tajsos.composeapp.generated.resources.notes_action_view
import tajsos.composeapp.generated.resources.notes_empty
import tajsos.composeapp.generated.resources.notes_placeholder_search
import tajsos.composeapp.generated.resources.notes_workspace_title
import tajsos.composeapp.generated.resources.screen_history
import tajsos.composeapp.generated.resources.tasks_context_title
import tajsos.composeapp.generated.resources.view_all
import kotlin.time.Instant

/**
 * Notes-first desktop workspace with separated reading/editing modes and contextual side panels.
 *
 * This screen is intentionally scoped to note-like items only and is not meant to become the
 * universal detail surface for all life objects.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun NotesWorkspaceDetail(
    viewModel: MainViewModel,
    noteId: Long,
    onBack: () -> Unit,
    onNavigateToNode: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    val allNodes by viewModel.allNodes.collectAsState()
    val current = remember(allNodes, noteId) { allNodes.find { it.node.id == noteId }?.node }
    val tags by viewModel.getTagsForNode(noteId).collectAsState(initial = emptyList())
    val relations by viewModel.getRelationsForNode(noteId).collectAsState(initial = emptyList())
    val attachments by viewModel.getAttachmentsForNode(noteId).collectAsState(initial = emptyList())
    val snapshots by viewModel.getSnapshotsForNode(noteId).collectAsState(initial = emptyList())
    val areas by viewModel.allAreas.collectAsState()
    val projects by viewModel.allProjects.collectAsState()

    if (current == null) {
        EmptyState(
            message = stringResource(Res.string.notes_empty),
            showContainer = false,
            description = null,
        )
        return
    }

    val note = current
    val noteItems =
        remember(allNodes) {
            allNodes
                .map { it.node }
                .filter { it.isNoteItem() && it.status != "archived" }
                .sortedByDescending { it.updatedAt }
        }

    var query by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var titleDraft by remember(note.id) { mutableStateOf(note.title) }
    var contentDraft by remember(note.id) { mutableStateOf(note.content) }

    LaunchedEffect(note.id, note.updatedAt, isEditMode) {
        if (!isEditMode) {
            titleDraft = note.title
            contentDraft = note.content
        }
    }

    val typeOptions =
        remember(noteItems) {
            noteItems
                .mapNotNull { it.noteType?.trim()?.takeIf(String::isNotBlank) }
                .distinct()
                .sorted()
        }

    val filtered =
        remember(noteItems, query, selectedType) {
            val q = query.trim().lowercase()
            noteItems.filter {
                (selectedType == null || it.noteType == selectedType) &&
                    (
                        q.isBlank() ||
                            it.title.lowercase().contains(q) ||
                            it.content
                                .lowercase()
                                .contains(q)
                    )
            }
        }

    val nodesById = remember(allNodes) { allNodes.associateBy { it.node.id } }
    val linked =
        remember(relations, noteId) {
            relations.mapNotNull { relation ->
                when (noteId) {
                    relation.fromNodeId -> relation.toNodeId
                    relation.toNodeId -> relation.fromNodeId
                    else -> null
                }
            }
        }
    val areaName = areas.find { it.id == note.areaId }?.title ?: "Unassigned"
    val projectName = projects.find { it.id == note.projectId }?.title ?: "None"
    val updatedAt =
        Instant
            .fromEpochMilliseconds(note.updatedAt)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
            .replace("T", " ")

    Row(
        modifier =
            Modifier
                .fillMaxSize()
                .background(TajsOSTheme.Background)
                .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.width(320.dp).fillMaxHeight(),
            color = TajsOSTheme.CardSurface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Text(
                        stringResource(Res.string.notes_workspace_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    label = { Text(stringResource(Res.string.notes_placeholder_search)) },
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { selectedType = null },
                        label = { Text(stringResource(Res.string.view_all)) },
                    )
                    typeOptions.take(8).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = if (selectedType == type) null else type },
                            label = { Text(type.uppercase()) },
                        )
                    }
                }
                HorizontalDivider(color = TajsOSTheme.GhostBorder)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.id }) { item ->
                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToNode(item.id) },
                            shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                            color =
                                if (item.id ==
                                    noteId
                                ) {
                                    TajsOSTheme.Primary.copy(alpha = 0.18f)
                                } else {
                                    TajsOSTheme.Background
                                },
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    item.title,
                                    maxLines = 2,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    (item.noteType ?: "note").uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TajsOSTheme.Muted,
                                )
                            }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            color = TajsOSTheme.CardSurface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { isEditMode = false },
                            label = { Text(stringResource(Res.string.notes_action_view)) },
                            leadingIcon = { Icon(Icons.Default.Visibility, null) },
                        )
                        AssistChip(
                            onClick = { isEditMode = true },
                            label = { Text(stringResource(Res.string.common_edit).uppercase()) },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                        )
                    }
                    if (isEditMode) {
                        AssistChip(
                            onClick = {
                                viewModel.updateNode(
                                    note.copy(
                                        title = titleDraft,
                                        content = contentDraft,
                                    ),
                                )
                                isEditMode = false
                            },
                            label = { Text(stringResource(Res.string.common_save).uppercase()) },
                            leadingIcon = { Icon(Icons.Default.Save, null) },
                        )
                    }
                }

                if (isEditMode) {
                    OutlinedTextField(
                        value = titleDraft,
                        onValueChange = { titleDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.detail_title)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = contentDraft,
                        onValueChange = { contentDraft = it },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        label = { Text(stringResource(Res.string.detail_content)) },
                    )
                } else {
                    Text(
                        note.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text((note.noteType ?: "note").uppercase()) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, null) },
                        )
                        tags.take(6).forEach { tag ->
                            AssistChip(onClick = {}, label = { Text(tag.name) })
                        }
                    }
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                    ) {
                        BasicTextField(
                            value = note.content,
                            onValueChange = {},
                            readOnly = true,
                            textStyle =
                                MaterialTheme.typography.bodyLarge.copy(
                                    color = TajsOSTheme.Text,
                                ),
                            cursorBrush = SolidColor(TajsOSTheme.Primary),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.width(320.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SidePanelCard(
                title = stringResource(Res.string.tasks_context_title),
                icon = Icons.AutoMirrored.Filled.MenuBook,
            ) {
                Text(
                    "${stringResource(Res.string.common_area)}: $areaName",
                    color = TajsOSTheme.Text,
                )
                Text(
                    "${stringResource(Res.string.common_project)}: $projectName",
                    color = TajsOSTheme.Text,
                )
                Text(
                    "${stringResource(Res.string.common_updated)}: $updatedAt",
                    color = TajsOSTheme.Muted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            SidePanelCard(
                title = stringResource(Res.string.detail_relations),
                icon = Icons.Default.Hub,
            ) {
                if (linked.isEmpty()) {
                    EmptyState(
                        message = stringResource(Res.string.detail_no_linked_items),
                        fillParent = false,
                        showContainer = false,
                        description = null,
                    )
                } else {
                    linked.take(6).forEach { id ->
                        val linkedNode = nodesById[id]?.node ?: return@forEach
                        Text(
                            "• ${linkedNode.title}",
                            modifier = Modifier.clickable { onNavigateToNode(linkedNode.id) },
                            color = TajsOSTheme.Text,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                AssistChip(
                    onClick = onNavigateToSearch,
                    label = { Text(stringResource(Res.string.notes_action_find_links)) },
                    leadingIcon = { Icon(Icons.Default.Link, null) },
                )
            }

            SidePanelCard(
                title = stringResource(Res.string.detail_attachments),
                icon = Icons.Default.Attachment,
            ) {
                if (attachments.isEmpty()) {
                    EmptyState(
                        message = stringResource(Res.string.detail_no_attachments_simple),
                        fillParent = false,
                        showContainer = false,
                        description = null,
                    )
                } else {
                    attachments.take(4).forEach {
                        Text("• ${it.title ?: it.uriOrPath}", color = TajsOSTheme.Text)
                    }
                }
            }

            SidePanelCard(
                title = stringResource(Res.string.screen_history),
                icon = Icons.Default.History,
            ) {
                if (snapshots.isEmpty()) {
                    EmptyState(
                        message = stringResource(Res.string.detail_no_snapshots),
                        fillParent = false,
                        showContainer = false,
                        description = null,
                    )
                } else {
                    snapshots.take(4).forEach { snapshot ->
                        val label =
                            Instant
                                .fromEpochMilliseconds(snapshot.timestamp)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .toString()
                                .replace("T", " ")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                label.take(19),
                                color = TajsOSTheme.Text,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            AssistChip(
                                onClick = { viewModel.restoreSnapshot(snapshot) },
                                label = { Text(stringResource(Res.string.archive_restore)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SidePanelCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(icon, null, tint = TajsOSTheme.Primary)
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            content()
        }
    }
}
