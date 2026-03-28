/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.media_article
import tajsos.composeapp.generated.resources.media_book
import tajsos.composeapp.generated.resources.media_link
import tajsos.composeapp.generated.resources.media_other
import tajsos.composeapp.generated.resources.media_podcast
import tajsos.composeapp.generated.resources.media_video
import tajsos.composeapp.generated.resources.notes_empty
import tajsos.composeapp.generated.resources.notes_ideas
import tajsos.composeapp.generated.resources.notes_no_results
import tajsos.composeapp.generated.resources.notes_notes
import tajsos.composeapp.generated.resources.notes_pinned_knowledge
import tajsos.composeapp.generated.resources.notes_resources
import tajsos.composeapp.generated.resources.notes_search_placeholder
import tajsos.composeapp.generated.resources.notes_title
import kotlin.time.Instant

object NotesDashboardBlockRegistry {
    private val renderers: Map<String, NotesDashboardBlockRenderer> =
        mapOf("notes_main" to ::renderNotesMainBlock)

    fun resolve(id: String): NotesDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderNotesMainBlock(context: NotesDashboardContext) {
    NotesMainBlock(viewModel = context.viewModel, onNoteClick = context.onNoteClick)
}

@Composable
internal fun NotesMainBlock(
    viewModel: MainViewModel,
    onNoteClick: (Long) -> Unit,
) {
    val activeNodes by viewModel.activeNodes.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("TYPE") } // TYPE, AREA, PROJECT, DATE, MEDIA

    val knowledgeNodes =
        remember(activeNodes) {
            activeNodes.filter { it.node.type in listOf("note", "idea", "resource", "record") }
        }

    val filteredNodes =
        remember(knowledgeNodes, searchQuery) {
            if (searchQuery.isBlank()) {
                knowledgeNodes
            } else {
                knowledgeNodes.filter {
                    it.node.title.contains(searchQuery, ignoreCase = true) ||
                        it.node.content.contains(searchQuery, ignoreCase = true)
                }
            }
        }

    Column(modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd)) {
        Text(
            stringResource(Res.string.notes_title),
            style = MaterialTheme.typography.displaySmall,
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(Res.string.notes_search_placeholder)) },
            leadingIcon = {
                androidx.compose.material3.Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                )
            },
            shape = MaterialTheme.shapes.medium,
        )

        Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        ) {
            listOf("TYPE", "AREA", "PROJECT", "DATE", "MEDIA").forEach { group ->
                FilterChip(
                    selected = selectedGroup == group,
                    onClick = { selectedGroup = group },
                    label = { Text(group, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }

        val typePinned = remember(filteredNodes) {
            filteredNodes.filter { it.node.isPinned }
        }
        val typeIdeas = remember(filteredNodes) {
            filteredNodes.filter { !it.node.isPinned && it.node.type == "idea" }
        }
        val typeNotes = remember(filteredNodes) {
            filteredNodes.filter { !it.node.isPinned && it.node.type == "note" }
        }
        val typeResources = remember(filteredNodes) {
            filteredNodes.filter { !it.node.isPinned && it.node.type == "resource" }
        }
        val typeRecords = remember(filteredNodes) {
            filteredNodes.filter { !it.node.isPinned && it.node.type == "record" }
        }

        val nodesByArea = remember(filteredNodes) {
            filteredNodes.groupBy { it.node.areaId }
        }

        val nodesByProject = remember(filteredNodes) {
            filteredNodes.groupBy { it.node.projectId }
        }

        val nodesByDate = remember(filteredNodes) {
            filteredNodes.groupBy {
                val instant = Instant.fromEpochMilliseconds(it.node.createdAt)
                instant.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            }
        }

        val nodesByMediaType = remember(filteredNodes) {
            filteredNodes.groupBy { it.node.mediaType }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (selectedGroup)
            {
                "TYPE" -> {
                    if (typePinned.isNotEmpty()) {
                        item { GroupHeader(stringResource(Res.string.notes_pinned_knowledge)) }
                        items(typePinned, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                    if (typeIdeas.isNotEmpty()) {
                        item { GroupHeader(stringResource(Res.string.notes_ideas)) }
                        items(typeIdeas, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                    if (typeNotes.isNotEmpty()) {
                        item { GroupHeader(stringResource(Res.string.notes_notes)) }
                        items(typeNotes, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                    if (typeResources.isNotEmpty()) {
                        item { GroupHeader(stringResource(Res.string.notes_resources)) }
                        items(typeResources, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                    if (typeRecords.isNotEmpty()) {
                        item { GroupHeader("RECORDS") }
                        items(typeRecords, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                }

                "AREA" -> {
                    allAreas.forEach { area ->
                        val nodesInArea = nodesByArea[area.id] ?: emptyList()
                        if (nodesInArea.isNotEmpty()) {
                            item { GroupHeader(area.title.uppercase()) }
                            items(nodesInArea, key = { it.node.id }) { node ->
                                KnowledgeItem(
                                    node,
                                    viewModel,
                                    onNoteClick,
                                )
                            }
                        }
                    }
                    val unassigned = nodesByArea[null] ?: emptyList()
                    if (unassigned.isNotEmpty()) {
                        item { GroupHeader("UNASSIGNED") }
                        items(unassigned, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                }

                "PROJECT" -> {
                    allProjects.forEach { project ->
                        val nodesInProject = nodesByProject[project.id] ?: emptyList()
                        if (nodesInProject.isNotEmpty()) {
                            item { GroupHeader(project.title.uppercase()) }
                            items(nodesInProject, key = { it.node.id }) { node ->
                                KnowledgeItem(
                                    node,
                                    viewModel,
                                    onNoteClick,
                                )
                            }
                        }
                    }
                    val unassigned = nodesByProject[null] ?: emptyList()
                    if (unassigned.isNotEmpty()) {
                        item { GroupHeader("UNASSIGNED") }
                        items(unassigned, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                }

                "DATE" -> {
                    nodesByDate.forEach { (date, nodes) ->
                        item { GroupHeader(date) }
                        items(nodes, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                }

                "MEDIA" -> {
                    val mediaTypes =
                        listOf(
                            "book" to Res.string.media_book,
                            "article" to Res.string.media_article,
                            "podcast" to Res.string.media_podcast,
                            "video" to Res.string.media_video,
                            "link" to Res.string.media_link,
                        )
                    mediaTypes.forEach { (type, res) ->
                        val nodesOfType = nodesByMediaType[type] ?: emptyList()
                        if (nodesOfType.isNotEmpty()) {
                            item { GroupHeader(stringResource(res)) }
                            items(nodesOfType, key = { it.node.id }) { node ->
                                KnowledgeItem(
                                    node,
                                    viewModel,
                                    onNoteClick,
                                )
                            }
                        }
                    }
                    val other = (nodesByMediaType[null] ?: emptyList()).filter { it.node.type == "resource" }
                    if (other.isNotEmpty()) {
                        item { GroupHeader(stringResource(Res.string.media_other)) }
                        items(other, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                }
            }

            if (filteredNodes.isEmpty()) {
                item {
                    EmptyState(
                        message =
                            if (searchQuery.isEmpty()) {
                                stringResource(Res.string.notes_empty)
                            } else {
                                stringResource(
                                    Res.string.notes_no_results,
                                )
                            },
                    )
                }
            }
        }
    }
}
