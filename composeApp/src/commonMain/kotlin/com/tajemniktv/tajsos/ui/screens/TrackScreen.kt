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
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

@Composable
fun TrackScreen(viewModel: MainViewModel) {
    val trackEntries by viewModel.trackEntries.collectAsState()
    val today = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    val todayEntry = trackEntries.find { it.date == today }

    var energy by remember(todayEntry) { mutableFloatStateOf(todayEntry?.energyScore?.toFloat() ?: 3f) }
    var mood by remember(todayEntry) { mutableFloatStateOf(todayEntry?.moodScore?.toFloat() ?: 3f) }
    var focus by remember(todayEntry) { mutableFloatStateOf(todayEntry?.focusScore?.toFloat() ?: 3f) }
    var sleep by remember(todayEntry) { mutableFloatStateOf(todayEntry?.sleepScore ?: 7f) }
    var tookMeds by remember(todayEntry) { mutableStateOf(todayEntry?.tookMeds ?: false) }
    var note by remember(todayEntry) { mutableStateOf(todayEntry?.symptomNote ?: "") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)
    ) {
        item {
            Text(
                "DAILY TRACKING",
                style = MaterialTheme.typography.displayMedium,
                color = TactileTheme.Text
            )
            Text(
                "HOW ARE YOU OPERATING TODAY?",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )
            Spacer(Modifier.height(TactileTheme.SpacingLg))
        }

        item {
            TrackSlider(
                label = "ENERGY",
                value = energy,
                onValueChange = { energy = it })
        }
        item {
            TrackSlider(
                label = "MOOD",
                value = mood,
                onValueChange = { mood = it })
        }
        item {
            TrackSlider(
                label = "FOCUS",
                value = focus,
                onValueChange = { focus = it })
        }

        item {
            Column {
                Text(
                    "SLEEP (HOURS): ${sleep.roundToInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted
                )
                Slider(
                    value = sleep,
                    onValueChange = { sleep = it },
                    valueRange = 0f..12f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = TactileTheme.Primary,
                        activeTrackColor = TactileTheme.Primary,
                        inactiveTrackColor = TactileTheme.Muted
                    )
                )
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = TactileTheme.SpacingSm)
            ) {
                Checkbox(
                    checked = tookMeds,
                    onCheckedChange = { tookMeds = it },
                    colors = CheckboxDefaults.colors(checkedColor = TactileTheme.Primary)
                )
                Text("TOOK MEDS", style = MaterialTheme.typography.bodyLarge, color = TactileTheme.Text)
            }
        }

        item {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("OBSERVATIONS / SYMPTOMS") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Any specific context for today?") },
                shape = RoundedCornerShape(TactileTheme.RadiusSm)
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
                shape = RoundedCornerShape(TactileTheme.RadiusMd)
            ) {
                Text("SAVE STATUS", style = MaterialTheme.typography.labelLarge)
            }
        }

        item {
            Spacer(Modifier.height(TactileTheme.SpacingLg))
            Text("HISTORY", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
        }

        items(trackEntries, key = { it.id }) { entry ->
            TrackHistoryItem(entry)
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
            Text(label, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
            Text(value.roundToInt().toString(), style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = TactileTheme.Primary,
                activeTrackColor = TactileTheme.Primary,
                inactiveTrackColor = TactileTheme.Muted
            )
        )
    }
}

@Composable
fun TrackHistoryItem(entry: TrackEntryEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface.copy(alpha = 0.5f),
        shape = RoundedCornerShape(TactileTheme.RadiusSm),
        border = androidx.compose.foundation.BorderStroke(1.dp, TactileTheme.Muted.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(entry.date, style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
                if (entry.tookMeds) {
                    Text("MEDS OK", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Success)
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)) {
                StatusChip("MOOD", entry.moodScore)
                StatusChip("ENERGY", entry.energyScore)
                StatusChip("FOCUS", entry.focusScore)
                if (entry.sleepScore != null) {
                    Text(
                        "SLEEP: ${entry.sleepScore}H",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Text
                    )
                }
            }
            if (entry.symptomNote.isNotEmpty()) {
                Spacer(Modifier.height(TactileTheme.SpacingSm))
                Text(entry.symptomNote, style = MaterialTheme.typography.bodySmall, color = TactileTheme.Muted)
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
        color = TactileTheme.Text
    )
}
