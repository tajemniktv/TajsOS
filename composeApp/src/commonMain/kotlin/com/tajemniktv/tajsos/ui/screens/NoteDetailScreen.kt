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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    viewModel: MainViewModel,
    noteId: Long,
    onBack: () -> Unit,
    onNavigateToNode: (Long) -> Unit
) {
    val nodes by viewModel.allNodes.collectAsState()
    val nodeWithPin = nodes.find { it.node.id == noteId }

    if (nodeWithPin == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Note not found")
        }
        return
    }

    val node = nodeWithPin.node
    var title by remember { mutableStateOf(node.title) }
    var content by remember { mutableStateOf(node.content) }
    var isPinned by remember { mutableStateOf(node.isPinned) }

    val tags by viewModel.getTagsForNode(noteId).collectAsState(initial = emptyList())
    val allTags by viewModel.allTags.collectAsState()
    val relations by viewModel.getRelationsForNode(noteId).collectAsState(initial = emptyList())
    val attachments by viewModel.getAttachmentsForNode(noteId).collectAsState(initial = emptyList())

    var showTagDialog by remember { mutableStateOf(false) }
    var showRelationDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

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
                        isPinned = !isPinned
                        viewModel.updateNode(node.copy(isPinned = isPinned))
                    }) {
                        Icon(
                            if (isPinned) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Pin knowledge",
                            tint = if (isPinned) TactileTheme.Primary else TactileTheme.Muted
                        )
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
                    SuggestionChip(
                        onClick = { /* TODO: Filter by tag? */ },
                        label = { Text(tag.name) }
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
                Text("RELATED", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
                relations.forEach { relation ->
                    val relatedId = if (relation.fromNodeId == noteId) relation.toNodeId else relation.fromNodeId
                    val relatedNode = nodes.find { it.node.id == relatedId }?.node
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
                                Text(relatedNode.title, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.weight(1f))
                                Text(relation.relationType, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
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

            val areas by viewModel.allAreas.collectAsState()
            val projects by viewModel.allProjects.collectAsState()
            
            val area = areas.find { it.id == node.areaId }
            val project = projects.find { it.id == node.projectId }

            Text(
                text = "Area: ${area?.title ?: "None"}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted
            )
            Text(
                text = "Project: ${project?.title ?: "None"}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted
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
        AlertDialog(
            onDismissRequest = { showRelationDialog = false },
            title = { Text("Link to Node") },
            text = {
                Column {
                    nodes.filter { it.node.id != noteId }.take(10).forEach { nodeWithPin ->
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
}
