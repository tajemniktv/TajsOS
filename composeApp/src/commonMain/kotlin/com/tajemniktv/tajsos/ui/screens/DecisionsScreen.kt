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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.DashCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionsScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
)
{
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(Res.string.decision_tab_inbox),
        stringResource(Res.string.decision_tab_pending),
        stringResource(Res.string.decision_tab_log),
    )

    val inbox by viewModel.decisionInbox.collectAsState()
    val pending by viewModel.allPendingDecisions.collectAsState()
    val log by viewModel.decisionLog.collectAsState()

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

        when (selectedTab)
        {
            0 -> DecisionList(inbox, onEditNode)
            1 -> DecisionList(pending, onEditNode)
            2 -> DecisionList(log, onEditNode)
        }
    }
}

@Composable
fun DecisionList(nodes: List<NodeWithPin>, onEdit: (Long) -> Unit)
{
    if (nodes.isEmpty())
    {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.decision_no_decisions_category),
                color = TactileTheme.Muted,
            )
        }
    } else
    {
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
                        if (node.node.content.isNotEmpty())
                        {
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

@Preview
@Composable
fun DecisionsScreenPreview()
{
    TajsOSTheme {
        // DecisionsScreen(...)
    }
}
