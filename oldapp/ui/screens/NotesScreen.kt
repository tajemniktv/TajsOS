/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.EmptyState
import com.tajemniktv.tajsos.ui.components.ItemCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme

/**
 * NotesScreen displays items of type "note" and "idea".
 * "Ideas" are listed first to reflect capture priority.
 * Added: Search, Pinned knowledge section, and Navigation to Detail.
 */
@Composable
fun NotesScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel, onNoteClick: (Long) -> Unit) {
    val items by viewModel.allItems.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredItems = items.filter { 
        it.item.title.contains(searchQuery, ignoreCase = true) || 
        it.item.body.contains(searchQuery, ignoreCase = true)
    }

    val pinnedKnowledge = filteredItems.filter { it.item.isPinned && (it.item.type == "note" || it.item.type == "idea") }
    val unpinnedIdeas = filteredItems.filter { !it.item.isPinned && it.item.type == "idea" }
    val unpinnedNotes = filteredItems.filter { !it.item.isPinned && it.item.type == "note" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("KNOWLEDGE & IDEAS", style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search knowledge...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.medium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (pinnedKnowledge.isNotEmpty()) {
            item {
                Text("PINNED KNOWLEDGE", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(pinnedKnowledge, key = { "pinned_${it.item.id}" }) { itemWithPin ->
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.ItemCard(
                    itemWithPin = itemWithPin,
                    modifier = Modifier.clickable { onNoteClick(itemWithPin.item.id) },
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
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        if (unpinnedIdeas.isNotEmpty()) {
            item {
                Text("IDEAS", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(unpinnedIdeas, key = { it.item.id }) { itemWithPin ->
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.ItemCard(
                    itemWithPin = itemWithPin,
                    modifier = Modifier.clickable { onNoteClick(itemWithPin.item.id) },
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
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        if (unpinnedNotes.isNotEmpty()) {
            item {
                Text("NOTES", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(unpinnedNotes, key = { it.item.id }) { itemWithPin ->
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.ItemCard(
                    itemWithPin = itemWithPin,
                    modifier = Modifier.clickable { onNoteClick(itemWithPin.item.id) },
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

        if (pinnedKnowledge.isEmpty() && unpinnedIdeas.isEmpty() && unpinnedNotes.isEmpty()) {
            item {
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.EmptyState(message = if (searchQuery.isEmpty()) "NO NOTES OR IDEAS FOUND" else "NO MATCHING RESULTS")
            }
        }
    }
}
