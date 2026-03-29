/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.AppPack
import com.tajemniktv.tajsos.data.MedicationEntity
import com.tajemniktv.tajsos.ui.theme.TactileTheme
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
import tajsos.composeapp.generated.resources.settings_export_data
import tajsos.composeapp.generated.resources.settings_force_crash

object SettingsDashboardBlockRegistry {
    private val renderers: Map<String, SettingsDashboardBlockRenderer> =
        mapOf(
            "settings_health" to ::renderSettingsHealth,
            "settings_feature_packs" to ::renderSettingsFeaturePacks,
            "settings_data" to ::renderSettingsData,
            "settings_debug" to ::renderSettingsDebug,
            "settings_appearance" to ::renderSettingsAppearance,
            "settings_preferences" to ::renderSettingsPreferences,
        )

    fun resolve(id: String): SettingsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderSettingsHealth(context: SettingsDashboardContext) {
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

        if (context.medications.isEmpty()) {
            Text(
                "No medication entries configured yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = TactileTheme.Muted,
            )
        } else {
            context.medications.forEach { medication ->
                SettingsMedicationItem(
                    medication = medication,
                    onDelete = { context.onDeleteMedication(medication) },
                )
                Spacer(Modifier.height(TactileTheme.SpacingSm))
            }
        }
    }

    if (showAddMedicationDialog) {
        SettingsAddMedicationDialog(
            onDismiss = { showAddMedicationDialog = false },
            onSave = { substance, brands, dosage, hour, optional ->
                context.onSaveMedication(substance, brands, dosage, hour, optional)
                showAddMedicationDialog = false
            },
        )
    }
}

@Composable
private fun renderSettingsFeaturePacks(context: SettingsDashboardContext) {
    val enabledPacks = context.enabledPacks
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
                            onClick = { context.onSetPackOwned(pack, true) },
                            shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        ) {
                            Text("Unlock")
                        }
                    }
                    if (isOwned) {
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { context.onSetPackEnabled(pack, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = TactileTheme.Primary),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun renderSettingsData(context: SettingsDashboardContext) {
    SettingsSimpleScaffold(
        title = "DATA MANAGEMENT",
        description = "Export or import your system data locally.",
    ) {
        Button(
            onClick = context.onExportData,
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
            onClick = context.onExportBundle,
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
            value = context.importPayload,
            onValueChange = context.onImportPayloadChange,
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
            onClick = context.onImportData,
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

@Composable
private fun renderSettingsDebug(context: SettingsDashboardContext) {
    SettingsSimpleScaffold(
        title = "DEBUG",
        description = "Internal diagnostics and crash testing controls.",
    ) {
        Button(
            onClick = context.onForceCrash,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(TactileTheme.RadiusMd),
            colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Error),
        ) {
            Text(stringResource(Res.string.settings_force_crash))
        }
    }
}

@Composable
private fun renderSettingsAppearance(context: SettingsDashboardContext) {
    var selectedAccentIndex by remember { mutableStateOf(0) }
    var glassmorphismEnabled by remember { mutableStateOf(true) }
    var hideLabelsOnCollapse by remember { mutableStateOf(false) }
    var reduceMotion by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TactileTheme.SpacingLg, vertical = TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingLg),
    ) {
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.headlineMedium,
            color = TactileTheme.Text,
        )
        Text(
            text = "Customize the visual identity and interface behavior of your operating system.",
            style = MaterialTheme.typography.bodyMedium,
            color = TactileTheme.Muted,
        )

        AppearanceSectionCard(title = "Theme Settings") {
            AppearanceSettingRow(
                title = "Theme Mode",
                description = "Switch between dark and light theme.",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                    OutlinedButton(
                        onClick = { context.onSetDarkTheme(true) },
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                containerColor =
                                    if (context.isDarkTheme) {
                                        TactileTheme.Primary.copy(alpha = 0.2f)
                                    } else {
                                        Color.Transparent
                                    },
                            ),
                    ) {
                        Text("Dark")
                    }
                    OutlinedButton(
                        onClick = { context.onSetDarkTheme(false) },
                        shape = RoundedCornerShape(TactileTheme.RadiusSm),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                containerColor =
                                    if (!context.isDarkTheme) {
                                        TactileTheme.Primary.copy(alpha = 0.2f)
                                    } else {
                                        Color.Transparent
                                    },
                            ),
                    ) {
                        Text("Light")
                    }
                }
            }

            AppearanceSettingRow(
                title = "Accent Color",
                description = "Primary color for actions and highlights.",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                    AppearanceAccentDot(
                        color = Color(0xFFB388FF),
                        selected = selectedAccentIndex == 0,
                        onClick = { selectedAccentIndex = 0 },
                    )
                    AppearanceAccentDot(
                        color = Color(0xFF60A5FA),
                        selected = selectedAccentIndex == 1,
                        onClick = { selectedAccentIndex = 1 },
                    )
                    AppearanceAccentDot(
                        color = Color(0xFFFB7185),
                        selected = selectedAccentIndex == 2,
                        onClick = { selectedAccentIndex = 2 },
                    )
                }
            }

            AppearanceSettingRow(
                title = "Glassmorphism Effects",
                description = "Enable translucent blur on navigation and modals.",
            ) {
                Switch(
                    checked = glassmorphismEnabled,
                    onCheckedChange = { glassmorphismEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TactileTheme.Primary),
                )
            }
        }

        AppearanceSectionCard(title = "Sidebar Behavior") {
            AppearanceSettingRow(
                title = "Default Sidebar State",
                description = "How the sidebar appears when the app starts.",
            ) {
                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(TactileTheme.RadiusSm),
                ) {
                    Text("Expanded")
                }
            }

            AppearanceSettingRow(
                title = "Hide Labels on Collapse",
                description = "Only show icons when sidebar is minimized.",
            ) {
                Switch(
                    checked = hideLabelsOnCollapse,
                    onCheckedChange = { hideLabelsOnCollapse = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TactileTheme.Primary),
                )
            }
        }

        AppearanceSectionCard(title = "Interface Refinement") {
            AppearanceSettingRow(
                title = "Reduce Motion",
                description = "Minimize system animations and transitions.",
            ) {
                Switch(
                    checked = reduceMotion,
                    onCheckedChange = { reduceMotion = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TactileTheme.Primary),
                )
            }
        }
    }
}

@Composable
private fun renderSettingsPreferences(context: SettingsDashboardContext) {
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
                    if (context.isBiometricHardwareAvailable) {
                        stringResource(Res.string.settings_biometric_desc)
                    } else {
                        stringResource(Res.string.settings_biometric_unavailable)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted,
                )
            }
            Switch(
                enabled = context.isBiometricHardwareAvailable,
                checked = context.isBiometricEnabled == true,
                onCheckedChange = { context.onSetBiometricEnabled(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = TactileTheme.Primary),
            )
        }
    }
}

@Composable
private fun SettingsSimpleScaffold(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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

@Composable
private fun AppearanceSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = TactileTheme.SurfaceLow.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(14.dp),
                ).border(
                    width = 1.dp,
                    color = TactileTheme.Border.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp),
                ).padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TactileTheme.Text,
        )
        HorizontalDivider(color = TactileTheme.Border.copy(alpha = 0.3f))
        content()
    }
}

@Composable
private fun AppearanceSettingRow(
    title: String,
    description: String,
    control: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingXs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TactileTheme.Text,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
        Box(contentAlignment = Alignment.CenterEnd) {
            control()
        }
    }
}

@Composable
private fun AppearanceAccentDot(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(22.dp)
                .background(color = color, shape = CircleShape)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) TactileTheme.Text else Color.Transparent,
                    shape = CircleShape,
                ).clickable(onClick = onClick),
    )
}

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
                ).padding(TactileTheme.SpacingMd),
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
                    onSave(
                        substance,
                        brands,
                        dosage.takeIf { it.isNotEmpty() },
                        hour.toIntOrNull(),
                        isOptional,
                    )
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
