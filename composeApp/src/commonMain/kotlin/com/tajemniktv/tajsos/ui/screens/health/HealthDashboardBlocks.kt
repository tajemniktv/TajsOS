/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.health

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
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.domain.lens.DomainLensQueries
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.screens.GroupedOpenLoopSection
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

object HealthDashboardBlockRegistry {
    private val renderers: Map<String, HealthDashboardBlockRenderer> =
        mapOf("health_main" to ::renderHealthMainBlock)

    fun resolve(id: String): HealthDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderHealthMainBlock(context: HealthDashboardContext) {
    HealthMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

/**
 * Primary Health lens block that surfaces maintenance and track snapshots.
 */
@Composable
internal fun HealthMainBlock(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val allNodes by viewModel.allNodes.collectAsState()
    val maintenanceSnapshot by viewModel.maintenanceSnapshot.collectAsState()
    val trackEntries by viewModel.trackEntries.collectAsState()

    val healthQueue = DomainLensQueries.healthMaintenanceItems(maintenanceSnapshot)
    val overdueHealth = DomainLensQueries.healthOverdueItems(maintenanceSnapshot)
    val healthActions = DomainLensQueries.healthActionItems(allNodes)
    val healthKnowledge = DomainLensQueries.healthKnowledgeItems(allNodes)
    val latestTrack = trackEntries.firstOrNull()

    Column(
        modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.CardSurface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        ) {
            Column(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    "HEALTH LENS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                )
                Text(
                    "Actions ${healthActions.size} • Maintenance ${healthQueue.size} • Knowledge ${healthKnowledge.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
                Text(
                    "Latest track: mood ${latestTrack?.moodScore ?: "-"} • energy ${latestTrack?.energyScore ?: "-"} • focus ${latestTrack?.focusScore ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
            }
        }

        if (healthQueue.isEmpty() && healthActions.isEmpty() && healthKnowledge.isEmpty()) {
            EmptyState("No health-related items detected yet.")
            return@Column
        }

        if (overdueHealth.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "OVERDUE HEALTH MAINTENANCE",
                items = overdueHealth.map { it.node.node.title },
            )
        }

        if (healthActions.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "HEALTH ACTIONS",
                items = healthActions.take(8).map { it.node.title },
            )
        }

        if (healthKnowledge.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "HEALTH NOTES & RECORDS",
                items = healthKnowledge.take(8).map { it.node.title },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
            healthQueue.forEach { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TajsOSTheme.CardSurface,
                    shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                    onClick = { onEditNode(item.node.node.id) },
                ) {
                    Column(
                        modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            item.node.node.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TajsOSTheme.Text,
                        )
                        Text(
                            "Type ${
                                (item.node.node.maintenanceType ?: "manual").replace(
                                    '_',
                                    ' ',
                                )
                            } • Urgency ${item.urgency}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TajsOSTheme.Muted,
                        )
                    }
                }
            }
        }
    }
}
