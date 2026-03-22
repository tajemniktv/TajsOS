/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSheet(
    onDismiss: () -> Unit,
    onCapture: (String, String, Long?, Long?, Boolean, String?, Long?) -> Unit,
    projects: List<NodeEntity> = emptyList(),
    areas: List<NodeEntity> = emptyList(),
    defaultProjectId: Long? = null,
    defaultAreaId: Long? = null
) {
    var text by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("task") }
    var selectedProjectId by remember { mutableStateOf<Long?>(defaultProjectId) }
    var selectedAreaId by remember { mutableStateOf<Long?>(defaultAreaId) }
    
    var isRecurring by remember { mutableStateOf(false) }
    var recurringInterval by remember { mutableStateOf<String?>(null) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }

    var multiCaptureMode by remember { mutableStateOf(false) }
    var brainDumpMode by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TactileTheme.Surface,
        shape = RoundedCornerShape(topStart = TactileTheme.RadiusLg, topEnd = TactileTheme.RadiusLg)
    ) {
        Column(
            modifier = Modifier
                .padding(TactileTheme.SpacingMd)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (brainDumpMode) "BRAIN DUMP ACTIVE" else "QUICK CAPTURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (brainDumpMode) TactileTheme.Primary else TactileTheme.Muted
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "MULTI",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted
                    )
                    Switch(
                        checked = multiCaptureMode || brainDumpMode,
                        onCheckedChange = { multiCaptureMode = it },
                        enabled = !brainDumpMode,
                        modifier = Modifier.scale(0.7f)
                    )
                    Spacer(Modifier.width(TactileTheme.SpacingSm))
                    FilterChip(
                        selected = brainDumpMode,
                        onClick = {
                            brainDumpMode = !brainDumpMode
                            if (brainDumpMode) multiCaptureMode = true
                        },
                        label = { Text("DUMP") }
                    )
                }
            }

            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = TactileTheme.SpacingMd),
                textStyle = MaterialTheme.typography.displayMedium.copy(color = TactileTheme.Text),
                cursorBrush = SolidColor(TactileTheme.Primary),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        val placeholder = when (selectedType) {
                            "project" -> "Project name..."
                            "area" -> "Area name..."
                            else -> if (brainDumpMode) "Next thought..." else "Dump thought..."
                        }
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.displayMedium,
                            color = TactileTheme.Muted
                        )
                    }
                    innerTextField()
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (text.isNotBlank()) {
                        onCapture(text, selectedType, selectedProjectId, selectedAreaId, isRecurring, recurringInterval, reminderTime)
                        if (multiCaptureMode || brainDumpMode) {
                            text = ""
                        } else {
                            onDismiss()
                        }
                    }
                })
            )

            if (!brainDumpMode) {
                Text(
                    "TYPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Primary
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)
                ) {
                    items(listOf("task", "note", "idea", "project", "area")) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.uppercase()) }
                        )
                    }
                }
            }

            if (selectedType != "area" && selectedType != "project") {
                if (areas.isNotEmpty() && !brainDumpMode) {
                    Text("AREA", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                        items(areas) { area ->
                            FilterChip(
                                selected = selectedAreaId == area.id,
                                onClick = { selectedAreaId = if (selectedAreaId == area.id) null else area.id },
                                label = { Text(area.title) }
                            )
                        }
                    }
                }

                if (projects.isNotEmpty() && !brainDumpMode) {
                    Text("PROJECT", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                        items(projects) { project ->
                            FilterChip(
                                selected = selectedProjectId == project.id,
                                onClick = { selectedProjectId = if (selectedProjectId == project.id) null else project.id },
                                label = { Text(project.title) }
                            )
                        }
                    }
                }

                if (!brainDumpMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd)
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isRecurring,
                                onCheckedChange = { isRecurring = it },
                                colors = CheckboxDefaults.colors(checkedColor = TactileTheme.Primary)
                            )
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                tint = TactileTheme.Muted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "RECURRING",
                                style = MaterialTheme.typography.labelSmall,
                                color = TactileTheme.Muted
                            )
                        }

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                reminderTime = if (reminderTime == null) {
                                    val now = kotlinx.datetime.Clock.System.now()
                                    val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
                                    val localDateTime = now.toLocalDateTime(tz)
                                    val evening = kotlinx.datetime.LocalDateTime(
                                        localDateTime.year,
                                        localDateTime.month,
                                        localDateTime.dayOfMonth,
                                        20,
                                        0
                                    )
                                    if (localDateTime.hour >= 20) {
                                        evening.toInstant(tz)
                                            .plus(1, kotlinx.datetime.DateTimeUnit.DAY, tz)
                                            .toEpochMilliseconds()
                                    } else {
                                        evening.toInstant(tz).toEpochMilliseconds()
                                    }
                                } else null
                        }) {
                            Icon(
                                Icons.Default.Notifications, 
                                contentDescription = null, 
                                tint = if (reminderTime != null) TactileTheme.Primary else TactileTheme.Muted
                            )
                        }
                        Text(
                            if (reminderTime == null) "NO REMINDER" else "PROCESS LATER", 
                            style = MaterialTheme.typography.labelSmall, 
                            color = if (reminderTime != null) TactileTheme.Primary else TactileTheme.Muted
                        )
                        }
                    }

                    if (isRecurring) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                            items(listOf("DAILY", "WEEKLY", "MONTHLY")) { interval ->
                                FilterChip(
                                    selected = recurringInterval == interval,
                                    onClick = {
                                        recurringInterval =
                                            if (recurringInterval == interval) null else interval
                                    },
                                    label = { Text(interval) }
                                )
                            }
                        }
                    }
                }
            } else if (selectedType == "project" && areas.isNotEmpty()) {
                Text("ASSIGN TO AREA", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                    items(areas) { area ->
                        FilterChip(
                            selected = selectedAreaId == area.id,
                            onClick = { selectedAreaId = if (selectedAreaId == area.id) null else area.id },
                            label = { Text(area.title) }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onCapture(
                            text,
                            selectedType,
                            selectedProjectId,
                            selectedAreaId,
                            isRecurring,
                            recurringInterval,
                            reminderTime
                        )
                        if (multiCaptureMode || brainDumpMode) {
                            text = ""
                        } else {
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusMd)
            ) {
                Text(
                    if (multiCaptureMode || brainDumpMode) "SAVE & CONTINUE" else "SAVE ${selectedType.uppercase()}",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
