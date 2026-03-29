/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.AppPack
import com.tajemniktv.tajsos.data.MedicationEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.common_back
import tajsos.composeapp.generated.resources.med_brand_names
import tajsos.composeapp.generated.resources.med_delete
import tajsos.composeapp.generated.resources.med_dosage
import tajsos.composeapp.generated.resources.med_is_optional
import tajsos.composeapp.generated.resources.med_save
import tajsos.composeapp.generated.resources.med_substance
import tajsos.composeapp.generated.resources.med_take_at
import tajsos.composeapp.generated.resources.profile_add_med
import tajsos.composeapp.generated.resources.profile_medications
import tajsos.composeapp.generated.resources.settings_biometric_desc
import tajsos.composeapp.generated.resources.settings_biometric_lock
import tajsos.composeapp.generated.resources.settings_biometric_unavailable
import tajsos.composeapp.generated.resources.settings_data_management
import tajsos.composeapp.generated.resources.settings_export_data
import tajsos.composeapp.generated.resources.settings_export_success
import tajsos.composeapp.generated.resources.settings_force_crash

/**
 * Renders the settings "Health" tab and links to the Health lens screen.
 */
@Composable
fun SettingsHealthScreen(
    viewModel: MainViewModel,
) {
    val medications by viewModel.medications.collectAsState()
    var showAddMedicationDialog by remember { mutableStateOf(false) }

    SettingsSimpleScaffold(
        title = "HEALTH",
        description = "Manage medications used by tracking and health workflows.",
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Res.string.profile_medications),
                style = MaterialTheme.typography.titleMedium,
                color = TactileTheme.Text,
            )
            OutlinedButton(
                onClick = { showAddMedicationDialog = true },
                shape = RoundedCornerShape(TactileTheme.RadiusSm),
            ) {
                Text(stringResource(Res.string.profile_add_med))
            }
        }

        Spacer(Modifier.height(TactileTheme.SpacingMd))

        if (medications.isEmpty()) {
            Text(
                "No medication entries configured yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = TactileTheme.Muted,
            )
        } else {
            medications.forEach { medication ->
                SettingsMedicationItem(
                    medication = medication,
                    onDelete = { viewModel.deleteMedication(medication) },
                )
                Spacer(Modifier.height(TactileTheme.SpacingSm))
            }
        }
    }

    if (showAddMedicationDialog) {
        SettingsAddMedicationDialog(
            onDismiss = { showAddMedicationDialog = false },
            onSave = { substance, brands, dosage, hour, optional ->
                viewModel.addMedication(substance, brands, dosage, hour, optional)
                showAddMedicationDialog = false
            },
        )
    }
}

/**
 * Renders the settings "Feature Packs" tab with ownership and enablement controls.
 */
@Composable
fun SettingsFeaturePacksScreen(
    viewModel: MainViewModel,
) {
    val enabledPacks by viewModel.enabledPacks.collectAsState()

    SettingsSimpleScaffold(
        title = "FEATURE PACKS",
        description = "Enable or unlock optional capability packs.",
    ) {
        AppPack.entries.forEach { pack ->
            val isOwned = enabledPacks.isOwned(pack)
            val isEnabled = enabledPacks.isEnabled(pack)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = TactileTheme.SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text =
                            buildString {
                                append(pack.key.replaceFirstChar { it.uppercase() })
                                append(if (pack.isFree) " (Free)" else " (Premium)")
                            },
                        style = MaterialTheme.typography.bodyLarge,
                        color = TactileTheme.Text,
                    )
                    Text(
                        text =
                            if (isOwned) {
                                "Enable ${pack.key} module surfaces and workflows"
                            } else {
                                "Unlock this premium pack to enable its modes and modules"
                            },
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isOwned && !pack.isFree) {
                        OutlinedButton(
                            onClick = { viewModel.setPackOwned(pack, true) },
                            shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        ) {
                            Text("Unlock")
                        }
                    }
                    if (isOwned) {
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { viewModel.setPackEnabled(pack, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = TactileTheme.Primary),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders the settings "Data" tab with import and export controls.
 */
@Composable
fun SettingsDataScreen(
    viewModel: MainViewModel,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var importPayload by remember { mutableStateOf("") }

    Scaffold(
        containerColor = TactileTheme.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(TactileTheme.Background)
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(TactileTheme.SpacingMd),
        ) {
            Text(
                stringResource(Res.string.settings_data_management),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(TactileTheme.SpacingSm))

            Button(
                onClick = {
                    scope.launch {
                        val json = viewModel.exportDataJson()
                        snackbarHostState.showSnackbar(
                            getString(
                                Res.string.settings_export_success,
                                json.length,
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = TactileTheme.Surface,
                        contentColor = TactileTheme.Primary,
                    ),
            ) {
                Text(stringResource(Res.string.settings_export_data))
            }

            Spacer(Modifier.height(TactileTheme.SpacingMd))

            Button(
                onClick = {
                    scope.launch {
                        val json = viewModel.exportBundleJson()
                        snackbarHostState.showSnackbar("Export bundle generated (${json.length} bytes)")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = TactileTheme.Surface,
                        contentColor = TactileTheme.Primary,
                    ),
            ) {
                Text("Export Full Bundle")
            }

            Spacer(Modifier.height(TactileTheme.SpacingMd))

            OutlinedTextField(
                value = importPayload,
                onValueChange = { importPayload = it },
                modifier = Modifier.fillMaxWidth().height(180.dp),
                label = { Text("Import JSON Payload") },
                placeholder = { Text("Paste ExportData/ExportBundle JSON here...") },
                singleLine = false,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = TactileTheme.Border,
                        focusedBorderColor = TactileTheme.Primary,
                    ),
            )

            Spacer(Modifier.height(TactileTheme.SpacingSm))

            Button(
                onClick = {
                    scope.launch {
                        val message = viewModel.importDataJson(importPayload)
                        snackbarHostState.showSnackbar(message)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = TactileTheme.Surface,
                        contentColor = TactileTheme.Primary,
                    ),
            ) {
                Text("Import JSON")
            }
        }
    }
}

/**
 * Renders the settings "Debug" tab with internal testing controls.
 */
@Composable
fun SettingsDebugScreen() {
    SettingsSimpleScaffold(
        title = "DEBUG",
        description = "Internal diagnostics and crash testing controls.",
    ) {
        Button(
            onClick = { throw RuntimeException("Test Crash") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(TactileTheme.RadiusMd),
            colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Error),
        ) {
            Text(stringResource(Res.string.settings_force_crash))
        }
    }
}

/**
 * Renders the settings "Preferences" content currently hosted in the root settings screen.
 */
@Composable
fun SettingsPreferencesScreen(
    viewModel: MainViewModel,
) {
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isBiometricHardwareAvailable by viewModel.isBiometricHardwareAvailable.collectAsState()

    SettingsSimpleScaffold(
        title = "PREFERENCES",
        description = "Security and behavior preferences for this device.",
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = TactileTheme.SpacingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.settings_biometric_lock),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TactileTheme.Text,
                )
                Text(
                    if (isBiometricHardwareAvailable) {
                        stringResource(Res.string.settings_biometric_desc)
                    } else {
                        stringResource(Res.string.settings_biometric_unavailable)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                )
            }
            Switch(
                enabled = isBiometricHardwareAvailable,
                checked = isBiometricEnabled == true,
                onCheckedChange = { viewModel.setBiometricEnabled(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = TactileTheme.Primary),
            )
        }
    }
}

/**
 * Shared settings tab scaffold used by all new settings tab screens.
 */
@Composable
private fun SettingsSimpleScaffold(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        containerColor = TactileTheme.Background,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(TactileTheme.Background)
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(TactileTheme.SpacingMd),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(TactileTheme.SpacingSm))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = TactileTheme.Muted,
            )
            Spacer(Modifier.height(TactileTheme.SpacingLg))
            content()
        }
    }
}

/**
 * Renders one medication row for the settings health screen.
 */
@Composable
private fun SettingsMedicationItem(
    medication: MedicationEntity,
    onDelete: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = TactileTheme.Surface,
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                )
                .padding(TactileTheme.SpacingMd),
    ) {
        Text(
            medication.substance,
            style = MaterialTheme.typography.titleSmall,
            color = TactileTheme.Text,
        )
        if (medication.brandNames.isNotBlank()) {
            Spacer(Modifier.height(TactileTheme.SpacingXs))
            Text(
                medication.brandNames,
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
        Spacer(Modifier.height(TactileTheme.SpacingSm))
        Text(
            buildString {
                if (!medication.dosage.isNullOrBlank()) append(medication.dosage)
                if (medication.takeAtHour != null) {
                    if (isNotBlank()) append(" • ")
                    append("@ ${medication.takeAtHour}:00")
                }
                if (medication.isOptional) {
                    if (isNotBlank()) append(" • ")
                    append("Optional")
                }
            }.ifBlank { "No dosage schedule configured." },
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )
        Spacer(Modifier.height(TactileTheme.SpacingSm))
        TextButton(onClick = onDelete) {
            Text(stringResource(Res.string.med_delete))
        }
    }
}

/**
 * Dialog used by the settings health tab to add medication records.
 */
@Composable
private fun SettingsAddMedicationDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String?, Int?, Boolean) -> Unit,
) {
    var substance by remember { mutableStateOf("") }
    var brands by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("") }
    var isOptional by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.profile_add_med)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                OutlinedTextField(
                    value = substance,
                    onValueChange = { substance = it },
                    label = { Text(stringResource(Res.string.med_substance)) },
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                )
                OutlinedTextField(
                    value = brands,
                    onValueChange = { brands = it },
                    label = { Text(stringResource(Res.string.med_brand_names)) },
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(stringResource(Res.string.med_dosage)) },
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                )
                OutlinedTextField(
                    value = hour,
                    onValueChange = { hour = it },
                    label = { Text(stringResource(Res.string.med_take_at)) },
                    shape = RoundedCornerShape(TactileTheme.RadiusMd),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isOptional, onCheckedChange = { isOptional = it })
                    Text(stringResource(Res.string.med_is_optional))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(substance, brands, dosage.takeIf { it.isNotEmpty() }, hour.toIntOrNull(), isOptional)
                },
                enabled = substance.isNotBlank(),
            ) {
                Text(stringResource(Res.string.med_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_back))
            }
        },
    )
}
