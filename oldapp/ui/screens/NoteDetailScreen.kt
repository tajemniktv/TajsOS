/*
 * Copyright (c) TajemnikTV 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.theme.TactileTheme

/**
 * NoteDetailScreen allows viewing and editing a single note or idea.
 * It focuses on "Knowledge" pillars: rich capture, long-form content, and persistent pinning.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(viewModel: com.tajemniktv.tajsos.ui.MainViewModel, noteId: Long, onBack: () -> Unit) {
    val items by viewModel.allItems.collectAsState()
    val itemWithPin = items.find { it.item.id == noteId }

    if (itemWithPin == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Note not found")
        }
        return
    }

    val item = itemWithPin.item
    var title by remember { mutableStateOf(item.title) }
    var body by remember { mutableStateOf(item.body) }
    var isPinned by remember { mutableStateOf(item.isPinned) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.type.uppercase(), style = MaterialTheme.typography.labelSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isPinned = !isPinned
                        viewModel.updateItem(item.copy(isPinned = isPinned))
                    }) {
                        Icon(
                            if (isPinned) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Pin knowledge",
                            tint = if (isPinned) _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary else _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted
                        )
                    }
                    IconButton(onClick = {
                        viewModel.archiveItem(item)
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Archive")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)
        ) {
            // Title Input
            BasicTextField(
                value = title,
                onValueChange = {
                    title = it
                    viewModel.updateItem(item.copy(title = it))
                },
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text),
                cursorBrush = SolidColor(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text("Untitled...", style = MaterialTheme.typography.headlineMedium, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
                    }
                    innerTextField()
                }
            )

            HorizontalDivider(color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.2f))

            // Body Input (Knowledge)
            BasicTextField(
                value = body,
                onValueChange = {
                    body = it
                    viewModel.updateItem(item.copy(body = it))
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Text),
                cursorBrush = SolidColor(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp),
                decorationBox = { innerTextField ->
                    if (body.isEmpty()) {
                        Text("Start writing...", style = MaterialTheme.typography.bodyLarge, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted)
                    }
                    innerTextField()
                }
            )
            
            Spacer(modifier = Modifier.height(_root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingLg))
            
            // Metadata: Project/Area
            Text("ORGANIZATION", style = MaterialTheme.typography.labelSmall, color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Primary)
            
            val areas by viewModel.allAreas.collectAsState()
            val projects by viewModel.allProjects.collectAsState()
            
            val area = areas.find { it.id == item.areaId }
            val project = projects.find { it.id == item.projectId }

            Text(
                text = "Area: ${area?.name ?: "None"}",
                style = MaterialTheme.typography.bodySmall,
                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted
            )
            Text(
                text = "Project: ${project?.name ?: "None"}",
                style = MaterialTheme.typography.bodySmall,
                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted
            )
            
            Text(
                text = "Last updated: ${java.text.SimpleDateFormat.getDateTimeInstance().format(java.util.Date(item.updatedAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.Muted.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = _root_ide_package_.com.tajemniktv.tajsos.ui.theme.TactileTheme.SpacingMd)
            )
        }
    }
}
