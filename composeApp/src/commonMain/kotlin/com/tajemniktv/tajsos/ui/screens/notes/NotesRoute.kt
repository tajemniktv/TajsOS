/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.isNoteItem
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.notes_empty
import tajsos.composeapp.generated.resources.notes_empty_desc
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.launch

/**
 * Responsive route for the notes workspace.
 *
 * - Desktop/tablet: 3-pane notes workspace with collapsible right context rail.
 * - Narrow/mobile: list-first flow with detail/editor screen.
 */
@Composable
fun NotesRoute(
    viewModel: MainViewModel,
    onNavigateToNode: (Long) -> Unit,
    initialSelectedNoteId: Long? = null,
) {
    val allNodes by viewModel.allNodes.collectAsState()
    val allRelations by viewModel.allRelations.collectAsState()
    val nodesById = remember(allNodes) { allNodes.associate { it.node.id to it.node } }
    val linkedTaskIndex =
        remember(allRelations, nodesById) { buildLinkedTaskIndex(allRelations, nodesById) }
    val allNotes =
        remember(allNodes, linkedTaskIndex) {
            allNodes
                .filter { it.node.isNoteItem() }
                .map { nodeWithPin ->
                    nodeWithPin.toNotesWorkspaceItem(
                        linkedTaskIds = linkedTaskIndex[nodeWithPin.node.id].orEmpty(),
                        inferredDomain = inferNotesDomain(nodeWithPin),
                    )
                }
        }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var listFilter by rememberSaveable { mutableStateOf(NotesListFilter.ALL) }
    var domainFilter by rememberSaveable { mutableStateOf<NotesDomain?>(null) }
    var sortOrder by rememberSaveable { mutableStateOf(NotesSortOrder.UPDATED) }
    var focusMode by rememberSaveable { mutableStateOf(false) }
    var contextVisible by rememberSaveable { mutableStateOf(true) }
    var selectedNoteId by rememberSaveable { mutableLongStateOf(-1L) }
    var mobileInDetail by rememberSaveable { mutableStateOf(false) }
    var focusTitleSignal by rememberSaveable { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(allNotes) {
        if (selectedNoteId == -1L) {
            selectedNoteId = allNotes.firstOrNull { !it.isArchived }?.id ?: -1L
        } else if (allNotes.none { it.id == selectedNoteId }) {
            selectedNoteId = allNotes.firstOrNull { !it.isArchived }?.id ?: -1L
        }
    }
    LaunchedEffect(initialSelectedNoteId, allNotes) {
        val requestedId = initialSelectedNoteId ?: return@LaunchedEffect
        if (allNotes.any { it.id == requestedId }) {
            selectedNoteId = requestedId
            mobileInDetail = true
        }
    }

    val filteredNotes =
        remember(allNotes, searchQuery, listFilter, domainFilter, sortOrder) {
            val now =
                kotlin.time.Clock.System
                    .now()
                    .toEpochMilliseconds()
            val recentWindow = 7L * 24 * 60 * 60 * 1000
            allNotes
                .asSequence()
                .filter { note ->
                    val query = searchQuery.trim().lowercase()
                    val matchesQuery =
                        query.isBlank() ||
                            note.title.lowercase().contains(query) ||
                            note.content.lowercase().contains(query) ||
                            note.tags.any { it.lowercase().contains(query) }
                    val matchesFilter =
                        when (listFilter)
                        {
                            NotesListFilter.ALL -> !note.isArchived
                            NotesListFilter.PINNED -> !note.isArchived && note.isPinned
                            NotesListFilter.RECENT -> !note.isArchived && (now - note.updatedAt) <= recentWindow
                            NotesListFilter.FAVORITES -> !note.isArchived && note.isFavorite
                            NotesListFilter.ARCHIVE -> note.isArchived
                        }
                    val matchesDomain = domainFilter == null || note.domain == domainFilter
                    matchesQuery && matchesFilter && matchesDomain
                }.sortedWith(
                    when (sortOrder)
                        {
                            NotesSortOrder.UPDATED -> compareByDescending<NotesWorkspaceItem> { it.updatedAt }
                            NotesSortOrder.CREATED -> compareByDescending<NotesWorkspaceItem> { it.createdAt }
                            NotesSortOrder.ALPHABETICAL -> compareBy { it.title.lowercase() }
                        },
                ).toList()
        }

    val selectedNote =
        remember(allNotes, selectedNoteId) { allNotes.find { it.id == selectedNoteId } }
    val selectedAttachments by viewModel
        .getAttachmentsForNode(selectedNote?.id ?: -1L)
        .collectAsState(initial = emptyList())
    val relatedNodes =
        remember(allRelations, nodesById, selectedNoteId) {
            if (selectedNoteId <= 0L) {
                emptyList()
            } else {
                allRelations
                    .asSequence()
                    .mapNotNull { relation ->
                        when (selectedNoteId)
                        {
                            relation.fromNodeId -> nodesById[relation.toNodeId]
                            relation.toNodeId -> nodesById[relation.fromNodeId]
                            else -> null
                        }
                    }.distinctBy { it.id }
                    .toList()
            }
        }

    ScreenScaffold(scrollBehavior = ScreenScrollBehavior.None) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
        val isNarrow = maxWidth < 960.dp

        fun selectNote(noteId: Long) {
            selectedNoteId = noteId
            if (isNarrow) {
                mobileInDetail = true
            }
        }

        fun createNote() {
            scope.launch {
                val newId =
                    viewModel.addNodeForResult(
                        title = "Untitled note",
                        type = "note",
                        inboxState = false,
                    )
                selectedNoteId = newId
                focusTitleSignal += 1
                if (isNarrow) {
                    mobileInDetail = true
                }
            }
        }

        if (isNarrow) {
            if (!mobileInDetail || selectedNote == null) {
                NotesListRail(
                    notes = filteredNotes,
                    selectedNoteId = selectedNoteId.takeIf { it > 0L },
                    searchQuery = searchQuery,
                    activeFilter = listFilter,
                    activeDomain = domainFilter,
                    sortOrder = sortOrder,
                    onCreateNote = { createNote() },
                    onSearchChange = { searchQuery = it },
                    onFilterChange = { listFilter = it },
                    onDomainChange = { domainFilter = it },
                    onSortOrderChange = { sortOrder = it },
                    onSelectNote = { selectNote(it) },
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { mobileInDetail = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                    NotesEditorPane(
                        note = selectedNote,
                        focusMode = focusMode,
                        contextPanelVisible = true,
                        focusTitleSignal = focusTitleSignal,
                        onTitleChange = { viewModel.updateNode(selectedNote.source.copy(title = it)) },
                        onContentChange = { viewModel.updateNode(selectedNote.source.copy(content = it)) },
                        onToggleFavorite = { viewModel.togglePermanentPin(selectedNote.source) },
                        onArchive = {
                            viewModel.archiveNode(selectedNote.source)
                            mobileInDetail = false
                        },
                        onToggleFocusMode = { focusMode = !focusMode },
                        onToggleContextPanel = {},
                        onDuplicate = {
                            scope.launch {
                                val newId =
                                    viewModel.addNodeForResult(
                                        title = "${selectedNote.title} (Copy)",
                                        content = selectedNote.content,
                                        type = "note",
                                        inboxState = false,
                                    )
                                selectedNoteId = newId
                                focusTitleSignal += 1
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(600.dp),
                    )
                    NotesContextPanel(
                        selectedNote = selectedNote,
                        relatedNodes = relatedNodes,
                        attachments = selectedAttachments,
                        onOpenNode = { id ->
                            if (allNotes.any { it.id == id }) {
                                selectNote(id)
                            } else {
                                onNavigateToNode(id)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(420.dp),
                    )
                }
            }
        } else {
            if (focusMode) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    NotesEditorPane(
                        note = selectedNote,
                        focusMode = true,
                        contextPanelVisible = false,
                        focusTitleSignal = focusTitleSignal,
                        onTitleChange = { title ->
                            selectedNote?.let {
                                viewModel.updateNode(
                                    it.source.copy(
                                        title = title,
                                    ),
                                )
                            }
                        },
                        onContentChange = { content ->
                            selectedNote?.let {
                                viewModel.updateNode(
                                    it.source.copy(
                                        content = content,
                                    ),
                                )
                            }
                        },
                        onToggleFavorite = { selectedNote?.let { viewModel.togglePermanentPin(it.source) } },
                        onArchive = { selectedNote?.let { viewModel.archiveNode(it.source) } },
                        onToggleFocusMode = { focusMode = false },
                        onToggleContextPanel = {},
                        onDuplicate = {
                            selectedNote?.let { item ->
                                scope.launch {
                                    val newId =
                                        viewModel.addNodeForResult(
                                            title = "${item.title} (Copy)",
                                            content = item.content,
                                            type = "note",
                                            inboxState = false,
                                        )
                                    selectedNoteId = newId
                                    focusTitleSignal += 1
                                }
                            }
                        },
                        modifier = Modifier.widthIn(max = 960.dp).fillMaxSize(),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                ) {
                    NotesListRail(
                        notes = filteredNotes,
                        selectedNoteId = selectedNoteId.takeIf { it > 0L },
                        searchQuery = searchQuery,
                        activeFilter = listFilter,
                        activeDomain = domainFilter,
                        sortOrder = sortOrder,
                        onCreateNote = { createNote() },
                        onSearchChange = { searchQuery = it },
                        onFilterChange = { listFilter = it },
                        onDomainChange = { domainFilter = it },
                        onSortOrderChange = { sortOrder = it },
                        onSelectNote = { selectNote(it) },
                        modifier = Modifier.width(330.dp),
                    )
                    if (allNotes.isEmpty()) {
                        EmptyState(
                            message = stringResource(Res.string.notes_empty),
                            description = stringResource(Res.string.notes_empty_desc),
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        NotesEditorPane(
                            note = selectedNote,
                            focusMode = false,
                            contextPanelVisible = contextVisible,
                            focusTitleSignal = focusTitleSignal,
                            onTitleChange = { title ->
                                selectedNote?.let {
                                    viewModel.updateNode(
                                        it.source.copy(
                                            title = title,
                                        ),
                                    )
                                }
                            },
                            onContentChange = { content ->
                                selectedNote?.let {
                                    viewModel.updateNode(
                                        it.source.copy(content = content),
                                    )
                                }
                            },
                            onToggleFavorite = { selectedNote?.let { viewModel.togglePermanentPin(it.source) } },
                            onArchive = { selectedNote?.let { viewModel.archiveNode(it.source) } },
                            onToggleFocusMode = { focusMode = true },
                            onToggleContextPanel = { contextVisible = !contextVisible },
                            onDuplicate = {
                                selectedNote?.let { item ->
                                    scope.launch {
                                        val newId =
                                            viewModel.addNodeForResult(
                                                title = "${item.title} (Copy)",
                                                content = item.content,
                                                type = "note",
                                                inboxState = false,
                                            )
                                        selectedNoteId = newId
                                        focusTitleSignal += 1
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (contextVisible) {
                        NotesContextPanel(
                            selectedNote = selectedNote,
                            relatedNodes = relatedNodes,
                            attachments = selectedAttachments,
                            onOpenNode = { id ->
                                if (allNotes.any { it.id == id }) {
                                    selectNote(id)
                                } else {
                                    onNavigateToNode(id)
                                }
                            },
                            modifier = Modifier.width(300.dp),
                        )
                    }
                }
            }
        }
        }
    }
}
