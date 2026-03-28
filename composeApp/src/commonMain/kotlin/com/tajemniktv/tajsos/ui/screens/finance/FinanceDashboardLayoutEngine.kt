/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.finance

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class FinanceLayoutJsonV1(
    val version: Int = 1,
    val primary: List<String> = emptyList(),
    val secondary: List<String> = emptyList(),
)

private val financeLayoutJson =
    Json {
        ignoreUnknownKeys = true
    }

private val mobileDefaults =
    listOf(
        "finance_header",
        "finance_metrics",
        "finance_activity",
        "finance_insights",
        "finance_vault",
        "finance_queue_controls",
        "finance_queue_list",
    )

private val desktopPrimaryDefaults =
    listOf(
        "finance_header",
        "finance_metrics",
        "finance_queue_controls",
        "finance_queue_list",
    )

private val desktopSecondaryDefaults =
    listOf(
        "finance_activity",
        "finance_insights",
        "finance_vault",
    )

/**
 * Builds finance dashboard plan from defaults or optional JSON override.
 */
fun buildFinanceDashboardPlan(
    surface: com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardSurface,
    layoutOverrideJson: String? = null,
): com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardPlan {
    val parsed =
        com.tajemniktv.tajsos.ui.screens.finance.parseStructured(
            layoutOverrideJson,
        )
    if (parsed != null) {
        return com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardPlan(
            primary =
                parsed.primary.distinct().map {
                    com.tajemniktv.tajsos.ui.screens.finance
                        .FinanceDashboardBlock(it)
                },
            secondary =
                parsed.secondary.distinct().map {
                    com.tajemniktv.tajsos.ui.screens.finance
                        .FinanceDashboardBlock(it)
                },
        )
    }

    return when (surface)
    {
        com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardSurface.MOBILE -> {
            com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardPlan(
                primary =
                    com.tajemniktv.tajsos.ui.screens.finance.mobileDefaults.map {
                        com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardBlock(
                            it,
                        )
                    },
            )
        }

        com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardSurface.DESKTOP -> {
            com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardPlan(
                primary =
                    com.tajemniktv.tajsos.ui.screens.finance.desktopPrimaryDefaults.map {
                        com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardBlock(
                            it,
                        )
                    },
                secondary =
                    com.tajemniktv.tajsos.ui.screens.finance.desktopSecondaryDefaults.map {
                        com.tajemniktv.tajsos.ui.screens.finance.FinanceDashboardBlock(
                            it,
                        )
                    },
            )
        }
    }
}

private fun parseStructured(raw: String?): com.tajemniktv.tajsos.ui.screens.finance.FinanceLayoutJsonV1? {
    if (raw.isNullOrBlank()) return null
    return runCatching {
        com.tajemniktv.tajsos.ui.screens.finance.financeLayoutJson
            .decodeFromString<com.tajemniktv.tajsos.ui.screens.finance.FinanceLayoutJsonV1>(
                raw,
            )
    }.getOrNull()
}
