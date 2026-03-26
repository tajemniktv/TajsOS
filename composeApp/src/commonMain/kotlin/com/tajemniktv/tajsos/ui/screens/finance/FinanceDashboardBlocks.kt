/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MaintenanceStatusItem
import com.tajemniktv.tajsos.ui.components.cards.MaintenanceCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.screens.maintenanceTypes
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
internal fun renderFinanceHeaderBlock(context: com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardContext) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface.copy(alpha = 0.6f),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "NEURAL FINANCES",
                style = MaterialTheme.typography.displaySmall,
                color = TactileTheme.Text,
            )
            Text(
                "Financial dashboard for liquidity, cashflow velocity, and maintenance operations.",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("LIVE") })
                AssistChip(onClick = {}, label = { Text("SYNC READY") })
                AssistChip(
                    onClick = {},
                    label = { Text("EXPENSE") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            null,
                            modifier = Modifier.size(14.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
internal fun renderFinanceMetricsBlock(context: com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardContext) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            color = TactileTheme.Surface.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, TactileTheme.Border),
            shape = RoundedCornerShape(TactileTheme.RadiusMd),
        ) {
            Column(
                modifier = Modifier.padding(TactileTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "TOTAL PORTFOLIO LIQUIDITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "$${"%,.2f".format(context.liquidity)}",
                    style = MaterialTheme.typography.displayMedium,
                    color = TactileTheme.Text,
                    fontWeight = FontWeight.ExtraBold,
                )
                _root_ide_package_.com.tajemniktv.tajsos.ui.screens.finance.FinanceMiniBars(
                    values = context.bars,
                    bigLast = false,
                )
            }
        }
        Surface(
            modifier = Modifier.weight(1f),
            color = TactileTheme.Surface.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, TactileTheme.Border),
            shape = RoundedCornerShape(TactileTheme.RadiusMd),
        ) {
            Column(
                modifier = Modifier.padding(TactileTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "CASH FLOW VELOCITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "IN ${context.recurring.size + context.queue.size} • OUT ${context.overdue.size + context.queue.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                )
                _root_ide_package_.com.tajemniktv.tajsos.ui.screens.finance.FinanceMiniBars(
                    values = context.bars,
                    bigLast = true,
                )
            }
        }
    }
}

@Composable
internal fun renderFinanceActivityBlock(context: com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardContext) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, TactileTheme.Border),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "RECENT ACTIVITY",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            for (item in context.allItems.take(5)) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { context.onEditNode(item.node.node.id) }
                            .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.node.node.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TactileTheme.Text,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            item.node.node.maintenanceType
                                ?.uppercase() ?: "MANUAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                        )
                    }
                    Text(
                        "$${
                            "%,.2f".format(
                                _root_ide_package_.com.tajemniktv.tajsos.ui.screens.finance.financeSyntheticTxn(
                                    item.node.node.title,
                                ),
                            )
                        }",
                        color = TactileTheme.Primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
internal fun renderFinanceInsightsBlock(context: com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardContext) {
    val snapshot =
        context.viewModel.maintenanceSnapshot
            .collectAsState()
            .value
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, TactileTheme.Border),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "NEURAL INSIGHTS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            val insightItems =
                listOfNotNull(
                    snapshot.overdueWarning,
                    if (snapshot.breakIfIgnored.isNotEmpty()) "Break risk: ${snapshot.breakIfIgnored.size} item(s)." else null,
                    "Admin debt: ${snapshot.adminDebtMeter}%",
                )
            for (item in insightItems) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TactileTheme.Background.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(TactileTheme.RadiusSm),
                    border = BorderStroke(1.dp, TactileTheme.Border.copy(alpha = 0.7f)),
                ) {
                    Text(
                        item,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                }
            }
            Text(
                "MODEL CONFIDENCE ${context.confidence}%",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            LinearProgressIndicator(
                progress = { context.confidence / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = TactileTheme.Primary,
                trackColor = TactileTheme.Border.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
internal fun renderFinanceVaultBlock(context: com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardContext) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, TactileTheme.Border),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "VAULT STATUS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                fontWeight = FontWeight.Bold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.Lock, null, tint = TactileTheme.Primary)
                Text(
                    "ENCRYPTED",
                    style = MaterialTheme.typography.titleMedium,
                    color = TactileTheme.Text,
                    fontWeight = FontWeight.Bold,
                )
            }
            AssistChip(
                onClick = {},
                label = { Text("SYNC VAULT") },
                leadingIcon = { Icon(Icons.Default.Sync, null, modifier = Modifier.size(14.dp)) },
            )
        }
    }
}

@Composable
internal fun renderFinanceQueueControlsBlock(context: com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardContext) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, TactileTheme.Border),
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "QUEUE",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                fontWeight = FontWeight.Bold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                _root_ide_package_.com.tajemniktv.tajsos.ui.screens.finance.FinanceMaintenanceView.entries.forEach { view ->
                    FilterChip(
                        selected = context.maintenanceView == view,
                        onClick = { context.onMaintenanceViewChange(view) },
                        label = { Text(view.label) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun renderFinanceQueueListBlock(context: com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardContext) {
    if (context.itemsInView.isEmpty()) {
        EmptyState(message = "No finance maintenance items in ${context.maintenanceView.label.lowercase()}.")
        return
    }
    for (item in context.itemsInView) {
        MaintenanceCard(
            item = item,
            areaName = context.allAreas.find { it.id == item.node.node.areaId }?.title,
            maintenanceTypes = maintenanceTypes,
            onEditNode = context.onEditNode,
            onSetType = { type -> context.viewModel.updateMaintenanceType(item.node.node, type) },
            onSetRecurring = { interval ->
                context.viewModel.setMaintenanceRecurring(
                    item.node.node,
                    interval,
                )
            },
            onSetOverdue = { timestamp ->
                context.viewModel.setMaintenanceOverdueAt(
                    item.node.node,
                    timestamp,
                )
            },
            onResolve = { context.viewModel.updateNodeStatus(item.node.node, "done") },
            onArchive = { context.viewModel.archiveNode(item.node.node) },
        )
    }
}

@Composable
private fun FinanceMiniBars(
    values: List<Int>,
    bigLast: Boolean,
) {
    val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    Row(
        modifier = Modifier.fillMaxWidth().height(88.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEachIndexed { index, value ->
            val factor = value.toFloat() / max
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height((16 + factor * 56 + if (bigLast && index == values.lastIndex) 8 else 0).dp)
                        .background(
                            color =
                                if (bigLast && index == values.lastIndex) {
                                    TactileTheme.Primary
                                } else {
                                    TactileTheme.Primary.copy(
                                        alpha = 0.55f,
                                    )
                                },
                            shape = RoundedCornerShape(6.dp),
                        ),
            )
        }
    }
}

internal fun financeSyntheticLiquidity(title: String): Double = 500.0 + title.length * 140.0 + (title.sumOf { it.code } % 91) * 13.0

internal fun financeSyntheticTxn(title: String): Double = 40.0 + title.length * 12.0 + (title.sumOf { it.code } % 33) * 4.0
