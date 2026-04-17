/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Topic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.data.isNoteItem
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.common.SelectorDialog
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

/**
 * Renders the study workspace as a block-based dashboard.
 *
 * The screen uses a shared layout engine and block registry so desktop/mobile structure and future
 * user customization can evolve without rewriting screen-level logic.
 */

/**
 * Central study entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of study state.
 * @param onEditNode Node edit callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun StudyRoute(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    val studentState by viewModel.studentBoardState.collectAsState()
    val allTemplates by viewModel.allTemplates.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()

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
            allNodes.filter {
                it.node.isNoteItem() && (it.node.noteType == "concept" || it.node.noteType == "lecture")
            }
        }
    val noteCandidates =
        remember(allNodes) {
            allNodes.filter { it.node.isNoteItem() }
        }
    val paperCandidates =
        remember(allNodes) {
            allNodes.filter {
                it.node.isNoteItem() && (it.node.noteType == "reading" || it.node.noteType == "research")
            }
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
        optionIcon = { Icons.Default.Link },
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

    val context =
        remember(
            viewModel,
            studentState,
            allTemplates,
            allNodes,
            courseId,
            courseName,
            semester,
            onEditNode,
            showTopicSourceDialog,
            showPaperSourceDialog,
        ) {
            StudyDashboardContext(
                viewModel = viewModel,
                state = studentState,
                allTemplates = allTemplates,
                allNodes = allNodes,
                courseId = courseId,
                courseName = courseName,
                semester = semester,
                onCourseIdChange = { courseId = it },
                onCourseNameChange = { courseName = it },
                onSemesterChange = { semester = it },
                onEditNode = onEditNode,
                onOpenTopicLink = { showTopicSourceDialog = true },
                onOpenPaperLink = { showPaperSourceDialog = true },
            )
        }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val surface =
            if (maxWidth > 980.dp) StudyDashboardSurface.DESKTOP else StudyDashboardSurface.MOBILE
        val plan = remember(surface) { buildStudyDashboardPlan(surface = surface) }

        StudyScreen(
            context = context,
            plan = plan,
            surface = surface,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless study screen content.
 *
 * @param context Study dashboard context.
 * @param plan Study dashboard plan.
 * @param surface Current UI surface mode.
 * @param onNavigate Navigation callback.
 */
@Composable
fun StudyScreen(
    context: StudyDashboardContext,
    plan: StudyDashboardPlan,
    surface: StudyDashboardSurface,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Education,
        onNavigate = onNavigate,
        scrollBehavior = ScreenScrollBehavior.None,
    ) {
        if (surface == StudyDashboardSurface.MOBILE) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
            ) {
                items(plan.primary, key = { it.id }) { block ->
                    StudyDashboardBlockRegistry.resolve(block.id)?.invoke(context)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1.35f),
                    verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                ) {
                    items(plan.primary, key = { it.id }) { block ->
                        StudyDashboardBlockRegistry.resolve(block.id)?.invoke(context)
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                ) {
                    items(plan.secondary, key = { it.id }) { block ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            StudyDashboardBlockRegistry.resolve(block.id)?.invoke(context)
                        }
                    }
                }
            }
        }
    }
}
