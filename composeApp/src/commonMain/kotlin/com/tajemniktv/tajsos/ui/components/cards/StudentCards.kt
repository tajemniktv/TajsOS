/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.cards

import com.tajemniktv.tajsos.ui.components.TactileOutlinedTextField
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.main.state.StudentBoardState
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.common_flashcard
import tajsos.composeapp.generated.resources.common_open
import tajsos.composeapp.generated.resources.common_revisit
import tajsos.composeapp.generated.resources.student_action_details
import tajsos.composeapp.generated.resources.student_course_id
import tajsos.composeapp.generated.resources.student_course_name
import tajsos.composeapp.generated.resources.student_id_label
import tajsos.composeapp.generated.resources.student_lecture
import tajsos.composeapp.generated.resources.student_lecture_missing
import tajsos.composeapp.generated.resources.student_lecture_ready
import tajsos.composeapp.generated.resources.student_next_exam
import tajsos.composeapp.generated.resources.student_no_exams
import tajsos.composeapp.generated.resources.student_paper
import tajsos.composeapp.generated.resources.student_paper_missing
import tajsos.composeapp.generated.resources.student_paper_ready
import tajsos.composeapp.generated.resources.student_reading
import tajsos.composeapp.generated.resources.student_reading_missing
import tajsos.composeapp.generated.resources.student_reading_ready
import tajsos.composeapp.generated.resources.student_semester
import tajsos.composeapp.generated.resources.student_semester_dashboard
import tajsos.composeapp.generated.resources.student_templates_desc
import tajsos.composeapp.generated.resources.study_label_assignments
import tajsos.composeapp.generated.resources.study_label_flashcards
import tajsos.composeapp.generated.resources.study_label_study_time
import tajsos.composeapp.generated.resources.templates_title

/**
 * Student summary card
 *
 * @param state
 */
@Composable
fun StudentSummaryCard(state: StudentBoardState) {
    Card(colors = CardDefaults.cardColors(containerColor = TajsOSTheme.CardSurface)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            Text(
                stringResource(Res.string.student_semester_dashboard),
                style = MaterialTheme.typography.titleMedium,
            )
            val examLine =
                state.examCountdownDays?.let {
                    stringResource(
                        Res.string.student_next_exam,
                        it.toString(),
                    )
                } ?: stringResource(Res.string.student_no_exams)
            Text(examLine, style = MaterialTheme.typography.bodyMedium, color = TajsOSTheme.Accent)
            Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            stringResource(
                                Res.string.study_label_assignments,
                                state.assignmentTracker.size.toString(),
                            ),
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            stringResource(
                                Res.string.study_label_study_time,
                                state.studyMinutesThisWeek.toString(),
                            ),
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            stringResource(
                                Res.string.study_label_flashcards,
                                state.flashcardCandidates.size.toString(),
                            ),
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                )
            }
        }
    }
}

/**
 * Template quick actions card
 *
 * @param state
 * @param templates
 * @param courseId
 * @param courseName
 * @param semester
 * @param onCourseIdChange
 * @param onCourseNameChange
 * @param onSemesterChange
 * @param onCreate
 * @receiver
 * @receiver
 * @receiver
 * @receiver
 */
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
    val lectureTemplate = remember(templates) {
        templates.firstOrNull { it.name.equals("Lecture Note Template", ignoreCase = true) }
    }
    val readingTemplate = remember(templates) {
        templates.firstOrNull { it.name.equals("Reading Note Template", ignoreCase = true) }
    }
    val paperTemplate = remember(templates) {
        templates.firstOrNull { it.name.equals("Paper Summary Template", ignoreCase = true) }
    }

    Card(colors = CardDefaults.cardColors(containerColor = TajsOSTheme.CardSurface)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            Text(
                stringResource(Res.string.templates_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(Res.string.student_templates_desc),
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (state.lectureTemplateReady) {
                                stringResource(Res.string.student_lecture_ready)
                            } else {
                                stringResource(
                                    Res.string.student_lecture_missing,
                                )
                            },
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (state.readingTemplateReady) {
                                stringResource(Res.string.student_reading_ready)
                            } else {
                                stringResource(
                                    Res.string.student_reading_missing,
                                )
                            },
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                        )
                    },
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (state.paperSummaryTemplateReady) {
                                stringResource(Res.string.student_paper_ready)
                            } else {
                                stringResource(
                                    Res.string.student_paper_missing,
                                )
                            },
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.LocalLibrary, contentDescription = null) },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                TactileOutlinedTextField(
                    containerModifier = Modifier.weight(1f),
                    value = courseId,
                    onValueChange = onCourseIdChange,
                    label = { Text(stringResource(Res.string.student_course_id)) },
                    singleLine = true,
                )
                TactileOutlinedTextField(
                    containerModifier = Modifier.weight(1f),
                    value = semester,
                    onValueChange = onSemesterChange,
                    label = { Text(stringResource(Res.string.student_semester)) },
                    singleLine = true,
                )
            }
            TactileOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = courseName,
                onValueChange = onCourseNameChange,
                label = { Text(stringResource(Res.string.student_course_name)) },
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                Button(
                    enabled = lectureTemplate != null,
                    onClick = { lectureTemplate?.let { onCreate(it, "lecture") } },
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(stringResource(Res.string.student_lecture))
                }
                Button(
                    enabled = readingTemplate != null,
                    onClick = { readingTemplate?.let { onCreate(it, "reading") } },
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(stringResource(Res.string.student_reading))
                }
                Button(
                    enabled = paperTemplate != null,
                    onClick = { paperTemplate?.let { onCreate(it, "research") } },
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(stringResource(Res.string.student_paper))
                }
            }
        }
    }
}

/**
 * Progress control card
 *
 * @param node
 * @param title
 * @param value
 * @param onDecrease
 * @param onIncrease
 * @param onOpen
 */
@Composable
fun ProgressControlCard(
    node: NodeEntity,
    title: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onOpen: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = TajsOSTheme.CardSurface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text("$value%", style = MaterialTheme.typography.bodySmall, color = TajsOSTheme.Accent)
            Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                OutlinedButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = onDecrease) { Text("-10") }
                OutlinedButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = onIncrease) { Text("+10") }
                OutlinedButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = onOpen) { Text(stringResource(Res.string.common_open)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                OutlinedButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = onOpen) { Text(stringResource(Res.string.student_action_details)) }
                OutlinedButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = {}) {
                    Text(
                        stringResource(
                            Res.string.student_id_label,
                            node.id.toString(),
                        ),
                    )
                }
            }
        }
    }
}

/**
 * Student node card
 *
 * @param viewModel
 * @param node
 * @param onEditNode
 * @receiver
 */
@Composable
fun StudentNodeCard(
    viewModel: MainViewModel,
    node: NodeWithPin,
    onEditNode: (Long) -> Unit,
) {
    NodeCard(
        nodeWithPin = node,
        onToggleDone = { viewModel.updateNodeStatus(node.node, it) },
        onTogglePin = { viewModel.togglePin(node.node, it) },
        onClick = { onEditNode(node.node.id) },
        onLongClick = { onEditNode(node.node.id) },
        onArchive = { viewModel.archiveNode(node.node) },
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = TajsOSTheme.SpacingSm),
        horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
    ) {
        OutlinedButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = {
            viewModel.toggleFlashcardCandidate(
                node.node,
                enabled = true,
            )
        }) {
            Text(stringResource(Res.string.common_flashcard))
        }
        OutlinedButton(modifier = Modifier.pointerHoverIcon(PointerIcon.Hand), onClick = { viewModel.toggleRevisitBeforeExam(node.node, enabled = true) }) {
            Text(stringResource(Res.string.common_revisit))
        }
    }
    HorizontalDivider(color = TajsOSTheme.GhostBorder)
}
