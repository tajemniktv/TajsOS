/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.areas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.ProjectState
import com.tajemniktv.tajsos.data.projectStateOrNull
import com.tajemniktv.tajsos.ui.AreaHealthMetrics
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.add_suggested_areas
import tajsos.composeapp.generated.resources.areas_balance
import tajsos.composeapp.generated.resources.areas_create_first
import tajsos.composeapp.generated.resources.areas_dominant
import tajsos.composeapp.generated.resources.areas_distribution
import tajsos.composeapp.generated.resources.areas_dialog_cancel
import tajsos.composeapp.generated.resources.areas_dialog_create
import tajsos.composeapp.generated.resources.areas_dialog_name
import tajsos.composeapp.generated.resources.areas_dialog_new
import tajsos.composeapp.generated.resources.areas_empty
import tajsos.composeapp.generated.resources.areas_enter
import tajsos.composeapp.generated.resources.areas_load
import tajsos.composeapp.generated.resources.areas_neglected
import tajsos.composeapp.generated.resources.areas_new
import tajsos.composeapp.generated.resources.areas_open_responsibilities
import tajsos.composeapp.generated.resources.areas_projects_active
import tajsos.composeapp.generated.resources.areas_recent_activity
import tajsos.composeapp.generated.resources.areas_status
import tajsos.composeapp.generated.resources.areas_title
import tajsos.composeapp.generated.resources.areas_upcoming_deadlines
import tajsos.composeapp.generated.resources.use_suggested_areas

object AreasDashboardBlockRegistry {
    private val renderers: Map<String, AreasDashboardBlockRenderer> =
        mapOf("areas_main" to ::renderAreasMainBlock)

    fun resolve(id: String): AreasDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderAreasMainBlock(context: AreasDashboardContext) {
    AreasMainBlock(viewModel = context.viewModel, onNavigateTo = context.onNavigateTo)
}

@Composable
internal fun AreasMainBlock(
    viewModel: MainViewModel,
    onNavigateTo: (String) -> Unit,
) {
    val areas by viewModel.allAreas.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val areaSnapshot by viewModel.areaHealthSnapshot.collectAsState()
    val metricsById = remember(areaSnapshot.areas) { areaSnapshot.areas.associateBy { it.areaId } }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
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
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                OutlinedButton(onClick = { viewModel.addSuggestedAreas() }) {
                    Text(stringResource(Res.string.add_suggested_areas))
                }
                OutlinedButton(onClick = { showAddDialog = true }) {
                    Text(stringResource(Res.string.areas_new))
                }
            }
        }

        if (areas.isEmpty()) {
            EmptyState(message = stringResource(Res.string.areas_empty)) {
                Spacer(modifier = Modifier.height(TactileTheme.SpacingMd))
                Button(onClick = { showAddDialog = true }) {
                    Text(stringResource(Res.string.areas_create_first))
                }
                Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
                OutlinedButton(onClick = { viewModel.addSuggestedAreas() }) {
                    Text(stringResource(Res.string.use_suggested_areas))
                }
            }
        } else {
            val dominantName = areas.find { it.id == areaSnapshot.dominantAreaId }?.title ?: "-"
            val overloadedCount = areaSnapshot.areas.count { it.status == "overloaded" || it.status == "on_fire" }
            val neglectedCount = areaSnapshot.disappearingAreaIds.size
            AreaTopSummary(
                dominantArea = dominantName,
                balanceLabel = areaSnapshot.imbalanceLabel.uppercase(),
                balanceScore = areaSnapshot.imbalanceScore,
                neglectedCount = neglectedCount,
                overloadedCount = overloadedCount,
                areas = areas,
                metricsById = metricsById,
            )
            AreasCards(areas = areas, allProjects = allProjects, metricsById = metricsById, onOpen = { id ->
                onNavigateTo(routeForAreaDetail(id))
            })
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
private fun AreaTopSummary(
    dominantArea: String,
    balanceLabel: String,
    balanceScore: Int,
    neglectedCount: Int,
    overloadedCount: Int,
    areas: List<NodeEntity>,
    metricsById: Map<Long, AreaHealthMetrics>,
) {
    Surface(color = TactileTheme.Surface, shape = RoundedCornerShape(TactileTheme.RadiusMd)) {
        Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd), verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd), verticalAlignment = Alignment.CenterVertically) {
                SummaryChip(Icons.Default.Layers, stringResource(Res.string.areas_dominant), dominantArea)
                SummaryChip(Icons.AutoMirrored.Filled.TrendingUp, stringResource(Res.string.areas_balance), "$balanceLabel ($balanceScore)")
                SummaryChip(Icons.Default.Warning, stringResource(Res.string.areas_neglected), neglectedCount.toString())
                SummaryChip(Icons.Default.Speed, stringResource(Res.string.areas_load), overloadedCount.toString())
            }
            Text(stringResource(Res.string.areas_distribution), style = MaterialTheme.typography.labelLarge, color = TactileTheme.Text)
            Row(modifier = Modifier.fillMaxWidth().height(22.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val total = areas.sumOf { (metricsById[it.id]?.activeItems ?: 0).coerceAtLeast(1) }
                areas.forEach { area ->
                    val weight = (metricsById[area.id]?.activeItems ?: 0).coerceAtLeast(1).toFloat() / total.toFloat()
                    val color = areaStatusColor(metricsById[area.id]?.status ?: "stable")
                    Box(
                        modifier = Modifier.weight(weight).fillMaxHeight().background(color.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(area.title.take(12), modifier = Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.labelSmall, color = TactileTheme.Text)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(icon: ImageVector, label: String, value: String) {
    Surface(color = TactileTheme.SurfaceLow, shape = RoundedCornerShape(TactileTheme.RadiusMd)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = TactileTheme.Primary, modifier = Modifier.size(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
                Text(value, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Text, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AreasCards(
    areas: List<NodeEntity>,
    allProjects: List<NodeEntity>,
    metricsById: Map<Long, AreaHealthMetrics>,
    onOpen: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val desktop = maxWidth > 900.dp
        if (desktop) {
            LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 300.dp), verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm), horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(count = areas.size, key = { index -> areas[index].id }) { index ->
                    val area = areas[index]
                    AreaCard(area = area, metrics = metricsById[area.id], activeProjects = allProjects.count { it.areaId == area.id && it.projectStateOrNull() == ProjectState.ACTIVE }, onClick = { onOpen(area.id) })
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                items(areas, key = { it.id }) { area ->
                    AreaCard(area = area, metrics = metricsById[area.id], activeProjects = allProjects.count { it.areaId == area.id && it.projectStateOrNull() == ProjectState.ACTIVE }, onClick = { onOpen(area.id) })
                }
            }
        }
    }
}

@Composable
private fun AreaCard(
    area: NodeEntity,
    metrics: AreaHealthMetrics?,
    activeProjects: Int,
    onClick: () -> Unit,
) {
    val status = metrics?.status ?: "stable"
    val color = areaStatusColor(status)
    val load = metrics?.stressLoad ?: 0
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(area.title, style = MaterialTheme.typography.titleMedium, color = TactileTheme.Text, fontWeight = FontWeight.Bold)
                Text(status.replace("_", " ").uppercase(), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = TactileTheme.GhostBorder)
            MetricLine(Icons.Default.Speed, stringResource(Res.string.areas_load), "$load%")
            MetricLine(Icons.Default.Folder, stringResource(Res.string.areas_projects_active), activeProjects.toString())
            MetricLine(Icons.Default.CheckCircle, stringResource(Res.string.areas_open_responsibilities), (metrics?.openLoops ?: 0).toString())
            MetricLine(Icons.Default.EventBusy, stringResource(Res.string.areas_upcoming_deadlines), "${metrics?.overdueDeadlines ?: 0}/${metrics?.deadlines ?: 0}")
            MetricLine(Icons.Default.History, stringResource(Res.string.areas_recent_activity), "${metrics?.recentActivity ?: 0}")
            MetricLine(Icons.Default.Warning, stringResource(Res.string.areas_neglected), "${metrics?.neglectedDays ?: 0}d")
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.areas_enter))
            }
        }
    }
}

@Composable
private fun MetricLine(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TactileTheme.Muted, modifier = Modifier.size(14.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Muted)
        }
        Text(value, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Text, fontWeight = FontWeight.SemiBold)
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
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text(stringResource(Res.string.areas_dialog_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.areas_dialog_cancel)) }
        },
    )
}

private fun areaStatusColor(status: String): Color =
    when (status) {
        "on_fire" -> TactileTheme.Error
        "overloaded" -> TactileTheme.Accent
        "neglected" -> TactileTheme.Muted
        "active" -> TactileTheme.Primary
        else -> TactileTheme.Success
    }

internal fun routeForAreaDetail(areaId: Long): String =
    Screen.AreaDetail.route.replace("{areaId}", areaId.toString())
