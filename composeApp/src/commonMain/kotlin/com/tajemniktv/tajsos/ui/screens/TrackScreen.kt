/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.TrackEntryEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.TactileCard
import com.tajemniktv.tajsos.ui.components.TactileSlider
import com.tajemniktv.tajsos.ui.components.TactileTextField
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*
import kotlin.math.roundToInt

@Composable
fun TrackScreen(viewModel: MainViewModel)
{
    val trackEntries by viewModel.trackEntries.collectAsState()
    val medications by viewModel.medications.collectAsState()

    val today = kotlin.time.Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    val todayEntry = trackEntries.find { it.date == today }

    var energy by remember(todayEntry) {
        mutableFloatStateOf(
            todayEntry?.energyScore?.toFloat() ?: 3f,
        )
    }
    var affective by remember(todayEntry) {
        mutableFloatStateOf(
            todayEntry?.moodScore?.toFloat() ?: 3f,
        )
    }
    var cognitive by remember(todayEntry) {
        mutableFloatStateOf(
            todayEntry?.focusScore?.toFloat() ?: 5f,
        )
    }
    var systemTension by remember(todayEntry) {
        mutableFloatStateOf(
            todayEntry?.anxietyScore?.toFloat() ?: 1f,
        )
    }
    var recovery by remember(todayEntry) { mutableFloatStateOf(todayEntry?.sleepScore ?: 7.5f) }

    var note by remember(todayEntry) { mutableStateOf(todayEntry?.symptomNote ?: "") }

    val selectedMedIds = remember { mutableStateOf(setOf<Long>()) }
    val allMedsTaken = medications.all { it.isOptional || selectedMedIds.value.contains(it.id) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        item {
            HeaderSection()
        }

        item {
            TactileSlider(
                label = stringResource(Res.string.track_label_energy_reserves),
                value = energy,
                onValueChange = { energy = it },
                minLabel = "DEPLETED",
                maxLabel = "OPTIMAL",
            )
        }
        item {
            TactileSlider(
                label = stringResource(Res.string.track_label_affective_state),
                value = affective,
                onValueChange = { affective = it },
                minLabel = "LOW",
                maxLabel = "ELEVATED",
            )
        }
        item {
            TactileSlider(
                label = stringResource(Res.string.track_label_cognitive_lock),
                value = cognitive,
                onValueChange = { cognitive = it },
                minLabel = "DIFFUSED",
                maxLabel = "HYPER-FOCUS",
            )
        }
        item {
            TactileSlider(
                label = stringResource(Res.string.track_label_system_tension),
                value = systemTension,
                onValueChange = { systemTension = it },
                minLabel = "CALM",
                maxLabel = "CRITICAL",
            )
        }
        item {
            TactileSlider(
                label = stringResource(Res.string.track_label_recovery_cycles),
                value = recovery,
                onValueChange = { recovery = it },
                valueRange = 0f..12f,
                steps = 11,
                minLabel = "00H",
                maxLabel = "12H",
                valueSuffix = "HRS",
            )
        }

        item {
            MedicationSyncCard(
                medications = medications,
                selectedMedIds = selectedMedIds.value,
                onToggleMed = { id ->
                    selectedMedIds.value = if (selectedMedIds.value.contains(id))
                    {
                        selectedMedIds.value - id
                    } else
                    {
                        selectedMedIds.value + id
                    }
                },
                onToggleAll = {
                    selectedMedIds.value =
                            if (allMedsTaken) emptySet() else medications.map { it.id }.toSet()
                },
                allMedsTaken = allMedsTaken,
            )
        }

        item {
            TactileCard(onClick = { /* Navigate to Daily Review or show status */ }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Settings, // Closest to bio icon in default icons
                        contentDescription = null,
                        tint = TactileTheme.Primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(TactileTheme.SpacingMd))
                    Column {
                        Text(
                            stringResource(Res.string.track_bio_feedback),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                            ),
                            color = TactileTheme.Text,
                        )
                        Text(
                            stringResource(Res.string.track_sync_in_progress),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TactileTheme.Muted,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        stringResource(Res.string.track_active),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF52525B),
                    )
                }
            }
        }

        item {
            TactileTextField(
                value = note,
                onValueChange = { note = it },
                label = stringResource(Res.string.track_observations_symptoms),
            )
        }

        item {
            Button(
                onClick = {
                    viewModel.addTrackEntry(
                        energyPulse = energy.roundToInt(),
                        affectivePulse = affective.roundToInt(),
                        cognitivePulse = cognitive.roundToInt(),
                        systemPulse = systemTension.roundToInt(),
                        recoveryPulse = recovery,
                        tookMeds = allMedsTaken,
                        note = note,
                        medicationIds = selectedMedIds.value,
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                colors = ButtonDefaults.buttonColors(containerColor = TactileTheme.Primary),
            ) {
                Text(
                    stringResource(Res.string.track_save),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    ),
                )
            }
        }

        item {
            Spacer(Modifier.height(TactileTheme.SpacingLg))
            Text(
                stringResource(Res.string.track_history),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
            )
        }

        items(trackEntries, key = { it.id }) { entry ->
            TrackHistoryItem(entry)
        }
    }
}

@Composable
fun HeaderSection()
{
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MailOutline, contentDescription = null, tint = TactileTheme.Primary)
            Spacer(Modifier.width(TactileTheme.SpacingSm))
            Text(
                "TAJS OS",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TactileTheme.Text,
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = TactileTheme.Muted)
        }
        Spacer(Modifier.height(TactileTheme.SpacingLg))
        Text(
            stringResource(Res.string.track_title),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            ),
            color = TactileTheme.Text,
        )
        Text(
            stringResource(Res.string.track_subtitle),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TactileTheme.Primary,
                letterSpacing = 2.sp,
            ),
        )
        Spacer(Modifier.height(TactileTheme.SpacingSm))
        Text(
            stringResource(Res.string.track_description),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF52525B),
        )
        Spacer(Modifier.height(TactileTheme.SpacingLg))
    }
}

@Composable
fun MedicationSyncCard(
    medications: List<com.tajemniktv.tajsos.data.MedicationEntity>,
    selectedMedIds: Set<Long>,
    onToggleMed: (Long) -> Unit,
    onToggleAll: () -> Unit,
    allMedsTaken: Boolean,
)
{
    TactileCard {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MailOutline, // Reusing icon for meds
                    contentDescription = null,
                    tint = TactileTheme.Primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(TactileTheme.SpacingMd))
                Column {
                    Text(
                        stringResource(Res.string.track_medication_synk),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
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

            if (medications.isNotEmpty())
            {
                Spacer(Modifier.height(TactileTheme.SpacingSm))
                HorizontalDivider(color = TactileTheme.Muted.copy(alpha = 0.2f))
                Spacer(Modifier.height(TactileTheme.SpacingSm))

                medications.forEach { med ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onToggleMed(med.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = med.substance,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedMedIds.contains(med.id)) TactileTheme.Text else TactileTheme.Muted,
                        )
                        if (!med.brandNames.isNullOrEmpty())
                        {
                            Text(
                                text = " (${med.brandNames})",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = TactileTheme.Muted,
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        if (selectedMedIds.contains(med.id))
                        {
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

@Composable
fun TrackHistoryItem(entry: TrackEntryEntity)
{
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TactileTheme.Muted.copy(alpha = 0.2f),
        ),
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    entry.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary,
                )
                if (entry.tookMeds)
                {
                    Text(
                        stringResource(Res.string.track_history_meds_ok),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Success,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                StatusChip("AFF", entry.moodScore)
                StatusChip("ENG", entry.energyScore)
                StatusChip("COG", entry.focusScore)
                StatusChip("SYS", entry.anxietyScore)
                if (entry.sleepScore != null)
                {
                    Text(
                        stringResource(Res.string.track_history_sleep, entry.sleepScore!!),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Text,
                    )
                }
            }
            if (entry.symptomNote.isNotEmpty())
            {
                Spacer(Modifier.height(TactileTheme.SpacingSm))
                Text(
                    entry.symptomNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                )
            }
        }
    }
}

@Composable
fun StatusChip(label: String, value: Int?)
{
    if (value == null) return
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.labelSmall,
        color = TactileTheme.Text,
    )
}
