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
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.ItemCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun ProjectDetailScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel, projectId: Long) {
    val projects by viewModel.allProjects.collectAsState()
    val project = projects.find { it.id == projectId }
    val itemsWithPin by viewModel.getItemsForProject(projectId).collectAsState(initial = emptyList())

    if (project == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Project not found", modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)
    ) {
        Text(
            text = project.name.uppercase(),
            style = MaterialTheme.typography.displaySmall,
            color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
        )
        if (project.description.isNotEmpty()) {
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted
            )
        }
        Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd))

        Text(
            text = "TASKS & NOTES",
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
