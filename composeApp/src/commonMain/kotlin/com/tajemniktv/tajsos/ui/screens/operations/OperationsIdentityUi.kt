/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.CapacitySnapshot
import com.tajemniktv.tajsos.ui.CombinedDirectionSnapshot
import com.tajemniktv.tajsos.ui.CoreLifeOSShiftSnapshot
import com.tajemniktv.tajsos.ui.LifeOSSecondBrainSnapshot
import com.tajemniktv.tajsos.ui.LifeOSSignatureSnapshot
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlin.time.Clock

@Composable
internal fun CapacityLayer(
    snapshot: CapacitySnapshot,
    allAreas: List<NodeEntity>,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "LOAD & CAPACITY",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Load ${snapshot.loadScore} • Fragmentation ${snapshot.fragmentationScore}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
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
                    color = TactileTheme.Error,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (snapshot.loadByArea.isNotEmpty()) {
            item {
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
        }
        if (snapshot.loadByMode.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "LOAD BY MODE",
                    items =
                        snapshot.loadByMode.entries
                            .sortedByDescending { it.value }
                            .map { (mode, score) -> "$mode • $score" },
                )
            }
        }
        if (snapshot.loadTrend.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "LOAD TREND",
                    items = snapshot.loadTrend.map { "${it.label} • L${it.load} / F${it.fragmentation}" },
                )
            }
        }
        if (snapshot.capacityAwareSuggestions.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "CAPACITY-AWARE SUGGESTIONS",
                    items = snapshot.capacityAwareSuggestions,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun SignatureLayer(
    viewModel: MainViewModel,
    snapshot: LifeOSSignatureSnapshot,
    onEditNode: (Long) -> Unit,
) {
    val capabilities =
        listOf(
            "Operating Modes" to snapshot.operatingModesEnabled,
            "Life Areas Health" to snapshot.areaHealthEnabled,
            "Open Loops" to snapshot.openLoopsEnabled,
            "Decision Queue" to snapshot.decisionSystemEnabled,
            "Maintenance" to snapshot.maintenanceEnabled,
            "Context Filtering" to snapshot.contextAwareFilteringEnabled,
            "Transition Protocols" to snapshot.transitionProtocolsEnabled,
            "Recovery Modes" to snapshot.recoveryModeEnabled,
            "Relationships" to snapshot.relationshipLayerEnabled,
            "Vault / Logistics" to snapshot.logisticsVaultEnabled,
            "Load / Capacity" to snapshot.loadCapacityEnabled,
            "Principles / Playbooks" to snapshot.personalPrinciplesPlaybooksEnabled,
        )

    val enabledCount = capabilities.count { it.second }
    val missingCapabilities = capabilities.filterNot { it.second }.map { it.first }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "LIFEOS SIGNATURE",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Capabilities $enabledCount/${capabilities.size} • Work-date coverage ${snapshot.workDateDueCoveragePercent}%",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "Mode of life: ${snapshot.modeOfLifeLabel.replace('_', ' ').uppercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Accent,
                fontWeight = FontWeight.Bold,
            )
            if (snapshot.modeOfLifeReason.isNotBlank()) {
                Text(
                    snapshot.modeOfLifeReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                )
            }
        }
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        capabilities.forEach { (label, enabled) ->
            FilterChip(
                selected = enabled,
                onClick = {},
                enabled = false,
                label = { Text("${label.uppercase()} • ${if (enabled) "ON" else "OFF"}") },
            )
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (missingCapabilities.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "MISSING SIGNATURE CAPABILITIES",
                    items = missingCapabilities,
                )
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Border),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "WORK DATE VS DUE DATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Coverage ${snapshot.workDateDueCoveragePercent}% • Missing work date ${snapshot.workDateDueItems.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    if (snapshot.workDateDueItems.isEmpty()) {
                        Text(
                            "All active due tasks have a work date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Success,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        items(snapshot.workDateDueItems, key = { it.node.id }) { item ->
            val node = item.node
            val dueAt = node.dueAt
            val todayWorkAt = Clock.System.now().toEpochMilliseconds()
            val dueMinusDay = dueAt?.minus(24L * 60 * 60 * 1000)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Border),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        node.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TactileTheme.Text,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Due ${dueAt?.let { formatProtocolTimestamp(it) } ?: "No due date"} • Work date missing",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        AssistChip(onClick = { onEditNode(node.id) }, label = { Text("OPEN") })
                        AssistChip(
                            onClick = { viewModel.setWorkDate(node, todayWorkAt) },
                            label = { Text("WORK TODAY") },
                        )
                        AssistChip(onClick = {
                            viewModel.setWorkDate(
                                node,
                                dueMinusDay ?: todayWorkAt,
                            )
                        }, label = { Text("WORK DUE-1D") })
                        AssistChip(
                            onClick = { viewModel.setWorkDate(node, null) },
                            label = { Text("CLEAR") },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DistinctionLayer(snapshot: LifeOSSecondBrainSnapshot) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "LIFEOS VS SECOND BRAIN",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Second Brain coverage ${snapshot.secondBrainCoveragePercent}% • LifeOS coverage ${snapshot.lifeOSCoveragePercent}%",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Text(
                "System posture: ${snapshot.postureLabel.replace('_', ' ').uppercase()}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Accent,
                fontWeight = FontWeight.Bold,
            )
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (snapshot.secondBrainQuestions.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "SECOND BRAIN-ORIENTED QUESTIONS",
                    items = snapshot.secondBrainQuestions.map { it.question },
                )
            }
            items(snapshot.secondBrainQuestions, key = { it.question }) { item ->
                DistinctionQuestionCard(item = item)
            }
        }

        if (snapshot.lifeOSQuestions.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "LIFEOS-ORIENTED QUESTIONS",
                    items = snapshot.lifeOSQuestions.map { it.question },
                )
            }
            items(snapshot.lifeOSQuestions, key = { it.question }) { item ->
                DistinctionQuestionCard(item = item)
            }
        }
    }
}

@Composable
internal fun DirectionLayer(snapshot: CombinedDirectionSnapshot) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "COMBINED DIRECTION",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Direction completion ${snapshot.completionPercent}% • Posture ${
                    snapshot.postureLabel.replace(
                        '_',
                        ' ',
                    ).uppercase()
                }",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (snapshot.practicalitySignals.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    title = "PRACTICALITY SIGNALS",
                    items = snapshot.practicalitySignals,
                )
            }
        }
        items(snapshot.commitments, key = { it.commitment }) { item ->
            DirectionCommitmentCard(item = item)
        }
    }
}

@Composable
internal fun CoreShiftLayer(snapshot: CoreLifeOSShiftSnapshot) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "CORE LIFEOS SHIFT",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Section 1 completion ${snapshot.completionPercent}% • Connected ${if (snapshot.connectedProperly) "YES" else "NO"}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            snapshot.integrationWarning?.let { warning ->
                Text(
                    warning,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Error,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        items(snapshot.items, key = { it.criterion }) { item ->
            CoreShiftCriterionCard(item = item)
        }
    }
}
