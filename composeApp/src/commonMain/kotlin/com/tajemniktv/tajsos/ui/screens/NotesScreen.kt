/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.components.nodes.NodeCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Instant

@Composable
fun NotesScreen(
    viewModel: MainViewModel,
    onNoteClick: (Long) -> Unit,
)
{
    val activeNodes by viewModel.activeNodes.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("TYPE") } // TYPE, AREA, PROJECT, DATE, MEDIA

    val knowledgeNodes = remember(activeNodes) {
        activeNodes.filter { it.node.type in listOf("note", "idea", "resource") }
    }

    val filteredNodes =
            remember(knowledgeNodes, searchQuery) {
                if (searchQuery.isBlank())
                {
                    knowledgeNodes
                } else
                {
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
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (selectedGroup)
            {
                "TYPE"    ->
                {
                    val pinned = filteredNodes.filter { it.node.isPinned }
                    val ideas = filteredNodes.filter { !it.node.isPinned && it.node.type == "idea" }
                    val notes = filteredNodes.filter { !it.node.isPinned && it.node.type == "note" }
                    val resources =
                            filteredNodes.filter { !it.node.isPinned && it.node.type == "resource" }

                    if (pinned.isNotEmpty())
                    {
                        item { GroupHeader(stringResource(Res.string.notes_pinned_knowledge)) }
                        items(pinned, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                    if (ideas.isNotEmpty())
                    {
                        item { GroupHeader(stringResource(Res.string.notes_ideas)) }
                        items(ideas, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                    if (notes.isNotEmpty())
                    {
                        item { GroupHeader(stringResource(Res.string.notes_notes)) }
                        items(notes, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                    if (resources.isNotEmpty())
                    {
                        item { GroupHeader(stringResource(Res.string.notes_resources)) }
                        items(resources, key = { it.node.id }) { node ->
                            KnowledgeItem(
                                node,
                                viewModel,
                                onNoteClick,
                            )
                        }
                    }
                }

                "AREA"    ->
                {
                    allAreas.forEach { area ->
                        val nodesInArea = filteredNodes.filter { it.node.areaId == area.id }
                        if (nodesInArea.isNotEmpty())
                        {
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
                    val unassigned = filteredNodes.filter { it.node.areaId == null }
                    if (unassigned.isNotEmpty())
                    {
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

                "PROJECT" ->
                {
                    allProjects.forEach { project ->
                        val nodesInProject =
                                filteredNodes.filter { it.node.projectId == project.id }
                        if (nodesInProject.isNotEmpty())
                        {
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
                    val unassigned = filteredNodes.filter { it.node.projectId == null }
                    if (unassigned.isNotEmpty())
                    {
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

                "DATE"    ->
                {
                    val groupedByDate = filteredNodes.groupBy {
                        val instant = Instant.fromEpochMilliseconds(it.node.createdAt)
                        val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                        date.toString()
                    }
                    groupedByDate.forEach { (date, nodes) ->
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

                "MEDIA"   ->
                {
                    val mediaTypes = listOf(
                        "book" to Res.string.media_book,
                        "article" to Res.string.media_article,
                        "podcast" to Res.string.media_podcast,
                        "video" to Res.string.media_video,
                        "link" to Res.string.media_link,
                    )
                    mediaTypes.forEach { (type, res) ->
                        val nodesOfType = filteredNodes.filter { it.node.mediaType == type }
                        if (nodesOfType.isNotEmpty())
                        {
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
                    val other =
                            filteredNodes.filter { it.node.type == "resource" && it.node.mediaType == null }
                    if (other.isNotEmpty())
                    {
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

            if (filteredNodes.isEmpty())
            {
                item {
                    EmptyState(
                        message = if (searchQuery.isEmpty()) stringResource(Res.string.notes_empty) else stringResource(
                            Res.string.notes_no_results,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun GroupHeader(title: String)
{
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = TactileTheme.Primary,
        modifier = Modifier.padding(top = TactileTheme.SpacingMd, bottom = TactileTheme.SpacingSm),
    )
}

@Composable
fun KnowledgeItem(
    node: com.tajemniktv.tajsos.data.NodeWithPin,
    viewModel: MainViewModel,
    onNoteClick: (Long) -> Unit,
)
{
    NodeCard(
        nodeWithPin = node,
        onClick = { onNoteClick(node.node.id) },
        onToggleDone = { status ->
            viewModel.updateNodeStatus(
                node.node,
                status,
            )
        },
        onTogglePin = { isPinned -> viewModel.togglePin(node.node, isPinned) },
        onArchive = { viewModel.archiveNode(node.node) },
    )
}
