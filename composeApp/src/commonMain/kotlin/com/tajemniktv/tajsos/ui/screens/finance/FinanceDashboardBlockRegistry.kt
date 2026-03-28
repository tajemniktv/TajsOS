/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.finance

import com.tajemniktv.tajsos.ui.screens.finance.renderFinanceActivityBlock
import com.tajemniktv.tajsos.ui.screens.finance.renderFinanceHeaderBlock
import com.tajemniktv.tajsos.ui.screens.finance.renderFinanceInsightsBlock
import com.tajemniktv.tajsos.ui.screens.finance.renderFinanceMetricsBlock
import com.tajemniktv.tajsos.ui.screens.finance.renderFinanceQueueControlsBlock
import com.tajemniktv.tajsos.ui.screens.finance.renderFinanceQueueListBlock
import com.tajemniktv.tajsos.ui.screens.finance.renderFinanceVaultBlock

/**
 * Registry for finance dashboard block renderers.
 */
object FinanceDashboardBlockRegistry {
    private val renderers: Map<String, com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardBlockRenderer> =
        mapOf(
            "finance_header" to ::renderFinanceHeaderBlock,
            "finance_metrics" to ::renderFinanceMetricsBlock,
            "finance_activity" to ::renderFinanceActivityBlock,
            "finance_insights" to ::renderFinanceInsightsBlock,
            "finance_vault" to ::renderFinanceVaultBlock,
            "finance_queue_controls" to ::renderFinanceQueueControlsBlock,
            "finance_queue_list" to ::renderFinanceQueueListBlock,
        )

    fun resolve(id: String): com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardBlockRenderer? = renderers[id]
}
