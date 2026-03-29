/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.finance

/**
 * Registry for finance dashboard block renderers.
 */
object FinanceDashboardBlockRegistry {
    private val renderers: Map<String, FinanceDashboardBlockRenderer> =
        mapOf(
            "finance_header" to ::renderFinanceHeaderBlock,
            "finance_metrics" to ::renderFinanceMetricsBlock,
            "finance_activity" to ::renderFinanceActivityBlock,
            "finance_insights" to ::renderFinanceInsightsBlock,
            "finance_vault" to ::renderFinanceVaultBlock,
            "finance_queue_controls" to ::renderFinanceQueueControlsBlock,
            "finance_queue_list" to ::renderFinanceQueueListBlock,
        )

    fun resolve(id: String): FinanceDashboardBlockRenderer? = renderers[id]
}
