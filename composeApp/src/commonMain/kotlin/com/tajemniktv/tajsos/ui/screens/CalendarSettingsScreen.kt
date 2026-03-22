/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@Composable
fun CalendarSettingsScreen(viewModel: MainViewModel) {
    val providers by viewModel.calendarProviders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TactileTheme.SpacingMd)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.cal_settings_title),
                style = MaterialTheme.typography.displaySmall,
                color = TactileTheme.Text
            )
            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.cal_settings_add),
                    tint = TactileTheme.Primary
                )
            }
        }

        Spacer(Modifier.height(TactileTheme.SpacingLg))

        if (providers.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.cal_settings_empty), color = TactileTheme.Muted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(providers) { provider ->
                    ProviderRow(
                        provider = provider,
                        onDelete = { viewModel.deleteCalendarProvider(provider) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCalendarDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, type, url ->
                viewModel.addCalendarProvider(name, type, url)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ProviderRow(
    provider: com.tajemniktv.tajsos.data.CalendarProviderEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    provider.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TactileTheme.Text
                )
                Text(
                    provider.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted
                )
                val url = provider.url
                if (url != null) {
                    Text(
                        url,
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                        maxLines = 1
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.archive_delete),
                    tint = TactileTheme.Error
                )
            }
        }
    }
}

@Composable
fun AddCalendarDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String?) -> Unit
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
                    label = { Text(stringResource(Res.string.cal_settings_dialog_name)) }
                )
                TextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(Res.string.cal_settings_dialog_url)) }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, type, url) }) {
                Text(stringResource(Res.string.cal_settings_dialog_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cal_settings_dialog_cancel))
            }
        }
    )
}
