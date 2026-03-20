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
import androidx.compose.ui.unit.sp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.AbortSlider
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlinx.coroutines.delay
import java.util.*

/**
 * FocusScreen helps the user execute a single task with minimal distraction.
 * 
 * Phase 2 Implementation:
 * - Persistent timer state via ViewModel.
 * - Auto-resume logic.
 * - Session history and tiny-step support.
 */
@Composable
fun FocusScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val todayItems by viewModel.todayItems.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    // Find the item associated with the active session, or the first today item
    val currentItem = remember(activeSession, todayItems, allItems) {
        activeSession?.let { session ->
            todayItems.find { it.id == session.itemId }
                ?: allItems.find { it.item.id == session.itemId }?.item
        } ?: todayItems.firstOrNull()
    }

    var timeSeconds by remember { mutableIntStateOf(0) }

    // Update timer based on session start time
    LaunchedEffect(activeSession) {
        val session = activeSession
        if (session != null) {
            while (true) {
                val now = System.currentTimeMillis()
                timeSeconds = ((now - session.startAt) / 1000).toInt()
                delay(1000L)
            }
        } else {
            timeSeconds = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentItem == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "NO ACTIVE TASK",
                    style = MaterialTheme.typography.labelSmall,
                    color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted
                )
            }
        } else {
            Spacer(Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg))
            Text(
                text = currentItem.title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary
            )

            Text(
                text = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    timeSeconds / 60,
                    timeSeconds % 60
                ),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text
            )

            // Tiny Step Support
            OutlinedTextField(
                value = currentItem.body,
                onValueChange = { 
                    viewModel.updateItem(currentItem.copy(body = it))
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd),
                label = { Text("NEXT TINY STEP", style = MaterialTheme.typography.labelSmall) },
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true
            )

            if (activeSession == null) {
                Button(
                    onClick = { viewModel.startFocusSession(currentItem.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.RadiusMd)
                ) {
                    Text("ENGAGE")
                }
            } else {
                _root_ide_package_.com.tajemniktv.tajsos.ui.components.AbortSlider(onAbort = {
                    viewModel.stopFocusSession()
                })
            }
            
            Spacer(Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg))
            
            Text(
                "RECENT SESSIONS",
                style = MaterialTheme.typography.labelSmall,
                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted,
                modifier = Modifier.align(Alignment.Start)
            )
            
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(allSessions.take(5)) { session ->
                    val sessionItem = remember(session, allItems) {
                        allItems.find { it.item.id == session.itemId }?.item
                    }
                    if (session.endAt != null) {
                        ListItem(
                            headlineContent = { Text(sessionItem?.title ?: "Unknown Task") },
                            supportingContent = { Text("${session.durationSec / 60}m ${session.durationSec % 60}s focus session") },
                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
