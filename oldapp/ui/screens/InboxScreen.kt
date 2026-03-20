/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.EmptyState
import com.tajemniktv.tajsos.ui.components.ItemCard

/**
 * Inbox is the "Zero Friction" capture zone.
 * It allows the user to quickly dump tasks, notes, or ideas without
 * worrying about organization, helping clear mental clutter immediately.
 * 
 * Phase 1 Implementation:
 * - Added Quick Capture with type selection (Task/Note/Idea)
 * - Added Empty State for when the brain is clear
 * - Added Archive functionality for completed items
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel) {
    var itemInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("task") }
    val items by viewModel.allItems.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Quick Capture", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedType == "task",
                onClick = { selectedType = "task" },
                label = { Text("Task") }
            )
            FilterChip(
                selected = selectedType == "note",
                onClick = { selectedType = "note" },
                label = { Text("Note") }
            )
            FilterChip(
                selected = selectedType == "idea",
                onClick = { selectedType = "idea" },
                label = { Text("Idea") }
            )
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = itemInput,
            onValueChange = { itemInput = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("What's on your mind?") },
            trailingIcon = {
                if (itemInput.isNotBlank()) {
                    IconButton(onClick = {
                        viewModel.addItem(itemInput, selectedType)
                        itemInput = ""
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (itemInput.isNotBlank()) {
                    viewModel.addItem(itemInput, selectedType)
                    itemInput = ""
                }
            }),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("Recent Entries", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        if (items.isEmpty()) {
            _root_ide_package_.com.tajemniktv.tajsos.ui.components.EmptyState(message = "NO ENTRIES FOUND // BRAIN CLEAR")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items, key = { it.item.id }) { itemWithPin ->
                    _root_ide_package_.com.tajemniktv.tajsos.ui.components.ItemCard(
                        itemWithPin = itemWithPin,
                        onToggleDone = { status: String ->
                            viewModel.updateItemStatus(
                                itemWithPin.item,
                                status
                            )
                        },
                        onTogglePin = { isPinned: Boolean ->
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
