/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.MedicationEntity
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.track_dosage_confirmed
import tajsos.composeapp.generated.resources.track_medication_synk

@Composable
fun MedicationSyncCard(
    medications: List<MedicationEntity>,
    selectedMedIds: Set<Long>,
    onToggleMed: (Long) -> Unit,
    onToggleAll: () -> Unit,
    allMedsTaken: Boolean,
) {
    TactileCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MailOutline,
                    contentDescription = null,
                    tint = TactileTheme.Primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(TactileTheme.SpacingMd))
                Column {
                    Text(
                        stringResource(Res.string.track_medication_synk),
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                letterSpacing = 1.sp,
                            ),
                        color = TactileTheme.Text,
                    )
                    Text(
                        stringResource(Res.string.track_dosage_confirmed),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TactileTheme.Muted,
                    )
                }
                Spacer(Modifier.weight(1f))
                Checkbox(
                    checked = allMedsTaken,
                    onCheckedChange = { onToggleAll() },
                    colors = CheckboxDefaults.colors(checkedColor = TactileTheme.Primary),
                )
            }

            if (medications.isNotEmpty()) {
                Spacer(Modifier.height(TactileTheme.SpacingSm))
                HorizontalDivider(color = TactileTheme.Muted.copy(alpha = 0.2f))
                Spacer(Modifier.height(TactileTheme.SpacingSm))

                medications.forEach { med ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onToggleMed(med.id) }
                                .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = med.substance,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedMedIds.contains(med.id)) TactileTheme.Text else TactileTheme.Muted,
                        )
                        if (!med.brandNames.isNullOrEmpty()) {
                            Text(
                                text = " (${med.brandNames})",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = TactileTheme.Muted,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        if (selectedMedIds.contains(med.id)) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = TactileTheme.Primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
