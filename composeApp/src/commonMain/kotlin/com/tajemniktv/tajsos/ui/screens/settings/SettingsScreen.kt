/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
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
                    // SnackBar logic would need a host, keeping it simple or delegating to VM if needed
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
            onForceCrash = { throw RuntimeException("Test Crash") },
        )

    val plan = remember(screenId) { buildSettingsPlan(screenId) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp),
    ) {
        plan.primary.forEach { block ->
            SettingsDashboardBlocks.resolve(block.id)?.invoke(context)
        }
    }
}
