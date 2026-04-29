/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.protocols

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.ModeEntity
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.Screen
import com.tajemniktv.tajsos.ui.components.screen.ScreenScaffold
import com.tajemniktv.tajsos.ui.components.screen.ScreenScrollBehavior
import com.tajemniktv.tajsos.ui.main.state.PlaybookSnapshot
import com.tajemniktv.tajsos.ui.main.state.ProtocolHistoryItem
import com.tajemniktv.tajsos.ui.main.state.TransitionProtocolItem
import com.tajemniktv.tajsos.ui.main.state.TransitionProtocolsSnapshot
import com.tajemniktv.tajsos.ui.screens.formatProtocolTimestamp
import com.tajemniktv.tajsos.ui.screens.parseProtocolChecklist
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.common_edit
import tajsos.composeapp.generated.resources.common_open
import tajsos.composeapp.generated.resources.common_pause
import tajsos.composeapp.generated.resources.common_resume
import tajsos.composeapp.generated.resources.notes_notes
import tajsos.composeapp.generated.resources.protocol_complete_step
import tajsos.composeapp.generated.resources.protocol_current_step
import tajsos.composeapp.generated.resources.protocol_no_steps
import tajsos.composeapp.generated.resources.protocol_session_paused
import tajsos.composeapp.generated.resources.protocol_session_running
import tajsos.composeapp.generated.resources.protocol_steps_complete
import tajsos.composeapp.generated.resources.protocol_up_next
import tajsos.composeapp.generated.resources.protocols_action_end_session
import tajsos.composeapp.generated.resources.protocols_action_mark_not_done
import tajsos.composeapp.generated.resources.protocols_action_open
import tajsos.composeapp.generated.resources.protocols_action_previous
import tajsos.composeapp.generated.resources.protocols_action_run
import tajsos.composeapp.generated.resources.protocols_action_run_item
import tajsos.composeapp.generated.resources.protocols_action_skip
import tajsos.composeapp.generated.resources.protocols_active
import tajsos.composeapp.generated.resources.protocols_category_admin
import tajsos.composeapp.generated.resources.protocols_category_all
import tajsos.composeapp.generated.resources.protocols_category_emotional
import tajsos.composeapp.generated.resources.protocols_category_health
import tajsos.composeapp.generated.resources.protocols_category_home
import tajsos.composeapp.generated.resources.protocols_category_relationships
import tajsos.composeapp.generated.resources.protocols_category_study
import tajsos.composeapp.generated.resources.protocols_category_work
import tajsos.composeapp.generated.resources.protocols_desc_full
import tajsos.composeapp.generated.resources.protocols_label_adjustments
import tajsos.composeapp.generated.resources.protocols_label_estimated_time
import tajsos.composeapp.generated.resources.protocols_label_last_run
import tajsos.composeapp.generated.resources.protocols_label_last_run_item
import tajsos.composeapp.generated.resources.protocols_label_search
import tajsos.composeapp.generated.resources.protocols_label_suggested
import tajsos.composeapp.generated.resources.protocols_label_use_when
import tajsos.composeapp.generated.resources.protocols_library
import tajsos.composeapp.generated.resources.protocols_no_active
import tajsos.composeapp.generated.resources.protocols_no_matches
import tajsos.composeapp.generated.resources.protocols_no_runs
import tajsos.composeapp.generated.resources.protocols_not_yet
import tajsos.composeapp.generated.resources.protocols_pick_to_start
import tajsos.composeapp.generated.resources.protocols_placeholder_notes
import tajsos.composeapp.generated.resources.protocols_placeholder_search
import tajsos.composeapp.generated.resources.protocols_session_notes
import tajsos.composeapp.generated.resources.screen_protocols
import kotlin.math.max

/**
 * Central protocols entry point that collects system state and coordinates layout.
 *
 * @param viewModel Source of protocols state.
 * @param onEditNode Node edit callback.
 * @param onNavigate Navigation callback.
 */
@Composable
fun ProtocolsRoute(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
    onNavigate: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface = if (maxWidth > 900.dp) ProtocolsDashboardSurface.DESKTOP else ProtocolsDashboardSurface.MOBILE
        val plan = remember(surface) { buildProtocolsDashboardPlan(surface) }
        val context = remember(viewModel, onEditNode) { ProtocolsDashboardContext(viewModel, onEditNode) }

        ProtocolsScreen(
            context = context,
            plan = plan,
            onNavigate = onNavigate,
        )
    }
}

/**
 * Stateless protocols screen content.
 *
 * @param context Protocols dashboard context.
 * @param plan Protocols dashboard plan.
 * @param onNavigate Navigation callback.
 */
@Composable
fun ProtocolsScreen(
    context: ProtocolsDashboardContext,
    plan: ProtocolsDashboardPlan,
    onNavigate: (String) -> Unit,
) {
    ScreenScaffold(
        screen = Screen.Protocols,
        onNavigate = onNavigate,
        scrollBehavior = ScreenScrollBehavior.None,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 80.dp),
        ) {
            plan.primary.forEach { block ->
                ProtocolsDashboardBlocks.resolve(block.id)?.invoke(context)
            }
        }
    }
}

private enum class ProtocolView(
    val label: @Composable () -> String,
) {
    Library({ stringResource(Res.string.protocols_library) }),
    Active({ stringResource(Res.string.protocols_active) }),
}

private enum class ProtocolCategory(
    val label: @Composable () -> String,
) {
    All({ stringResource(Res.string.protocols_category_all) }),
    Health({ stringResource(Res.string.protocols_category_health) }),
    Work({ stringResource(Res.string.protocols_category_work) }),
    Study({ stringResource(Res.string.protocols_category_study) }),
    Home({ stringResource(Res.string.protocols_category_home) }),
    EmotionalRegulation({ stringResource(Res.string.protocols_category_emotional) }),
    Admin({ stringResource(Res.string.protocols_category_admin) }),
    Relationships({ stringResource(Res.string.protocols_category_relationships) }),
}

private data class ProtocolDescriptor(
    val category: ProtocolCategory,
    val purpose: String,
    val useWhen: String,
)

private data class ProtocolLibraryItem(
    val title: String,
    val category: ProtocolCategory,
    val purpose: String,
    val useWhen: String,
    val estimatedDurationMinutes: Int,
    val stepCount: Int,
    val triggerCount: Int,
    val lastRunAt: Long?,
    val nodeId: Long? = null,
)

/**
 * Primary protocol UI layer with two surfaces:
 * - Library: browse and choose reusable protocols.
 * - Active: run a selected protocol step-by-step with progress and step controls.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
@Suppress("UNUSED_PARAMETER")
internal fun ProtocolsLayer(
    viewModel: MainViewModel,
    snapshot: TransitionProtocolsSnapshot,
    playbookSnapshot: PlaybookSnapshot,
    allModes: List<ModeEntity>,
    allAreas: List<NodeEntity>,
    history: List<ProtocolHistoryItem>,
    onEditNode: (Long) -> Unit,
) {
    val protocolViewState = rememberSaveable { mutableStateOf(ProtocolView.Library) }
    val selectedCategoryState = rememberSaveable { mutableStateOf(ProtocolCategory.All) }
    val selectedProtocolTitleState = rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sessionNotes by rememberSaveable { mutableStateOf("") }
    var sessionPaused by rememberSaveable { mutableStateOf(false) }

    val libraryItems = remember(snapshot, history) { buildLibraryItems(snapshot, history) }
    val activeProtocol =
        selectedProtocolTitleState.value?.let { selected ->
            snapshot.protocols.firstOrNull {
                normalizeLabel(it.node.node.title) ==
                    normalizeLabel(
                        selected,
                    )
            }
        }

    if (activeProtocol == null && protocolViewState.value == ProtocolView.Active && snapshot.protocols.isNotEmpty()) {
        selectedProtocolTitleState.value =
            snapshot.protocols
                .firstOrNull()
                ?.node?.node?.title
    }

    val newestRun = history.maxByOrNull { it.executedAt }?.executedAt

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
    ) {
        ProtocolsHeader(
            protocolCount = libraryItems.size,
            activeCount = snapshot.protocols.size,
            lastRunAt = newestRun,
            recommendedLabel = snapshot.recommendedLabel,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.SurfaceLowest,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingSm),
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                ProtocolView.entries.forEach { view ->
                    FilterChip(
                        selected = protocolViewState.value == view,
                        onClick = { protocolViewState.value = view },
                        label = { Text(view.label()) },
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
        ) {
            when (protocolViewState.value)
            {
                ProtocolView.Library -> {
                    ProtocolLibrarySurface(
                        items = libraryItems,
                        selectedCategory = selectedCategoryState.value,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onCategorySelected = { selectedCategoryState.value = it },
                        onRun = { title ->
                            selectedProtocolTitleState.value = title
                            sessionPaused = false
                            viewModel.triggerProtocol(title, source = "protocol_library")
                            protocolViewState.value = ProtocolView.Active
                        },
                        onOpen = onEditNode,
                    )
                }

                ProtocolView.Active -> {
                    ProtocolRunSurface(
                        activeProtocol = activeProtocol,
                        sessionNotes = sessionNotes,
                        onSessionNotesChange = { sessionNotes = it },
                        sessionPaused = sessionPaused,
                        onTogglePause = { sessionPaused = !sessionPaused },
                        onCompleteStep = { node, index ->
                            viewModel.toggleProtocolChecklistStep(node, index, true)
                        },
                        onUncheckStep = { node, index ->
                            viewModel.toggleProtocolChecklistStep(node, index, false)
                        },
                        onSkipToLibrary = {
                            protocolViewState.value = ProtocolView.Library
                            selectedProtocolTitleState.value = null
                            sessionPaused = false
                            sessionNotes = ""
                        },
                        onSelectProtocol = { title ->
                            selectedProtocolTitleState.value = title
                            sessionPaused = false
                            viewModel.triggerProtocol(title, source = "protocol_active")
                        },
                        suggestedProtocols = libraryItems.take(4),
                        onOpen = onEditNode,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtocolsHeader(
    protocolCount: Int,
    activeCount: Int,
    lastRunAt: Long?,
    recommendedLabel: String?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        TajsOSTheme.SurfaceHighest.copy(alpha = 0.52f),
                                        TajsOSTheme.Surface.copy(alpha = 0.9f),
                                    ),
                            ),
                    ).padding(TajsOSTheme.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            Text(
                text = stringResource(Res.string.screen_protocols),
                style = MaterialTheme.typography.displaySmall,
                color = TajsOSTheme.Text,
            )
            Text(
                text = stringResource(Res.string.protocols_desc_full),
                style = MaterialTheme.typography.bodyMedium,
                color = TajsOSTheme.Muted,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                HeaderStatChip(
                    label = stringResource(Res.string.protocols_library),
                    value = "$protocolCount",
                )
                HeaderStatChip(
                    label = stringResource(Res.string.protocols_active),
                    value = "$activeCount",
                )
                HeaderStatChip(
                    label = stringResource(Res.string.protocols_label_last_run),
                    value =
                        lastRunAt?.let(::formatProtocolTimestamp)
                            ?: stringResource(Res.string.protocols_no_runs),
                )
                if (!recommendedLabel.isNullOrBlank()) {
                    HeaderStatChip(
                        label = stringResource(Res.string.protocols_label_suggested),
                        value = recommendedLabel,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderStatChip(
    label: String,
    value: String,
) {
    Surface(
        color = TajsOSTheme.SurfaceHighest.copy(alpha = 0.9f),
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Muted,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ProtocolLibrarySurface(
    items: List<ProtocolLibraryItem>,
    selectedCategory: ProtocolCategory,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (ProtocolCategory) -> Unit,
    onRun: (String) -> Unit,
    onOpen: (Long) -> Unit,
) {
    val filtered =
        remember(items, selectedCategory, searchQuery) {
            items.filter { item ->
                val categoryMatches =
                    selectedCategory == ProtocolCategory.All || item.category == selectedCategory
                val query = searchQuery.trim().lowercase()
                val queryMatches =
                    query.isBlank() ||
                        item.title.lowercase().contains(query) ||
                        item.purpose.lowercase().contains(query) ||
                        item.useWhen.lowercase().contains(query)
                categoryMatches && queryMatches
            }
        }

    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.protocols_label_search)) },
            placeholder = { Text(stringResource(Res.string.protocols_placeholder_search)) },
            singleLine = true,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            ProtocolCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.label()) },
                )
            }
        }

        if (filtered.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TajsOSTheme.CardSurface,
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            ) {
                Text(
                    text = stringResource(Res.string.protocols_no_matches),
                    modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
            ) {
                filtered.forEach { item ->
                    ProtocolLibraryCard(
                        item = item,
                        onRun = { onRun(item.title) },
                        onOpen = { item.nodeId?.let(onOpen) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtocolLibraryCard(
    item: ProtocolLibraryItem,
    onRun: () -> Unit,
    onOpen: () -> Unit,
) {
    Surface(
        modifier = Modifier.width(360.dp),
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            Text(
                text = item.category.label().uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = item.purpose,
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            Text(
                text = "${stringResource(Res.string.protocols_label_use_when)}: ${item.useWhen}",
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            Text(
                text =
                    stringResource(
                        Res.string.protocols_label_estimated_time,
                        item.estimatedDurationMinutes.toString(),
                        item.stepCount.toString(),
                        item.triggerCount.toString(),
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Text,
            )
            Text(
                text =
                    stringResource(
                        Res.string.protocols_label_last_run_item,
                        item.lastRunAt?.let(::formatProtocolTimestamp)
                            ?: stringResource(Res.string.protocols_not_yet),
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
                Button(onClick = onRun) {
                    Text(stringResource(Res.string.protocols_action_run))
                }
                AssistChip(
                    onClick = onOpen,
                    enabled = item.nodeId != null,
                    label = { Text(stringResource(Res.string.common_open)) },
                )
                AssistChip(
                    onClick = onOpen,
                    enabled = item.nodeId != null,
                    label = { Text(stringResource(Res.string.common_edit)) },
                )
            }
        }
    }
}

@Composable
private fun ProtocolRunSurface(
    activeProtocol: TransitionProtocolItem?,
    sessionNotes: String,
    onSessionNotesChange: (String) -> Unit,
    sessionPaused: Boolean,
    onTogglePause: () -> Unit,
    onCompleteStep: (NodeEntity, Int) -> Unit,
    onUncheckStep: (NodeEntity, Int) -> Unit,
    onSkipToLibrary: () -> Unit,
    onSelectProtocol: (String) -> Unit,
    suggestedProtocols: List<ProtocolLibraryItem>,
    onOpen: (Long) -> Unit,
) {
    if (activeProtocol == null) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = TajsOSTheme.CardSurface,
            shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                Text(
                    stringResource(Res.string.protocols_no_active),
                    style = MaterialTheme.typography.titleMedium,
                    color = TajsOSTheme.Text,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(Res.string.protocols_pick_to_start),
                    style = MaterialTheme.typography.bodySmall,
                    color = TajsOSTheme.Muted,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                    verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                ) {
                    suggestedProtocols.forEach { item ->
                        AssistChip(
                            onClick = { onSelectProtocol(item.title) },
                            label = {
                                Text(
                                    stringResource(
                                        Res.string.protocols_action_run_item,
                                        item.title,
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        }
        return
    }

    val checklist = parseProtocolChecklist(activeProtocol.node.node.content)
    val stepCount = checklist.size
    val doneCount = checklist.count { it.first }
    val progress = if (stepCount == 0) 0f else doneCount.toFloat() / stepCount.toFloat()
    val protocolDescriptor = describeProtocol(activeProtocol.node.node.title)
    val defaultStepIndex = checklist.indexOfFirst { !it.first }.takeIf { it >= 0 } ?: 0
    var currentStepIndex by remember(activeProtocol.node.node.id, checklist) {
        mutableStateOf(defaultStepIndex.coerceIn(0, max(stepCount - 1, 0)))
    }

    if (stepCount > 0 && currentStepIndex > stepCount - 1) {
        currentStepIndex = stepCount - 1
    }

    val currentStep = checklist.getOrNull(currentStepIndex)
    val upNext = findNextPendingStep(checklist, currentStepIndex)

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isDesktop = maxWidth >= 980.dp
        if (isDesktop) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
                ) {
                    ProtocolRunMainPanel(
                        protocolTitle = activeProtocol.node.node.title,
                        paused = sessionPaused,
                        doneCount = doneCount,
                        stepCount = stepCount,
                        progress = progress,
                        currentStepIndex = currentStepIndex,
                        currentStep = currentStep,
                        upNext = upNext,
                        onCompleteStep = {
                            if (currentStep != null) {
                                onCompleteStep(activeProtocol.node.node, currentStepIndex)
                                currentStepIndex = findNextPendingIndex(checklist, currentStepIndex)
                            }
                        },
                        onSkipStep = {
                            currentStepIndex = findNextPendingIndex(checklist, currentStepIndex)
                        },
                        onPreviousStep = {
                            currentStepIndex = (currentStepIndex - 1).coerceAtLeast(0)
                        },
                        onTogglePause = onTogglePause,
                        onEnd = onSkipToLibrary,
                    )
                }

                ProtocolRunSidebar(
                    modifier = Modifier.width(340.dp),
                    descriptor = protocolDescriptor,
                    activeProtocol = activeProtocol,
                    sessionNotes = sessionNotes,
                    onSessionNotesChange = onSessionNotesChange,
                    upNext = upNext,
                    checklist = checklist,
                    onUncheckStep = { index -> onUncheckStep(activeProtocol.node.node, index) },
                    onOpen = onOpen,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
                ProtocolRunMainPanel(
                    protocolTitle = activeProtocol.node.node.title,
                    paused = sessionPaused,
                    doneCount = doneCount,
                    stepCount = stepCount,
                    progress = progress,
                    currentStepIndex = currentStepIndex,
                    currentStep = currentStep,
                    upNext = upNext,
                    onCompleteStep = {
                        if (currentStep != null) {
                            onCompleteStep(activeProtocol.node.node, currentStepIndex)
                            currentStepIndex = findNextPendingIndex(checklist, currentStepIndex)
                        }
                    },
                    onSkipStep = {
                        currentStepIndex = findNextPendingIndex(checklist, currentStepIndex)
                    },
                    onPreviousStep = {
                        currentStepIndex = (currentStepIndex - 1).coerceAtLeast(0)
                    },
                    onTogglePause = onTogglePause,
                    onEnd = onSkipToLibrary,
                )
                ProtocolRunSidebar(
                    modifier = Modifier.fillMaxWidth(),
                    descriptor = protocolDescriptor,
                    activeProtocol = activeProtocol,
                    sessionNotes = sessionNotes,
                    onSessionNotesChange = onSessionNotesChange,
                    upNext = upNext,
                    checklist = checklist,
                    onUncheckStep = { index -> onUncheckStep(activeProtocol.node.node, index) },
                    onOpen = onOpen,
                )
            }
        }
    }
}

@Composable
private fun ProtocolRunMainPanel(
    protocolTitle: String,
    paused: Boolean,
    doneCount: Int,
    stepCount: Int,
    progress: Float,
    currentStepIndex: Int,
    currentStep: Pair<Boolean, String>?,
    upNext: String?,
    onCompleteStep: () -> Unit,
    onSkipStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onTogglePause: () -> Unit,
    onEnd: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingLg),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
        ) {
            Text(
                text = protocolTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text =
                    if (paused) {
                        stringResource(Res.string.protocol_session_paused)
                    } else {
                        stringResource(
                            Res.string.protocol_session_running,
                        )
                    },
                style = MaterialTheme.typography.bodySmall,
                color = if (paused) TajsOSTheme.AccentAmber else TajsOSTheme.Success,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text =
                    stringResource(
                        Res.string.protocol_steps_complete,
                        doneCount.toString(),
                        stepCount.toString(),
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = TajsOSTheme.Primary,
                trackColor = TajsOSTheme.SurfaceHighest,
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TajsOSTheme.SurfaceLowest,
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                ) {
                    Text(
                        text = stringResource(Res.string.protocol_current_step),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Primary,
                        fontWeight = FontWeight.Bold,
                    )
                    if (currentStep != null) {
                        Text(
                            text = "${currentStepIndex + 1}. ${currentStep.second}",
                            style = MaterialTheme.typography.titleLarge,
                            color = TajsOSTheme.Text,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (!upNext.isNullOrBlank()) {
                            Text(
                                text = stringResource(Res.string.protocol_up_next, upNext),
                                style = MaterialTheme.typography.bodySmall,
                                color = TajsOSTheme.Muted,
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(Res.string.protocol_no_steps),
                            style = MaterialTheme.typography.bodySmall,
                            color = TajsOSTheme.Muted,
                        )
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
            ) {
                Button(
                    onClick = onCompleteStep,
                    enabled = currentStep != null && !currentStep.first && !paused,
                ) {
                    Text(stringResource(Res.string.protocol_complete_step))
                }
                AssistChip(
                    onClick = onPreviousStep,
                    enabled = currentStepIndex > 0 && !paused,
                    label = { Text(stringResource(Res.string.protocols_action_previous)) },
                )
                AssistChip(
                    onClick = onSkipStep,
                    enabled = currentStep != null && !currentStep.first && !paused,
                    label = { Text(stringResource(Res.string.protocols_action_skip)) },
                )
                AssistChip(
                    onClick = onTogglePause,
                    label = {
                        Text(
                            if (paused) {
                                stringResource(Res.string.common_resume)
                            } else {
                                stringResource(
                                    Res.string.common_pause,
                                )
                            },
                        )
                    },
                )
                AssistChip(
                    onClick = onEnd,
                    label = { Text(stringResource(Res.string.protocols_action_end_session)) },
                )
            }
        }
    }
}

@Composable
private fun ProtocolRunSidebar(
    modifier: Modifier = Modifier,
    descriptor: ProtocolDescriptor,
    activeProtocol: TransitionProtocolItem,
    sessionNotes: String,
    onSessionNotesChange: (String) -> Unit,
    upNext: String?,
    checklist: List<Pair<Boolean, String>>,
    onUncheckStep: (Int) -> Unit,
    onOpen: (Long) -> Unit,
) {
    Surface(
        modifier = modifier,
        color = TajsOSTheme.CardSurface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusLg),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd),
        ) {
            Text(
                text = stringResource(Res.string.protocols_session_notes),
                style = MaterialTheme.typography.titleSmall,
                color = TajsOSTheme.Text,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = sessionNotes,
                onValueChange = onSessionNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.notes_notes)) },
                placeholder = { Text(stringResource(Res.string.protocols_placeholder_notes)) },
                minLines = 4,
            )
            Text(
                text = "${stringResource(Res.string.protocols_label_use_when)}: ${descriptor.useWhen}",
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            if (!upNext.isNullOrBlank()) {
                Surface(
                    color = TajsOSTheme.SurfaceHighest.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                ) {
                    Text(
                        text = stringResource(Res.string.protocol_up_next, upNext),
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TajsOSTheme.Text,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Text(
                text =
                    stringResource(
                        Res.string.protocols_label_last_run_item,
                        activeProtocol.lastTriggeredAt?.let(::formatProtocolTimestamp)
                            ?: stringResource(Res.string.protocols_not_yet),
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted,
            )
            if (checklist.any { it.first }) {
                Text(
                    text = stringResource(Res.string.protocols_label_adjustments),
                    style = MaterialTheme.typography.labelSmall,
                    color = TajsOSTheme.Primary,
                    fontWeight = FontWeight.Bold,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                    verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                ) {
                    checklist.forEachIndexed { index, step ->
                        if (step.first) {
                            AssistChip(
                                onClick = { onUncheckStep(index) },
                                label = {
                                    Text(
                                        stringResource(
                                            Res.string.protocols_action_mark_not_done,
                                            (index + 1).toString(),
                                        ),
                                    )
                                },
                            )
                        }
                    }
                }
            }
            AssistChip(
                onClick = { onOpen(activeProtocol.node.node.id) },
                label = { Text(stringResource(Res.string.protocols_action_open)) },
            )
        }
    }
}

private fun buildLibraryItems(
    snapshot: TransitionProtocolsSnapshot,
    history: List<ProtocolHistoryItem>,
): List<ProtocolLibraryItem> {
    val historyByLabel =
        history.groupBy { normalizeLabel(it.protocolLabel) }.mapValues { (_, value) ->
            value.maxOfOrNull { it.executedAt }
        }
    val existingByLabel = snapshot.protocols.associateBy { normalizeLabel(it.node.node.title) }

    val items = mutableListOf<ProtocolLibraryItem>()

    snapshot.protocols.forEach { protocol ->
        val checklist = parseProtocolChecklist(protocol.node.node.content)
        val descriptor = describeProtocol(protocol.node.node.title)
        val estimatedDuration = max(5, checklist.size * 3)
        items.add(
            ProtocolLibraryItem(
                title = protocol.node.node.title,
                category = descriptor.category,
                purpose = descriptor.purpose,
                useWhen = descriptor.useWhen,
                estimatedDurationMinutes = estimatedDuration,
                stepCount = checklist.size,
                triggerCount = protocol.triggerCount,
                lastRunAt =
                    protocol.lastTriggeredAt
                        ?: historyByLabel[normalizeLabel(protocol.node.node.title)],
                nodeId = protocol.node.node.id,
            ),
        )
    }

    snapshot.templates.forEach { template ->
        if (existingByLabel.containsKey(normalizeLabel(template.label))) return@forEach
        val descriptor = describeProtocol(template.label)
        val estimatedDuration = max(5, template.checklist.size * 3)
        items.add(
            ProtocolLibraryItem(
                title = template.label,
                category = descriptor.category,
                purpose = descriptor.purpose,
                useWhen = descriptor.useWhen,
                estimatedDurationMinutes = estimatedDuration,
                stepCount = template.checklist.size,
                triggerCount = 0,
                lastRunAt = historyByLabel[normalizeLabel(template.label)],
            ),
        )
    }

    return items.sortedWith(
        compareByDescending<ProtocolLibraryItem> { it.lastRunAt ?: 0L }
            .thenBy { it.title.lowercase() },
    )
}

private fun describeProtocol(title: String): ProtocolDescriptor {
    val normalized = normalizeLabel(title)
    return when
        {
            normalized.contains("morning") || normalized.contains("sleep") || normalized.contains("migraine") -> {
                ProtocolDescriptor(
                    category = ProtocolCategory.Health,
                    purpose = "Support physical stability and energy before the day or during recovery.",
                    useWhen = "Sleep transitions, low energy, or symptom management.",
                )
            }

            normalized.contains("panic") || normalized.contains("anxiety") || normalized.contains("grounding") -> {
                ProtocolDescriptor(
                    category = ProtocolCategory.EmotionalRegulation,
                    purpose = "Guide a calm, structured response during overwhelm or acute stress.",
                    useWhen = "Rising panic, anxiety spikes, or emotional overload.",
                )
            }

            normalized.contains("study") || normalized.contains("exam") || normalized.contains("learning") -> {
                ProtocolDescriptor(
                    category = ProtocolCategory.Study,
                    purpose = "Set up focused learning sessions with clear startup and follow-through steps.",
                    useWhen = "Before classes, revision blocks, or exam prep sessions.",
                )
            }

            normalized.contains("home") || normalized.contains("leaving") -> {
                ProtocolDescriptor(
                    category = ProtocolCategory.Home,
                    purpose = "Reduce friction and mistakes in repeated home transitions.",
                    useWhen = "Leaving home, arriving home, or house reset moments.",
                )
            }

            normalized.contains("shutdown") || normalized.contains("email") || normalized.contains("admin") -> {
                ProtocolDescriptor(
                    category = ProtocolCategory.Admin,
                    purpose = "Close loops and handle maintenance tasks in a consistent way.",
                    useWhen = "End-of-day shutdown or backlog maintenance windows.",
                )
            }

            normalized.contains("relationship") || normalized.contains("check-in") -> {
                ProtocolDescriptor(
                    category = ProtocolCategory.Relationships,
                    purpose = "Preserve relationship consistency with intentional check-ins.",
                    useWhen = "Planned relationship maintenance or difficult follow-ups.",
                )
            }

            else -> {
                ProtocolDescriptor(
                    category = ProtocolCategory.Work,
                    purpose = "Provide a repeatable execution flow to reduce decision friction.",
                    useWhen = "Any recurring situation where step order improves outcomes.",
                )
            }
        }
}

private fun normalizeLabel(value: String): String = value.trim().lowercase()

private fun findNextPendingStep(
    checklist: List<Pair<Boolean, String>>,
    currentStepIndex: Int,
): String? {
    if (checklist.isEmpty()) return null
    val afterCurrent =
        checklist
            .withIndex()
            .firstOrNull { it.index > currentStepIndex && !it.value.first }
            ?.value
            ?.second
    if (afterCurrent != null) return afterCurrent
    return checklist
        .withIndex()
        .firstOrNull { !it.value.first }
        ?.value
        ?.second
}

private fun findNextPendingIndex(
    checklist: List<Pair<Boolean, String>>,
    currentStepIndex: Int,
): Int {
    if (checklist.isEmpty()) return 0
    val nextIndex = checklist.indexOfFirstFrom(currentStepIndex + 1) { !it.first }
    if (nextIndex >= 0) return nextIndex
    val wrapped = checklist.indexOfFirst { !it.first }
    return if (wrapped >= 0) wrapped else (checklist.lastIndex).coerceAtLeast(0)
}

private inline fun <T> List<T>.indexOfFirstFrom(
    fromIndex: Int,
    predicate: (T) -> Boolean,
): Int {
    if (isEmpty()) return -1
    val safeFrom = fromIndex.coerceAtLeast(0)
    for (index in safeFrom until size) {
        if (predicate(this[index])) return index
    }
    return -1
}

