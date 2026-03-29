/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.lens

import com.tajemniktv.tajsos.ui.Screen
import org.jetbrains.compose.resources.StringResource
import tajsos.composeapp.generated.resources.*

/**
 * Typed UI copy for a top-level lens surface.
 */
data class LensCopy(
    val title: StringResource,
    val subtitle: StringResource,
)

/**
 * Typed descriptor for a systems dashboard module card.
 */
data class SystemsModuleCopy(
    val screen: Screen,
    val summary: StringResource,
)

/**
 * Source-of-truth copy contract for lens framing and systems-module summaries.
 */
object LensUiContract {
    val decisionLens =
        LensCopy(
            title = Res.string.lens_decision_title,
            subtitle = Res.string.lens_decision_subtitle,
        )

    val unresolvedLens =
        LensCopy(
            title = Res.string.lens_unresolved_title,
            subtitle = Res.string.lens_unresolved_subtitle,
        )

    val referenceLens =
        LensCopy(
            title = Res.string.lens_reference_title,
            subtitle = Res.string.lens_reference_subtitle,
        )

    val financeLens =
        LensCopy(
            title = Res.string.lens_finance_title,
            subtitle = Res.string.lens_finance_subtitle,
        )

    val systemsModules: List<SystemsModuleCopy> =
        listOf(
            SystemsModuleCopy(Screen.OpenLoops, Res.string.systems_summary_open_loops),
            SystemsModuleCopy(Screen.Protocols, Res.string.systems_summary_protocols),
            SystemsModuleCopy(Screen.TimeArchitecture, Res.string.systems_summary_time_architecture),
            SystemsModuleCopy(Screen.Places, Res.string.systems_summary_places),
            SystemsModuleCopy(Screen.Finances, Res.string.systems_summary_finances),
            SystemsModuleCopy(Screen.Health, Res.string.systems_summary_health),
            SystemsModuleCopy(Screen.Relationships, Res.string.systems_summary_relationships),
            SystemsModuleCopy(Screen.Education, Res.string.systems_summary_education),
            SystemsModuleCopy(Screen.Rules, Res.string.systems_summary_rules),
            SystemsModuleCopy(Screen.Vaults, Res.string.systems_summary_vaults),
            SystemsModuleCopy(Screen.Capacity, Res.string.systems_summary_capacity),
            SystemsModuleCopy(Screen.Identity, Res.string.systems_summary_identity),
        )
}
