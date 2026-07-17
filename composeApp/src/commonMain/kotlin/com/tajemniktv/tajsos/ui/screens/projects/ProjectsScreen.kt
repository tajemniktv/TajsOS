/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.components.nodes.ProjectItem
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.projects_create_first
import tajsos.composeapp.generated.resources.projects_dialog_cancel
import tajsos.composeapp.generated.resources.projects_dialog_create
import tajsos.composeapp.generated.resources.projects_dialog_description
import tajsos.composeapp.generated.resources.projects_dialog_name
import tajsos.composeapp.generated.resources.projects_dialog_new
import tajsos.composeapp.generated.resources.projects_empty
import tajsos.composeapp.generated.resources.projects_filter_active
import tajsos.composeapp.generated.resources.projects_filter_someday
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

/**
 * Central projects entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of project and node state.
 * @param onNavigate Navigation callback.
 */
@Composable
fun ProjectsRoute(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) ProjectsDashboardSurface.DESKTOP else ProjectsDashboardSurface.MOBILE
        val plan = remember(surface) { buildProjectsDashboardPlan(surface) }
        val context =
            remember(viewModel, onNavigate) { ProjectsDashboardContext(viewModel, onNavigate) }

        ProjectsScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless projects screen content.
 *
 * @param context Projects dashboard context.
 * @param plan Projects dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun ProjectsScreen(
    context: ProjectsDashboardContext,
    plan: ProjectsDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Projects,
        onNavigate = onNavigate,
        // Disable body scroll to allow inner LazyColumn to handle its own scrolling and measurement
        scrollBehavior = ScreenScrollBehavior.None,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            plan.primary.forEach { block ->
                item(key = block.id) {
                    ProjectsDashboardBlockRegistry.resolve(block.id)?.invoke(context)
                }
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
                Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
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
            TextButton(onClick = onDismiss, modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)) {
                Text(stringResource(Res.string.projects_dialog_cancel))
            }
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
        EmptyState(
            message = stringResource(Res.string.projects_empty),
        ) {
            Spacer(modifier = Modifier.height(TajsOSTheme.SpacingMd))
            Button(onClick = onShowAddDialog, modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)) {
                Text(stringResource(Res.string.projects_create_first))
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
            state.filteredProjects.forEach { project ->
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
                            Screen.ProjectDetail.route.replace(
                                "{projectId}",
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
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
