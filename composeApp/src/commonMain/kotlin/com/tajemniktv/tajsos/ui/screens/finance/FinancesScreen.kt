/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.domain.lens.DomainLensQueries
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

@Composable
fun FinancesScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val snapshot by viewModel.maintenanceSnapshot.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    var maintenanceView by remember { mutableStateOf(FinanceMaintenanceView.Queue) }

    val queue = DomainLensQueries.financeMaintenanceItems(snapshot)
    val recurring = DomainLensQueries.financeRecurringItems(snapshot)
    val overdue = DomainLensQueries.financeOverdueItems(snapshot)
    val actionItems = DomainLensQueries.financeActionItems(allNodes)
    val knowledgeItems = DomainLensQueries.financeKnowledgeItems(allNodes)
    val deadlineItems = DomainLensQueries.financeDeadlineItems(allNodes)
    val itemsInView =
        when (maintenanceView)
        {
            FinanceMaintenanceView.Queue -> queue
            FinanceMaintenanceView.Recurring -> recurring
            FinanceMaintenanceView.Overdue -> overdue
        }
    val allItems = (queue + recurring + overdue).distinctBy { it.node.node.id }
    val recentItems =
        (deadlineItems + actionItems + knowledgeItems)
            .distinctBy { it.node.id }
            .sortedByDescending { it.node.updatedAt }
    val liquidity =
        recentItems.sumOf {
            financeSyntheticLiquidity(
                it.node.title,
            )
        }
    val bars =
        listOf(
            (actionItems.size + 1).coerceAtLeast(1),
            (knowledgeItems.size + 1).coerceAtLeast(1),
            (deadlineItems.size + 1).coerceAtLeast(1),
            (queue.size + recurring.size + 1).coerceAtLeast(1),
            (overdue.size + 1).coerceAtLeast(1),
        )
    val confidence = (100 - snapshot.adminDebtMeter / 2).coerceIn(35, 98)

    val context =
        remember(
            viewModel,
            allAreas,
            allNodes,
            queue,
            recurring,
            overdue,
            allItems,
            itemsInView,
            actionItems,
            knowledgeItems,
            deadlineItems,
            recentItems,
            confidence,
            liquidity,
            bars,
            maintenanceView,
            onEditNode,
        ) {
            FinanceDashboardContext(
                viewModel = viewModel,
                allAreas = allAreas,
                queue = queue,
                recurring = recurring,
                overdue = overdue,
                allItems = allItems,
                itemsInView = itemsInView,
                actionItems = actionItems,
                knowledgeItems = knowledgeItems,
                deadlineItems = deadlineItems,
                recentItems = recentItems,
                confidence = confidence,
                liquidity = liquidity,
                bars = bars,
                maintenanceView = maintenanceView,
                onMaintenanceViewChange = { maintenanceView = it },
                onEditNode = onEditNode,
            )
        }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(TajsOSTheme.Background, TajsOSTheme.Surface.copy(alpha = 0.45f))
                    )
                ),
    ) {
        val surface =
            if (maxWidth >
                980.dp
            ) {
                FinanceDashboardSurface.DESKTOP
            } else {
                FinanceDashboardSurface.MOBILE
            }
        val plan =
            remember(surface) {
                buildFinanceDashboardPlan(
                    surface,
                )
            }

        if (surface == FinanceDashboardSurface.MOBILE) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)
            ) {
                items(plan.primary, key = { it.id }) { block ->
                    FinanceDashboardBlockRegistry
                        .resolve(
                            block.id,
                        )?.invoke(context)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize().padding(TajsOSTheme.SpacingMd),
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1.3f),
                    verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)
                ) {
                    items(plan.primary, key = { it.id }) { block ->
                        FinanceDashboardBlockRegistry
                            .resolve(
                                block.id,
                            )?.invoke(context)
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)
                ) {
                    items(plan.secondary, key = { it.id }) { block ->
                        FinanceDashboardBlockRegistry
                            .resolve(
                                block.id,
                            )?.invoke(context)
                    }
                }
            }
        }
    }
}
