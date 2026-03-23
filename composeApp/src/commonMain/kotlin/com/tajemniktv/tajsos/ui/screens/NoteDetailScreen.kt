/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.*
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.time.Clock

/**
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    /**
     *
     */
    viewModel: MainViewModel,
    /**
     *
     */
    noteId: Long,
    /**
     *
     */
    onBack: () -> Unit,
    /**
     *
     */
    onNavigateToNode: (Long) -> Unit,
    /**
     *
     */
    onNavigateToSearch: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val nodes by viewModel.allNodes.collectAsState()
    val nodeWithPin = remember(nodes, noteId) { nodes.find { it.node.id == noteId } }

    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (nodes.isEmpty()) {
                CircularProgressIndicator(color = TactileTheme.Primary)
            } else {
                Text(
                    stringResource(Res.string.detail_not_found, noteId),
                    color = TactileTheme.Muted
                )
            }
        }
        return
    }

    val node = nodeWithPin.node
    var title by remember { mutableStateOf(node.title) }
    var content by remember { mutableStateOf(node.content) }

    val tags by viewModel.getTagsForNode(noteId).collectAsState(initial = emptyList())
    val allTags by viewModel.allTags.collectAsState()
    val relations by viewModel.getRelationsForNode(noteId).collectAsState(initial = emptyList())
    val attachments by viewModel.getAttachmentsForNode(noteId).collectAsState(initial = emptyList())
    val areas by viewModel.allAreas.collectAsState()
    val projects by viewModel.allProjects.collectAsState()
    val snapshots by viewModel.getSnapshotsForNode(noteId).collectAsState(initial = emptyList())

    var showTagDialog by remember { mutableStateOf(false) }
    var showRelationDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showAreaDialog by remember { mutableStateOf(false) }
    var showProjectDialog by remember { mutableStateOf(false) }
    var showNoteTypeDialog by remember { mutableStateOf(false) }
    var showNoteStateDialog by remember { mutableStateOf(false) }
    var showSnapshotDialog by remember { mutableStateOf(false) }
    var showMergeDialog by remember { mutableStateOf(false) }
    var isAtomicMode by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showDueDialog by remember { mutableStateOf(false) }
    var showEstimateDialog by remember { mutableStateOf(false) }
    var showPostponeDialog by remember { mutableStateOf(false) }
    var showWhyDialog by remember { mutableStateOf(false) }
    var showMediaTypeDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }

    val suggestions by viewModel.getNoteSuggestions(noteId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val typeLabel = when (node.type) {
                        "task" -> stringResource(Res.string.type_task)
                        "note" -> stringResource(Res.string.type_note)
                        "idea" -> stringResource(Res.string.type_idea)
                        "project" -> stringResource(Res.string.type_project)
                        "area" -> stringResource(Res.string.type_area)
                        else -> node.type
                    }
                    Text(typeLabel.uppercase(), style = MaterialTheme.typography.labelSmall)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.detail_back)
                        )
                    }
                },
                actions = {
                    if (node.type == "note" || node.type == "idea") {
                        IconButton(onClick = { isAtomicMode = !isAtomicMode }) {
                            Icon(
                                if (isAtomicMode) Icons.Default.ZoomInMap else Icons.Default.ZoomOutMap,
                                contentDescription = stringResource(Res.string.detail_atomic_mode),
                                tint = if (isAtomicMode) TactileTheme.Primary else TactileTheme.Muted
                            )
                        }
                        IconButton(onClick = {
                            viewModel.createSnapshot(noteId)
                        }) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = stringResource(Res.string.detail_create_snapshot),
                                tint = TactileTheme.Primary
                            )
                        }
                        IconButton(onClick = { showSnapshotDialog = true }) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = stringResource(Res.string.detail_versions),
                                tint = TactileTheme.Muted
                            )
                        }
                        IconButton(onClick = { showMergeDialog = true }) {
                            Icon(
                                Icons.Default.Merge,
                                contentDescription = stringResource(Res.string.detail_merge)
                            )
                        }
                    }
                    IconButton(onClick = {
                        viewModel.togglePermanentPin(node)
                    }) {
                        Icon(
                            if (node.isPinned) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(Res.string.detail_favorite),
                            tint = if (node.isPinned) TactileTheme.Primary else TactileTheme.Muted,
                        )
                    }
                    if (node.inboxState) {
                        IconButton(onClick = {
                            viewModel.markAsProcessed(noteId)
                        }) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = stringResource(Res.string.detail_process),
                                tint = TactileTheme.Primary
                            )
                        }
                    }
                    if (node.type == "note" || node.type == "idea" || node.type == "task") {
                        IconButton(onClick = {
                            scope.launch {
                                viewModel.getNodeById(noteId)?.let { original ->
                                    val targetType = when (original.type) {
                                        "note", "idea" -> {
                                            // If it's already an idea/note, it can become a task or project seed
                                            if (original.noteType == "idea") "project" else "task"
                                        }
                                        "task" -> "project"
                                        else -> original.type
                                    }
                                    viewModel.updateNode(original.copy(type = targetType))
                                }
                            }
                        }) {
                            val icon = when (node.type) {
                                "note" -> Icons.Default.CheckCircle
                                "idea" -> Icons.Default.AccountTree
                                "task" -> Icons.AutoMirrored.Filled.List
                                else -> Icons.Default.Transform
                            }
                            Icon(
                                icon,
                                contentDescription = stringResource(Res.string.detail_convert)
                            )
                        }
                    }
                    if (node.type == "task") {
                        IconButton(onClick = {
                            scope.launch {
                                viewModel.getNodeById(noteId)?.let { original ->
                                    viewModel.addNode(
                                        title = original.title,
                                        content = original.content,
                                        type = original.type,
                                        projectId = original.projectId,
                                        areaId = original.areaId,
                                        inboxState = false
                                    )
                                }
                            }
                        }) {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = stringResource(Res.string.detail_repeat)
                            )
                        }
                    }
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.getNodeById(noteId)?.let { original ->
                                viewModel.addNode(
                                    title = "${original.title} (COPY)",
                                    content = original.content,
                                    type = original.type,
                                    projectId = original.projectId,
                                    areaId = original.areaId,
                                )
                            }
                        }
                    }) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = stringResource(Res.string.detail_duplicate)
                        )
                    }
                    IconButton(onClick = {
                        viewModel.archiveNode(node)
                        onBack()
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.detail_archive)
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(TactileTheme.SpacingMd)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
        ) {
            BasicTextField(
                value = title,
                onValueChange = {
                    title = it
                    viewModel.updateNode(node.copy(title = it))
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = TactileTheme.Text),
                cursorBrush = SolidColor(TactileTheme.Primary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            stringResource(Res.string.detail_untitled),
                            style = MaterialTheme.typography.headlineMedium,
                            color = TactileTheme.Muted
                        )
                    }
                    innerTextField()
                },
            )

            // Tags Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(tags) { tag ->
                    InputChip(
                        selected = false,
                        onClick = {
                            viewModel.updateSearchQuery("#${tag.name}")
                            onNavigateToSearch()
                        },
                        label = { Text(tag.name) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(Res.string.detail_remove_tag),
                                modifier =
                                    Modifier
                                        .size(16.dp)
                                        .clickable { viewModel.detachTagFromNode(noteId, tag.id) },
                            )
                        },
                    )
                }
                item {
                    IconButton(onClick = { showTagDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(Res.string.detail_add_tag),
                            tint = TactileTheme.Primary
                        )
                    }
                }
            }

            HorizontalDivider(color = TactileTheme.Muted.copy(alpha = 0.2f))

            if (node.type == "task") {
                Text(
                    stringResource(Res.string.detail_next_step),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                BasicTextField(
                    value = node.nextSmallestStep ?: "",
                    onValueChange = {
                        viewModel.updateNode(node.copy(nextSmallestStep = it))
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TactileTheme.Accent),
                    cursorBrush = SolidColor(TactileTheme.Accent),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) {
                                if (node.nextSmallestStep.isNullOrEmpty()) {
                                    Text(
                                        stringResource(Res.string.detail_next_step_placeholder),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TactileTheme.Muted
                                    )
                                }
                                innerTextField()
                            }
                            if (node.nextSmallestStep.isNullOrBlank() && node.content.isNotBlank()) {
                                IconButton(
                                    onClick = { viewModel.extractNextStep(noteId) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AutoFixHigh,
                                        contentDescription = stringResource(Res.string.detail_auto_extract),
                                        tint = TactileTheme.Accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    },
                )
                Spacer(Modifier.height(TactileTheme.SpacingSm))
                HorizontalDivider(color = TactileTheme.Muted.copy(alpha = 0.1f))
            }

            if (node.status == "done") {
                Text(
                    stringResource(Res.string.detail_completion_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Success
                )
                BasicTextField(
                    value = node.completionNote ?: "",
                    onValueChange = {
                        viewModel.updateNode(node.copy(completionNote = it))
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TactileTheme.Success),
                    cursorBrush = SolidColor(TactileTheme.Success),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (node.completionNote.isNullOrEmpty()) {
                            Text(
                                stringResource(Res.string.detail_completion_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TactileTheme.Muted
                            )
                        }
                    },
                )
                Spacer(Modifier.height(TactileTheme.SpacingSm))
                HorizontalDivider(color = TactileTheme.Muted.copy(alpha = 0.1f))
            }

            BasicTextField(
                value = content,
                onValueChange = {
                    content = it
                    viewModel.updateNode(node.copy(content = it))
                },
                textStyle = if (isAtomicMode) {
                    MaterialTheme.typography.titleLarge.copy(
                        color = TactileTheme.Text,
                        lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
                    )
                } else {
                    MaterialTheme.typography.bodyLarge.copy(color = TactileTheme.Text)
                },
                cursorBrush = SolidColor(TactileTheme.Primary),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = if (isAtomicMode) 100.dp else 200.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (content.isEmpty()) {
                            Text(
                                stringResource(Res.string.detail_start_writing),
                                style = MaterialTheme.typography.bodyLarge,
                                color = TactileTheme.Muted
                            )
                        }
                        innerTextField()

                        if (node.type == "task" && content.lines()
                                .any { it.trim().startsWith("-") || it.trim().startsWith("*") }
                        ) {
                            IconButton(
                                onClick = { viewModel.splitIntoSubtasks(noteId) },
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.CallSplit,
                                    contentDescription = stringResource(Res.string.detail_split_subtasks),
                                    tint = TactileTheme.Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if ((node.type == "note" || node.type == "idea") && content.contains("# ")) {
                            IconButton(
                                onClick = {
                                    viewModel.splitNote(noteId)
                                    onBack()
                                },
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.DashboardCustomize,
                                    contentDescription = stringResource(Res.string.detail_split_note),
                                    tint = TactileTheme.Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                },
            )

            // Relations Section
            if (relations.isNotEmpty() || suggestions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(Res.string.detail_relationship_inspector),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary
                    )
                    TextButton(onClick = {
                        viewModel.clearSearchFilters()
                        viewModel.updateSearchLinkedToFilter(noteId)
                        onNavigateToSearch()
                    }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(Res.string.detail_find_all),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                val nodesMap = remember(nodes) { nodes.associateBy { it.node.id } }

                val forwardLinks = relations.filter { it.fromNodeId == noteId }
                val backLinks = relations.filter { it.toNodeId == noteId }

                // Group by type
                val groupedForward = forwardLinks.groupBy { it.relationType }

                groupedForward.forEach { (type, typeRelations) ->
                    Text(
                        type,
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                    typeRelations.forEach { relation ->
                        val relatedId = relation.toNodeId
                        val relatedNode = nodesMap[relatedId]?.node
                        if (relatedNode != null) {
                            RelationshipItem(relatedNode, type) { onNavigateToNode(relatedId) }
                        }
                    }
                }

                if (backLinks.isNotEmpty()) {
                    Text(
                        stringResource(Res.string.detail_backlinks),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Accent
                    )
                    backLinks.forEach { relation ->
                        val relatedId = relation.fromNodeId
                        val relatedNode = nodesMap[relatedId]?.node
                        if (relatedNode != null) {
                            RelationshipItem(
                                relatedNode,
                                "INCOMING"
                            ) { onNavigateToNode(relatedId) }
                        }
                    }
                }
            }

            // Suggestions Section
            if ((node.type == "note" || node.type == "idea") && suggestions.isNotEmpty()) {
                Spacer(Modifier.height(TactileTheme.SpacingSm))
                Text(
                    stringResource(Res.string.detail_suggestions),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted
                )
                suggestions.forEach { suggestion ->
                    Surface(
                        onClick = { onNavigateToNode(suggestion.node.id) },
                        color = TactileTheme.Surface.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (suggestion.node.type) {
                                    "idea" -> Icons.Default.Lightbulb
                                    "project" -> Icons.Default.AccountTree
                                    else -> Icons.Default.Description
                                },
                                contentDescription = null,
                                tint = TactileTheme.Muted,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                suggestion.node.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = TactileTheme.Muted,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    viewModel.addRelation(
                                        noteId,
                                        suggestion.node.id,
                                        "RELATED"
                                    )
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.AddLink,
                                    contentDescription = stringResource(Res.string.detail_link_node),
                                    tint = TactileTheme.Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    showRelationDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = TactileTheme.Surface,
                        contentColor = TactileTheme.Primary,
                    ),
            ) {
                Icon(Icons.Default.Link, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.detail_link_node))
            }

            // Attachments Section
            if (attachments.isNotEmpty()) {
                Text(
                    stringResource(Res.string.detail_attachments),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                attachments.forEach { attachment ->
                    ListItem(
                        headlineContent = { Text(attachment.title ?: attachment.uriOrPath) },
                        supportingContent = { Text(attachment.assetType) },
                        leadingContent = { Icon(Icons.Default.FilePresent, contentDescription = null) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteAttachment(attachment) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(Res.string.detail_remove_attachment),
                                    tint = TactileTheme.Error,
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                    )
                }
            }
            Button(
                onClick = {
                    viewModel.addAttachment(noteId, "URL", "https://example.com", "Example Link")
                },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = TactileTheme.Surface,
                        contentColor = TactileTheme.Primary,
                    ),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.detail_add_link))
            }

            Spacer(modifier = Modifier.height(TactileTheme.SpacingLg))

            if (node.type == "note" || node.type == "idea") {
                Text(
                    stringResource(Res.string.detail_knowledge),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )

                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_note_type)) },
                    supportingContent = { Text((node.noteType ?: "standard").uppercase()) },
                    modifier = Modifier.clickable { showNoteTypeDialog = true },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                    trailingContent = {
                        Icon(
                            when (node.noteType) {
                                "thought" -> Icons.Default.Psychology
                                "idea" -> Icons.Default.Lightbulb
                                "lecture" -> Icons.Default.School
                                "research" -> Icons.Default.Search
                                "reflection" -> Icons.Default.SelfImprovement
                                "bug" -> Icons.Default.BugReport
                                "concept" -> Icons.Default.Category
                                "evergreen" -> Icons.Default.Park
                                else -> Icons.Default.Description
                            },
                            contentDescription = null,
                            tint = TactileTheme.Primary
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_progressive_state)) },
                    supportingContent = { Text((node.noteState ?: "raw").uppercase()) },
                    modifier = Modifier.clickable { showNoteStateDialog = true },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                    trailingContent = {
                        Icon(
                            when (node.noteState) {
                                "raw" -> Icons.Default.Description
                                "highlighted" -> Icons.Default.FormatQuote
                                "distilled" -> Icons.Default.FilterAlt
                                "takeaway" -> Icons.Default.Star
                                else -> Icons.Default.HistoryEdu
                            },
                            contentDescription = null,
                            tint = TactileTheme.Accent
                        )
                    }
                )

                val daysSinceUpdate = (Clock.System.now()
                    .toEpochMilliseconds() - node.updatedAt) / (1000 * 60 * 60 * 24)
                if (daysSinceUpdate > 30) {
                    Surface(
                        color = TactileTheme.Error.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = TactileTheme.Error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(Res.string.detail_stale_warning, daysSinceUpdate),
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
            }

            if (node.type == "resource") {
                Text(
                    stringResource(Res.string.detail_media),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )

                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_media_type)) },
                    supportingContent = { Text((node.mediaType ?: "link").uppercase()) },
                    modifier = Modifier.clickable { showMediaTypeDialog = true },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                    trailingContent = {
                        Icon(
                            when (node.mediaType) {
                                "book" -> Icons.AutoMirrored.Filled.LibraryBooks
                                "article" -> Icons.Default.Description
                                "podcast" -> Icons.Default.Podcasts
                                "video" -> Icons.Default.PlayCircle
                                else -> Icons.Default.Link
                            },
                            contentDescription = null,
                            tint = TactileTheme.Primary
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_author)) },
                    supportingContent = {
                        BasicTextField(
                            value = node.author ?: "",
                            onValueChange = { viewModel.updateNode(node.copy(author = it)) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TactileTheme.Text),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                )

                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_publisher)) },
                    supportingContent = {
                        BasicTextField(
                            value = node.publisher ?: "",
                            onValueChange = { viewModel.updateNode(node.copy(publisher = it)) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TactileTheme.Text),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                )

                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_rating)) },
                    supportingContent = {
                        Text(
                            if (node.rating != null) stringResource(
                                Res.string.detail_stars,
                                node.rating!!
                            )
                            else stringResource(Res.string.detail_not_set)
                        )
                    },
                    modifier = Modifier.clickable { showRatingDialog = true },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                    trailingContent = {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = TactileTheme.Primary
                        )
                    }
                )

                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
            }

            Text(
                stringResource(Res.string.detail_organization),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )

            ListItem(
                headlineContent = { Text(stringResource(Res.string.detail_status)) },
                supportingContent = { Text(node.status.uppercase()) },
                modifier = Modifier.clickable { showStatusDialog = true },
                colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
            )

            if (node.type == "task") {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_energy_required)) },
                    supportingContent = {
                        Text(
                            when (node.energyLevel) {
                                1 -> stringResource(Res.string.detail_energy_low)
                                2 -> stringResource(Res.string.detail_energy_med)
                                3 -> stringResource(Res.string.detail_energy_high)
                                else -> stringResource(Res.string.detail_not_set)
                            }
                        )
                    },
                    modifier = Modifier.clickable {
                        val nextLevel = ((node.energyLevel ?: 0) % 3) + 1
                        viewModel.updateNode(node.copy(energyLevel = nextLevel))
                    },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                    trailingContent = {
                        Icon(
                            Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = when (node.energyLevel) {
                                1 -> TactileTheme.Success
                                2 -> TactileTheme.Primary
                                3 -> TactileTheme.Error
                                else -> TactileTheme.Muted
                            }
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_task_friction)) },
                    supportingContent = {
                        Text(
                            (node.friction ?: stringResource(Res.string.detail_not_set)).uppercase()
                                .replace("_", " ")
                        )
                    },
                    modifier = Modifier.clickable {
                        val frictions = listOf("easy", "annoying", "mentally_heavy", "unclear")
                        val currentIdx = frictions.indexOf(node.friction)
                        val nextFriction = frictions[(currentIdx + 1) % frictions.size]
                        viewModel.updateNode(node.copy(friction = nextFriction))
                    },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                    trailingContent = {
                        Icon(
                            when (node.friction) {
                                "easy" -> Icons.Default.Mood
                                "annoying" -> Icons.Default.SentimentDissatisfied
                                "mentally_heavy" -> Icons.Default.Psychology
                                "unclear" -> Icons.Default.QuestionMark
                                else -> Icons.Default.Edit
                            },
                            contentDescription = null,
                            tint = TactileTheme.Primary
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_time_estimate)) },
                    supportingContent = {
                        Text(node.estimatedMinutes?.let {
                            stringResource(
                                Res.string.detail_estimate_mins,
                                it
                            )
                        } ?: stringResource(Res.string.detail_not_set))
                    },
                    modifier = Modifier.clickable { showEstimateDialog = true },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                    trailingContent = {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = TactileTheme.Primary
                        )
                    }
                )

                if (node.postponeCount > 0) {
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.detail_postpone_count)) },
                        supportingContent = {
                            Text(
                                stringResource(
                                    Res.string.detail_postpone_times,
                                    node.postponeCount
                                )
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                        trailingContent = {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = TactileTheme.Error
                            )
                        }
                    )
                }
            }

            // Area & Project Selection
            val area = areas.find { it.id == node.areaId }
            val project = projects.find { it.id == node.projectId }

            if (node.type != "area") {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_area)) },
                    supportingContent = {
                        Text(
                            area?.title ?: stringResource(Res.string.detail_unassigned)
                        )
                    },
                    modifier = Modifier.clickable { showAreaDialog = true },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                )
            }

            if (node.type != "area" && node.type != "project") {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_project)) },
                    supportingContent = {
                        Text(
                            project?.title ?: stringResource(Res.string.detail_none)
                        )
                    },
                    modifier = Modifier.clickable { showProjectDialog = true },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                )
            }

            if (node.type == "project") {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_project_why)) },
                    supportingContent = {
                        Text(
                            node.projectWhy ?: stringResource(Res.string.detail_project_purpose)
                        )
                    },
                    modifier = Modifier.clickable { showWhyDialog = true },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                )
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_project_status)) },
                    supportingContent = { Text((node.projectStatus ?: "active").uppercase()) },
                    modifier = Modifier.clickable {
                        val statuses =
                            listOf("active", "slowing_down", "neglected", "exploratory", "on_hold")
                        val currentIdx = statuses.indexOf(node.projectStatus ?: "active")
                        val nextStatus = statuses[(currentIdx + 1) % statuses.size]
                        viewModel.updateNode(node.copy(projectStatus = nextStatus))
                    },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                )
            }

            if (node.type == "task") {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.detail_hard_deadline)) },
                    supportingContent = {
                        Text(
                            if (node.isHardDeadline) stringResource(Res.string.detail_critical) else stringResource(
                                Res.string.detail_soft
                            )
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = node.isHardDeadline,
                            onCheckedChange = { viewModel.updateNode(node.copy(isHardDeadline = it)) }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                )
            }

            // Due Date
            ListItem(
                headlineContent = { Text(stringResource(Res.string.detail_due_at)) },
                supportingContent = {
                    Text(
                        node.dueAt?.let {
                            kotlin.time.Instant
                                .fromEpochMilliseconds(it)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                                .toString()
                        } ?: stringResource(Res.string.detail_no_due_date),
                    )
                },
                modifier = Modifier.clickable { showDueDialog = true },
                trailingContent = {
                    Row {
                        if (node.dueAt != null) {
                            IconButton(onClick = {
                                val nextDay = node.dueAt!! + (24 * 60 * 60 * 1000L)
                                viewModel.updateNode(node.copy(dueAt = nextDay))
                                showPostponeDialog = true
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "+1d"
                                )
                            }
                            IconButton(onClick = { viewModel.updateNode(node.copy(dueAt = null)) }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = stringResource(Res.string.search_clear)
                                )
                            }
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
            )

            // Reminder & Recurrence
            ListItem(
                headlineContent = { Text(stringResource(Res.string.detail_reminder_at)) },
                supportingContent = {
                    Text(
                        node.reminderAt?.let {
                            kotlin.time.Instant
                                .fromEpochMilliseconds(it)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .toString()
                                .replace("T", " ")
                        } ?: stringResource(Res.string.detail_no_reminder),
                    )
                },
                modifier = Modifier.clickable { showReminderDialog = true },
                trailingContent = {
                    if (node.reminderAt != null) {
                        IconButton(onClick = { viewModel.updateNode(node.copy(reminderAt = null)) }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(Res.string.search_clear)
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
            )

            ListItem(
                headlineContent = { Text(stringResource(Res.string.capture_recurring)) },
                supportingContent = {
                    Text(
                        if (node.isRecurring) stringResource(
                            Res.string.detail_recurrence_interval,
                            node.recurringInterval ?: ""
                        ) else stringResource(Res.string.detail_one_time)
                    )
                },
                modifier =
                    Modifier.clickable {
                        viewModel.updateNode(
                            node.copy(
                                isRecurring = !node.isRecurring,
                                recurringInterval = if (!node.isRecurring) "DAILY" else null,
                            ),
                        )
                    },
                colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
            )

            val updatedDate =
                kotlin.time.Instant
                    .fromEpochMilliseconds(
                        node.updatedAt,
                    ).toLocalDateTime(TimeZone.currentSystemDefault())
                    .toString()
            Text(
                text = stringResource(Res.string.detail_last_updated, updatedDate),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = TactileTheme.SpacingMd),
            )
        }
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text(stringResource(Res.string.detail_tag_dialog_title)) },
            text = {
                Column {
                    allTags.forEach { tag ->
                        ListItem(
                            headlineContent = { Text(tag.name) },
                            modifier =
                                Modifier.clickable {
                                    viewModel.attachTagToNode(noteId, tag.id)
                                    showTagDialog = false
                                },
                        )
                    }
                    var newTagName by remember { mutableStateOf("") }
                    TextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        placeholder = { Text(stringResource(Res.string.detail_tag_new_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = {
                        if (newTagName.isNotBlank()) {
                            viewModel.addTag(newTagName)
                            newTagName = ""
                        }
                    }) { Text(stringResource(Res.string.detail_tag_create)) }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text(
                        stringResource(
                            Res.string.common_back
                        )
                    )
                }
            },
        )
    }

    if (showRelationDialog) {
        val linkedNodeIds =
            remember(relations, noteId) {
                relations.map { if (it.fromNodeId == noteId) it.toNodeId else it.fromNodeId }
                    .toSet()
            }
        var selectedTypeForRelation by remember { mutableStateOf("RELATED") }
        AlertDialog(
            onDismissRequest = { showRelationDialog = false },
            title = { Text(stringResource(Res.string.detail_relation_dialog_title)) },
            text = {
                Column {
                    val relationTypes =
                        listOf(
                            "RELATED",
                            "DEPENDS_ON",
                            "BELONGS_TO",
                            "MENTION",
                            "INSPIRED_BY",
                            "REFERENCE"
                        )
                    LazyRow(
                        modifier = Modifier.padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(relationTypes) { type ->
                            FilterChip(
                                selected = selectedTypeForRelation == type,
                                onClick = { selectedTypeForRelation = type },
                                label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    nodes
                        .filter { it.node.id != noteId && it.node.id !in linkedNodeIds }
                        .take(10)
                        .forEach { nodeWithPin ->
                            ListItem(
                                headlineContent = { Text(nodeWithPin.node.title) },
                                supportingContent = {
                                    val typeLabel = when (nodeWithPin.node.type) {
                                        "task" -> stringResource(Res.string.type_task)
                                        "note" -> stringResource(Res.string.type_note)
                                        "idea" -> stringResource(Res.string.type_idea)
                                        "project" -> stringResource(Res.string.type_project)
                                        "area" -> stringResource(Res.string.type_area)
                                        else -> nodeWithPin.node.type
                                    }
                                    Text(typeLabel)
                                },
                                modifier =
                                    Modifier.clickable {
                                        viewModel.addRelation(
                                            noteId,
                                            nodeWithPin.node.id,
                                            selectedTypeForRelation
                                        )
                                        showRelationDialog = false
                                    },
                            )
                        }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showRelationDialog = false
                }) { Text(stringResource(Res.string.projects_dialog_cancel)) }
            },
        )
    }

    if (showStatusDialog) {
        val statuses = listOf("active", "done", "archived", "on_hold", "someday", "blocked")
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text(stringResource(Res.string.detail_status_dialog_title)) },
            text = {
                Column {
                    statuses.forEach { status ->
                        ListItem(
                            headlineContent = { Text(status.uppercase()) },
                            modifier =
                                Modifier.clickable {
                                    viewModel.updateNodeStatus(node, status)
                                    showStatusDialog = false
                                },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showStatusDialog = false
                }) { Text(stringResource(Res.string.projects_dialog_cancel)) }
            },
        )
    }

    if (showNoteTypeDialog) {
        val types = listOf(
            "thought",
            "lecture",
            "research",
            "idea",
            "reflection",
            "bug",
            "concept",
            "evergreen",
            "read_later",
            "quote",
            "meeting",
            "reading",
            "journal"
        )
        AlertDialog(
            onDismissRequest = { showNoteTypeDialog = false },
            title = { Text(stringResource(Res.string.detail_type_dialog_title)) },
            text = {
                Column {
                    types.forEach { type ->
                        ListItem(
                            headlineContent = { Text(type.uppercase().replace("_", " ")) },
                            modifier = Modifier.clickable {
                                viewModel.updateNode(node.copy(noteType = type))
                                showNoteTypeDialog = false
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNoteTypeDialog = false }) {
                    Text(
                        stringResource(Res.string.projects_dialog_cancel)
                    )
                }
            },
        )
    }

    if (showSnapshotDialog) {
        AlertDialog(
            onDismissRequest = { showSnapshotDialog = false },
            title = { Text(stringResource(Res.string.detail_snapshot_dialog_title)) },
            text = {
                LazyColumn {
                    items(snapshots) { snapshot ->
                        ListItem(
                            headlineContent = {
                                Text(snapshot.timestamp.let {
                                    kotlin.time.Instant.fromEpochMilliseconds(it).toString()
                                })
                            },
                            supportingContent = { Text(snapshot.content.take(50) + "...") },
                            modifier = Modifier.clickable {
                                viewModel.restoreSnapshot(snapshot)
                                content = snapshot.content
                                title = snapshot.title
                                showSnapshotDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSnapshotDialog = false }) {
                    Text(
                        stringResource(Res.string.projects_dialog_cancel)
                    )
                }
            }
        )
    }

    if (showMergeDialog) {
        val otherNodes =
            nodes.filter { it.node.id != noteId && (it.node.type == "note" || it.node.type == "idea") }
        AlertDialog(
            onDismissRequest = { showMergeDialog = false },
            title = { Text(stringResource(Res.string.detail_merge_dialog_title)) },
            text = {
                LazyColumn {
                    items(otherNodes) { other ->
                        ListItem(
                            headlineContent = { Text(other.node.title) },
                            modifier = Modifier.clickable {
                                viewModel.mergeNodes(noteId, listOf(other.node.id))
                                showMergeDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMergeDialog = false }) {
                    Text(
                        stringResource(Res.string.projects_dialog_cancel)
                    )
                }
            }
        )
    }

    if (showNoteStateDialog) {
        val states = listOf("raw", "highlighted", "distilled", "takeaway")
        AlertDialog(
            onDismissRequest = { showNoteStateDialog = false },
            title = { Text(stringResource(Res.string.detail_state_dialog_title)) },
            text = {
                Column {
                    states.forEach { state ->
                        ListItem(
                            headlineContent = { Text(state.uppercase()) },
                            modifier = Modifier.clickable {
                                viewModel.updateNode(node.copy(noteState = state))
                                showNoteStateDialog = false
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNoteStateDialog = false }) {
                    Text(
                        stringResource(Res.string.projects_dialog_cancel)
                    )
                }
            },
        )
    }

    if (showAreaDialog) {
        AlertDialog(
            onDismissRequest = { showAreaDialog = false },
            title = { Text(stringResource(Res.string.detail_area_dialog_title)) },
            text = {
                Column {
                    areas.forEach { area ->
                        ListItem(
                            headlineContent = { Text(area.title) },
                            modifier =
                                Modifier.clickable {
                                    viewModel.updateNode(node.copy(areaId = area.id))
                                    showAreaDialog = false
                                },
                        )
                    }
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.detail_unassign)) },
                        modifier =
                            Modifier.clickable {
                                viewModel.updateNode(node.copy(areaId = null))
                                showAreaDialog = false
                            },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showAreaDialog = false
                }) { Text(stringResource(Res.string.projects_dialog_cancel)) }
            },
        )
    }

    if (showProjectDialog) {
        AlertDialog(
            onDismissRequest = { showProjectDialog = false },
            title = { Text(stringResource(Res.string.detail_project_dialog_title)) },
            text = {
                Column {
                    projects.forEach { project ->
                        ListItem(
                            headlineContent = { Text(project.title) },
                            modifier =
                                Modifier.clickable {
                                    viewModel.updateNode(node.copy(projectId = project.id))
                                    showProjectDialog = false
                                },
                        )
                    }
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.detail_none)) },
                        modifier =
                            Modifier.clickable {
                                viewModel.updateNode(node.copy(projectId = null))
                                showProjectDialog = false
                            },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showProjectDialog = false
                }) { Text(stringResource(Res.string.projects_dialog_cancel)) }
            },
        )
    }

    if (showDueDialog) {
        AlertDialog(
            onDismissRequest = { showDueDialog = false },
            title = { Text(stringResource(Res.string.detail_due_dialog_title)) },
            text = {
                Column {
                    val options =
                        listOf(
                            "Today" to
                                    kotlin.time.Clock.System
                                        .now()
                                        .toEpochMilliseconds(),
                            "Tomorrow" to (
                                    kotlin.time.Clock.System
                                        .now()
                                        .toEpochMilliseconds() + 86400000
                                    ),
                            "In 1 Week" to (
                                    kotlin.time.Clock.System
                                        .now()
                                        .toEpochMilliseconds() + 86400000 * 7
                                    ),
                        )
                    options.forEach { (label, time) ->
                        ListItem(
                            headlineContent = { Text(label) },
                            modifier =
                                Modifier.clickable {
                                    if (time > (node.dueAt ?: 0)) {
                                        showPostponeDialog = true
                                    }
                                    viewModel.updateNode(node.copy(dueAt = time))
                                    showDueDialog = false
                                },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDueDialog = false }) {
                    Text(
                        stringResource(
                            Res.string.projects_dialog_cancel
                        )
                    )
                }
            },
        )
    }

    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text(stringResource(Res.string.detail_reminder_dialog_title)) },
            text = {
                Column {
                    val options =
                        listOf(
                            "In 1 Hour" to (
                                    kotlin.time.Clock.System
                                        .now()
                                        .toEpochMilliseconds() + 3600000
                                    ),
                            "Tomorrow Morning" to (
                                    kotlin.time.Clock.System
                                        .now()
                                        .toEpochMilliseconds() + 86400000
                                    ),
                            "Next Week" to (
                                    kotlin.time.Clock.System
                                        .now()
                                        .toEpochMilliseconds() + 86400000 * 7
                                    ),
                        )
                    options.forEach { (label, time) ->
                        ListItem(
                            headlineContent = { Text(label) },
                            modifier =
                                Modifier.clickable {
                                    viewModel.updateNode(node.copy(reminderAt = time))
                                    showReminderDialog = false
                                },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showReminderDialog = false
                }) { Text(stringResource(Res.string.projects_dialog_cancel)) }
            },
        )
    }

    if (showEstimateDialog) {
        AlertDialog(
            onDismissRequest = { showEstimateDialog = false },
            title = { Text(stringResource(Res.string.detail_estimate_dialog_title)) },
            text = {
                Column {
                    val options = listOf(5, 15, 30, 60, 120)
                    options.forEach { mins ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    stringResource(
                                        Res.string.detail_estimate_mins,
                                        mins
                                    )
                                )
                            },
                            modifier = Modifier.clickable {
                                viewModel.updateNode(node.copy(estimatedMinutes = mins))
                                showEstimateDialog = false
                            }
                        )
                    }
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.search_clear)) },
                        modifier = Modifier.clickable {
                            viewModel.updateNode(node.copy(estimatedMinutes = null))
                            showEstimateDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showEstimateDialog = false
                }) { Text(stringResource(Res.string.projects_dialog_cancel)) }
            }
        )
    }

    if (showPostponeDialog) {
        AlertDialog(
            onDismissRequest = { showPostponeDialog = false },
            title = { Text(stringResource(Res.string.detail_postpone_dialog_title)) },
            text = {
                Column {
                    val reasons = listOf(
                        stringResource(Res.string.detail_postpone_low_energy),
                        stringResource(Res.string.detail_postpone_not_enough_info),
                        stringResource(Res.string.detail_postpone_scared),
                        stringResource(Res.string.detail_postpone_waiting),
                        stringResource(Res.string.detail_postpone_too_much)
                    )
                    reasons.forEach { reason ->
                        ListItem(
                            headlineContent = { Text(reason) },
                            modifier = Modifier.clickable {
                                val currentContent = node.content
                                val stamp = "\n[POSTPONED: $reason]"
                                viewModel.updateNode(node.copy(content = currentContent + stamp))
                                showPostponeDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPostponeDialog = false }) { Text("SKIP") }
            }
        )
    }

    if (showWhyDialog) {
        var whyText by remember { mutableStateOf(node.projectWhy ?: "") }
        AlertDialog(
            onDismissRequest = { showWhyDialog = false },
            title = { Text(stringResource(Res.string.detail_why_dialog_title)) },
            text = {
                TextField(
                    value = whyText,
                    onValueChange = { whyText = it },
                    placeholder = { Text(stringResource(Res.string.detail_why_placeholder)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateNode(node.copy(projectWhy = whyText))
                    showWhyDialog = false
                }) { Text(stringResource(Res.string.detail_save)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showWhyDialog = false
                }) { Text(stringResource(Res.string.projects_dialog_cancel)) }
            }
        )
    }

    if (showMediaTypeDialog) {
        val mediaTypes = listOf(
            "book" to Res.string.media_book,
            "article" to Res.string.media_article,
            "podcast" to Res.string.media_podcast,
            "video" to Res.string.media_video,
            "link" to Res.string.media_link
        )
        AlertDialog(
            onDismissRequest = { showMediaTypeDialog = false },
            title = { Text(stringResource(Res.string.detail_media_type)) },
            text = {
                Column {
                    mediaTypes.forEach { (type, res) ->
                        ListItem(
                            headlineContent = { Text(stringResource(res)) },
                            modifier = Modifier.clickable {
                                viewModel.updateNode(node.copy(mediaType = type))
                                showMediaTypeDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showMediaTypeDialog = false
                }) { Text(stringResource(Res.string.projects_dialog_cancel)) }
            }
        )
    }

    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text(stringResource(Res.string.detail_rating)) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    (1..5).forEach { stars ->
                        IconButton(onClick = {
                            viewModel.updateNode(node.copy(rating = stars))
                            showRatingDialog = false
                        }) {
                            Icon(
                                if ((node.rating
                                        ?: 0) >= stars
                                ) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if ((node.rating
                                        ?: 0) >= stars
                                ) TactileTheme.Primary else TactileTheme.Muted
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showRatingDialog = false
                }) { Text(stringResource(Res.string.projects_dialog_cancel)) }
            }
        )
    }
}

@Composable
fun RelationshipItem(
    node: com.tajemniktv.tajsos.data.NodeEntity,
    type: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = TactileTheme.Surface,
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Border),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (node.type) {
                    "project" -> Icons.Default.AccountTree
                    "area" -> Icons.Default.Place
                    "task" -> Icons.Default.CheckCircle
                    else -> Icons.Default.Description
                },
                contentDescription = null,
                tint = TactileTheme.Muted,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    node.title,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = TactileTheme.Muted
            )
        }
    }
}
