/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.AreaHealthMetrics
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.cards.AreaHealthOverviewCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.areas_create_first
import tajsos.composeapp.generated.resources.areas_dialog_cancel
import tajsos.composeapp.generated.resources.areas_dialog_create
import tajsos.composeapp.generated.resources.areas_dialog_name
import tajsos.composeapp.generated.resources.areas_dialog_new
import tajsos.composeapp.generated.resources.areas_empty
import tajsos.composeapp.generated.resources.areas_title

@Composable
fun AreasScreen(
    viewModel: MainViewModel,
    onNavigateTo: (String) -> Unit,
) {
    val areas by viewModel.allAreas.collectAsState()
    val areaSnapshot by viewModel.areaHealthSnapshot.collectAsState()
    val metricsById = remember(areaSnapshot.areas) { areaSnapshot.areas.associateBy { it.areaId } }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.areas_title),
                style = MaterialTheme.typography.displaySmall,
                color = TactileTheme.Text,
            )
            OutlinedButton(onClick = { showAddDialog = true }) {
                Text("NEW")
            }
        }

        if (areas.isNotEmpty()) {
            AreaHealthOverviewCard(
                dominantArea = areas.find { it.id == areaSnapshot.dominantAreaId }?.title,
                imbalanceScore = areaSnapshot.imbalanceScore,
                imbalanceLabel = areaSnapshot.imbalanceLabel,
                disappearingCount = areaSnapshot.disappearingAreaIds.size,
            )
            OutlinedButton(
                onClick = { viewModel.addSuggestedAreas() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("ADD SUGGESTED AREAS")
            }
        }

        if (areas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(Res.string.areas_empty), color = TactileTheme.Muted)
                    Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))
                    Button(onClick = { showAddDialog = true }) {
                        Text(stringResource(Res.string.areas_create_first))
                    }
                    Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
                    OutlinedButton(onClick = { viewModel.addSuggestedAreas() }) {
                        Text("USE SUGGESTED AREAS")
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(areas, key = { it.id }) { area ->
                    AreaItem(
                        area = area,
                        metrics = metricsById[area.id],
                    ) {
                        onNavigateTo(
                            Screen.AreaDetail.route.replace(
                                "{areaId}",
                                area.id.toString(),
                            ),
                        )
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
            },
        )
    }
}

@Composable
fun AreaItem(
    area: NodeEntity,
    metrics: AreaHealthMetrics?,
    onClick: () -> Unit,
) {
    val status = metrics?.status ?: "stable"
    val color = areaStatusColor(status)
    val stressLoad = metrics?.stressLoad ?: 0

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    area.title.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TactileTheme.Primary,
                )
                Text(
                    status.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                "Open loops: ${metrics?.openLoops ?: 0} • Deadlines: ${metrics?.deadlines ?: 0} • Overdue: ${metrics?.overdueDeadlines ?: 0}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "Recent activity: ${metrics?.recentActivity ?: 0} • Neglected: ${metrics?.neglectedDays ?: 0}d",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Load $stressLoad%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    modifier = Modifier.width(72.dp),
                )
                LinearProgressIndicator(
                    progress = { (stressLoad / 100f).coerceIn(0f, 1f) },
                    color = color,
                    trackColor = TactileTheme.Border,
                    modifier = Modifier.weight(1f).height(6.dp),
                )
            }
            if (metrics?.isDisappearing == true) {
                HorizontalDivider(color = TactileTheme.Error.copy(alpha = 0.25f))
                Text(
                    "Disappearing from radar",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Error,
                )
            }
        }
    }
}

@Composable
fun AddAreaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.areas_dialog_new)) },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.areas_dialog_name)) },
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(Res.string.areas_dialog_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.areas_dialog_cancel)) }
        },
    )
}

private fun areaStatusColor(status: String): Color =
    when (status)
    {
        "on_fire" -> TactileTheme.Error
        "overloaded" -> TactileTheme.Accent
        "neglected" -> TactileTheme.Muted
        "active" -> TactileTheme.Primary
        else -> TactileTheme.Success
    }
