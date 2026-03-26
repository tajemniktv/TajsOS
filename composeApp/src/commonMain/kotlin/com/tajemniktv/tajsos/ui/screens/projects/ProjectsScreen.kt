/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.nodes.ProjectItem
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 * Displays the projects screen with search, status filters, project list, and add-project flow.
 *
 * Collects projects and nodes from the provided ViewModel, derives filtered projects and nodes grouped
 * by project ID, renders the header, search field, status chips, and the project list, and shows an
 * add-project dialog that creates a new project node and updates its status.
 *
 * @param viewModel Source of project and node state and actions for creating/updating nodes.
 * @param onNavigateTo Callback invoked with a navigation route when navigating from a project item.
 */
@Composable
fun ProjectsScreen(
    viewModel: MainViewModel,
    onNavigateTo: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth >
                900.dp
            ) {
                ProjectsDashboardSurface.DESKTOP
            } else {
                ProjectsDashboardSurface.MOBILE
            }
        val plan =
            remember(surface) {
                buildProjectsDashboardPlan(
                    surface,
                )
            }
        val context =
            remember(viewModel, onNavigateTo) {
                ProjectsDashboardContext(
                    viewModel,
                    onNavigateTo,
                )
            }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                ProjectsDashboardBlockRegistry
                    .resolve(
                        block.id,
                    )?.invoke(context)
            }
        }
    }
}

/**
 * Shows a dialog for creating a new project.
 *
 * The dialog collects a project name, description, and status; the confirm button is enabled only when the name is not blank.
 *
 * @param onDismiss Called when the dialog is dismissed or the cancel action is chosen.
 * @param onConfirm Called with the entered project `name`, `description`, and `status` (`"active"` or `"someday"`) when the user confirms creation.
 */
@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("active") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.projects_dialog_new)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.projects_dialog_name)) },
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(Res.string.projects_dialog_description)) },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                    listOf("active", "someday").forEach { s ->
                        val label =
                            if (s == "active") {
                                stringResource(Res.string.projects_filter_active)
                            } else {
                                stringResource(
                                    Res.string.projects_filter_someday,
                                )
                            }
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(label.uppercase()) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, description, status) },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(Res.string.projects_dialog_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.projects_dialog_cancel)) }
        },
    )
}

data class ProjectListState(
    val filteredProjects: List<NodeEntity>,
    val searchQuery: String,
    val nodesByProjectId: Map<Long?, List<NodeWithPin>>,
)

/**
 * Renders either an empty-state prompt or a scrollable list of projects based on the given state.
 *
 * If there are no filtered projects and the search query is empty, displays a centered message and a button that invokes the add-project callback.
 * Otherwise displays a vertically spaced list of project items that show each project's progress; project items invoke navigation via the provided callbacks on click and long-click.
 *
 * @param state Current view state containing filtered projects, the search query, and nodes grouped by project id.
 * @param onNavigateTo Callback invoked with a destination route string to navigate to a screen.
 * @param onShowAddDialog Callback invoked to request showing the add-project dialog.
 */
@Composable
fun ProjectListContent(
    state: ProjectListState,
    onNavigateTo: (String) -> Unit,
    onShowAddDialog: () -> Unit,
) {
    if (state.filteredProjects.isEmpty() && state.searchQuery.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.projects_empty), color = TactileTheme.Muted)
                Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))
                Button(onClick = onShowAddDialog) {
                    Text(stringResource(Res.string.projects_create_first))
                }
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
            items(state.filteredProjects, key = { it.id }) { project ->
                val projectNodes = state.nodesByProjectId[project.id] ?: emptyList()
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
                                project.id.toString(),
                            ),
                        )
                    },
                ) {
                    onNavigateTo(
                        Screen.ProjectDetail.route.replace(
                            "{projectId}",
                            project.id.toString(),
                        ),
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
