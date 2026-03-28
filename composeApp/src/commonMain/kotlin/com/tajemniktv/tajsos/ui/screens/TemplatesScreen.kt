/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.components.layout.LocalHeaderActions
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 * Displays the templates screen: a header action for adding templates, a full-screen list of existing
 * templates (or an empty-state message), delete controls for each template, and a dialog to create a
 * new template.
 *
 * The list shows each template's name and an uppercase localized type label when available.
 * The Add action is provided to the surrounding UI via `LocalHeaderActions`. Tapping Add opens a
 * dialog that collects a template name and type; the create button is enabled only when the name
 * is not blank.
 *
 * @param viewModel Provides template data and actions (observe `allTemplates`, `addTemplate`, `deleteTemplate`).
 * @param onBack Callback invoked to perform back navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
)
{
    val templates by viewModel.allTemplates.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = { showAddDialog = true }) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(Res.string.templates_add_desc),
            )
        }
    }

    CompositionLocalProvider(LocalHeaderActions provides actions) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TactileTheme.Background),
        ) {
            if (templates.isEmpty())
            {
                EmptyState(message = stringResource(Res.string.templates_empty))
            } else
            {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(TactileTheme.SpacingMd),
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
                                    "record"  -> stringResource(Res.string.type_record)
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
                        listOf("task", "note", "record", "project", "area").forEach { t ->
                            val typeLabel = when (t)
                            {
                                "task"    -> stringResource(Res.string.type_task)
                                "note"    -> stringResource(Res.string.type_note)
                                "record"  -> stringResource(Res.string.type_record)
                                "project" -> stringResource(Res.string.type_project)
                                "area"    -> stringResource(Res.string.type_area)
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
