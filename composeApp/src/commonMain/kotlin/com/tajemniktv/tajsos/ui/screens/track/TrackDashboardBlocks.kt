/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.data.TrackEntryEntity
import com.tajemniktv.tajsos.ui.components.TactileSlider
import com.tajemniktv.tajsos.ui.components.TactileTextField
import com.tajemniktv.tajsos.ui.components.cards.MedicationSyncCard
import com.tajemniktv.tajsos.ui.components.cards.TactileCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.track_active
import tajsos.composeapp.generated.resources.track_bio_feedback
import tajsos.composeapp.generated.resources.track_description
import tajsos.composeapp.generated.resources.track_empty
import tajsos.composeapp.generated.resources.track_history
import tajsos.composeapp.generated.resources.track_history_meds_ok
import tajsos.composeapp.generated.resources.track_history_sleep
import tajsos.composeapp.generated.resources.track_label_affective_state
import tajsos.composeapp.generated.resources.track_label_cognitive_lock
import tajsos.composeapp.generated.resources.track_label_energy_reserves
import tajsos.composeapp.generated.resources.track_label_recovery_cycles
import tajsos.composeapp.generated.resources.track_label_system_tension
import tajsos.composeapp.generated.resources.track_observations_symptoms
import tajsos.composeapp.generated.resources.track_save
import tajsos.composeapp.generated.resources.track_subtitle
import tajsos.composeapp.generated.resources.track_sync_in_progress
import tajsos.composeapp.generated.resources.track_title

object TrackDashboardBlocks {
    private val renderers: Map<String, TrackDashboardBlockRenderer> =
        mapOf(
            "track_header" to ::renderTrackHeader,
            "track_energy" to ::renderTrackEnergy,
            "track_affective" to ::renderTrackAffective,
            "track_cognitive" to ::renderTrackCognitive,
            "track_tension" to ::renderTrackTension,
            "track_recovery" to ::renderTrackRecovery,
            "track_medication" to ::renderTrackMedication,
            "track_bio" to ::renderTrackBio,
            "track_note" to ::renderTrackNote,
            "track_save_button" to ::renderTrackSaveButton,
            "track_history_header" to ::renderTrackHistoryHeader,
            "track_history_list" to ::renderTrackHistoryList,
        )

    fun resolve(id: String): TrackDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderTrackHeader(context: TrackDashboardContext) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MailOutline, contentDescription = null, tint = TajsOSTheme.Primary)
            Spacer(Modifier.width(TajsOSTheme.SpacingSm))
            Text(
                "TAJS OS",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TajsOSTheme.Text,
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = TajsOSTheme.Muted)
        }
        Spacer(Modifier.height(TajsOSTheme.SpacingLg))
        Text(
            stringResource(Res.string.track_title),
            style =
                MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
            color = TajsOSTheme.Text,
        )
        Text(
            stringResource(Res.string.track_subtitle),
            style =
                MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TajsOSTheme.Primary,
                    letterSpacing = 2.sp,
                ),
        )
        Spacer(Modifier.height(TajsOSTheme.SpacingSm))
        Text(
            stringResource(Res.string.track_description),
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted,
        )
        Spacer(Modifier.height(TajsOSTheme.SpacingLg))
    }
}

@Composable
private fun renderTrackEnergy(context: TrackDashboardContext) {
    TactileSlider(
        label = stringResource(Res.string.track_label_energy_reserves),
        value = context.energy,
        onValueChange = context.onEnergyChange,
        minLabel = "DEPLETED",
        maxLabel = "OPTIMAL",
    )
}

@Composable
private fun renderTrackAffective(context: TrackDashboardContext) {
    TactileSlider(
        label = stringResource(Res.string.track_label_affective_state),
        value = context.affective,
        onValueChange = context.onAffectiveChange,
        minLabel = "LOW",
        maxLabel = "ELEVATED",
    )
}

@Composable
private fun renderTrackCognitive(context: TrackDashboardContext) {
    TactileSlider(
        label = stringResource(Res.string.track_label_cognitive_lock),
        value = context.cognitive,
        onValueChange = context.onCognitiveChange,
        minLabel = "DIFFUSED",
        maxLabel = "HYPER-FOCUS",
    )
}

@Composable
private fun renderTrackTension(context: TrackDashboardContext) {
    TactileSlider(
        label = stringResource(Res.string.track_label_system_tension),
        value = context.systemTension,
        onValueChange = context.onSystemTensionChange,
        minLabel = "CALM",
        maxLabel = "CRITICAL",
    )
}

@Composable
private fun renderTrackRecovery(context: TrackDashboardContext) {
    TactileSlider(
        label = stringResource(Res.string.track_label_recovery_cycles),
        value = context.recovery,
        onValueChange = context.onRecoveryChange,
        valueRange = 0f..12f,
        steps = 11,
        minLabel = "00H",
        maxLabel = "12H",
        valueSuffix = "HRS",
    )
}

@Composable
private fun renderTrackMedication(context: TrackDashboardContext) {
    MedicationSyncCard(
        medications = context.medications,
        selectedMedIds = context.selectedMedIds,
        onToggleMed = context.onToggleMed,
        onToggleAll = context.onToggleAllMeds,
        allMedsTaken = context.allMedsTaken,
    )
}

@Composable
private fun renderTrackBio(context: TrackDashboardContext) {
    TactileCard(onClick = { /* Navigate to Daily Review or show status */ }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                tint = TajsOSTheme.Primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(TajsOSTheme.SpacingMd))
            Column {
                Text(
                    stringResource(Res.string.track_bio_feedback),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        ),
                    color = TajsOSTheme.Text,
                )
                Text(
                    stringResource(Res.string.track_sync_in_progress),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TajsOSTheme.Muted,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(Res.string.track_active),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TajsOSTheme.Muted,
            )
        }
    }
}

@Composable
private fun renderTrackNote(context: TrackDashboardContext) {
    TactileTextField(
        value = context.note,
        onValueChange = context.onNoteChange,
        label = stringResource(Res.string.track_observations_symptoms),
    )
}

@Composable
private fun renderTrackSaveButton(context: TrackDashboardContext) {
    Button(
        onClick = context.onSave,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        colors = ButtonDefaults.buttonColors(containerColor = TajsOSTheme.Primary),
    ) {
        Text(
            stringResource(Res.string.track_save),
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
        )
    }
}

@Composable
private fun renderTrackHistoryHeader(context: TrackDashboardContext) {
    Column {
        Spacer(Modifier.height(TajsOSTheme.SpacingLg))
        Text(
            stringResource(Res.string.track_history),
            style = MaterialTheme.typography.labelSmall,
            color = TajsOSTheme.Primary,
        )
    }
}

@Composable
private fun renderTrackHistoryList(context: TrackDashboardContext) {
    if (context.trackEntries.isEmpty()) {
        EmptyState(stringResource(Res.string.track_empty))
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
            context.trackEntries.forEach { entry ->
                TrackHistoryItem(entry)
            }
        }
    }
}

@Composable
private fun TrackHistoryItem(entry: TrackEntryEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(TajsOSTheme.RadiusSm),
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                TajsOSTheme.Muted.copy(alpha = 0.2f),
            ),
    ) {
        Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    entry.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                )
                if (entry.tookMeds) {
                    Text(
                        stringResource(Res.string.track_history_meds_ok),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Success,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
                StatusChip("AFF", entry.moodScore)
                StatusChip("ENG", entry.energyScore)
                StatusChip("COG", entry.focusScore)
                StatusChip("SYS", entry.anxietyScore)
                val sleep = entry.sleepScore
                if (sleep != null) {
                    Text(
                        stringResource(Res.string.track_history_sleep, sleep),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Text,
                    )
                }
            }
            if (entry.symptomNote.isNotEmpty()) {
                Spacer(Modifier.height(TajsOSTheme.SpacingSm))
                Text(
                    entry.symptomNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    value: Int?,
) {
    if (value == null) return
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.labelSmall,
        color = TajsOSTheme.Text,
    )
}

