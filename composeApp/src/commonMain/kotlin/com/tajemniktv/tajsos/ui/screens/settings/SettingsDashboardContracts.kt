/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.settings

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.data.AppPack
import com.tajemniktv.tajsos.data.MedicationEntity
import com.tajemniktv.tajsos.data.PackRegistry
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.SidebarMode

/**
 * Defines the supported surfaces for settings dashboard layout planning.
 */
enum class SettingsDashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Identifies a logical settings dashboard block.
 */
data class SettingsDashboardBlock(
    val id: String,
)

/**
 * Structured layout plan for the settings dashboard screen.
 */
data class SettingsDashboardPlan(
    val primary: List<SettingsDashboardBlock> = emptyList(),
)

/**
 * Shared state and actions for settings dashboard block renderers.
 */
data class SettingsDashboardContext(
    val viewModel: MainViewModel,
    val medications: List<MedicationEntity>,
    val enabledPacks: PackRegistry,
    val isBiometricEnabled: Boolean?,
    val isBiometricHardwareAvailable: Boolean,
    val isDarkTheme: Boolean,
    val accentColorHex: String,
    val isGlassmorphismEnabled: Boolean,
    val reduceMotion: Boolean,
    val sidebarMode: SidebarMode,
    val importPayload: String,
    val onImportPayloadChange: (String) -> Unit,
    val onSaveMedication: (String, String, String?, Int?, Boolean) -> Unit,
    val onDeleteMedication: (MedicationEntity) -> Unit,
    val onSetPackOwned: (AppPack, Boolean) -> Unit,
    val onSetPackEnabled: (AppPack, Boolean) -> Unit,
    val onExportData: () -> Unit,
    val onExportBundle: () -> Unit,
    val onImportData: () -> Unit,
    val onSetBiometricEnabled: (Boolean) -> Unit,
    val onSetDarkTheme: (Boolean) -> Unit,
    val onSetAccentColor: (String) -> Unit,
    val onSetGlassmorphismEnabled: (Boolean) -> Unit,
    val onSetReduceMotion: (Boolean) -> Unit,
    val onSetSidebarMode: (SidebarMode) -> Unit,
    val onForceCrash: () -> Unit,
)

/**
 * Functional interface for rendering a settings dashboard block.
 */
typealias SettingsDashboardBlockRenderer = @Composable (SettingsDashboardContext) -> Unit
