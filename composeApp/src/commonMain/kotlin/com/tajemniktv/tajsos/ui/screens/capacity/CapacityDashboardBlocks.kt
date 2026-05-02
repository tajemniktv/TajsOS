/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.capacity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.main.state.CapacitySnapshot
import com.tajemniktv.tajsos.ui.screens.GroupedOpenLoopSection
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.capacity_desc
import tajsos.composeapp.generated.resources.capacity_title

object CapacityDashboardBlocks {
    private val renderers: Map<String, CapacityDashboardBlockRenderer> =
        mapOf("capacity_main" to ::renderCapacityMainBlock)

    fun resolve(id: String): CapacityDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderCapacityMainBlock(context: CapacityDashboardContext) {
    CapacityMainBlock(viewModel = context.viewModel)
}

@Composable
internal fun CapacityMainBlock(viewModel: MainViewModel) {
    val capacitySnapshot by viewModel.capacitySnapshot.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        Text(
            text = stringResource(Res.string.capacity_title),
            style = MaterialTheme.typography.displaySmall,
            color = TajsOSTheme.Text,
        )
        Text(
            text = stringResource(Res.string.capacity_desc),
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted,
        )

        CapacityLayer(snapshot = capacitySnapshot, allAreas = allAreas)
    }
}

@Composable
internal fun CapacityLayer(
    snapshot: CapacitySnapshot,
    allAreas: List<NodeEntity>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "LOAD & CAPACITY",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Load ${snapshot.loadScore} • Fragmentation ${snapshot.fragmentationScore}",
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            listOfNotNull(
                snapshot.tooManyActiveProjectsWarning,
                snapshot.adminDebtWarning,
                snapshot.openLoopsOverloadWarning,
                snapshot.capacityMismatch,
                snapshot.unrealisticWeekSignal,
                snapshot.tooManyActiveFrontsIndicator,
                snapshot.attentionFragmentedIndicator,
                snapshot.weeklyStructuralOverloadWarning,
            ).forEach { warning ->
                Text(
                    warning,
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Error,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        if (snapshot.loadByArea.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "LOAD BY AREA",
                items =
                    snapshot.loadByArea.entries
                        .sortedByDescending { it.value }
                        .map { (areaId, score) ->
                            val areaName =
                                if (areaId == null) {
                                    "UNASSIGNED"
                                } else {
                                    allAreas.find { it.id == areaId }?.title ?: "UNKNOWN"
                                }
                            "$areaName • $score"
                        },
            )
        }
        if (snapshot.loadByMode.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "LOAD BY MODE",
                items =
                    snapshot.loadByMode.entries
                        .sortedByDescending { it.value }
                        .map { (mode, score) -> "$mode • $score" },
            )
        }
        if (snapshot.loadTrend.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "LOAD TREND",
                items = snapshot.loadTrend.map { "${it.label} • L${it.load} / F${it.fragmentation}" },
            )
        }
        if (snapshot.capacityAwareSuggestions.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "CAPACITY-AWARE SUGGESTIONS",
                items = snapshot.capacityAwareSuggestions,
            )
        }
    }
}
