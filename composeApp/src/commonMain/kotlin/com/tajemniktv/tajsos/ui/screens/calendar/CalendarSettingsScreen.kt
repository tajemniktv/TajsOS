/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.cal_settings_dialog_add
import tajsos.composeapp.generated.resources.cal_settings_dialog_cancel
import tajsos.composeapp.generated.resources.cal_settings_dialog_name
import tajsos.composeapp.generated.resources.cal_settings_dialog_title
import tajsos.composeapp.generated.resources.cal_settings_dialog_url

@Composable
fun CalendarSettingsScreen(viewModel: MainViewModel) {
    val providers by viewModel.calendarProviders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val context =
        CalendarSettingsContext(
            viewModel = viewModel,
            providers = providers,
            onAddProvider = { name, type, url ->
                viewModel.addCalendarProvider(name, type, url)
            },
            onDeleteProvider = { viewModel.deleteCalendarProvider(it) },
            onShowAddDialog = { showAddDialog = true },
        )

    val plan = remember { buildCalendarSettingsPlan() }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        plan.primary.forEach { block ->
            CalendarSettingsBlockRegistry.resolve(block.id)?.invoke(context)
        }
    }

    if (showAddDialog) {
        AddCalendarDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, type, url ->
                context.onAddProvider(name, type, url)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun AddCalendarDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String?) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("ICS") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.cal_settings_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.cal_settings_dialog_name)) },
                )
                TextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(Res.string.cal_settings_dialog_url)) },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, type, url) },
                enabled = name.isNotBlank() && url.isNotBlank(),
            ) {
                Text(stringResource(Res.string.cal_settings_dialog_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cal_settings_dialog_cancel))
            }
        },
    )
}
