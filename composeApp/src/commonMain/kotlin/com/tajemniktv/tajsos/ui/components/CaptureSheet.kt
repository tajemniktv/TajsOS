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
    areas: List<NodeEntity> = emptyList()
) {
    var text by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("task") }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }
    var selectedAreaId by remember { mutableStateOf<Long?>(null) }
    
    var isRecurring by remember { mutableStateOf(false) }
    var recurringInterval by remember { mutableStateOf<String?>(null) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }

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
                            else -> "Dump thought..."
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
                    }
                })
            )

            Text("TYPE", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Primary)
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

            if (selectedType != "area" && selectedType != "project") {
                if (areas.isNotEmpty()) {
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

                if (projects.isNotEmpty()) {
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
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = TactileTheme.Muted, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("RECURRING", style = MaterialTheme.typography.labelSmall, color = TactileTheme.Muted)
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { 
                            reminderTime = if (reminderTime == null) kotlin.time.Clock.System.now().toEpochMilliseconds() + 3600000 else null
                        }) {
                            Icon(
                                Icons.Default.Notifications, 
                                contentDescription = null, 
                                tint = if (reminderTime != null) TactileTheme.Primary else TactileTheme.Muted
                            )
                        }
                        Text(
                            if (reminderTime == null) "NO REMINDER" else "+1H", 
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
                                onClick = { recurringInterval = if (recurringInterval == interval) null else interval },
                                label = { Text(interval) }
                            )
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
                onClick = { if (text.isNotBlank()) onCapture(text, selectedType, selectedProjectId, selectedAreaId, isRecurring, recurringInterval, reminderTime) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(TactileTheme.RadiusMd)
            ) {
                Text("SAVE ${selectedType.uppercase()}", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
