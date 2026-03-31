/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.data.ProjectState
import com.tajemniktv.tajsos.data.projectStateOrNull
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.projects_filter_active
import tajsos.composeapp.generated.resources.projects_filter_someday
import tajsos.composeapp.generated.resources.projects_search_placeholder
import tajsos.composeapp.generated.resources.projects_title

object ProjectsDashboardBlockRegistry {
    private val renderers: Map<String, ProjectsDashboardBlockRenderer> =
        mapOf("projects_main" to ::renderProjectsMainBlock)

    fun resolve(id: String): ProjectsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderProjectsMainBlock(context: ProjectsDashboardContext) {
    ProjectsMainBlock(viewModel = context.viewModel, onNavigateTo = context.onNavigateTo)
}

@Composable
internal fun ProjectsMainBlock(
    viewModel: MainViewModel,
    onNavigateTo: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val projects by viewModel.allProjects.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("active") }

    val filteredProjects =
        remember(projects, searchQuery, selectedStatusFilter) {
            projects.filter {
                it.title.contains(searchQuery, ignoreCase = true) &&
                    (
                        if (selectedStatusFilter == "active") {
                            it.projectStateOrNull() in
                                setOf(
                                    ProjectState.ACTIVE,
                                    ProjectState.ON_HOLD,
                                )
                        } else {
                            it.projectStateOrNull() == ProjectState.SOMEDAY
                        }
                    )
            }
        }
    val nodesByProjectId =
        remember(allNodes) {
            allNodes.groupBy { it.node.projectId }
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TajsOSTheme.SpacingMd)
    ) {
        Text(
            text = stringResource(Res.string.projects_title),
            style = MaterialTheme.typography.displaySmall,
            color = TajsOSTheme.Text
        )
        Spacer(modifier = Modifier.height(TajsOSTheme.SpacingMd))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(Res.string.projects_search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium,
        )

        Spacer(modifier = Modifier.height(TajsOSTheme.SpacingSm))

        Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
            FilterChip(
                selected = selectedStatusFilter == "active",
                onClick = { selectedStatusFilter = "active" },
                label = { Text(stringResource(Res.string.projects_filter_active)) },
            )
            FilterChip(
                selected = selectedStatusFilter == "someday",
                onClick = { selectedStatusFilter = "someday" },
                label = { Text(stringResource(Res.string.projects_filter_someday)) },
            )
        }

        Spacer(modifier = Modifier.height(TajsOSTheme.SpacingSm))

        ProjectListContent(
            state = ProjectListState(filteredProjects, searchQuery, nodesByProjectId),
            onNavigateTo = onNavigateTo,
            onShowAddDialog = { showAddDialog = true },
        )
    }

    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, content, status ->
                scope.launch {
                    val id =
                        viewModel.addNodeForResult(
                            title,
                            content,
                            "project",
                            inboxState = false,
                        )
                    viewModel.getNodeById(id)?.let { node ->
                        if (node.status != status) {
                            viewModel.updateNodeStatus(node, status)
                        }
                    }
                }
                showAddDialog = false
            },
        )
    }
}
