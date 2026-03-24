/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.data.NodeEntity
import com.tajemniktv.tajsos.data.NodeWithPin
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.components.common.ModuleHeader
import com.tajemniktv.tajsos.ui.components.DashCard
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme
import org.jetbrains.compose.resources.stringResource
import tajsos.composeapp.generated.resources.*

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

    Scaffold(
        topBar = {
            ModuleHeader(
                title = stringResource(Res.string.dash_decisions).uppercase(),
                icon = Icons.Default.QuestionMark,
                color = TactileTheme.Primary,
            )
        },
        containerColor = TactileTheme.Background,
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = TactileTheme.Background,
                contentColor = TactileTheme.Primary,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(selectedTab, matchContentSize = true),
                        height = 2.dp,
                        color = TactileTheme.Primary,
                    )
                },
                divider = {
                    HorizontalDivider(
                        thickness = DividerDefaults.Thickness,
                        color = TactileTheme.Border,
                    )
                },
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }

            val currentNodes = when (selectedTab)
            {
                0 -> inbox
                1 -> pending
                else -> log
            }

            if (currentNodes.isEmpty())
            {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(Res.string.decision_no_decisions_category),
                        style = MaterialTheme.typography.labelSmall,
                        color = TactileTheme.Muted,
                    )
                }
            } else
            {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
                ) {
                    items(currentNodes) { nodeWithPin ->
                        DecisionItem(nodeWithPin, onEditNode)
                    }
                }
            }
        }
    }
}

@Composable
fun DecisionItem(
    nodeWithPin: NodeWithPin,
    onEditNode: (Long) -> Unit,
)
{
    val node = nodeWithPin.node
    DashCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onEditNode(node.id) },
    ) {
        Column(modifier = Modifier.padding(TactileTheme.SpacingMd)) {
            DecisionHeader(node)
            Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
            DecisionContent(node)
            DecisionOutcome(node)
        }
    }
}

@Composable
private fun DecisionHeader(node: NodeEntity)
{
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DecisionCategoryIcon(node.decisionCategory)
        DecisionStatusBadge(node.decisionStatus)
    }
}

@Composable
private fun DecisionCategoryIcon(category: String?)
{
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (category == "major") Icons.Default.PriorityHigh else Icons.Default.Circle,
            contentDescription = if (category == "major") stringResource(Res.string.cd_decision_major) else stringResource(
                Res.string.cd_decision_tiny,
            ),
            tint = if (category == "major") TactileTheme.Accent else TactileTheme.Muted,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(TactileTheme.SpacingSm))
        Text(
            category?.uppercase() ?: "TINY",
            style = MaterialTheme.typography.labelSmall,
            color = if (category == "major") TactileTheme.Accent else TactileTheme.Muted,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DecisionStatusBadge(status: String?)
{
    if (status != null)
    {
        Surface(
            color = getStatusColor(status).copy(alpha = 0.1f),
            shape = RoundedCornerShape(2.dp),
        ) {
            Text(
                status.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = getStatusColor(status),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DecisionContent(node: NodeEntity)
{
    Text(
        node.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TactileTheme.Text,
    )

    if (node.content.isNotEmpty())
    {
        Text(
            node.content,
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
            maxLines = 2,
        )
    }
}

@Composable
private fun DecisionOutcome(node: NodeEntity)
{
    if (node.decisionStatus == "decided" && node.decisionOutcome != null)
    {
        Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
        HorizontalDivider(
            Modifier,
            DividerDefaults.Thickness,
            color = TactileTheme.Border.copy(alpha = 0.3f),
        )
        Spacer(modifier = Modifier.height(TactileTheme.SpacingSm))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = stringResource(Res.string.cd_decision_outcome),
                tint = TactileTheme.Success,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(TactileTheme.SpacingSm))
            Text(
                stringResource(Res.string.decision_outcome_prefix, node.decisionOutcome ?: ""),
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Success,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

fun getStatusColor(status: String): Color =
        when (status)
        {
            "pending" -> Color(0xFFE9C46A)
            "decided" -> Color(0xFF2A9D8F)
            "expired" -> Color(0xFFE76F51)
            "parked"  -> Color(0xFF264653)
            else      -> Color.Gray
        }

@Preview
@Composable
fun DecisionItemPreview()
{
    TajsOSTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DecisionItem(
                nodeWithPin = NodeWithPin(
                    node = NodeEntity(
                        id = 1,
                        type = "decision",
                        title = "Major Pending Decision",
                        content = "This is a major decision that needs careful consideration.",
                        decisionStatus = "pending",
                        decisionCategory = "major",
                    ),
                    pin = null,
                ),
                onEditNode = {},
            )

            DecisionItem(
                nodeWithPin = NodeWithPin(
                    node = NodeEntity(
                        id = 2,
                        type = "decision",
                        title = "Tiny Decided Decision",
                        content = "Which pizza to order?",
                        decisionStatus = "decided",
                        decisionCategory = "tiny",
                        decisionOutcome = "Pepperoni",
                    ),
                    pin = null,
                ),
                onEditNode = {},
            )
        }
    }
}
