/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.AppPack
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 * Renders the app settings screen with security, calendar, templates, data management, and a test crash action.
 *
 * The security section shows a biometric toggle that reflects and updates the ViewModel biometric state and is disabled when biometric hardware is unavailable. The data export action calls the ViewModel to obtain exported JSON and displays a snackbar indicating the exported byte length.
 *
 * @param viewModel Provides observable biometric state and actions for toggling biometric protection and exporting data.
 * @param onNavigateToCalendarSettings Callback invoked when the user requests calendar integration settings.
 * @param onNavigateToTemplates Callback invoked when the user requests template management.
 */
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToCalendarSettings: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {},
) {
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isBiometricHardwareAvailable by viewModel.isBiometricHardwareAvailable.collectAsState()
    val enabledPacks by viewModel.enabledPacks.collectAsState()
    var importPayload by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

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
                stringResource(Res.string.settings_security),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
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

            Spacer(Modifier.height(TactileTheme.SpacingLg))

            Text(
                stringResource(Res.string.settings_calendar_integration),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(TactileTheme.SpacingSm))

            Button(
                onClick = onNavigateToCalendarSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = TactileTheme.Surface,
                        contentColor = TactileTheme.Primary,
                    ),
            ) {
                Text(stringResource(Res.string.settings_configure_calendars))
            }

            Spacer(Modifier.height(TactileTheme.SpacingMd))

            Button(
                onClick = onNavigateToTemplates,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = TactileTheme.Surface,
                        contentColor = TactileTheme.Primary,
                    ),
            ) {
                Text(stringResource(Res.string.settings_manage_templates))
            }

            Spacer(Modifier.height(TactileTheme.SpacingLg))

            Text(
                "Feature Packs",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
            Spacer(Modifier.height(TactileTheme.SpacingSm))

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

            Spacer(Modifier.height(TactileTheme.SpacingLg))

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

            Spacer(Modifier.height(TactileTheme.SpacingLg))

        }
    }
}
