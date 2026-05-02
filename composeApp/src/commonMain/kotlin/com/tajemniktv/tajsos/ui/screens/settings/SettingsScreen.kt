/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import kotlinx.coroutines.launch

/**
 * Central settings entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of settings state.
 * @param onNavigate Navigation callback.
 * @param screenId The ID of the settings screen to display.
 */
@Composable
fun SettingsRoute(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit,
    screenId: String = "preferences",
) {
    val medications by viewModel.medications.collectAsState()
    val enabledPacks by viewModel.enabledPacks.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isBiometricHardwareAvailable by viewModel.isBiometricHardwareAvailable.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val accentColorHex by viewModel.accentColorHex.collectAsState()
    val isGlassmorphismEnabled by viewModel.isGlassmorphismEnabled.collectAsState()
    val reduceMotion by viewModel.reduceMotion.collectAsState()
    val sidebarMode by viewModel.sidebarMode.collectAsState()
    val desktopWindowStartupMode by viewModel.desktopWindowStartupMode.collectAsState()

    var importPayload by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val context =
        SettingsDashboardContext(
            viewModel = viewModel,
            medications = medications,
            enabledPacks = enabledPacks,
            isBiometricEnabled = isBiometricEnabled,
            isBiometricHardwareAvailable = isBiometricHardwareAvailable,
            isDarkTheme = isDarkTheme,
            accentColorHex = accentColorHex,
            isGlassmorphismEnabled = isGlassmorphismEnabled,
            reduceMotion = reduceMotion,
            sidebarMode = sidebarMode,
            desktopWindowStartupMode = desktopWindowStartupMode,
            importPayload = importPayload,
            onImportPayloadChange = { importPayload = it },
            onSaveMedication = { substance, brands, dosage, hour, optional ->
                viewModel.addMedication(substance, brands, dosage, hour, optional)
            },
            onDeleteMedication = { viewModel.deleteMedication(it) },
            onSetPackOwned = { pack, owned -> viewModel.setPackOwned(pack, owned) },
            onSetPackEnabled = { pack, enabled -> viewModel.setPackEnabled(pack, enabled) },
            onExportData = {
                scope.launch {
                    viewModel.exportDataJson()
                }
            },
            onExportBundle = {
                scope.launch {
                    viewModel.exportBundleJson()
                }
            },
            onImportData = {
                scope.launch {
                    viewModel.importDataJson(importPayload)
                }
            },
            onSetBiometricEnabled = { viewModel.setBiometricEnabled(it) },
            onSetDarkTheme = { viewModel.setDarkTheme(it) },
            onSetAccentColor = { viewModel.setAccentColor(it) },
            onSetGlassmorphismEnabled = { viewModel.setGlassmorphismEnabled(it) },
            onSetReduceMotion = { viewModel.setReduceMotion(it) },
            onSetSidebarMode = { viewModel.setSidebarMode(it) },
            onSetDesktopWindowStartupMode = { viewModel.setDesktopWindowStartupMode(it) },
            onForceCrash = { throw RuntimeException("Test Crash") },
        )

    val plan = remember(screenId) { buildSettingsPlan(screenId) }

    SettingsScreen(
        context = context,
        plan = plan,
        screenId = screenId,
        onNavigate = onNavigate,
    )
}

/**
 * Stateless settings screen content.
 *
 * @param context Settings dashboard context.
 * @param plan Settings dashboard plan.
 * @param screenId The ID of the settings screen to display.
 * @param onNavigate Navigation callback.
 */
@Composable
fun SettingsScreen(
    context: SettingsDashboardContext,
    plan: SettingsDashboardPlan,
    screenId: String,
    onNavigate: (String) -> Unit,
) {
    val targetScreen =
        when (screenId) {
            "appearance" -> Screen.SettingsAppearance
            "health" -> Screen.SettingsHealth
            "feature_packs" -> Screen.SettingsFeaturePacks
            "data" -> Screen.SettingsData
            "debug" -> Screen.SettingsDebug
            else -> Screen.Settings
        }

    ScreenScaffold(
        screen = targetScreen,
        onNavigate = onNavigate,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                SettingsDashboardBlocks.resolve(block.id)?.invoke(context)
            }
        }
    }
}
