/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.DashCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

/**
 * Displays a tabbed UI for viewing decisions in Inbox, Pending, and Log categories.
 *
 * Observes the view model's decision flows and shows the corresponding list for the selected tab.
 *
 * @param viewModel Provides `decisionInbox`, `allPendingDecisions`, and `decisionLog` state flows that drive the displayed lists.
 * @param onEditNode Callback invoked with a node's id when the user selects a decision to edit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionsScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs =
        listOf(
            stringResource(Res.string.decision_tab_inbox),
            stringResource(Res.string.decision_tab_pending),
            stringResource(Res.string.decision_tab_log),
        )

    val inbox by viewModel.decisionInbox.collectAsState()
    val pending by viewModel.allPendingDecisions.collectAsState()
    val log by viewModel.decisionLog.collectAsState()
    val stale by viewModel.stalePendingDecisions.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(TactileTheme.Background),
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = TactileTheme.Surface,
            contentColor = TactileTheme.Primary,
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                )
            }
        }

        if (selectedTab == 1 && stale.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(TactileTheme.SpacingMd),
                color = TactileTheme.Error.copy(alpha = 0.08f),
                border =
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        TactileTheme.Error.copy(alpha = 0.25f),
                    ),
                shape =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(TactileTheme.RadiusMd),
            ) {
                Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                    Text(
                        "UNRESOLVED DECISIONS SITTING TOO LONG",
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Error,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    stale.take(3).forEach { item ->
                        Text(
                            "• ${item.node.node.title} (${item.ageDays}d)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Text,
                        )
                    }
                }
            }
        }

        when (selectedTab)
        {
            0 -> DecisionList(inbox, onEditNode)
            1 -> DecisionList(pending, onEditNode)
            2 -> DecisionList(log, onEditNode)
        }
    }
}

/**
 * Displays a list of decision nodes or a centered empty-state message when the list is empty.
 *
 * Shows each node as a tappable card with the node's title and up to two lines of content.
 *
 * @param nodes The list of decision nodes to display.
 * @param onEdit Callback invoked when a node card is tapped; receives the tapped node's id.
 */
@Composable
fun DecisionList(
    nodes: List<NodeWithPin>,
    onEdit: (Long) -> Unit,
) {
    if (nodes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.decision_no_decisions_category),
                color = TactileTheme.Muted,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
        ) {
            items(nodes) { node ->
                DashCard(onClick = { onEdit(node.node.id) }) {
                    Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
                        Text(
                            node.node.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TactileTheme.Text,
                        )
                        if (node.node.content.isNotEmpty()) {
                            Text(
                                node.node.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = TactileTheme.Muted,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview wrapper that renders the DecisionsScreen content inside the app theme for Compose previews.
 *
 * Use in Android Studio's preview tooling to visualize the DecisionsScreen UI while developing.
 */
@Preview
@Composable
private fun DecisionsScreenPreview() {
    TajsOSTheme {
        // DecisionsScreen(...)
    }
}
