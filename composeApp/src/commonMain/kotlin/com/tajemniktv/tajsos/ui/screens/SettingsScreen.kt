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

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToCalendarSettings: () -> Unit = {},
    onNavigateToTemplates: () -> Unit = {}
) {
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isBiometricHardwareAvailable by viewModel.isBiometricHardwareAvailable.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = TactileTheme.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(TactileTheme.SpacingMd)
        ) {
            Text("SETTINGS", style = MaterialTheme.typography.displayMedium, color = TactileTheme.Text)
            Spacer(Modifier.height(TactileTheme.SpacingLg))

            Text("SECURITY", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = TactileTheme.SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "BIOMETRIC LOCK",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TactileTheme.Text
                    )
                    Text(
                        if (isBiometricHardwareAvailable) "Require authentication to open"
                        else "Hardware not available",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                }
                Switch(
                    enabled = isBiometricHardwareAvailable,
                    checked = isBiometricEnabled == true,
                    onCheckedChange = { viewModel.setBiometricEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = TactileTheme.Primary)
                )
            }

            Spacer(Modifier.height(TactileTheme.SpacingLg))

            Text(
                "CALENDAR INTEGRATION",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(Modifier.height(TactileTheme.SpacingSm))

            Button(
                onClick = onNavigateToCalendarSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TactileTheme.Surface,
                    contentColor = TactileTheme.Primary
                )
            ) {
                Text("CONFIGURE EXTERNAL CALENDARS")
            }

            Spacer(Modifier.height(TactileTheme.SpacingMd))

            Button(
                onClick = onNavigateToTemplates,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TactileTheme.Surface,
                    contentColor = TactileTheme.Primary
                )
            ) {
                Text("MANAGE TEMPLATES")
            }

            Spacer(Modifier.height(TactileTheme.SpacingLg))

            Text("DATA MANAGEMENT", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
            Spacer(Modifier.height(TactileTheme.SpacingSm))

            Button(
                onClick = {
                    scope.launch {
                        val json = viewModel.exportDataJson()
                        snackbarHostState.showSnackbar("Data exported (${json.length} chars)")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Surface, contentColor = TactileTheme.Primary)
            ) {
                Text("EXPORT LOCAL DATA")
            }

            Spacer(Modifier.height(TactileTheme.SpacingLg))

            Button(
                onClick = { throw RuntimeException("Test Crash") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Error)
            ) {
                Text("FORCE TEST CRASH")
            }
        }
    }
}
