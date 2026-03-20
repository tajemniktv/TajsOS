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
import com.tajemniktv.tajsos.ui.components.EmptyState
import com.tajemniktv.tajsos.ui.components.ItemCard

/**
 * TasksScreen displays all items with type "task".
 */
@Composable
fun TasksScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel) {
    val items by viewModel.allItems.collectAsState()
    val tasks = items.filter { it.item.type == "task" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("TASKS", style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.height(24.dp))

        if (tasks.isEmpty()) {
            _root_ide_package_.com.tajemniktv.tajsos.ui.components.EmptyState(message = "NO TASKS FOUND")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tasks, key = { it.item.id }) { itemWithPin ->
                    _root_ide_package_.com.tajemniktv.tajsos.ui.components.ItemCard(
                        itemWithPin = itemWithPin,
                        onToggleDone = { status ->
                            viewModel.updateItemStatus(
                                itemWithPin.item,
                                status
                            )
                        },
                        onTogglePin = { isPinned ->
                            viewModel.togglePin(
                                itemWithPin.item,
                                isPinned
                            )
                        },
                        onArchive = { viewModel.archiveItem(itemWithPin.item) }
                    )
                }
            }
        }
    }
}
