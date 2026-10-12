/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.AnnotatedString
import com.tajemniktv.tajos.ui.platform.toClipEntry
import com.tajemniktv.tajos.ui.MainViewModel
import com.tajemniktv.tajos.ui.Screen
import com.tajemniktv.tajos.ui.components.screen.ScreenScaffold
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.settings_transfer_export_ready
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
    key(screenId) {
        val transfer = rememberSettingsTransfer(viewModel)
        val context = settingsDashboardContext(viewModel, transfer)
        val plan = remember(screenId) { buildSettingsPlan(screenId) }
        SettingsScreen(context, plan, screenId, onNavigate)
        if (screenId == Screen.SettingsData.ID) {
            val scope = rememberCoroutineScope()
            SettingsExportDialog(
                state = context.dataTransfer,
                onCopy = { scope.launch { transfer.copyExport() } },
                onDismiss = transfer::dismissExport,
            )
        }
    }
}

/** Keeps private backup JSON out of Android's clip label and system preview. */
@Composable
private fun rememberSettingsTransfer(viewModel: MainViewModel): SettingsDataTransfer {
    val clipboard = LocalClipboard.current
    val label by rememberUpdatedState(stringResource(Res.string.settings_transfer_export_ready))
    return remember(viewModel, clipboard) {
        SettingsDataTransfer(
            exportNodes = viewModel::exportDataJson,
            exportBundle = viewModel::exportBundleJson,
            importJson = viewModel::importDataJson,
            copyJson = { clipboard.setClipEntry(AnnotatedString(it).toClipEntry(label, sensitive = true)) },
        )
    }
}

/** Collects the existing preferences projection and wires bounded Settings events. */
@Composable
private fun settingsDashboardContext(
    viewModel: MainViewModel,
    transfer: SettingsDataTransfer,
): SettingsDashboardContext {
    var importPayload by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val dataTransfer by transfer.state.collectAsState()
    return SettingsDashboardContext(
        viewModel = viewModel,
        medications = viewModel.medications.collectAsState().value,
        enabledPacks = viewModel.enabledPacks.collectAsState().value,
        isBiometricEnabled = viewModel.isBiometricEnabled.collectAsState().value,
        isBiometricHardwareAvailable = viewModel.isBiometricHardwareAvailable.collectAsState().value,
        isDarkTheme = viewModel.isDarkTheme.collectAsState().value,
        accentColorHex = viewModel.accentColorHex.collectAsState().value,
        isGlassmorphismEnabled = viewModel.isGlassmorphismEnabled.collectAsState().value,
        reduceMotion = viewModel.reduceMotion.collectAsState().value,
        sidebarMode = viewModel.sidebarMode.collectAsState().value,
        desktopWindowStartupMode = viewModel.desktopWindowStartupMode.collectAsState().value,
        importPayload = importPayload,
        dataTransfer = dataTransfer,
        onImportPayloadChange = { importPayload = it },
        onSaveMedication = viewModel::addMedication,
        onDeleteMedication = viewModel::deleteMedication,
        onSetPackOwned = viewModel::setPackOwned,
        onSetPackEnabled = viewModel::setPackEnabled,
        onExportData = { scope.launch { transfer.export(fullBundle = false) } },
        onExportBundle = { scope.launch { transfer.export(fullBundle = true) } },
        onImportData = { scope.launch { transfer.importPayload(importPayload) } },
        onSetBiometricEnabled = viewModel::setBiometricEnabled,
        onSetDarkTheme = viewModel::setDarkTheme,
        onSetAccentColor = viewModel::setAccentColor,
        onSetGlassmorphismEnabled = viewModel::setGlassmorphismEnabled,
        onSetReduceMotion = viewModel::setReduceMotion,
        onSetSidebarMode = viewModel::setSidebarMode,
        onSetDesktopWindowStartupMode = viewModel::setDesktopWindowStartupMode,
        onForceCrash = { throw RuntimeException("Test Crash") },
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
