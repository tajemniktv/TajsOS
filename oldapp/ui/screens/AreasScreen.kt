/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
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
import com.tajemniktv.tajsos.data.AreaEntity

@Composable
fun AreasScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel, onNavigateTo: (String) -> Unit) {
    val areas by viewModel.allAreas.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)
    ) {
        Text(
            text = "AREAS",
            style = MaterialTheme.typography.displaySmall,
            color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
        )
        Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd))

        if (areas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No areas yet.", color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
                    Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd))
                    Button(onClick = { showAddDialog = true }) {
                        Text("CREATE FIRST AREA")
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm)) {
                items(areas) { area ->
                    _root_ide_package_.com.tajemniktv.tajsos.ui.screens.AreaItem(area) {
                        onNavigateTo(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.AreaDetail.route.replace(
                            "{areaId}",
                            area.id.toString()
                        )
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Leave space for global FAB
                }
            }
        }
    }

    if (showAddDialog) {
        _root_ide_package_.com.tajemniktv.tajsos.ui.screens.AddAreaDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addArea(name)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AreaItem(area: AreaEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
        shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(1.dp, _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)) {
            Text(area.name.uppercase(), style = MaterialTheme.typography.titleMedium, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
        }
    }
}

@Composable
fun AddAreaDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NEW AREA") },
        text = {
            TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text("CREATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}
