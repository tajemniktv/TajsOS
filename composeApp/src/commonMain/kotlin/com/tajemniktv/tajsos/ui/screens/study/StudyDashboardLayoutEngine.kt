/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.study

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class StudyLayoutJsonV1(
    val version: Int = 1,
    val primary: List<String> = emptyList(),
    val secondary: List<String> = emptyList(),
)

private val studyLayoutJson =
    Json {
        ignoreUnknownKeys = true
    }

private val mobileDefaults =
    listOf(
        "study_header",
        "study_summary",
        "study_quick_actions",
        "assignment_tracker",
        "exam_prep",
        "assignment_deadlines",
        "revisit_before_exam",
        "reading_progress",
        "topic_mastery",
        "knowledge_vaults",
        "flashcard_candidates",
        "links_graph",
        "course_dashboard",
        "semester_dashboard",
    )

private val desktopPrimaryDefaults =
    listOf(
        "study_header",
        "study_summary",
        "study_quick_actions",
        "assignment_tracker",
        "exam_prep",
        "assignment_deadlines",
        "revisit_before_exam",
        "links_graph",
    )

private val desktopSecondaryDefaults =
    listOf(
        "reading_progress",
        "topic_mastery",
        "knowledge_vaults",
        "flashcard_candidates",
        "course_dashboard",
        "semester_dashboard",
    )

/**
 * Builds the study dashboard plan.
 *
 * `layoutOverrideJson` is optional and supports a future user-editable layout format.
 */
fun buildStudyDashboardPlan(
    surface: StudyDashboardSurface,
    layoutOverrideJson: String? = null,
): StudyDashboardPlan {
    val parsed = parseStructured(layoutOverrideJson)
    if (parsed != null) {
        return StudyDashboardPlan(
            primary = parsed.primary.distinct().map { StudyDashboardBlock(it) },
            secondary = parsed.secondary.distinct().map { StudyDashboardBlock(it) },
        )
    }

    return when (surface) {
        StudyDashboardSurface.MOBILE -> {
            StudyDashboardPlan(primary = mobileDefaults.map { StudyDashboardBlock(it) })
        }

        StudyDashboardSurface.DESKTOP -> {
            StudyDashboardPlan(
                primary = desktopPrimaryDefaults.map { StudyDashboardBlock(it) },
                secondary = desktopSecondaryDefaults.map { StudyDashboardBlock(it) },
            )
        }
    }
}

private inline fun <T> safeDecode(block: () -> T): T? =
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

private fun parseStructured(raw: String?): StudyLayoutJsonV1? {
    if (raw.isNullOrBlank()) return null
    return safeDecode { studyLayoutJson.decodeFromString<StudyLayoutJsonV1>(raw) }
}
