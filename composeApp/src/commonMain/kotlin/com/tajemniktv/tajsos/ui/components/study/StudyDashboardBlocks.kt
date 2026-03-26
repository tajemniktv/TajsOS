/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.components.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.components.cards.ProgressControlCard
import com.tajemniktv.tajsos.ui.components.cards.StudentNodeCard
import com.tajemniktv.tajsos.ui.components.cards.StudentSummaryCard
import com.tajemniktv.tajsos.ui.components.cards.TemplateQuickActionsCard
import com.tajemniktv.tajsos.ui.components.common.EmptyState
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
internal fun renderStudyHeaderBlock(context: StudyDashboardContext) {
    Column(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        Text(
            text = "STUDENT / UNIVERSITY BOARD",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text,
        )
        Text(
            text = "Assignments, exam prep, sessions, concept linking, and progress tracking.",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )
    }
}

@Composable
internal fun renderStudySummaryBlock(context: StudyDashboardContext) {
    StudentSummaryCard(context.state)
}

@Composable
internal fun renderStudyQuickActionsBlock(context: StudyDashboardContext) {
    TemplateQuickActionsCard(
        state = context.state,
        templates = context.allTemplates,
        courseId = context.courseId,
        courseName = context.courseName,
        semester = context.semester,
        onCourseIdChange = context.onCourseIdChange,
        onCourseNameChange = context.onCourseNameChange,
        onSemesterChange = context.onSemesterChange,
        onCreate = { template, noteType ->
            context.viewModel.addStudentNote(
                title = template.defaultTitle ?: template.name,
                content = template.defaultContent.orEmpty(),
                noteType = noteType,
                courseId = context.courseId,
                courseName = context.courseName,
                semester = context.semester,
            )
        },
    )
}

@Composable
internal fun renderAssignmentTrackerBlock(context: StudyDashboardContext) {
    SectionTitle("ASSIGNMENT TRACKER")
    if (context.state.assignmentTracker.isEmpty()) {
        EmptyState("No assignments yet.")
        return
    }
    context.state.assignmentTracker.take(10).forEach { node ->
        StudentNodeCard(context.viewModel, node, context.onEditNode)
    }
}

@Composable
internal fun renderExamPrepBlock(context: StudyDashboardContext) {
    SectionTitle("EXAM PREP BOARD")
    if (context.state.examPrepBoard.isEmpty()) {
        EmptyState("No exam prep items yet.")
        return
    }
    context.state.examPrepBoard.take(10).forEach { node ->
        StudentNodeCard(context.viewModel, node, context.onEditNode)
    }
}

@Composable
internal fun renderAssignmentDeadlinesBlock(context: StudyDashboardContext) {
    SectionTitle("ASSIGNMENT DEADLINE SUMMARY")
    if (context.state.assignmentDeadlines.isEmpty()) {
        EmptyState("No upcoming assignment deadlines.")
        return
    }
    context.state.assignmentDeadlines.forEach { node ->
        StudentNodeCard(context.viewModel, node, context.onEditNode)
    }
}

@Composable
internal fun renderRevisitBeforeExamBlock(context: StudyDashboardContext) {
    SectionTitle("REVISIT BEFORE EXAM")
    if (context.state.revisitBeforeExam.isEmpty()) {
        EmptyState("No revisit queue yet.")
        return
    }
    context.state.revisitBeforeExam.take(10).forEach { node ->
        StudentNodeCard(context.viewModel, node, context.onEditNode)
    }
}

@Composable
internal fun renderReadingProgressBlock(context: StudyDashboardContext) {
    SectionTitle("READING PROGRESS TRACKER")
    if (context.state.readingProgress.isEmpty()) {
        EmptyState("Set reading progress from this dashboard using +/- controls.")
        context.state.readingBacklog.take(8).forEach { item ->
            ProgressControlCard(
                node = item.node,
                title = item.node.title,
                value = 0,
                onDecrease = { context.viewModel.setReadingProgress(item.node, 0) },
                onIncrease = { context.viewModel.setReadingProgress(item.node, 10) },
                onOpen = { context.onEditNode(item.node.id) },
            )
        }
        return
    }
    context.state.readingProgress.forEach { item ->
        ProgressControlCard(
            node = item.node.node,
            title = item.node.node.title,
            value = item.progressPercent,
            onDecrease = {
                context.viewModel.setReadingProgress(
                    item.node.node,
                    (item.progressPercent - 10).coerceAtLeast(0),
                )
            },
            onIncrease = {
                context.viewModel.setReadingProgress(
                    item.node.node,
                    (item.progressPercent + 10).coerceAtMost(100),
                )
            },
            onOpen = { context.onEditNode(item.node.node.id) },
        )
    }
}

@Composable
internal fun renderTopicMasteryBlock(context: StudyDashboardContext) {
    SectionTitle("TOPIC MASTERY TRACKER")
    if (context.state.topicMastery.isEmpty()) {
        EmptyState("No mastery values yet.")
        return
    }
    context.state.topicMastery.forEach { item ->
        ProgressControlCard(
            node = item.node.node,
            title = item.topic,
            value = item.masteryPercent,
            onDecrease = {
                context.viewModel.setTopicMastery(
                    item.node.node,
                    topic = item.topic,
                    masteryPercent = (item.masteryPercent - 10).coerceAtLeast(0),
                )
            },
            onIncrease = {
                context.viewModel.setTopicMastery(
                    item.node.node,
                    topic = item.topic,
                    masteryPercent = (item.masteryPercent + 10).coerceAtMost(100),
                )
            },
            onOpen = { context.onEditNode(item.node.node.id) },
        )
    }
}

@Composable
internal fun renderKnowledgeVaultsBlock(context: StudyDashboardContext) {
    SectionTitle("KNOWLEDGE VAULTS")
    NodeSection(context, "PSYCHOLOGY CONCEPT MAPS", context.state.psychologyConceptMaps)
    NodeSection(context, "GLOSSARY / KNOWLEDGE CARDS", context.state.glossaryCards)
    NodeSection(context, "RESEARCH IDEA VAULT", context.state.researchIdeaVault)
    NodeSection(context, "QUOTE BANK", context.state.quoteBank)
    NodeSection(context, "CASE / REFLECTION NOTES", context.state.caseReflectionNotes)
    NodeSection(context, "READING BACKLOG", context.state.readingBacklog)
}

@Composable
internal fun renderFlashcardCandidatesBlock(context: StudyDashboardContext) {
    SectionTitle("FLASHCARD EXPORT LATER")
    if (context.state.flashcardCandidates.isEmpty()) {
        EmptyState("Mark notes as flashcard candidates from note detail or trackers.")
        return
    }
    context.state.flashcardCandidates.take(12).forEach { item ->
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
                    context.viewModel.toggleFlashcardCandidate(
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

@Composable
internal fun renderLinksGraphBlock(context: StudyDashboardContext) {
    SectionTitle("LINKS & GRAPH")
    Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd)) {
            Text("TOPIC-TO-NOTE LINKING", style = MaterialTheme.typography.titleMedium)
            Text(
                "Existing links: ${context.state.topicToNoteLinks}",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(TactileTheme.SpacingSm))
            Button(onClick = context.onOpenTopicLink) {
                Icon(Icons.Default.Link, contentDescription = null)
                Text("Create Topic Link")
            }
        }
    }

    Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd)) {
            Text("PAPER-TO-NOTE LINKING", style = MaterialTheme.typography.titleMedium)
            Text(
                "Existing links: ${context.state.paperToNoteLinks}",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(TactileTheme.SpacingSm))
            Button(onClick = context.onOpenPaperLink) {
                Icon(Icons.Default.Link, contentDescription = null)
                Text("Create Paper Link")
            }
        }
    }

    Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd)) {
            Text("PSYCHOLOGY CONCEPT KNOWLEDGE GRAPH", style = MaterialTheme.typography.titleMedium)
            Text(
                "Concept nodes: ${context.state.conceptGraphNodes}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Concept edges: ${context.state.conceptGraphEdges}",
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

@Composable
internal fun renderCourseDashboardBlock(context: StudyDashboardContext) {
    SectionTitle("COURSE DASHBOARD")
    if (context.state.courseDashboard.isEmpty()) {
        EmptyState("No course metadata yet.")
        return
    }
    context.state.courseDashboard.forEach { summary ->
        Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd)) {
                Text(summary.courseName, style = MaterialTheme.typography.titleMedium)
                Text("Course: ${summary.courseId}", style = MaterialTheme.typography.bodySmall)
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

@Composable
internal fun renderSemesterDashboardBlock(context: StudyDashboardContext) {
    SectionTitle("SEMESTER DASHBOARD")
    if (context.state.semesterDashboard.isEmpty()) {
        EmptyState("No semester metadata yet.")
        return
    }
    context.state.semesterDashboard.forEach { summary ->
        Card(colors = CardDefaults.cardColors(containerColor = TactileTheme.Surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd)) {
                Text(summary.semester, style = MaterialTheme.typography.titleMedium)
                Text("Courses: ${summary.courseCount}", style = MaterialTheme.typography.bodySmall)
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

@Composable
private fun NodeSection(
    context: StudyDashboardContext,
    title: String,
    nodes: List<NodeWithPin>,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = TactileTheme.Text,
        fontWeight = FontWeight.Medium,
    )
    if (nodes.isEmpty()) {
        EmptyState("No entries yet.")
    } else {
        for (item in nodes.take(10)) {
            StudentNodeCard(context.viewModel, item, context.onEditNode)
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
