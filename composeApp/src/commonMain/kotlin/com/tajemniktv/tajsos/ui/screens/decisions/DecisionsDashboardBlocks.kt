/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.decisions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import com.tajemniktv.tajsos.ui.lens.LensUiContract
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.Res
import tajsos.composeapp.generated.resources.decision_no_decisions_category
import tajsos.composeapp.generated.resources.decision_tab_inbox
import tajsos.composeapp.generated.resources.decision_tab_log
import tajsos.composeapp.generated.resources.decision_tab_pending
import tajsos.composeapp.generated.resources.lens_decision_stale_header
import tajsos.composeapp.generated.resources.lens_decision_stale_item_age

object DecisionsDashboardBlocks {
    private val renderers: Map<String, DecisionsDashboardBlockRenderer> =
        mapOf("decisions_main" to ::renderDecisionsMainBlock)

    fun resolve(id: String): DecisionsDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderDecisionsMainBlock(context: DecisionsDashboardContext) {
    DecisionsMainBlock(viewModel = context.viewModel, onEditNode = context.onEditNode)
}

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
                .background(TajsOSTheme.Background),
    ) {
        Text(
            stringResource(LensUiContract.decisionLens.title),
            modifier = Modifier.padding(horizontal = TajsOSTheme.SpacingMd, vertical = 8.dp),
            style = MaterialTheme.typography.displaySmall,
            color = TajsOSTheme.Text,
            fontWeight = FontWeight.Bold,
        )
        Text(
            stringResource(LensUiContract.decisionLens.subtitle),
            modifier = Modifier.padding(horizontal = TajsOSTheme.SpacingMd),
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted,
        )
        Spacer(Modifier.height(8.dp))
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = TajsOSTheme.CardSurface,
            contentColor = TajsOSTheme.Primary,
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
                modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
                color = TajsOSTheme.Error.copy(alpha = 0.08f),
                border =
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        TajsOSTheme.Error.copy(alpha = 0.25f),
                    ),
                shape =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(TajsOSTheme.RadiusMd),
            ) {
                Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
                    Text(
                        stringResource(Res.string.lens_decision_stale_header),
                        style = MaterialTheme.typography.labelSmall,
                        color = TajsOSTheme.Error,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    stale.take(3).forEach { item ->
                        Text(
                            "• ${
                                stringResource(
                                    Res.string.lens_decision_stale_item_age,
                                    item.node.node.title,
                                    item.ageDays,
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = TajsOSTheme.Text,
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
                color = TajsOSTheme.Muted,
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
        ) {
            nodes.forEach { node ->
                DashCard(onClick = { onEdit(node.node.id) }) {
                    Column(modifier = Modifier.padding(TajsOSTheme.SpacingMd)) {
                        Text(
                            node.node.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TajsOSTheme.Text,
                        )
                        if (node.node.content.isNotEmpty()) {
                            Text(
                                node.node.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = TajsOSTheme.Muted,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
    }
}

