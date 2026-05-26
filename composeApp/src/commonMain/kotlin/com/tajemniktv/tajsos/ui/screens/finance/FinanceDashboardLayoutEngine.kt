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
    surface: FinanceDashboardSurface,
    layoutOverrideJson: String? = null,
): FinanceDashboardPlan {
    val parsed =
        parseStructured(
            layoutOverrideJson,
        )
    if (parsed != null) {
        return FinanceDashboardPlan(
            primary =
                parsed.primary.distinct().map {
                    FinanceDashboardBlock(it)
                },
            secondary =
                parsed.secondary.distinct().map {
                    FinanceDashboardBlock(it)
                },
        )
    }

    return when (surface) {
        FinanceDashboardSurface.MOBILE -> {
            FinanceDashboardPlan(
                primary =
                    mobileDefaults.map {
                        FinanceDashboardBlock(
                            it,
                        )
                    },
            )
        }

        FinanceDashboardSurface.DESKTOP -> {
            FinanceDashboardPlan(
                primary =
                    desktopPrimaryDefaults.map {
                        FinanceDashboardBlock(
                            it,
                        )
                    },
                secondary =
                    desktopSecondaryDefaults.map {
                        FinanceDashboardBlock(
                            it,
                        )
                    },
            )
        }
    }
}

private fun parseStructured(raw: String?): FinanceLayoutJsonV1? {
    if (raw.isNullOrBlank()) return null
    return runCatching {
        financeLayoutJson
            .decodeFromString<FinanceLayoutJsonV1>(
                raw,
            )
    }.getOrNull()
}
