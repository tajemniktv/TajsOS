/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
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
fun SettingsScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel) {
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isBiometricHardwareAvailable by viewModel.isBiometricHardwareAvailable.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)
        ) {
            Text("SETTINGS", style = MaterialTheme.typography.displayMedium, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text)
            Spacer(Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg))

            Text("SECURITY", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "BIOMETRIC LOCK",
                        style = MaterialTheme.typography.bodyLarge,
                        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
                    )
                    Text(
                        if (isBiometricHardwareAvailable) "Require authentication to open"
                        else "Hardware not available",
                        style = MaterialTheme.typography.labelSmall,
                        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted
                    )
                }
                Switch(
                    enabled = isBiometricHardwareAvailable,
                    checked = isBiometricEnabled == true,
                    onCheckedChange = { viewModel.setBiometricEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
                )
            }

            Spacer(Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg))

            Text("DATA MANAGEMENT", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
            Spacer(Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm))

            Button(
                onClick = {
                    scope.launch {
                        val json = viewModel.exportDataJson()
                        // MVP: log the json length for verification
                        snackbarHostState.showSnackbar("Data exported (${json.length} chars)")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd),
                colors = ButtonDefaults.buttonColors(containerColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface, contentColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
            ) {
                Text("EXPORT LOCAL DATA")
            }

            Spacer(Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg))

            Button(
                onClick = { throw RuntimeException("Test Crash") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd),
                colors = ButtonDefaults.buttonColors(containerColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Error)
            ) {
                Text("FORCE TEST CRASH")
            }
        }
    }
}
