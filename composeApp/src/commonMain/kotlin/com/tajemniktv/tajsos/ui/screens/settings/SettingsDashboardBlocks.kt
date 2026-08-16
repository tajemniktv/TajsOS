/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.settings

import com.tajemniktv.tajsos.ui.components.TactileOutlinedTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tajemniktv.tajsos.data.DesktopWindowStartupMode
import com.tajemniktv.tajsos.data.MedicationEntity
import com.tajemniktv.tajsos.ui.SidebarMode
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.components.common.GlassMaterial
import com.tajemniktv.tajsos.ui.components.common.glassChrome
import com.tajemniktv.tajsos.ui.components.common.glassContainerColor
import com.tajemniktv.tajsos.ui.components.common.mouseClickable
import com.tajemniktv.tajsos.ui.theme.PaletteAccentAmber
import com.tajemniktv.tajsos.ui.theme.PaletteAccentBlue
import com.tajemniktv.tajsos.ui.theme.PaletteAccentGreen
import com.tajemniktv.tajsos.ui.theme.PaletteAccentRose
import com.tajemniktv.tajsos.ui.theme.PrimaryPurple
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
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
import tajsos.composeapp.generated.resources.settings_appearance_desc
import tajsos.composeapp.generated.resources.settings_appearance_title
import tajsos.composeapp.generated.resources.settings_biometric_desc
import tajsos.composeapp.generated.resources.settings_biometric_lock
import tajsos.composeapp.generated.resources.settings_biometric_unavailable
import tajsos.composeapp.generated.resources.settings_export_data
import tajsos.composeapp.generated.resources.settings_force_crash
import tajsos.composeapp.generated.resources.settings_no_medication_entries_configured_yet
import tajsos.composeapp.generated.resources.settings_theme_dark
import tajsos.composeapp.generated.resources.settings_theme_light
import tajsos.composeapp.generated.resources.settings_theme_mode
import tajsos.composeapp.generated.resources.settings_theme_mode_desc
import tajsos.composeapp.generated.resources.settings_theme_settings
import tajsos.composeapp.generated.resources.settings_window_startup_always_maximized
import tajsos.composeapp.generated.resources.settings_window_startup_desc
import tajsos.composeapp.generated.resources.settings_window_startup_mode
import tajsos.composeapp.generated.resources.settings_window_startup_mode_desc
import tajsos.composeapp.generated.resources.settings_window_startup_restore_last
import tajsos.composeapp.generated.resources.settings_window_startup_title

object SettingsDashboardBlocks {
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
        SettingsHealthHeader(onAddMedicationClick = { showAddMedicationDialog = true })
        Spacer(Modifier.height(TajsOSTheme.SpacingMd))
        SettingsHealthMedicationList(context = context)
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
private fun SettingsHealthHeader(onAddMedicationClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(Res.string.profile_medications),
            style = MaterialTheme.typography.titleMedium,
            color = TajsOSTheme.Text,
        )
        OutlinedButton(
            onClick = onAddMedicationClick,
            shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
        ) {
            Text(stringResource(Res.string.profile_add_med))
        }
    }
}

@Composable
private fun SettingsHealthMedicationList(context: SettingsDashboardContext) {
    if (context.medications.isEmpty()) {
        EmptyState(
            message = stringResource(Res.string.settings_no_medication_entries_configured_yet),
            description = null,
            fillParent = false,
            showContainer = false,
        )
    } else {
        context.medications.forEach { medication ->
            SettingsMedicationItem(
                medication = medication,
                onDelete = { context.onDeleteMedication(medication) },
            )
            Spacer(Modifier.height(TajsOSTheme.SpacingSm))
        }
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
                        .padding(vertical = TajsOSTheme.SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text =
                            buildString {
                                append(pack.displayName)
                                append(if (pack.isFree) " (Free)" else " (Premium)")
                            },
                        style = MaterialTheme.typography.bodyLarge,
                        color = TajsOSTheme.Text,
                    )
                    Text(
                        text =
                            if (isOwned) {
                                "Enable ${pack.key} module surfaces and workflows"
                            } else {
                                "Unlock this premium pack to enable its modes and modules"
                            },
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Muted,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isOwned && !pack.isFree) {
                        OutlinedButton(
                            onClick = { context.onSetPackOwned(pack, true) },
                            shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                        ) {
                            Text("Unlock")
                        }
                    }
                    if (isOwned) {
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { context.onSetPackEnabled(pack, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = TajsOSTheme.Primary),
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
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = TajsOSTheme.CardSurface,
                    contentColor = TajsOSTheme.Primary,
                ),
        ) {
            Text(stringResource(Res.string.settings_export_data))
        }

        Spacer(Modifier.height(TajsOSTheme.SpacingMd))

        Button(
            onClick = context.onExportBundle,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = TajsOSTheme.CardSurface,
                    contentColor = TajsOSTheme.Primary,
                ),
        ) {
            Text("Export Full Bundle")
        }

        Spacer(Modifier.height(TajsOSTheme.SpacingMd))

        TactileOutlinedTextField(
            value = context.importPayload,
            onValueChange = context.onImportPayloadChange,
            modifier = Modifier.fillMaxWidth().height(180.dp),
            label = { Text("Import JSON Payload") },
            placeholder = { Text("Paste ExportData/ExportBundle JSON here...") },
            singleLine = false,
            colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = TajsOSTheme.Border,
                    focusedBorderColor = TajsOSTheme.GhostBorder,
                ),
        )

        Spacer(Modifier.height(TajsOSTheme.SpacingSm))

        Button(
            onClick = context.onImportData,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = TajsOSTheme.CardSurface,
                    contentColor = TajsOSTheme.Primary,
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
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            colors = ButtonDefaults.buttonColors(containerColor = TajsOSTheme.Error),
        ) {
            Text(stringResource(Res.string.settings_force_crash))
        }
    }
}

@Composable
private fun renderSettingsAppearance(context: SettingsDashboardContext) {
    var hideLabelsOnCollapse by remember { mutableStateOf(false) }

    val accentOptions =
        listOf(
            PrimaryPurple,
            PaletteAccentBlue,
            PaletteAccentRose,
            PaletteAccentAmber,
            PaletteAccentGreen,
        )

    fun Color.toHex(): String {
        val r = (red * 255).toInt().toString(16).padStart(2, '0')
        val g = (green * 255).toInt().toString(16).padStart(2, '0')
        val b = (blue * 255).toInt().toString(16).padStart(2, '0')
        return "#$r$g$b".uppercase()
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = TajsOSTheme.SpacingLg, vertical = TajsOSTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingLg),
    ) {
        Text(
            text = stringResource(Res.string.settings_appearance_title),
            style = MaterialTheme.typography.headlineMedium,
            color = TajsOSTheme.Text,
        )
        Text(
            text = stringResource(Res.string.settings_appearance_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = TajsOSTheme.Muted,
        )

        AppearanceSectionCard(title = stringResource(Res.string.settings_theme_settings)) {
            AppearanceSettingRow(
                title = stringResource(Res.string.settings_theme_mode),
                description = stringResource(Res.string.settings_theme_mode_desc),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                    OutlinedButton(
                        onClick = { context.onSetDarkTheme(true) },
                        shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                containerColor =
                                    if (context.isDarkTheme) {
                                        TajsOSTheme.Primary.copy(alpha = 0.2f)
                                    } else {
                                        Color.Transparent
                                    },
                            ),
                    ) {
                        Text(stringResource(Res.string.settings_theme_dark))
                    }
                    OutlinedButton(
                        onClick = { context.onSetDarkTheme(false) },
                        shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                containerColor =
                                    if (!context.isDarkTheme) {
                                        TajsOSTheme.Primary.copy(alpha = 0.2f)
                                    } else {
                                        Color.Transparent
                                    },
                            ),
                    ) {
                        Text(stringResource(Res.string.settings_theme_light))
                    }
                }
            }

            AppearanceSettingRow(
                title = "Accent Color",
                description = "Primary color for actions and highlights.",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                    accentOptions.forEach { color ->
                        AppearanceAccentDot(
                            color = color,
                            selected =
                                context.accentColorHex.uppercase() ==
                                    color
                                        .toHex()
                                        .uppercase(),
                            onClick = { context.onSetAccentColor(color.toHex()) },
                        )
                    }
                }
            }

            AppearanceSettingRow(
                title = "Glassmorphism Effects",
                description = "Enable translucent blur on navigation and modals.",
            ) {
                Switch(
                    checked = context.isGlassmorphismEnabled,
                    onCheckedChange = { context.onSetGlassmorphismEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = TajsOSTheme.Primary),
                )
            }
        }

        AppearanceSectionCard(title = "Sidebar Behavior") {
            AppearanceSettingRow(
                title = "Sidebar Mode",
                description = "Choose how the sidebar should behave.",
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                    SidebarMode.entries.forEach { mode ->
                        val label =
                            when (mode) {
                                SidebarMode.EXPANDED -> "Expanded"
                                SidebarMode.COLLAPSED -> "Collapsed"
                                SidebarMode.HOVER_EXPAND -> "Hover"
                            }
                        OutlinedButton(
                            onClick = { context.onSetSidebarMode(mode) },
                            shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor =
                                        if (context.sidebarMode == mode) {
                                            TajsOSTheme.Primary.copy(alpha = 0.2f)
                                        } else {
                                            Color.Transparent
                                        },
                                ),
                        ) {
                            Text(label)
                        }
                    }
                }
            }

            AppearanceSettingRow(
                title = "Hide Labels on Collapse",
                description = "Only show icons when sidebar is minimized.",
            ) {
                Switch(
                    checked = hideLabelsOnCollapse,
                    onCheckedChange = { hideLabelsOnCollapse = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TajsOSTheme.Primary),
                )
            }
        }

        AppearanceSectionCard(title = "Interface Refinement") {
            AppearanceSettingRow(
                title = "Reduce Motion",
                description = "Minimize system animations and transitions.",
            ) {
                Switch(
                    checked = context.reduceMotion,
                    onCheckedChange = { context.onSetReduceMotion(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = TajsOSTheme.Primary),
                )
            }
        }

        AppearanceSectionCard(title = stringResource(Res.string.settings_window_startup_title)) {
            AppearanceSettingRow(
                title = stringResource(Res.string.settings_window_startup_mode),
                description = stringResource(Res.string.settings_window_startup_mode_desc),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                    DesktopWindowStartupMode.entries.forEach { mode ->
                        val label =
                            when (mode) {
                                DesktopWindowStartupMode.RESTORE_LAST -> {
                                    stringResource(Res.string.settings_window_startup_restore_last)
                                }

                                DesktopWindowStartupMode.ALWAYS_MAXIMIZED -> {
                                    stringResource(Res.string.settings_window_startup_always_maximized)
                                }
                            }
                        OutlinedButton(
                            onClick = { context.onSetDesktopWindowStartupMode(mode) },
                            shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor =
                                        if (context.desktopWindowStartupMode == mode) {
                                            TajsOSTheme.Primary.copy(alpha = 0.2f)
                                        } else {
                                            Color.Transparent
                                        },
                                ),
                        ) {
                            Text(label)
                        }
                    }
                }
            }
            Text(
                text = stringResource(Res.string.settings_window_startup_desc),
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
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
                    .padding(vertical = TajsOSTheme.SpacingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(Res.string.settings_biometric_lock),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TajsOSTheme.Text,
                )
                Text(
                    if (context.isBiometricHardwareAvailable) {
                        stringResource(Res.string.settings_biometric_desc)
                    } else {
                        stringResource(Res.string.settings_biometric_unavailable)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Muted,
                )
            }
            Switch(
                enabled = context.isBiometricHardwareAvailable,
                checked = context.isBiometricEnabled == true,
                onCheckedChange = { context.onSetBiometricEnabled(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = TajsOSTheme.Primary),
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
                .fillMaxWidth()
                .padding(TajsOSTheme.SpacingMd),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Primary,
        )
        Spacer(Modifier.height(TajsOSTheme.SpacingSm))
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = TajsOSTheme.Muted,
        )
        Spacer(Modifier.height(TajsOSTheme.SpacingLg))
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
                .glassChrome(shape = RoundedCornerShape(TajsOSTheme.RadiusLg), material = GlassMaterial.REGULAR)
                .background(
                    color = glassContainerColor(TajsOSTheme.SurfaceLow.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
                ).border(
                    width = 1.dp,
                    color = TajsOSTheme.Border.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
                ).padding(TajsOSTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TajsOSTheme.Text,
        )
        HorizontalDivider(color = TajsOSTheme.GhostBorder)
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
            modifier = Modifier.weight(1f).padding(end = TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingXs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TajsOSTheme.Text,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
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
                    color = if (selected) TajsOSTheme.Text else Color.Transparent,
                    shape = CircleShape,
                ).mouseClickable(
                    onClick = onClick,
                    onSecondaryClick = onClick,
                    middleClickFallbackToPrimary = true,
                ),
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
                .glassChrome(
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                    material = GlassMaterial.REGULAR,
                ).background(
                    color = glassContainerColor(TajsOSTheme.Surface),
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                ).padding(TajsOSTheme.SpacingMd),
    ) {
        Text(
            medication.substance,
            style = MaterialTheme.typography.titleSmall,
            color = TajsOSTheme.Text,
        )
        if (medication.brandNames.isNotBlank()) {
            Spacer(Modifier.height(TajsOSTheme.SpacingXs))
            Text(
                medication.brandNames,
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
        }
        Spacer(Modifier.height(TajsOSTheme.SpacingSm))
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
            color = TajsOSTheme.Muted,
        )
        Spacer(Modifier.height(TajsOSTheme.SpacingSm))
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
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                TactileOutlinedTextField(
                    value = substance,
                    onValueChange = { substance = it },
                    label = { Text(stringResource(Res.string.med_substance)) },
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                )
                TactileOutlinedTextField(
                    value = brands,
                    onValueChange = { brands = it },
                    label = { Text(stringResource(Res.string.med_brand_names)) },
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                )
                TactileOutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(stringResource(Res.string.med_dosage)) },
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                )
                TactileOutlinedTextField(
                    value = hour,
                    onValueChange = { hour = it },
                    label = { Text(stringResource(Res.string.med_take_at)) },
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
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
