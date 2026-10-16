/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajos.ui.components.common

import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.common_back
import tajsos.composeapp.generated.resources.med_brand_names
import tajsos.composeapp.generated.resources.med_dosage
import tajsos.composeapp.generated.resources.med_hour_invalid
import tajsos.composeapp.generated.resources.med_is_optional
import tajsos.composeapp.generated.resources.med_save
import tajsos.composeapp.generated.resources.med_substance
import tajsos.composeapp.generated.resources.med_take_at
import tajsos.composeapp.generated.resources.profile_add_med

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import com.tajemniktv.tajos.ui.components.TactileOutlinedTextField
import com.tajemniktv.tajos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun MedicationEditorDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String?, Int?, Boolean) -> Unit,
) {
    var substance by remember { mutableStateOf("") }
    var brands by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("") }
    var isOptional by remember { mutableStateOf(false) }

    val hourValid = com.tajemniktv.tajos.data.isValidMedicationHour(hour)
    val canSave = hourValid && com.tajemniktv.tajos.data.isValidMedication(substance, hour.trim().toIntOrNull())

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
                    isError = !hourValid,
                    supportingText = if (!hourValid) ({ Text(stringResource(Res.string.med_hour_invalid)) }) else null,
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
                        substance.trim(),
                        brands.trim(),
                        dosage.takeIf { it.isNotEmpty() },
                        hour.trim().toIntOrNull(),
                        isOptional,
                    )
                },
                enabled = canSave,
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
