/*
 * Copyright (c) TajemnikTV 2026. All rights reserved. 
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.ItemCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun AreaDetailScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel, areaId: Long, onNavigateToProject: (Long) -> Unit) {
    val areas by viewModel.allAreas.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val area = areas.find { it.id == areaId }
    val projects by viewModel.getProjectsForArea(areaId).collectAsState(initial = emptyList())
    val itemsWithPin by viewModel.getItemsForArea(areaId).collectAsState(initial = emptyList())

    if (area == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Area not found", modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)
    ) {
        Text(
            text = area.name.uppercase(),
            style = MaterialTheme.typography.displaySmall,
            color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
        )
        Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd))

        if (projects.isNotEmpty()) {
            Text(
                text = "PROJECTS",
                style = MaterialTheme.typography.labelSmall,
                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
            )
            Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm))
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm)
            ) {
                items(projects) { project ->
                    val projectItems = allItems.filter { it.item.projectId == project.id }
                    val total = projectItems.size
                    val completed = projectItems.count { it.item.status == "done" }
                    val progress = if (total > 0) completed.toFloat() / total else 0f

                    _root_ide_package_.com.tajemniktv.tajsos.ui.screens.ProjectItem(
                        project,
                        progress,
                        total
                    ) { onNavigateToProject(project.id) }
                }
            }
            Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd))
        }

        Text(
            text = "DIRECT ITEMS",
            style = MaterialTheme.typography.labelSmall,
            color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
        )
        Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm)) {
            items(itemsWithPin) { itemWithPin ->
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.ItemCard(
                    itemWithPin = itemWithPin,
                    onToggleDone = { status ->
                        viewModel.updateItemStatus(
                            itemWithPin.item,
                            status
                        )
                    },
                    onTogglePin = { isPinned -> viewModel.togglePin(itemWithPin.item, isPinned) },
                    onArchive = { viewModel.archiveItem(itemWithPin.item) }
                )
            }
        }
    }
}
