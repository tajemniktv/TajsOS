/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
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
import com.tajemniktv.tajsos.data.ProjectEntity

@Composable
fun ProjectsScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel, onNavigateTo: (String) -> Unit) {
    val projects by viewModel.allProjects.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredProjects = projects.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)
    ) {
        Text(
            text = "PROJECTS",
            style = MaterialTheme.typography.displaySmall,
            color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
        )
        Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search projects...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd))

        if (filteredProjects.isEmpty() && searchQuery.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No projects yet.", color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
                    Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd))
                    Button(onClick = { showAddDialog = true }) {
                        Text("CREATE FIRST PROJECT")
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm)) {
                items(filteredProjects) { project ->
                    val projectItems = allItems.filter { it.item.projectId == project.id }
                    val total = projectItems.size
                    val completed = projectItems.count { it.item.status == "done" }
                    val progress = if (total > 0) completed.toFloat() / total else 0f

                    _root_ide_package_.com.tajemniktv.tajsos.ui.screens.ProjectItem(
                        project,
                        progress,
                        total
                    ) {
                        onNavigateTo(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.ProjectDetail.route.replace(
                            "{projectId}",
                            project.id.toString()
                        )
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Leave space for global FAB
                }
            }
        }
    }

    if (showAddDialog) {
        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc ->
                viewModel.addProject(name, desc)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProjectItem(project: ProjectEntity, progress: Float, totalItems: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
        shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(1.dp, _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(project.name.uppercase(), style = MaterialTheme.typography.titleMedium, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
                if (totalItems > 0) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted
                    )
                }
            }
            if (project.description.isNotEmpty()) {
                Text(project.description, style = MaterialTheme.typography.bodySmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
            }
            if (totalItems > 0) {
                Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary,
                    trackColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun AddProjectDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NEW PROJECT") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, description) }) {
                Text("CREATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}
