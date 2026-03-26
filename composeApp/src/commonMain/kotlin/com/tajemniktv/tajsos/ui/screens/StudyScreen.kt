/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.StudentBoardState
import com.tajemniktv.tajsos.ui.components.cards.NodeCard
import com.tajemniktv.tajsos.ui.components.cards.ProgressControlCard
import com.tajemniktv.tajsos.ui.components.cards.StudentNodeCard
import com.tajemniktv.tajsos.ui.components.cards.StudentSummaryCard
import com.tajemniktv.tajsos.ui.components.cards.TemplateQuickActionsCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.components.common.SelectorDialog
import com.tajemniktv.tajsos.ui.theme.TactileTheme

private enum class StudentTab(
    val label: String,
) {
    Dashboard("DASHBOARD"),
    Notes("NOTES & KNOWLEDGE"),
    Tracking("TRACKERS"),
    Links("LINKS & GRAPH"),
}

/**
 * Renders the unified student study workspace.
 *
 * This is the single entry point for study features (dashboard, notes, tracking, and links).
 */
@Composable
fun StudyScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val studentState by viewModel.studentBoardState.collectAsState()
    val allTemplates by viewModel.allTemplates.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()

    var tab by remember { mutableStateOf(StudentTab.Dashboard) }
    var courseId by remember { mutableStateOf("") }
    var courseName by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }

    var showTopicSourceDialog by remember { mutableStateOf(false) }
    var showTopicTargetDialog by remember { mutableStateOf(false) }
    var selectedTopicNode by remember { mutableStateOf<NodeWithPin?>(null) }

    var showPaperSourceDialog by remember { mutableStateOf(false) }
    var showPaperTargetDialog by remember { mutableStateOf(false) }
    var selectedPaperNode by remember { mutableStateOf<NodeWithPin?>(null) }

    val topicCandidates =
        remember(allNodes) {
            allNodes.filter { it.node.type == "note" && (it.node.noteType == "concept" || it.node.noteType == "lecture") }
        }
    val noteCandidates =
        remember(allNodes) {
            allNodes.filter { it.node.type == "note" || it.node.type == "idea" }
        }
    val paperCandidates =
        remember(allNodes) {
            allNodes.filter { it.node.type == "note" && (it.node.noteType == "reading" || it.node.noteType == "research") }
        }

    SelectorDialog(
        show = showTopicSourceDialog,
        onDismiss = { showTopicSourceDialog = false },
        title = "SELECT TOPIC",
        prefix = "STUDENT_BOARD // TOPIC_LINK",
        options = topicCandidates,
        selectedOption = selectedTopicNode,
        onSelect = {
            selectedTopicNode = it
            showTopicSourceDialog = false
            showTopicTargetDialog = true
        },
        optionName = { it.node.title },
        optionIcon = { Icons.Default.Topic },
        optionSubtext = { "NODE_${it.node.id}" },
    )

    SelectorDialog(
        show = showTopicTargetDialog,
        onDismiss = { showTopicTargetDialog = false },
        title = "SELECT NOTE",
        prefix = "STUDENT_BOARD // TOPIC_LINK",
        options = noteCandidates,
        selectedOption = null,
        onSelect = {
            val source = selectedTopicNode
            if (source != null) {
                viewModel.linkTopicToNote(source.node.id, it.node.id)
                showTopicTargetDialog = false
                selectedTopicNode = null
            }
        },
        optionName = { it.node.title },
        optionIcon = { Icons.Default.Link },
        optionSubtext = { "NODE_${it.node.id}" },
    )

    SelectorDialog(
        show = showPaperSourceDialog,
        onDismiss = { showPaperSourceDialog = false },
        title = "SELECT PAPER",
        prefix = "STUDENT_BOARD // PAPER_LINK",
        options = paperCandidates,
        selectedOption = selectedPaperNode,
        onSelect = {
            selectedPaperNode = it
            showPaperSourceDialog = false
            showPaperTargetDialog = true
        },
        optionName = { it.node.title },
        optionIcon = { Icons.Default.AutoStories },
        optionSubtext = { "NODE_${it.node.id}" },
    )

    SelectorDialog(
        show = showPaperTargetDialog,
        onDismiss = { showPaperTargetDialog = false },
        title = "SELECT NOTE",
        prefix = "STUDENT_BOARD // PAPER_LINK",
        options = noteCandidates,
        selectedOption = null,
        onSelect = {
            val source = selectedPaperNode
            if (source != null) {
                viewModel.linkPaperToNote(source.node.id, it.node.id)
                showPaperTargetDialog = false
                selectedPaperNode = null
            }
        },
        optionName = { it.node.title },
        optionIcon = { Icons.Default.Link },
        optionSubtext = { "NODE_${it.node.id}" },
    )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            text = "STUDENT / UNIVERSITY BOARD",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text,
        )
        Text(
            text = "Assignments, exam prep, study sessions, concept linking, and progress tracking in one workspace.",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )

        StudentSummaryCard(studentState)
        TemplateQuickActionsCard(
            state = studentState,
            templates = allTemplates,
            courseId = courseId,
            courseName = courseName,
            semester = semester,
            onCourseIdChange = { courseId = it },
            onCourseNameChange = { courseName = it },
            onSemesterChange = { semester = it },
            onCreate = { template, noteType ->
                viewModel.addStudentNote(
                    title = template.defaultTitle ?: template.name,
                    content = template.defaultContent.orEmpty(),
                    noteType = noteType,
                    courseId = courseId,
                    courseName = courseName,
                    semester = semester,
                )
            },
        )

        ScrollableTabRow(
            selectedTabIndex = tab.ordinal,
            edgePadding = TactileTheme.SpacingSm,
            containerColor = TactileTheme.Surface,
        ) {
            StudentTab.entries.forEach { entry ->
                Tab(
                    selected = entry == tab,
                    onClick = { tab = entry },
                    text = {
                        Text(
                            text = entry.label,
                            fontWeight = if (entry == tab) FontWeight.Bold else FontWeight.Medium,
                        )
                    },
                )
            }
        }

        when (tab)
        {
            StudentTab.Dashboard -> {
                StudentDashboardTab(viewModel, studentState, onEditNode)
            }

            StudentTab.Notes -> {
                StudentNotesTab(viewModel, studentState, onEditNode)
            }

            StudentTab.Tracking -> {
                StudentTrackingTab(viewModel, studentState, onEditNode)
            }

            StudentTab.Links -> {
                StudentLinksTab(
                    state = studentState,
                    onOpenTopicLink = { showTopicSourceDialog = true },
                    onOpenPaperLink = { showPaperSourceDialog = true },
                )
            }
        }
    }
}

@Composable
private fun StudentDashboardTab(
    viewModel: MainViewModel,
    state: StudentBoardState,
    onEditNode: (Long) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        item {
            SectionTitle("ASSIGNMENT TRACKER")
        }
        if (state.assignmentTracker.isEmpty()) {
            item { EmptyState("No assignments yet.") }
        } else {
            items(state.assignmentTracker.take(10), key = { it.node.id }) { node ->
                StudentNodeCard(viewModel, node, onEditNode)
            }
        }

        item { SectionTitle("EXAM PREP BOARD") }
        if (state.examPrepBoard.isEmpty()) {
            item { EmptyState("No exam prep items yet.") }
        } else {
            items(state.examPrepBoard.take(10), key = { it.node.id }) { node ->
                StudentNodeCard(viewModel, node, onEditNode)
            }
        }

        item { SectionTitle("ASSIGNMENT DEADLINE SUMMARY") }
        if (state.assignmentDeadlines.isEmpty()) {
            item { EmptyState("No upcoming assignment deadlines.") }
        } else {
            items(state.assignmentDeadlines, key = { it.node.id }) { node ->
                StudentNodeCard(viewModel, node, onEditNode)
            }
        }

        item { SectionTitle("REVISIT BEFORE EXAM") }
        if (state.revisitBeforeExam.isEmpty()) {
            item { EmptyState("No revisit queue yet.") }
        } else {
            items(state.revisitBeforeExam.take(10), key = { it.node.id }) { node ->
                StudentNodeCard(viewModel, node, onEditNode)
            }
        }

        item { SectionTitle("COURSE DASHBOARD") }
        if (state.courseDashboard.isEmpty()) {
            item { EmptyState("No course metadata yet.") }
        } else {
            items(state.courseDashboard, key = { it.courseId }) { summary ->
                Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd)) {
                        Text(summary.courseName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Course: ${summary.courseId}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "Semester: ${summary.semester ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "Open assignments: ${summary.openAssignments}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "Upcoming exams: ${summary.upcomingExams}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "Topic mastery avg: ${summary.avgMasteryPercent?.let { "$it%" } ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        item { SectionTitle("SEMESTER DASHBOARD") }
        if (state.semesterDashboard.isEmpty()) {
            item { EmptyState("No semester metadata yet.") }
        } else {
            items(state.semesterDashboard, key = { it.semester }) { summary ->
                Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd)) {
                        Text(summary.semester, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Courses: ${summary.courseCount}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "Open assignments: ${summary.openAssignments}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "Upcoming exams: ${summary.upcomingExams}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "Due in 7 days: ${summary.dueSoon}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentNotesTab(
    viewModel: MainViewModel,
    state: StudentBoardState,
    onEditNode: (Long) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        SectionTitle("PSYCHOLOGY TOPIC CONCEPT MAPS")
        NodeSection(viewModel, state.psychologyConceptMaps, onEditNode)

        SectionTitle("GLOSSARY / KNOWLEDGE CARDS")
        NodeSection(viewModel, state.glossaryCards, onEditNode)

        SectionTitle("RESEARCH IDEA VAULT")
        NodeSection(viewModel, state.researchIdeaVault, onEditNode)

        SectionTitle("QUOTE BANK")
        NodeSection(viewModel, state.quoteBank, onEditNode)

        SectionTitle("CASE / REFLECTION NOTES")
        NodeSection(viewModel, state.caseReflectionNotes, onEditNode)

        SectionTitle("READING BACKLOG")
        NodeSection(viewModel, state.readingBacklog, onEditNode)

        SectionTitle("FLASHCARD EXPORT LATER")
        if (state.flashcardCandidates.isEmpty()) {
            EmptyState("Mark notes as flashcard candidates from note detail or trackers.")
        } else {
            state.flashcardCandidates.take(12).forEach { item ->
                Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.node.title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Candidate for flashcard export",
                                style = MaterialTheme.typography.bodySmall,
                                color = TactileTheme.Muted,
                            )
                        }
                        OutlinedButton(onClick = {
                            viewModel.toggleFlashcardCandidate(
                                item.node,
                                false,
                            )
                        }) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentTrackingTab(
    viewModel: MainViewModel,
    state: StudentBoardState,
    onEditNode: (Long) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        SectionTitle("STUDY SESSION TIMER")
        if (state.assignmentTracker.isEmpty()) {
            EmptyState("Open an assignment to start a study session timer.")
        } else {
            state.assignmentTracker.take(6).forEach { item ->
                Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.node.title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Start focus session on this assignment",
                                style = MaterialTheme.typography.bodySmall,
                                color = TactileTheme.Muted,
                            )
                        }
                        Button(onClick = { viewModel.startStudySession(item.node.id) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Text("Start")
                        }
                    }
                }
            }
        }

        SectionTitle("READING PROGRESS TRACKER")
        if (state.readingProgress.isEmpty()) {
            EmptyState("Set reading progress from this tab using +/- controls.")
            state.readingBacklog.take(8).forEach { item ->
                ProgressControlCard(
                    node = item.node,
                    title = item.node.title,
                    value = 0,
                    onDecrease = { viewModel.setReadingProgress(item.node, 0) },
                    onIncrease = { viewModel.setReadingProgress(item.node, 10) },
                    onOpen = { onEditNode(item.node.id) },
                )
            }
        } else {
            state.readingProgress.forEach { item ->
                ProgressControlCard(
                    node = item.node.node,
                    title = item.node.node.title,
                    value = item.progressPercent,
                    onDecrease = {
                        viewModel.setReadingProgress(
                            item.node.node,
                            (item.progressPercent - 10).coerceAtLeast(0),
                        )
                    },
                    onIncrease = {
                        viewModel.setReadingProgress(
                            item.node.node,
                            (item.progressPercent + 10).coerceAtMost(100),
                        )
                    },
                    onOpen = { onEditNode(item.node.node.id) },
                )
            }
        }

        SectionTitle("TOPIC MASTERY TRACKER")
        if (state.topicMastery.isEmpty()) {
            EmptyState("No mastery values yet.")
        } else {
            state.topicMastery.forEach { item ->
                ProgressControlCard(
                    node = item.node.node,
                    title = item.topic,
                    value = item.masteryPercent,
                    onDecrease = {
                        viewModel.setTopicMastery(
                            item.node.node,
                            topic = item.topic,
                            masteryPercent = (item.masteryPercent - 10).coerceAtLeast(0),
                        )
                    },
                    onIncrease = {
                        viewModel.setTopicMastery(
                            item.node.node,
                            topic = item.topic,
                            masteryPercent = (item.masteryPercent + 10).coerceAtMost(100),
                        )
                    },
                    onOpen = { onEditNode(item.node.node.id) },
                )
            }
        }
    }
}

@Composable
private fun StudentLinksTab(
    state: StudentBoardState,
    onOpenTopicLink: () -> Unit,
    onOpenPaperLink: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd)) {
                Text("TOPIC-TO-NOTE LINKING", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Existing links: ${state.topicToNoteLinks}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(TactileTheme.SpacingSm))
                Button(onClick = onOpenTopicLink) {
                    Icon(Icons.Default.Link, contentDescription = null)
                    Text("Create Topic Link")
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd)) {
                Text("PAPER-TO-NOTE LINKING", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Existing links: ${state.paperToNoteLinks}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(TactileTheme.SpacingSm))
                Button(onClick = onOpenPaperLink) {
                    Icon(Icons.Default.Link, contentDescription = null)
                    Text("Create Paper Link")
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd)) {
                Text(
                    "PSYCHOLOGY CONCEPT KNOWLEDGE GRAPH",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "Concept nodes: ${state.conceptGraphNodes}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "Concept edges: ${state.conceptGraphEdges}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "Use Graph screen for full visual graph exploration.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TactileTheme.Muted,
                )
            }
        }
    }
}

@Composable
private fun NodeSection(
    viewModel: MainViewModel,
    nodes: List<NodeWithPin>,
    onEditNode: (Long) -> Unit,
) {
    if (nodes.isEmpty()) {
        EmptyState("No entries yet.")
    } else {
        nodes.take(10).forEach { item ->
            StudentNodeCard(viewModel, item, onEditNode)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = TactileTheme.Text,
        fontWeight = FontWeight.SemiBold,
    )
}
