/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.finance

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.MaintenanceStatusItem
import org.jetbrains.compose.resources.StringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.view_overdue
import tajsos.composeapp.generated.resources.view_queue
import tajsos.composeapp.generated.resources.view_recurring

/**
 * Surface variants for finance dashboard layouts.
 */
enum class FinanceDashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * One block instance in a finance dashboard plan.
 */
data class FinanceDashboardBlock(
    val id: String,
)

/**
 * Finance dashboard layout plan.
 */
data class FinanceDashboardPlan(
    val primary: List<FinanceDashboardBlock>,
    val secondary: List<FinanceDashboardBlock> = emptyList(),
)

/**
 * Queue filter selection for finance maintenance items.
 */
enum class FinanceMaintenanceView(
    val label: StringResource,
) {
    Queue(Res.string.view_queue),
    Recurring(Res.string.view_recurring),
    Overdue(Res.string.view_overdue),
}

/**
 * Shared render context for finance blocks.
 */
data class FinanceDashboardContext(
    val viewModel: MainViewModel,
    val allAreas: List<NodeEntity>,
    val queue: List<MaintenanceStatusItem>,
    val recurring: List<MaintenanceStatusItem>,
    val overdue: List<MaintenanceStatusItem>,
    val allItems: List<MaintenanceStatusItem>,
    val itemsInView: List<MaintenanceStatusItem>,
    val actionItems: List<NodeWithPin>,
    val knowledgeItems: List<NodeWithPin>,
    val deadlineItems: List<NodeWithPin>,
    val recentItems: List<NodeWithPin>,
    val confidence: Int,
    val liquidity: Double,
    val bars: List<Int>,
    val maintenanceView: FinanceMaintenanceView,
    val onMaintenanceViewChange: (FinanceMaintenanceView) -> Unit,
    val onEditNode: (Long) -> Unit,
)

/**
 * Function signature for one finance dashboard block renderer.
 */
typealias FinanceDashboardBlockRenderer = @Composable (FinanceDashboardContext) -> Unit
