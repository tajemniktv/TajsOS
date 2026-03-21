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

@Composable
fun FocusScreen(viewModel: MainViewModel) {
    val activeSession by viewModel.activeSession.collectAsState()
    val todayNodes by viewModel.todayNodes.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    val currentNode = remember(activeSession, todayNodes, allNodes) {
        activeSession?.let { session ->
            todayNodes.find { it.id == session.nodeId }
                ?: allNodes.find { it.node.id == session.nodeId }?.node
        } ?: todayNodes.firstOrNull()
    }

    var timeSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(activeSession) {
        if (activeSession != null) {
            while (true) {
                val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
                val session = activeSession
                if (session != null) {
                    timeSeconds = ((now - session.startedAt) / 1000L).toInt()
                }
                delay(1000L)
            }
        } else {
            timeSeconds = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TactileTheme.SpacingLg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentNode == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "NO ACTIVE TASK",
                    style = MaterialTheme.typography.labelSmall,
                    color = TactileTheme.Muted
                )
            }
        } else {
            Spacer(Modifier.height(TactileTheme.SpacingLg))
            Text(
                text = currentNode.title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary
            )

            val minutes = timeSeconds / 60
            val seconds = timeSeconds % 60
            val timeString = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                color = TactileTheme.Text
            )

            OutlinedTextField(
                value = currentNode.content,
                onValueChange = { 
                    viewModel.updateNode(currentNode.copy(content = it))
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = TactileTheme.SpacingMd),
                label = { Text("NEXT TINY STEP", style = MaterialTheme.typography.labelSmall) },
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true
            )

            if (activeSession == null) {
                Button(
                    onClick = { viewModel.startFocusSession(currentNode.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(TactileTheme.RadiusMd)
                ) {
                    Text("ENGAGE")
                }
            } else {
                AbortSlider(onAbort = {
                    viewModel.stopFocusSession()
                })
            }
            
            Spacer(Modifier.height(TactileTheme.SpacingLg))
            
            Text(
                "RECENT SESSIONS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Muted,
                modifier = Modifier.align(Alignment.Start)
            )
            
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(allSessions.take(5)) { session ->
                    val sessionNode = remember(session, allNodes) {
                        allNodes.find { it.node.id == session.nodeId }?.node
                    }
                    if (session.endedAt != null) {
                        ListItem(
                            headlineContent = { Text(sessionNode?.title ?: "Unknown Task") },
                            supportingContent = { Text("${session.durationSec / 60}m ${session.durationSec % 60}s focus session") },
                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
