/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToCalendarSettings: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {},
)
{
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isBiometricHardwareAvailable by viewModel.isBiometricHardwareAvailable.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = TactileTheme.Background,
    ) { padding ->
        Column(
            modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(TactileTheme.SpacingMd),
        ) {
            Text(
                stringResource(Res.string.settings_title),
                style = MaterialTheme.typography.displayMedium,
                color = TactileTheme.Text,
            )
            Spacer(Modifier.height(TactileTheme.SpacingLg))

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
                        if (isBiometricHardwareAvailable)
                        {
                            stringResource(Res.string.settings_biometric_desc)
                        } else
                        {
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = TactileTheme.Surface,
                    contentColor = TactileTheme.Primary,
                ),
            ) {
                Text(stringResource(Res.string.settings_export_data))
            }

            Spacer(Modifier.height(TactileTheme.SpacingLg))

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
}
