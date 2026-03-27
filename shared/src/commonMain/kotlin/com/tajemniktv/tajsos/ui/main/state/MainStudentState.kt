/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui

import com.tajemniktv.tajsos.data.NodeWithPin

/**
 * A data class summarizing the status and workload of a specific student course.
 *
 * @param courseId The unique identifier or code for the course (e.g., "PSY101").
 * @param courseName The full descriptive name of the course.
 * @param semester The semester or academic term this course belongs to.
 * @param openAssignments The number of currently active or incomplete assignments for the course.
 * @param upcomingExams The number of scheduled exams approaching for the course.
 * @param avgMasteryPercent The computed average mastery percentage across all topics within the course, if applicable.
 */
data class StudentCourseSummary(
    val courseId: String,
    val courseName: String,
    val semester: String?,
    val openAssignments: Int,
    val upcomingExams: Int,
    val avgMasteryPercent: Int?,
)

/**
 * A data class providing an aggregated overview of a specific academic semester.
 *
 * @param semester The name or identifier of the semester (e.g., "Fall 2026").
 * @param courseCount The total number of enrolled courses in this semester.
 * @param openAssignments The total sum of open assignments across all courses in this semester.
 * @param upcomingExams The total sum of upcoming exams across all courses in this semester.
 * @param dueSoon The total number of tasks, assignments, or exams due within the near future.
 */
data class StudentSemesterSummary(
    val semester: String,
    val courseCount: Int,
    val openAssignments: Int,
    val upcomingExams: Int,
    val dueSoon: Int,
)

/**
 * A data class tracking the completion progress of a specific student task or reading material.
 *
 * @param node The [NodeWithPin] wrapper representing the task, assignment, or reading.
 * @param progressPercent The estimated or calculated completion percentage (0-100).
 */
data class StudentProgressItem(
    val node: NodeWithPin,
    val progressPercent: Int,
)

/**
 * A data class indicating the self-reported or assessed mastery level of a specific topic.
 *
 * @param node The [NodeWithPin] wrapper representing the topic, concept, or study note.
 * @param topic The name or title of the subject topic.
 * @param masteryPercent The estimated level of mastery or comprehension of the topic (0-100).
 */
data class StudentMasteryItem(
    val node: NodeWithPin,
    val topic: String,
    val masteryPercent: Int,
)

/**
 * A comprehensive data class aggregating the state of the student dashboard, including templates, trackers, and study metrics.
 *
 * @param lectureTemplateReady Indicates if the standard lecture note template is configured and ready for use.
 * @param readingTemplateReady Indicates if the standard reading summary template is configured and ready for use.
 * @param paperSummaryTemplateReady Indicates if the standard academic paper summary template is configured and ready for use.
 * @param assignmentTracker A list of tasks classified as active student assignments.
 * @param examPrepBoard A list of tasks or nodes specifically tagged or scheduled for exam preparation.
 * @param psychologyConceptMaps A list of nodes representing specific psychological concepts or models.
 * @param glossaryCards A list of nodes structured as glossary terms or definitions.
 * @param researchIdeaVault A list of nodes capturing ideas or hypotheses for future research papers.
 * @param quoteBank A list of nodes containing saved quotes from academic readings or lectures.
 * @param caseReflectionNotes A list of nodes detailing personal reflections on specific academic cases.
 * @param readingBacklog Populated from all active reading notes (not just queued ones).
 * @param revisitBeforeExam A list of nodes explicitly flagged to be reviewed prior to an upcoming exam.
 * @param readingProgress A tracked list of [StudentProgressItem] objects reflecting ongoing reading materials.
 * @param assignmentDeadlines A list of nodes sorted or filtered by strict upcoming academic deadlines.
 * @param topicMastery A list of [StudentMasteryItem] objects indicating mastery levels for specific study topics.
 * @param courseDashboard A list of [StudentCourseSummary] objects summarizing all active courses.
 * @param semesterDashboard A list of [StudentSemesterSummary] objects summarizing broader semester statistics.
 * @param examCountdownNode Chosen as the first exam-like node found (not necessarily the next chronological exam).
 * @param examCountdownDays The calculated number of days remaining until the [examCountdownNode]'s due date.
 * @param topicToNoteLinks The total number of mapped relationships between distinct topics and their supporting notes.
 * @param paperToNoteLinks The total number of mapped relationships between academic papers and summary notes.
 * @param conceptGraphNodes The total number of nodes participating in the student knowledge graph.
 * @param conceptGraphEdges The total number of interconnected edges (relations) within the student knowledge graph.
 * @param flashcardCandidates A list of nodes formatted or suitable to be exported as spaced repetition flashcards.
 * @param studySessionsThisWeek Count of sessions attached to active student nodes (any session linked to an active student node).
 * @param studyMinutesThisWeek Total minutes from sessions attached to active student nodes (any session linked to an active student node).
 */
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