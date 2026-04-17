/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.templates_add_desc
import tajsos.composeapp.generated.resources.templates_dialog_cancel
import tajsos.composeapp.generated.resources.templates_dialog_create
import tajsos.composeapp.generated.resources.templates_dialog_name
import tajsos.composeapp.generated.resources.templates_new_dialog
import tajsos.composeapp.generated.resources.type_area
import tajsos.composeapp.generated.resources.type_note
import tajsos.composeapp.generated.resources.type_project
import tajsos.composeapp.generated.resources.type_record
import tajsos.composeapp.generated.resources.type_task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
) {
    val templates by viewModel.allTemplates.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val context =
        TemplatesDashboardContext(
            viewModel = viewModel,
            templates = templates,
            onAddTemplate = { name, type ->
                viewModel.addTemplate(name, type)
            },
            onDeleteTemplate = { viewModel.deleteTemplate(it) },
            onShowAddDialog = { showAddDialog = true },
        )

    val actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = { showAddDialog = true }) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(Res.string.templates_add_desc),
            )
        }
    }

    val surface = TemplatesDashboardSurface.MOBILE // Default for now
    val plan = remember(surface) { buildTemplatesDashboardPlan(surface) }

    ScreenScaffold(
        backgroundColor = TajsOSTheme.Background,
        actions = actions,
        scrollBehavior = ScreenScrollBehavior.None,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(TajsOSTheme.Background)
                    .padding(TajsOSTheme.SpacingMd),
        ) {
            plan.primary.forEach { block ->
                TemplatesDashboardBlocks.resolve(block.id)?.invoke(context)
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("task") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(Res.string.templates_new_dialog)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(Res.string.templates_dialog_name)) },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                        listOf("task", "note", "record", "project", "area").forEach { t ->
                            val typeLabel =
                                when (t) {
                                    "task" -> stringResource(Res.string.type_task)
                                    "note" -> stringResource(Res.string.type_note)
                                    "record" -> stringResource(Res.string.type_record)
                                    "project" -> stringResource(Res.string.type_project)
                                    "area" -> stringResource(Res.string.type_area)
                                    else -> t
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
                        context.onAddTemplate(name, type)
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
