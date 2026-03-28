/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.StudentBoardState
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
fun StudentSummaryCard(state: StudentBoardState) {
    Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        ) {
            Text("SEMESTER DASHBOARD", style = MaterialTheme.typography.titleMedium)
            val examLine =
                state.examCountdownDays?.let { "Next exam in ${it}d" } ?: "No exam deadlines yet"
            Text(examLine, style = MaterialTheme.typography.bodyMedium, color = TactileTheme.Accent)
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                AssistChip(
                    onClick = {},
                    label = { Text("Assignments ${state.assignmentTracker.size}") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                )
                AssistChip(
                    onClick = {},
                    label = { Text("Study ${state.studyMinutesThisWeek}m") },
                    leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                )
                AssistChip(
                    onClick = {},
                    label = { Text("Flashcards ${state.flashcardCandidates.size}") },
                    leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                )
            }
        }
    }
}

@Composable
fun TemplateQuickActionsCard(
    state: StudentBoardState,
    templates: List<TemplateEntity>,
    courseId: String,
    courseName: String,
    semester: String,
    onCourseIdChange: (String) -> Unit,
    onCourseNameChange: (String) -> Unit,
    onSemesterChange: (String) -> Unit,
    onCreate: (TemplateEntity, String) -> Unit,
) {
    val lectureTemplate =
        templates.firstOrNull { it.name.equals("Lecture Note Template", ignoreCase = true) }
    val readingTemplate =
        templates.firstOrNull { it.name.equals("Reading Note Template", ignoreCase = true) }
    val paperTemplate =
        templates.firstOrNull { it.name.equals("Paper Summary Template", ignoreCase = true) }

    Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        ) {
            Text("TEMPLATES", style = MaterialTheme.typography.titleMedium)
            Text(
                "Lecture, reading, and paper summary templates are seeded and can be inserted directly.",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                AssistChip(
                    onClick = {},
                    label = { Text(if (state.lectureTemplateReady) "Lecture Ready" else "Lecture Missing") },
                    leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                )
                AssistChip(
                    onClick = {},
                    label = { Text(if (state.readingTemplateReady) "Reading Ready" else "Reading Missing") },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                        )
                    },
                )
                AssistChip(
                    onClick = {},
                    label = { Text(if (state.paperSummaryTemplateReady) "Paper Ready" else "Paper Missing") },
                    leadingIcon = { Icon(Icons.Default.LocalLibrary, contentDescription = null) },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = courseId,
                    onValueChange = onCourseIdChange,
                    label = { Text("Course ID") },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = semester,
                    onValueChange = onSemesterChange,
                    label = { Text("Semester") },
                    singleLine = true,
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = courseName,
                onValueChange = onCourseNameChange,
                label = { Text("Course Name") },
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                Button(
                    enabled = lectureTemplate != null,
                    onClick = { lectureTemplate?.let { onCreate(it, "lecture") } },
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Lecture")
                }
                Button(
                    enabled = readingTemplate != null,
                    onClick = { readingTemplate?.let { onCreate(it, "reading") } },
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Reading")
                }
                Button(
                    enabled = paperTemplate != null,
                    onClick = { paperTemplate?.let { onCreate(it, "research") } },
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Paper")
                }
            }
        }
    }
}

@Composable
fun ProgressControlCard(
    node: NodeEntity,
    title: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onOpen: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text("$value%", style = MaterialTheme.typography.bodySmall, color = TactileTheme.Accent)
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                OutlinedButton(onClick = onDecrease) { Text("-10") }
                OutlinedButton(onClick = onIncrease) { Text("+10") }
                OutlinedButton(onClick = onOpen) { Text("Open") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
                OutlinedButton(onClick = onOpen) { Text("Details") }
                OutlinedButton(onClick = {}) { Text("ID ${node.id}") }
            }
        }
    }
}

@Composable
fun StudentNodeCard(
    viewModel: MainViewModel,
    node: NodeWithPin,
    onEditNode: (Long) -> Unit,
) {
    NodeCard(
        nodeWithPin = node,
        onToggleDone = { status -> viewModel.updateNodeStatus(node.node, status) },
        onTogglePin = { isPinned -> viewModel.togglePin(node.node, isPinned) },
        onClick = { onEditNode(node.node.id) },
        onLongClick = { onEditNode(node.node.id) },
        onArchive = { viewModel.archiveNode(node.node) },
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = TactileTheme.SpacingSm),
        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        OutlinedButton(onClick = { viewModel.toggleFlashcardCandidate(node.node, true) }) {
            Text("Flashcard")
        }
        OutlinedButton(onClick = { viewModel.toggleRevisitBeforeExam(node.node, true) }) {
            Text("Revisit")
        }
    }
    androidx.compose.material3.HorizontalDivider()
}
