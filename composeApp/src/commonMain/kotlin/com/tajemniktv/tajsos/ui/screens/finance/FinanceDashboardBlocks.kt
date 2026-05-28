/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

@file:Suppress("FunctionName")

package com.tajemniktv.tajsos.ui.screens.finance

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.isNoteItem
import com.tajemniktv.tajsos.data.isRecordItem
import com.tajemniktv.tajsos.data.isTaskItem
import com.tajemniktv.tajsos.ui.components.cards.MaintenanceCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.lens.LensUiContract
import com.tajemniktv.tajsos.ui.screens.maintenanceTypes
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.finance_chip_expense
import tajsos.composeapp.generated.resources.finance_chip_live
import tajsos.composeapp.generated.resources.finance_chip_sync_ready
import tajsos.composeapp.generated.resources.finance_deadlines_chip
import tajsos.composeapp.generated.resources.finance_empty_queue
import tajsos.composeapp.generated.resources.finance_header_tracking
import tajsos.composeapp.generated.resources.finance_item_action
import tajsos.composeapp.generated.resources.finance_item_note
import tajsos.composeapp.generated.resources.finance_item_record
import tajsos.composeapp.generated.resources.finance_metrics_reference_count
import tajsos.composeapp.generated.resources.finance_queue_label
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

/**
 * Renders the finance dashboard header shell with high-level status chips and dataset volume context.
 *
 * @param context Finance dashboard state and callbacks used to project header metadata.
 */
@Composable
internal fun renderFinanceHeaderBlock(context: FinanceDashboardContext) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface.copy(alpha = 0.6f),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(LensUiContract.financeLens.title),
                style = MaterialTheme.typography.displaySmall,
                color = TajsOSTheme.Text,
            )
            Text(
                stringResource(LensUiContract.financeLens.subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            Text(
                stringResource(
                    Res.string.finance_header_tracking,
                    context.actionItems.size,
                    context.knowledgeItems.size,
                    context.deadlineItems.size,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(Res.string.finance_chip_live)) },
                )
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(Res.string.finance_chip_sync_ready)) },
                )
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(Res.string.finance_chip_expense)) },
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
internal fun renderFinanceMetricsBlock(context: FinanceDashboardContext) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            color = TajsOSTheme.CardSurface.copy(alpha = 0.65f),
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        ) {
            Column(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "REFERENCE WEIGHT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    formatCurrency(context.liquidity),
                    style = MaterialTheme.typography.displayMedium,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    stringResource(
                        Res.string.finance_metrics_reference_count,
                        context.knowledgeItems.size,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
                FinanceMiniBars(
                    values = context.bars,
                    bigLast = false,
                )
            }
        }
        Surface(
            modifier = Modifier.weight(1f),
            color = TajsOSTheme.CardSurface.copy(alpha = 0.65f),
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        ) {
            Column(
                modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "PRESSURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${context.deadlineItems.size} dated items • ${context.overdue.size} overdue • ${context.queue.size + context.recurring.size} recurring/admin",
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
                FinanceMiniBars(
                    values = context.bars,
                    bigLast = true,
                )
            }
        }
    }
}

@Composable
internal fun renderFinanceActivityBlock(context: FinanceDashboardContext) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "RECENT ACTIVITY",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            for (item in context.recentItems.take(5)) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { context.onEditNode(item.node.id) }
                            .pointerHoverIcon(PointerIcon.Hand)
                            .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.node.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TajsOSTheme.Text,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            financeItemLabel(item.node),
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted,
                        )
                    }
                    Text(
                        formatCurrency(
                            financeSyntheticTxn(
                                item.node.title,
                            ),
                        ),
                        color = TajsOSTheme.Primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
internal fun renderFinanceInsightsBlock(context: FinanceDashboardContext) {
    val snapshot =
        context.viewModel.maintenanceSnapshot
            .collectAsState()
            .value
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "LENS INSIGHTS",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.Bold,
            )
            val insightItems =
                listOfNotNull(
                    snapshot.overdueWarning,
                    if (snapshot.breakIfIgnored.isNotEmpty()) "Break risk: ${snapshot.breakIfIgnored.size} item(s)." else null,
                    "Admin debt: ${snapshot.adminDebtMeter}%",
                    if (context.deadlineItems.isNotEmpty()) "Upcoming finance deadlines: ${context.deadlineItems.size}" else null,
                )
            for (item in insightItems) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TajsOSTheme.Background.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                ) {
                    Text(
                        item,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Muted,
                    )
                }
            }
            Text(
                "MODEL CONFIDENCE ${context.confidence}%",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            LinearProgressIndicator(
                progress = { context.confidence / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = TajsOSTheme.Primary,
                trackColor = TajsOSTheme.Border.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
internal fun renderFinanceVaultBlock(context: FinanceDashboardContext) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "REFERENCE COVERAGE",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
                fontWeight = FontWeight.Bold,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.Lock, null, tint = TajsOSTheme.Primary)
                Text(
                    "${context.knowledgeItems.size} SAVED",
                    style = MaterialTheme.typography.titleMedium,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.Bold,
                )
            }
            AssistChip(
                onClick = {},
                label = {
                    Text(
                        stringResource(
                            Res.string.finance_deadlines_chip,
                            context.deadlineItems.size,
                        ),
                    )
                },
                leadingIcon = { Icon(Icons.Default.Sync, null, modifier = Modifier.size(14.dp)) },
            )
        }
    }
}

@Composable
internal fun renderFinanceQueueControlsBlock(context: FinanceDashboardContext) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(Res.string.finance_queue_label),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
                fontWeight = FontWeight.Bold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                FinanceMaintenanceView.entries.forEach { view ->
                    FilterChip(
                        selected = context.maintenanceView == view,
                        onClick = { context.onMaintenanceViewChange(view) },
                        label = { Text(stringResource(view.label)) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun renderFinanceQueueListBlock(context: FinanceDashboardContext) {
    if (context.itemsInView.isEmpty()) {
        EmptyState(
            message =
                stringResource(
                    Res.string.finance_empty_queue,
                    stringResource(context.maintenanceView.label).lowercase(),
                ),
        )
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
private fun financeItemLabel(node: NodeEntity): String =
    when {
        node.isTaskItem() -> {
            node.maintenanceType?.uppercase()
                ?: stringResource(Res.string.finance_item_action)
        }

        node.isRecordItem() -> {
            stringResource(Res.string.finance_item_record)
        }

        node.isNoteItem() -> {
            stringResource(Res.string.finance_item_note)
        }

        else -> {
            node.type.uppercase()
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
                                    TajsOSTheme.Primary
                                } else {
                                    TajsOSTheme.Primary.copy(
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

/**
 * Formats a numeric amount as USD-like currency in common code without relying on JVM-only format APIs.
 */
private fun formatCurrency(amount: Double): String {
    val roundedCents = (amount * 100).roundToInt()
    val sign = if (roundedCents < 0) "-" else ""
    val absoluteCents = abs(roundedCents)
    val wholePart = absoluteCents / 100
    val centsPart = absoluteCents % 100
    val groupedWhole =
        wholePart
            .toString()
            .reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
    return "$sign$$groupedWhole.${centsPart.toString().padStart(2, '0')}"
}
