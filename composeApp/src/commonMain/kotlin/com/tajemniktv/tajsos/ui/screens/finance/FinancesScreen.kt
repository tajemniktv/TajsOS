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
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme

private val financeMaintenanceTypes = setOf("bill", "subscription", "renewal")

@Composable
fun FinancesScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val snapshot by viewModel.maintenanceSnapshot.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    var maintenanceView by remember { mutableStateOf(FinanceMaintenanceView.Queue) }

    val queue = snapshot.active.filter { it.node.node.maintenanceType in financeMaintenanceTypes }
    val recurring =
        snapshot.recurring.filter { it.node.node.maintenanceType in financeMaintenanceTypes }
    val overdue =
        snapshot.overdue.filter { it.node.node.maintenanceType in financeMaintenanceTypes }
    val itemsInView =
        when (maintenanceView)
        {
            FinanceMaintenanceView.Queue -> queue
            FinanceMaintenanceView.Recurring -> recurring
            FinanceMaintenanceView.Overdue -> overdue
        }
    val allItems = (queue + recurring + overdue).distinctBy { it.node.node.id }
    val liquidity =
        allItems.sumOf {
            financeSyntheticLiquidity(
                it.node.node.title,
            )
        }
    val bars =
        listOf(
            queue.size + 2,
            recurring.size + 1,
            overdue.size + 1,
            snapshot.breakIfIgnored.size + 1,
            (snapshot.adminDebtMeter / 10).coerceAtLeast(1),
        )
    val confidence = (100 - snapshot.adminDebtMeter / 2).coerceIn(35, 98)

    val context =
        remember(
            viewModel,
            allAreas,
            queue,
            recurring,
            overdue,
            allItems,
            itemsInView,
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
                        listOf(TactileTheme.Background, TactileTheme.Surface.copy(alpha = 0.45f)),
                    ),
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
                modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
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
                modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1.3f),
                    verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
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
                    verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
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
