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
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun ProjectsScreen(viewModel: MainViewModel, onNavigateTo: (String) -> Unit) {
    val projects by viewModel.allProjects.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredProjects = projects.filter { it.title.contains(searchQuery, ignoreCase = true) }

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

        Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))

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
                items(filteredProjects) { project ->
                    val projectNodes = allNodes.filter { it.node.projectId == project.id }
                    val total = projectNodes.size
                    val completed = projectNodes.count { it.node.status == "done" }
                    val progress = if (total > 0) completed.toFloat() / total else 0f

                    ProjectItem(
                        project,
                        progress,
                        total
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
            onConfirm = { title, content ->
                viewModel.addProject(title, content)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProjectItem(project: NodeEntity, progress: Float, totalItems: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(project.title.uppercase(), style = MaterialTheme.typography.titleMedium, color = TactileTheme.Primary)
                if (totalItems > 0) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
            }
            if (project.content.isNotEmpty()) {
                Text(project.content, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Muted)
            }
            if (totalItems > 0) {
                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = TactileTheme.Primary,
                    trackColor = TactileTheme.Muted.copy(alpha = 0.2f)
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
