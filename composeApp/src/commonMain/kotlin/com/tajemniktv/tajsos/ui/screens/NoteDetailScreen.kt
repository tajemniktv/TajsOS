/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.RelationEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    viewModel: MainViewModel,
    noteId: Long,
    onBack: () -> Unit,
    onNavigateToNode: (Long) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val nodes by viewModel.allNodes.collectAsState()
    val nodeWithPin = remember(nodes, noteId) { nodes.find { it.node.id == noteId } }

    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (nodes.isEmpty()) {
                CircularProgressIndicator(color = TactileTheme.Primary)
            } else {
                Text("Note not found (ID: $noteId)", color = TactileTheme.Muted)
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

    var showTagDialog by remember { mutableStateOf(false) }
    var showRelationDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showAreaDialog by remember { mutableStateOf(false) }
    var showProjectDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showDueDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(node.type.uppercase(), style = MaterialTheme.typography.labelSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.togglePermanentPin(node)
                    }) {
                        Icon(
                            if (node.isPinned) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Pin knowledge",
                            tint = if (node.isPinned) TactileTheme.Primary else TactileTheme.Muted
                        )
                    }
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.getNodeById(noteId)?.let { original ->
                                viewModel.addNode(
                                    title = "${original.title} (COPY)",
                                    content = original.content,
                                    type = original.type,
                                    projectId = original.projectId,
                                    areaId = original.areaId
                                )
                            }
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate")
                    }
                    IconButton(onClick = {
                        viewModel.archiveNode(node)
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Archive")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TactileTheme.SpacingMd)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)
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
                        Text("Untitled...", style = MaterialTheme.typography.headlineMedium, color = TactileTheme.Muted)
                    }
                    innerTextField()
                }
            )

            // Tags Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalAlignment = Alignment.CenterVertically
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
                                contentDescription = "Remove Tag",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.detachTagFromNode(noteId, tag.id) }
                            )
                        }
                    )
                }
                item {
                    IconButton(onClick = { showTagDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add Tag", tint = TactileTheme.Primary)
                    }
                }
            }

            HorizontalDivider(color = TactileTheme.Muted.copy(alpha = 0.2f))

            BasicTextField(
                value = content,
                onValueChange = {
                    content = it
                    viewModel.updateNode(node.copy(content = it))
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = TactileTheme.Text),
                cursorBrush = SolidColor(TactileTheme.Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) {
                        Text("Start writing...", style = MaterialTheme.typography.bodyLarge, color = TactileTheme.Muted)
                    }
                    innerTextField()
                }
            )
            
            // Relations Section
            if (relations.isNotEmpty()) {
                val nodesMap = remember(nodes) { nodes.associateBy { it.node.id } }
                Text("RELATED", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
                relations.forEach { relation ->
                    val relatedId = if (relation.fromNodeId == noteId) relation.toNodeId else relation.fromNodeId
                    val relatedNode = nodesMap[relatedId]?.node
                    if (relatedNode != null) {
                        Surface(
                            onClick = { onNavigateToNode(relatedId) },
                            color = TactileTheme.Surface,
                            shape = MaterialTheme.shapes.small,
                            border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(TactileTheme.SpacingMd), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = TactileTheme.Muted, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    relatedNode.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(relation.relationType, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
                                if (relation.relationType != "BELONGS_TO") {
                                    IconButton(
                                        onClick = { viewModel.deleteRelation(relation) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.LinkOff,
                                            contentDescription = "Unlink",
                                            tint = TactileTheme.Error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Button(onClick = { showRelationDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Surface, contentColor = TactileTheme.Primary)) {
                Icon(Icons.Default.Link, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("LINK NODE")
            }

            // Attachments Section
            if (attachments.isNotEmpty()) {
                Text("ATTACHMENTS", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
                attachments.forEach { attachment ->
                    ListItem(
                        headlineContent = { Text(attachment.title ?: attachment.uriOrPath) },
                        supportingContent = { Text(attachment.assetType) },
                        leadingContent = { Icon(Icons.Default.FilePresent, contentDescription = null) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteAttachment(attachment) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove Attachment",
                                    tint = TactileTheme.Error
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface)
                    )
                }
            }
            Button(onClick = { 
                viewModel.addAttachment(noteId, "URL", "https://example.com", "Example Link")
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Surface, contentColor = TactileTheme.Primary)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("ADD LINK")
            }

            Spacer(modifier = Modifier.height(TactileTheme.SpacingLg))
            
            Text("ORGANIZATION", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
            
            ListItem(
                headlineContent = { Text("Status") },
                supportingContent = { Text(node.status.uppercase()) },
                modifier = Modifier.clickable { showStatusDialog = true },
                colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface)
            )

            // Area & Project Selection
            val area = areas.find { it.id == node.areaId }
            val project = projects.find { it.id == node.projectId }

            if (node.type != "area") {
                ListItem(
                    headlineContent = { Text("Area") },
                    supportingContent = { Text(area?.title ?: "Unassigned") },
                    modifier = Modifier.clickable { showAreaDialog = true },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface)
                )
            }

            if (node.type != "area" && node.type != "project") {
                ListItem(
                    headlineContent = { Text("Project") },
                    supportingContent = { Text(project?.title ?: "None") },
                    modifier = Modifier.clickable { showProjectDialog = true },
                    colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface)
                )
            }

            // Due Date
            ListItem(
                headlineContent = { Text("Due Date") },
                supportingContent = {
                    Text(node.dueAt?.let {
                        kotlin.time.Instant.fromEpochMilliseconds(it)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                    } ?: "No due date")
                },
                modifier = Modifier.clickable { showDueDialog = true },
                trailingContent = {
                    if (node.dueAt != null) {
                        IconButton(onClick = { viewModel.updateNode(node.copy(dueAt = null)) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface)
            )

            // Reminder & Recurrence
            ListItem(
                headlineContent = { Text("Reminder") },
                supportingContent = {
                    Text(node.reminderAt?.let {
                        kotlin.time.Instant.fromEpochMilliseconds(it)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).toString()
                            .replace("T", " ")
                    } ?: "No reminder set")
                },
                modifier = Modifier.clickable { showReminderDialog = true },
                trailingContent = {
                    if (node.reminderAt != null) {
                        IconButton(onClick = { viewModel.updateNode(node.copy(reminderAt = null)) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface)
            )

            ListItem(
                headlineContent = { Text("Recurrence") },
                supportingContent = { Text(if (node.isRecurring) "Interval: ${node.recurringInterval}" else "One-time") },
                modifier = Modifier.clickable {
                    viewModel.updateNode(
                        node.copy(
                            isRecurring = !node.isRecurring,
                            recurringInterval = if (!node.isRecurring) "DAILY" else null
                        )
                    )
                },
                colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface)
            )
            
            val updatedDate = kotlin.time.Instant.fromEpochMilliseconds(node.updatedAt).toLocalDateTime(TimeZone.currentSystemDefault()).toString()
            Text(
                text = "Last updated: $updatedDate",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = TactileTheme.SpacingMd)
            )
        }
    }

    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Add Tag") },
            text = {
                Column {
                    allTags.forEach { tag ->
                        ListItem(
                            headlineContent = { Text(tag.name) },
                            modifier = Modifier.clickable { 
                                viewModel.attachTagToNode(noteId, tag.id)
                                showTagDialog = false
                            }
                        )
                    }
                    var newTagName by remember { mutableStateOf("") }
                    TextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        placeholder = { Text("New tag...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = {
                        if (newTagName.isNotBlank()) {
                            viewModel.addTag(newTagName)
                            newTagName = ""
                        }
                    }) { Text("Create Tag") }
                }
            },
            confirmButton = { TextButton(onClick = { showTagDialog = false }) { Text("Close") } }
        )
    }

    if (showRelationDialog) {
        val linkedNodeIds = remember(relations, noteId) {
            relations.map { if (it.fromNodeId == noteId) it.toNodeId else it.fromNodeId }.toSet()
        }
        AlertDialog(
            onDismissRequest = { showRelationDialog = false },
            title = { Text("Link to Node") },
            text = {
                Column {
                    nodes.filter { it.node.id != noteId && it.node.id !in linkedNodeIds }.take(10)
                        .forEach { nodeWithPin ->
                        ListItem(
                            headlineContent = { Text(nodeWithPin.node.title) },
                            supportingContent = { Text(nodeWithPin.node.type) },
                            modifier = Modifier.clickable {
                                viewModel.addRelation(noteId, nodeWithPin.node.id, "RELATED")
                                showRelationDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showRelationDialog = false }) { Text("Cancel") } }
        )
    }

    if (showStatusDialog) {
        val statuses = listOf("active", "done", "archived", "on_hold", "someday", "blocked")
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Set Status") },
            text = {
                Column {
                    statuses.forEach { status ->
                        ListItem(
                            headlineContent = { Text(status.uppercase()) },
                            modifier = Modifier.clickable {
                                viewModel.updateNodeStatus(node, status)
                                showStatusDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showStatusDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAreaDialog) {
        AlertDialog(
            onDismissRequest = { showAreaDialog = false },
            title = { Text("Assign to Area") },
            text = {
                Column {
                    areas.forEach { area ->
                        ListItem(
                            headlineContent = { Text(area.title) },
                            modifier = Modifier.clickable {
                                viewModel.updateNode(node.copy(areaId = area.id))
                                showAreaDialog = false
                            }
                        )
                    }
                    ListItem(
                        headlineContent = { Text("Unassign") },
                        modifier = Modifier.clickable {
                            viewModel.updateNode(node.copy(areaId = null))
                            showAreaDialog = false
                        }
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showAreaDialog = false }) { Text("Cancel") } }
        )
    }

    if (showProjectDialog) {
        AlertDialog(
            onDismissRequest = { showProjectDialog = false },
            title = { Text("Assign to Project") },
            text = {
                Column {
                    projects.forEach { project ->
                        ListItem(
                            headlineContent = { Text(project.title) },
                            modifier = Modifier.clickable {
                                viewModel.updateNode(node.copy(projectId = project.id))
                                showProjectDialog = false
                            }
                        )
                    }
                    ListItem(
                        headlineContent = { Text("None") },
                        modifier = Modifier.clickable {
                            viewModel.updateNode(node.copy(projectId = null))
                            showProjectDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showProjectDialog = false
                }) { Text("Cancel") }
            }
        )
    }

    if (showDueDialog) {
        AlertDialog(
            onDismissRequest = { showDueDialog = false },
            title = { Text("Set Due Date") },
            text = {
                Column {
                    val options = listOf(
                        "Today" to kotlin.time.Clock.System.now().toEpochMilliseconds(),
                        "Tomorrow" to (kotlin.time.Clock.System.now()
                            .toEpochMilliseconds() + 86400000),
                        "In 1 Week" to (kotlin.time.Clock.System.now()
                            .toEpochMilliseconds() + 86400000 * 7)
                    )
                    options.forEach { (label, time) ->
                        ListItem(
                            headlineContent = { Text(label) },
                            modifier = Modifier.clickable {
                                viewModel.updateNode(node.copy(dueAt = time))
                                showDueDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDueDialog = false }) { Text("Cancel") } }
        )
    }

    if (showReminderDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            title = { Text("Set Reminder") },
            text = {
                Column {
                    val options = listOf(
                        "In 1 Hour" to (kotlin.time.Clock.System.now()
                            .toEpochMilliseconds() + 3600000),
                        "Tomorrow Morning" to (kotlin.time.Clock.System.now()
                            .toEpochMilliseconds() + 86400000),
                        "Next Week" to (kotlin.time.Clock.System.now()
                            .toEpochMilliseconds() + 86400000 * 7)
                    )
                    options.forEach { (label, time) ->
                        ListItem(
                            headlineContent = { Text(label) },
                            modifier = Modifier.clickable {
                                viewModel.updateNode(node.copy(reminderAt = time))
                                showReminderDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showReminderDialog = false
                }) { Text("Cancel") }
            }
        )
    }
}
