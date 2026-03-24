/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.data.MedicationEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@Composable
fun ProfileScreen(viewModel: MainViewModel)
{
    val user by viewModel.user.collectAsState()
    val medications by viewModel.medications.collectAsState()

    var editingName by remember(user) { mutableStateOf(user?.name ?: "OPERATOR") }
    var showAddMedDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        item {
            Text(
                stringResource(Res.string.profile_title),
                style = MaterialTheme.typography.displayMedium,
                color = TactileTheme.Text,
            )
            Spacer(Modifier.height(TactileTheme.SpacingLg))
        }

        item {
            OutlinedTextField(
                value = editingName,
                onValueChange = {
                    editingName = it
                    viewModel.updateUserName(it)
                },
                label = { Text(stringResource(Res.string.profile_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusSm),
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Res.string.profile_medications),
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
                IconButton(onClick = { showAddMedDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = TactileTheme.Primary)
                }
            }
        }

        items(medications) { med ->
            MedicationItem(med, onDelete = { viewModel.deleteMedication(med) })
        }
    }

    if (showAddMedDialog)
    {
        AddMedicationDialog(
            onDismiss = { showAddMedDialog = false },
            onSave = { substance, brands, dosage, hour, optional ->
                viewModel.addMedication(substance, brands, dosage, hour, optional)
                showAddMedDialog = false
            },
        )
    }
}

@Composable
fun MedicationItem(medication: MedicationEntity, onDelete: () -> Unit)
{
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
    ) {
        Row(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    medication.substance,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TactileTheme.Text,
                )
                if (medication.brandNames.isNotEmpty())
                {
                    Text(
                        medication.brandNames,
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
                Text(
                    "${medication.dosage ?: ""} ${if (medication.takeAtHour != null) "@ ${medication.takeAtHour}:00" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TactileTheme.Error)
            }
        }
    }
}

@Composable
fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String?, Int?, Boolean) -> Unit,
)
{
    var substance by remember { mutableStateOf("") }
    var brands by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("") }
    var isOptional by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.profile_add_med)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                OutlinedTextField(
                    value = substance,
                    onValueChange = { substance = it },
                    label = { Text(stringResource(Res.string.med_substance)) },
                )
                OutlinedTextField(
                    value = brands,
                    onValueChange = { brands = it },
                    label = { Text(stringResource(Res.string.med_brand_names)) },
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(stringResource(Res.string.med_dosage)) },
                )
                OutlinedTextField(
                    value = hour,
                    onValueChange = { hour = it },
                    label = { Text(stringResource(Res.string.med_take_at)) },
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
