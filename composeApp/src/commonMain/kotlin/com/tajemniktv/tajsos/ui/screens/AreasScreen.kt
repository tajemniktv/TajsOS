/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@Composable
fun AreasScreen(viewModel: MainViewModel, onNavigateTo: (String) -> Unit) {
    val areas by viewModel.allAreas.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TactileTheme.SpacingMd)
    ) {
        Text(
            text = stringResource(Res.string.areas_title),
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text
        )
        Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))

        if (areas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(Res.string.areas_empty), color = TactileTheme.Muted)
                    Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))
                    Button(onClick = { showAddDialog = true }) {
                        Text(stringResource(Res.string.areas_create_first))
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(areas) { area ->
                    AreaItem(area) {
                        onNavigateTo(Screen.AreaDetail.route.replace("{areaId}", area.id.toString()))
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddAreaDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addArea(name)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AreaItem(area: NodeEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Text(area.title.uppercase(), style = MaterialTheme.typography.titleMedium, color = TactileTheme.Primary)
        }
    }
}

@Composable
fun AddAreaDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.areas_dialog_new)) },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.areas_dialog_name)) })
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(Res.string.areas_dialog_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.areas_dialog_cancel)) }
        }
    )
}
