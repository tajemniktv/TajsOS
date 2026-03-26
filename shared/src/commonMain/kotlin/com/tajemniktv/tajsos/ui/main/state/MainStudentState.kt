/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeWithPin

data class StudentCourseSummary(
    val courseId: String,
    val courseName: String,
    val semester: String?,
    val openAssignments: Int,
    val upcomingExams: Int,
    val avgMasteryPercent: Int?,
)

data class StudentSemesterSummary(
    val semester: String,
    val courseCount: Int,
    val openAssignments: Int,
    val upcomingExams: Int,
    val dueSoon: Int,
)

data class StudentProgressItem(
    val node: NodeWithPin,
    val progressPercent: Int,
)

data class StudentMasteryItem(
    val node: NodeWithPin,
    val topic: String,
    val masteryPercent: Int,
)

data class StudentBoardState(
    val lectureTemplateReady: Boolean = false,
    val readingTemplateReady: Boolean = false,
    val paperSummaryTemplateReady: Boolean = false,
    val assignmentTracker: List<NodeWithPin> = emptyList(),
    val examPrepBoard: List<NodeWithPin> = emptyList(),
    val psychologyConceptMaps: List<NodeWithPin> = emptyList(),
    val glossaryCards: List<NodeWithPin> = emptyList(),
    val researchIdeaVault: List<NodeWithPin> = emptyList(),
    val quoteBank: List<NodeWithPin> = emptyList(),
    val caseReflectionNotes: List<NodeWithPin> = emptyList(),
    val readingBacklog: List<NodeWithPin> = emptyList(),
    val revisitBeforeExam: List<NodeWithPin> = emptyList(),
    val readingProgress: List<StudentProgressItem> = emptyList(),
    val assignmentDeadlines: List<NodeWithPin> = emptyList(),
    val topicMastery: List<StudentMasteryItem> = emptyList(),
    val courseDashboard: List<StudentCourseSummary> = emptyList(),
    val semesterDashboard: List<StudentSemesterSummary> = emptyList(),
    val examCountdownNode: NodeWithPin? = null,
    val examCountdownDays: Long? = null,
    val topicToNoteLinks: Int = 0,
    val paperToNoteLinks: Int = 0,
    val conceptGraphNodes: Int = 0,
    val conceptGraphEdges: Int = 0,
    val flashcardCandidates: List<NodeWithPin> = emptyList(),
    val studySessionsThisWeek: Int = 0,
    val studyMinutesThisWeek: Int = 0,
)
