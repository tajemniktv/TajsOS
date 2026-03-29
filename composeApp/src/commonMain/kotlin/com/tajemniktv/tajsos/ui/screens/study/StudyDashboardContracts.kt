/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.study

import androidx.compose.runtime.Composable
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.TemplateEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.main.state.StudentBoardState

/**
 * Surface variants for study dashboard layouts.
 */
enum class StudyDashboardSurface {
    MOBILE,
    DESKTOP,
}

/**
 * Block instance in a study dashboard layout.
 */
data class StudyDashboardBlock(
    val id: String,
)

/**
 * Render plan for study dashboard layout.
 */
data class StudyDashboardPlan(
    val primary: List<StudyDashboardBlock>,
    val secondary: List<StudyDashboardBlock> = emptyList(),
)

/**
 * Shared context passed to study block renderers.
 */
data class StudyDashboardContext(
    val viewModel: MainViewModel,
    val state: StudentBoardState,
    val allTemplates: List<TemplateEntity>,
    val allNodes: List<NodeWithPin>,
    val courseId: String,
    val courseName: String,
    val semester: String,
    val onCourseIdChange: (String) -> Unit,
    val onCourseNameChange: (String) -> Unit,
    val onSemesterChange: (String) -> Unit,
    val onEditNode: (Long) -> Unit,
    val onOpenTopicLink: () -> Unit,
    val onOpenPaperLink: () -> Unit,
)

/**
 * Function signature for one study dashboard block renderer.
 */
typealias StudyDashboardBlockRenderer = @Composable (StudyDashboardContext) -> Unit
