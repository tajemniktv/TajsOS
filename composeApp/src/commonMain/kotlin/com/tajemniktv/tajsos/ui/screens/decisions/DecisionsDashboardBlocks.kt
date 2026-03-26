/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.decisions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.cards.DashCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.decision_no_decisions_category
import tajsos.composeapp.generated.resources.decision_tab_inbox
import tajsos.composeapp.generated.resources.decision_tab_log
import tajsos.composeapp.generated.resources.decision_tab_pending

object DecisionsDashboardBlockRegistry {
    private val renderers: Map<String, DecisionsDashboardBlockRenderer> =
        mapOf("decisions_main" to ::renderDecisionsMainBlock)

    fun resolve(id: String): DecisionsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderDecisionsMainBlock(context: DecisionsDashboardContext) {
    DecisionsMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DecisionsMainBlock(
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
