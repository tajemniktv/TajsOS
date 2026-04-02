/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.identity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.tajemniktv.tajsos.ui.components.cards.CoreShiftCriterionCard
import com.tajemniktv.tajsos.ui.components.cards.DirectionCommitmentCard
import com.tajemniktv.tajsos.ui.components.cards.DistinctionQuestionCard
import com.tajemniktv.tajsos.ui.screens.GroupedOpenLoopSection
import com.tajemniktv.tajsos.ui.screens.formatProtocolTimestamp
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.identity_desc
import tajsos.composeapp.generated.resources.identity_title

object IdentityDashboardBlockRegistry {
    private val renderers: Map<String, IdentityDashboardBlockRenderer> =
        mapOf(
            "identity_header" to ::renderIdentityHeader,
            "identity_signature" to ::renderIdentitySignature,
            "identity_distinction" to ::renderIdentityDistinction,
            "identity_direction" to ::renderIdentityDirection,
            "identity_coreshift" to ::renderIdentityCoreShift,
        )

    fun resolve(id: String): IdentityDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderIdentityHeader(context: IdentityDashboardContext) {
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        Text(
            text = stringResource(Res.string.identity_title),
            style = MaterialTheme.typography.displaySmall,
            color = TajsOSTheme.Text,
        )
        Text(
            text = stringResource(Res.string.identity_desc),
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun renderIdentitySignature(context: IdentityDashboardContext) {
    val snapshot = context.signatureSnapshot
    val viewModel = context.viewModel
    val onEditNode = context.onEditNode

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

    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.Surface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            border = BorderStroke(1.dp, TajsOSTheme.Border),
        ) {
            Column(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "LIFEOS SIGNATURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Capabilities $enabledCount/${capabilities.size} • Work-date coverage ${snapshot.workDateDueCoveragePercent}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
                Text(
                    "Mode of life: ${snapshot.modeOfLifeLabel.replace('_', ' ').uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Accent,
                    fontWeight = FontWeight.Bold,
                )
                if (snapshot.modeOfLifeReason.isNotBlank()) {
                    Text(
                        snapshot.modeOfLifeReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                    )
                }
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
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

        Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
            if (missingCapabilities.isNotEmpty()) {
                GroupedOpenLoopSection(
                    title = "MISSING SIGNATURE CAPABILITIES",
                    items = missingCapabilities,
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TajsOSTheme.Surface,
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                border = BorderStroke(1.dp, TajsOSTheme.Border),
            ) {
                Column(
                    modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "WORK DATE VS DUE DATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Coverage ${snapshot.workDateDueCoveragePercent}% • Missing work date ${snapshot.workDateDueItems.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                    )
                    if (snapshot.workDateDueItems.isEmpty()) {
                        Text(
                            "All active due tasks have a work date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TajsOSTheme.Success,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            snapshot.workDateDueItems.forEach { item ->
                val node = item.node
                val dueAt = node.dueAt
                val todayWorkAt =
                    kotlin.time.Clock.System
                        .now()
                        .toEpochMilliseconds()
                val dueMinusDay = dueAt?.minus(24L * 60 * 60 * 1000)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TajsOSTheme.Surface,
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                    border = BorderStroke(1.dp, TajsOSTheme.Border),
                ) {
                    Column(
                        modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            node.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TajsOSTheme.Text,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Due ${dueAt?.let { formatProtocolTimestamp(it) } ?: "No due date"} • Work date missing",
                            style = MaterialTheme.typography.bodySmall,
                            color = TajsOSTheme.Muted,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
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
}

@Composable
private fun renderIdentityDistinction(context: IdentityDashboardContext) {
    val snapshot = context.secondBrainSnapshot
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.Surface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            border = BorderStroke(1.dp, TajsOSTheme.Border),
        ) {
            Column(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "LIFEOS VS SECOND BRAIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Second Brain coverage ${snapshot.secondBrainCoveragePercent}% • LifeOS coverage ${snapshot.lifeOSCoveragePercent}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
                Text(
                    "System posture: ${snapshot.postureLabel.replace('_', ' ').uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Accent,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (snapshot.secondBrainQuestions.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "SECOND BRAIN-ORIENTED QUESTIONS",
                items = snapshot.secondBrainQuestions.map { it.question },
            )
            snapshot.secondBrainQuestions.forEach { item ->
                DistinctionQuestionCard(item = item)
            }
        }

        if (snapshot.lifeOSQuestions.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "LIFEOS-ORIENTED QUESTIONS",
                items = snapshot.lifeOSQuestions.map { it.question },
            )
            snapshot.lifeOSQuestions.forEach { item ->
                DistinctionQuestionCard(item = item)
            }
        }
    }
}

@Composable
private fun renderIdentityDirection(context: IdentityDashboardContext) {
    val snapshot = context.directionSnapshot
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.Surface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            border = BorderStroke(1.dp, TajsOSTheme.Border),
        ) {
            Column(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "COMBINED DIRECTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
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
                    color = TajsOSTheme.Muted,
                )
            }
        }

        if (snapshot.practicalitySignals.isNotEmpty()) {
            GroupedOpenLoopSection(
                title = "PRACTICALITY SIGNALS",
                items = snapshot.practicalitySignals,
            )
        }
        snapshot.commitments.forEach { item ->
            DirectionCommitmentCard(item = item)
        }
    }
}

@Composable
private fun renderIdentityCoreShift(context: IdentityDashboardContext) {
    val snapshot = context.coreShiftSnapshot
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.Surface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            border = BorderStroke(1.dp, TajsOSTheme.Border),
        ) {
            Column(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "CORE LIFEOS SHIFT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Section 1 completion ${snapshot.completionPercent}% • Connected ${if (snapshot.connectedProperly) "YES" else "NO"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
                snapshot.integrationWarning?.let { warning ->
                    Text(
                        warning,
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        snapshot.items.forEach { item ->
            CoreShiftCriterionCard(item = item)
        }
    }
}
