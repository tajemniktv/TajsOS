/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.settings

/**
 * Builds a settings dashboard layout plan based on the screen type.
 */
fun buildSettingsPlan(screenId: String): SettingsDashboardPlan {
    val primary = mutableListOf<SettingsDashboardBlock>()

    when (screenId) {
        "health" -> primary.add(SettingsDashboardBlock("settings_health"))
        "feature_packs" -> primary.add(SettingsDashboardBlock("settings_feature_packs"))
        "data" -> primary.add(SettingsDashboardBlock("settings_data"))
        "debug" -> primary.add(SettingsDashboardBlock("settings_debug"))
        "appearance" -> primary.add(SettingsDashboardBlock("settings_appearance"))
        else -> primary.add(SettingsDashboardBlock("settings_preferences"))
    }

    return SettingsDashboardPlan(primary = primary)
}
