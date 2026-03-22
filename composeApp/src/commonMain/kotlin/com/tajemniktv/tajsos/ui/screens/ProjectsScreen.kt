/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.ProjectItem
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.launch

@Composable
fun ProjectsScreen(viewModel: MainViewModel, onNavigateTo: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    val projects by viewModel.allProjects.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("active") }

    val filteredProjects = remember(projects, searchQuery, selectedStatusFilter) {
        projects.filter {
            it.title.contains(searchQuery, ignoreCase = true) &&
                    (if (selectedStatusFilter == "active") it.status == "active" || it.status == "on_hold" else it.status == "someday")
        }
    }
    val nodesByProjectId = remember(allNodes) {
        allNodes.groupBy { it.node.projectId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TactileTheme.SpacingMd)
    ) {
        Text(
            text = "PROJECTS",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text
        )
        Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search projects...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

        Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            FilterChip(
                selected = selectedStatusFilter == "active",
                onClick = { selectedStatusFilter = "active" },
                label = { Text("ACTIVE") }
            )
            FilterChip(
                selected = selectedStatusFilter == "someday",
                onClick = { selectedStatusFilter = "someday" },
                label = { Text("SOMEDAY") }
            )
        }

        Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))

        if (filteredProjects.isEmpty() && searchQuery.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No projects yet.", color = TactileTheme.Muted)
                    Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))
                    Button(onClick = { showAddDialog = true }) {
                        Text("CREATE FIRST PROJECT")
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(filteredProjects, key = { it.id }) { project ->
                    val projectNodes = nodesByProjectId[project.id] ?: emptyList()
                    val total = projectNodes.size
                    val completed = projectNodes.count { it.node.status == "done" }
                    val progress = if (total > 0) completed.toFloat() / total else 0f

                    ProjectItem(
                        project = project,
                        progress = progress,
                        totalItems = total,
                        onLongClick = {
                            onNavigateTo(
                                Screen.NoteDetail.route.replace(
                                    "{noteId}",
                                    project.id.toString()
                                )
                            )
                        }
                    ) {
                        onNavigateTo(Screen.ProjectDetail.route.replace("{projectId}", project.id.toString()))
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, content, status ->
                viewModel.addNode(title, content, "project", inboxState = false)
                // Wait, addProject in MainViewModel might not support status.
                // I'll use addNode directly or update MainViewModel.
                scope.launch {
                    val id =
                        viewModel.addNodeForResult(title, content, "project", inboxState = false)
                    viewModel.updateNodeStatus(viewModel.getNodeById(id)!!, status)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddProjectDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("active") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NEW PROJECT") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                    listOf("active", "someday").forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s.uppercase()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, description, status) }) {
                Text("CREATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}
