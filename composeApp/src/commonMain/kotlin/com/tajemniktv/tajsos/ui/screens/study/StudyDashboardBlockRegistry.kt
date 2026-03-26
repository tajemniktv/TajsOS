/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.study

/**
 * Registry for study dashboard block renderers.
 */
object StudyDashboardBlockRegistry {
    private val renderers: Map<String, StudyDashboardBlockRenderer> =
        mapOf(
            "study_header" to ::renderStudyHeaderBlock,
            "study_summary" to ::renderStudySummaryBlock,
            "study_quick_actions" to ::renderStudyQuickActionsBlock,
            "assignment_tracker" to ::renderAssignmentTrackerBlock,
            "exam_prep" to ::renderExamPrepBlock,
            "assignment_deadlines" to ::renderAssignmentDeadlinesBlock,
            "revisit_before_exam" to ::renderRevisitBeforeExamBlock,
            "reading_progress" to ::renderReadingProgressBlock,
            "topic_mastery" to ::renderTopicMasteryBlock,
            "knowledge_vaults" to ::renderKnowledgeVaultsBlock,
            "flashcard_candidates" to ::renderFlashcardCandidatesBlock,
            "links_graph" to ::renderLinksGraphBlock,
            "course_dashboard" to ::renderCourseDashboardBlock,
            "semester_dashboard" to ::renderSemesterDashboardBlock,
        )

    fun resolve(id: String): StudyDashboardBlockRenderer? = renderers[id]
}
