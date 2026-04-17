/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
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
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.components.screen.SplitScreenScaffold
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Finance dashboard screen rendering the unified state of financial systems.
 *
 * Performance note: Complex list filtering and metric calculations are hoisted into `remember`
 * blocks keyed by their source data (`snapshot` and `allNodes`) to prevent expensive redundant
 * traversals during unrelated recompositions (e.g., when toggling the maintenance view tab).
 *
 * @param viewModel Main view model to collect state.
 * @param onEditNode Callback to open the editor for a node.
 */
/**
 * Central finance entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of finance state.
 * @param onEditNode Node edit callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun FinancesRoute(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    val snapshot by viewModel.maintenanceSnapshot.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    var maintenanceView by remember { mutableStateOf(FinanceMaintenanceView.Queue) }

    val queue = remember(snapshot) { DomainLensQueries.financeMaintenanceItems(snapshot) }
    val recurring = remember(snapshot) { DomainLensQueries.financeRecurringItems(snapshot) }
    val overdue = remember(snapshot) { DomainLensQueries.financeOverdueItems(snapshot) }

    val actionItems = remember(allNodes) { DomainLensQueries.financeActionItems(allNodes) }
    val knowledgeItems = remember(allNodes) { DomainLensQueries.financeKnowledgeItems(allNodes) }
    val deadlineItems = remember(allNodes) { DomainLensQueries.financeDeadlineItems(allNodes) }

    val itemsInView =
        when (maintenanceView)
        {
            FinanceMaintenanceView.Queue -> queue
            FinanceMaintenanceView.Recurring -> recurring
            FinanceMaintenanceView.Overdue -> overdue
        }

    val allItems = remember(queue, recurring, overdue) {
        (queue + recurring + overdue).distinctBy { it.node.node.id }
    }

    val recentItems = remember(deadlineItems, actionItems, knowledgeItems) {
        (deadlineItems + actionItems + knowledgeItems)
            .distinctBy { it.node.id }
            .sortedByDescending { it.node.updatedAt }
    }

    val liquidity = remember(recentItems) {
        recentItems.sumOf {
            financeSyntheticLiquidity(
                it.node.title,
            )
        }
    }

    val bars = remember(actionItems, knowledgeItems, deadlineItems, queue, recurring, overdue) {
        listOf(
            (actionItems.size + 1).coerceAtLeast(1),
            (knowledgeItems.size + 1).coerceAtLeast(1),
            (deadlineItems.size + 1).coerceAtLeast(1),
            (queue.size + recurring.size + 1).coerceAtLeast(1),
            (overdue.size + 1).coerceAtLeast(1),
        )
    }

    val confidence = remember(snapshot) { (100 - snapshot.adminDebtMeter / 2).coerceIn(35, 98) }

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
                        listOf(TajsOSTheme.Background, TajsOSTheme.Surface.copy(alpha = 0.45f)),
                    ),
                ),
    ) {
        val surface =
            if (maxWidth > 980.dp) FinanceDashboardSurface.DESKTOP else FinanceDashboardSurface.MOBILE
        val plan = remember(surface) { buildFinanceDashboardPlan(surface) }

        FinancesScreen(
            context = context,
            plan = plan,
            surface = surface,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless finance screen content.
 *
 * @param context Finance dashboard context.
 * @param plan Finance dashboard plan.
 * @param surface Current UI surface mode.
 * @param onNavigate Navigation callback.
 */
@Composable
fun FinancesScreen(
    context: FinanceDashboardContext,
    plan: FinanceDashboardPlan,
    surface: FinanceDashboardSurface,
    onNavigate: (String) -> Unit,
) {
    SplitScreenScaffold(
        isSplitLayout = surface == FinanceDashboardSurface.DESKTOP,
        screen = Screen.Finances,
        onNavigate = onNavigate,
        scrollBehavior = ScreenScrollBehavior.None,
        primaryWeight = 1.3f,
        secondaryWeight = 1f,
        primary = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
            ) {
                items(plan.primary, key = { it.id }) { block ->
                    FinanceDashboardBlockRegistry.resolve(block.id)?.invoke(context)
                }
            }
        },
        secondary =
            if (surface == FinanceDashboardSurface.DESKTOP) {
                {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                    ) {
                        items(plan.secondary, key = { it.id }) { block ->
                            FinanceDashboardBlockRegistry.resolve(block.id)?.invoke(context)
                        }
                    }
                }
            } else {
                null
            },
    )
}
