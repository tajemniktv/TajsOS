/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val templates by viewModel.allTemplates.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TEMPLATES", style = MaterialTheme.typography.labelSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Template")
                    }
                }
            )
        }
    ) { padding ->
        if (templates.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("NO TEMPLATES DEFINED", color = TactileTheme.Muted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(TactileTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
            ) {
                items(templates) { template ->
                    ListItem(
                        headlineContent = { Text(template.name) },
                        supportingContent = { Text(template.nodeType.uppercase()) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteTemplate(template) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = TactileTheme.Error
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface)
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("task") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("NEW TEMPLATE") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                    TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                        listOf("task", "note", "project").forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(t.uppercase()) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addTemplate(name, type)
                        showAddDialog = false
                    }
                }) { Text("CREATE") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("CANCEL") } }
        )
    }
}
