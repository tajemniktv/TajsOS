/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.TrackEntryEntity
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * TrackScreen handles Phase 5: State Tracking.
 * It uses micro-inputs (sliders/toggles) to capture the user's daily state
 * without causing form fatigue.
 */
@Composable
fun TrackScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel) {
    val trackEntries by viewModel.trackEntries.collectAsState()
    val today = LocalDate.now().toString()
    val todayEntry = trackEntries.find { it.date == today }

    // Using remember(todayEntry) to reset state if a new entry is loaded/saved
    var energy by remember(todayEntry) { mutableFloatStateOf(todayEntry?.energy?.toFloat() ?: 3f) }
    var mood by remember(todayEntry) { mutableFloatStateOf(todayEntry?.mood?.toFloat() ?: 3f) }
    var focus by remember(todayEntry) { mutableFloatStateOf(todayEntry?.focus?.toFloat() ?: 3f) }
    var sleep by remember(todayEntry) { mutableFloatStateOf(todayEntry?.sleep ?: 7f) }
    var tookMeds by remember(todayEntry) { mutableStateOf(todayEntry?.tookMeds ?: false) }
    var note by remember(todayEntry) { mutableStateOf(todayEntry?.note ?: "") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)
    ) {
        item {
            Text(
                "DAILY TRACKING",
                style = MaterialTheme.typography.displayMedium,
                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
            )
            Text(
                "HOW ARE YOU OPERATING TODAY?",
                style = MaterialTheme.typography.labelSmall,
                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
            )
            Spacer(Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg))
        }

        item {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.TrackSlider(
                label = "ENERGY",
                value = energy,
                onValueChange = { energy = it })
        }
        item {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.TrackSlider(
                label = "MOOD",
                value = mood,
                onValueChange = { mood = it })
        }
        item {
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.TrackSlider(
                label = "FOCUS",
                value = focus,
                onValueChange = { focus = it })
        }

        item {
            Column {
                Text(
                    "SLEEP (HOURS): ${sleep.roundToInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted
                )
                Slider(
                    value = sleep,
                    onValueChange = { sleep = it },
                    valueRange = 0f..12f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary,
                        activeTrackColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary,
                        inactiveTrackColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted
                    )
                )
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm)
            ) {
                Checkbox(
                    checked = tookMeds,
                    onCheckedChange = { tookMeds = it },
                    colors = CheckboxDefaults.colors(checkedColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
                )
                Text("TOOK MEDS", style = MaterialTheme.typography.bodyLarge, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text)
            }
        }

        item {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("OBSERVATIONS / SYMPTOMS") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Any specific context for today?") },
                shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusSm)
            )
        }

        item {
            Button(
                onClick = {
                    viewModel.addTrackEntry(
                        mood = mood.roundToInt(),
                        energy = energy.roundToInt(),
                        focus = focus.roundToInt(),
                        sleep = sleep,
                        tookMeds = tookMeds,
                        note = note
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd)
            ) {
                Text("SAVE STATUS", style = MaterialTheme.typography.labelLarge)
            }
        }

        item {
            Spacer(Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg))
            Text("HISTORY", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
        }

        items(trackEntries, key = { it.date }) { entry ->
            _root_ide_package_.com.tajemniktv.tajsos.ui.screens.TrackHistoryItem(entry)
        }
    }
}

@Composable
fun TrackSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
            Text(value.roundToInt().toString(), style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary,
                activeTrackColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary,
                inactiveTrackColor = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted
            )
        )
    }
}

@Composable
fun TrackHistoryItem(entry: TrackEntryEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(1.dp, _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(entry.date, style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
                if (entry.tookMeds) {
                    Text("MEDS OK", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Success)
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)) {
                _root_ide_package_.com.tajemniktv.tajsos.ui.screens.StatusChip("MOOD", entry.mood)
                _root_ide_package_.com.tajemniktv.tajsos.ui.screens.StatusChip(
                    "ENERGY",
                    entry.energy
                )
                _root_ide_package_.com.tajemniktv.tajsos.ui.screens.StatusChip("FOCUS", entry.focus)
                if (entry.sleep != null) {
                    Text(
                        "SLEEP: ${entry.sleep}H",
                        style = MaterialTheme.typography.labelSmall,
                        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
                    )
                }
            }
            if (entry.note.isNotEmpty()) {
                Spacer(Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm))
                Text(entry.note, style = MaterialTheme.typography.bodySmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
            }
        }
    }
}

@Composable
fun StatusChip(label: String, value: Int?) {
    if (value == null) return
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.labelSmall,
        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
    )
}
