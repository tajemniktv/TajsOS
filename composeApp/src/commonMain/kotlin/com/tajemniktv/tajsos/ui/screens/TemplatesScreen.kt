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
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
)
{
    val templates by viewModel.allTemplates.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.templates_title),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(Res.string.templates_add_desc),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (templates.isEmpty())
        {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(Res.string.templates_empty), color = TactileTheme.Muted)
            }
        } else
        {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(TactileTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                items(templates) { template ->
                    ListItem(
                        headlineContent = { Text(template.name) },
                        supportingContent = {
                            val typeLabel = when (template.nodeType)
                            {
                                "task"    -> stringResource(Res.string.type_task)
                                "note"    -> stringResource(Res.string.type_note)
                                "idea"    -> stringResource(Res.string.type_idea)
                                "project" -> stringResource(Res.string.type_project)
                                "area"    -> stringResource(Res.string.type_area)
                                else      -> template.nodeType
                            }
                            Text(typeLabel.uppercase())
                        },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteTemplate(template) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(Res.string.archive_delete),
                                    tint = TactileTheme.Error,
                                )
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = TactileTheme.Surface),
                    )
                }
            }
        }
    }

    if (showAddDialog)
    {
        var name by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("task") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(Res.string.templates_new_dialog)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(Res.string.templates_dialog_name)) },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                        listOf("task", "note", "project").forEach { t ->
                            val typeLabel = when (t)
                            {
                                "task"    -> stringResource(Res.string.type_task)
                                "note"    -> stringResource(Res.string.type_note)
                                "project" -> stringResource(Res.string.type_project)
                                else      -> t
                            }
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(typeLabel.uppercase()) },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addTemplate(name, type)
                        showAddDialog = false
                    },
                    enabled = name.isNotBlank(),
                ) { Text(stringResource(Res.string.templates_dialog_create)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(
                        stringResource(
                            Res.string.templates_dialog_cancel,
                        ),
                    )
                }
            },
        )
    }
}
