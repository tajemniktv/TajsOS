/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.ModuleButton
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import java.time.LocalDate

/**
 * The Dashboard is the "Control Center" of TajsOS.
 * It provides a quick overview of the current status and helps the user
 * decide "WHAT NOW" to reduce decision paralysis—a key ADHD-friendly feature.
 * 
 * Phase 2 Implementation:
 * - Session status integrated into Active Context.
 * - Resume logic support.
 */
@Composable
fun DashboardScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel, onNavigateTo: (com.tajemniktv.tajsos.ui.Screen) -> Unit) {
    val todayItems by viewModel.todayItems.collectAsState()
    val trackEntries by viewModel.trackEntries.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val allAreas by viewModel.allAreas.collectAsState()
    val allItems by viewModel.allItems.collectAsState()

    val moodToday = trackEntries.find { it.date == LocalDate.now().toString() }
    val tasksCount = allItems.count { it.item.type == "task" }
    val notesCount = allItems.count { it.item.type == "note" || it.item.type == "idea" }
    val pinnedKnowledge = allItems.filter { it.item.isPinned && (it.item.type == "note" || it.item.type == "idea") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg)
    ) {
        Text(
            text = "COMMAND",
            style = MaterialTheme.typography.displayMedium,
            color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
        )

        // Surface Active Context or Resume logic
        if (activeSession != null || todayItems.isNotEmpty() || allSessions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm))
            val activeItem = activeSession?.let { session ->
                todayItems.find { it.id == session.itemId }
                    ?: viewModel.allItems.value.find { it.item.id == session.itemId }?.item
            } ?: todayItems.firstOrNull() ?: allSessions.firstOrNull()?.let { session ->
                viewModel.allItems.value.find { it.item.id == session.itemId }?.item
            }

            activeItem?.let { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm),
                    color = if (activeSession != null) _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary.copy(alpha = 0.15f) else _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
                    shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusLg),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (activeSession != null) _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary else _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Border
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (activeSession != null) "OPERATING" else "NEXT CONTEXT",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                                color = if (activeSession != null) _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Accent else _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.title.uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
                            )
                        }

                        Button(
                            onClick = { if (activeSession != null) onNavigateTo(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Focus) else viewModel.startFocusSession(item.id) },
                            shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusSm),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeSession != null) _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary else _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
                                contentColor = if (activeSession != null) _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Background else _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
                            ),
                            border = if (activeSession == null) androidx.compose.foundation.BorderStroke(1.dp, _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary) else null
                        ) {
                            Text(if (activeSession != null) "VIEW" else "ENGAGE")
                        }
                    }
                }
            }
        }

        // Pinned Knowledge Section (Second Brain)
        if (pinnedKnowledge.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingSm)) {
                Text(
                    text = "PINNED KNOWLEDGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
                )
                pinnedKnowledge.take(2).forEach { itemWithPin ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Surface,
                        shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusSm),
                        border = androidx.compose.foundation.BorderStroke(1.dp, _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd))
                            Text(
                                text = itemWithPin.item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
                            )
                        }
                    }
                }
            }
        }

        // 2x2 Grid
        Column(verticalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)) {
            Row(horizontalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)) {
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "TODAY",
                    icon = Icons.Default.DateRange,
                    status = if (todayItems.isNotEmpty()) "${todayItems.size} TASKS" else "EMPTY",
                    onClick = { onNavigateTo(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Today) }
                )
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "FOCUS",
                    icon = Icons.Default.PlayArrow,
                    status = if (todayItems.isNotEmpty()) "READY" else "WAITING",
                    onClick = { onNavigateTo(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Focus) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)) {
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "TRACK",
                    icon = Icons.Default.CheckCircle,
                    status = moodToday?.let { "LOGGED" } ?: "PENDING",
                    onClick = { onNavigateTo(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Track) }
                )
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "TASKS",
                    icon = Icons.AutoMirrored.Filled.List,
                    status = if (tasksCount > 0) "$tasksCount TOTAL" else "NONE",
                    onClick = { onNavigateTo(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Tasks) }
                )
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "KNOWLEDGE",
                    icon = Icons.Default.Edit,
                    status = if (notesCount > 0) "$notesCount NOTES" else "EMPTY",
                    onClick = { onNavigateTo(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Notes) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)) {
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "PROJ",
                    icon = Icons.Default.List,
                    status = if (allProjects.isNotEmpty()) "${allProjects.size} ACTIVE" else "EMPTY",
                    onClick = { onNavigateTo(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Projects) }
                )
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.ModuleButton(
                    modifier = Modifier.weight(1f),
                    title = "AREA",
                    icon = Icons.Default.LocationOn,
                    status = if (allAreas.isNotEmpty()) "${allAreas.size} TOTAL" else "EMPTY",
                    onClick = { onNavigateTo(_root_ide_package_.com.tajemniktv.tajsos.ui.Screen.Areas) }
                )
            }
        }
    }
}
