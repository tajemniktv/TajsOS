/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.main.state.PersonalRulesSnapshot
import com.tajemniktv.tajsos.ui.main.state.PlaybookSnapshot
import com.tajemniktv.tajsos.ui.theme.TactileTheme

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun RulesScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    val personalRulesSnapshot by viewModel.personalRulesSnapshot.collectAsState()
    val playbookSnapshot by viewModel.playbookSnapshot.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(TactileTheme.SpacingMd),
        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingMd),
    ) {
        Text(
            text = "RULES",
            style = MaterialTheme.typography.displaySmall,
            color = TactileTheme.Text,
        )
        Text(
            text = "Keep anti-goals, principles, constraints, and recovery reminders in one retrievable system.",
            style = MaterialTheme.typography.bodySmall,
            color = TactileTheme.Muted,
        )

        RulesLayer(
            viewModel = viewModel,
            snapshot = personalRulesSnapshot,
            playbookSnapshot = playbookSnapshot,
            onEditNode = onEditNode,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun RulesLayer(
    viewModel: MainViewModel,
    snapshot: PersonalRulesSnapshot,
    playbookSnapshot: PlaybookSnapshot,
    onEditNode: (Long) -> Unit,
) {
    var ruleTitle by remember { mutableStateOf("") }
    var ruleContent by remember { mutableStateOf("") }
    var selectedRuleTag by remember { mutableStateOf("rule_foundational") }

    val ruleCategories =
        listOf(
            "rule_anti_goal" to "ANTI-GOAL",
            "rule_red_flag" to "RED FLAG",
            "rule_green_flag" to "GREEN FLAG",
            "rule_priority" to "PRIORITY",
            "rule_tend_to_forget" to "TEND TO FORGET",
            "rule_messes_me_up" to "MESSES ME UP",
            "rule_helps_off_balance" to "HELPS OFF-BALANCE",
            "rule_decision_principle" to "DECISION PRINCIPLE",
            "rule_constraint" to "CONSTRAINT",
            "rule_foundational" to "FOUNDATIONAL RULE",
            "rule_recovery_reminder" to "RECOVERY REMINDER",
            "rule_distrust_brain" to "DO NOT TRUST BRAIN WHEN",
            "rule_what_works" to "WHAT WORKS FOR ME",
        )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "PERSONAL RULES VAULT",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Rules ${snapshot.vault.size} • Pinned ${snapshot.pinnedPrinciples.size} • Playbook links ${snapshot.playbookLinksCount}",
                style = MaterialTheme.typography.bodySmall,
                color = TactileTheme.Muted,
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TactileTheme.Surface,
        shape = RoundedCornerShape(TactileTheme.RadiusMd),
        border = BorderStroke(1.dp, TactileTheme.Border),
    ) {
        Column(
            modifier = Modifier.padding(TactileTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = ruleTitle,
                onValueChange = { ruleTitle = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rule title") },
            )
            OutlinedTextField(
                value = ruleContent,
                onValueChange = { ruleContent = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rule details") },
                minLines = 2,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                ruleCategories.forEach { (tag, label) ->
                    FilterChip(
                        selected = selectedRuleTag == tag,
                        onClick = { selectedRuleTag = tag },
                        label = { Text(label) },
                    )
                }
            }
            AssistChip(
                onClick = {
                    viewModel.addPersonalRule(
                        title = ruleTitle,
                        content = ruleContent,
                        categoryTag = selectedRuleTag,
                    )
                    ruleTitle = ""
                    ruleContent = ""
                },
                label = { Text("SAVE TO RULES VAULT") },
            )
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (snapshot.antiGoals.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "ANTI-GOALS",
                    snapshot.antiGoals.map { it.node.title },
                )
            }
        }
        if (snapshot.redFlags.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "RED FLAGS",
                    snapshot.redFlags.map { it.node.title },
                )
            }
        }
        if (snapshot.greenFlags.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "GREEN FLAGS",
                    snapshot.greenFlags.map { it.node.title },
                )
            }
        }
        if (snapshot.priorities.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "PRIORITIES",
                    snapshot.priorities.map { it.node.title },
                )
            }
        }
        if (snapshot.tendToForget.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "WHAT I TEND TO FORGET",
                    snapshot.tendToForget.map { it.node.title },
                )
            }
        }
        if (snapshot.messesMeUp.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "WHAT RELIABLY MESSES ME UP",
                    snapshot.messesMeUp.map { it.node.title },
                )
            }
        }
        if (snapshot.helpsOffBalance.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "WHAT HELPS WHEN OFF-BALANCE",
                    snapshot.helpsOffBalance.map { it.node.title },
                )
            }
        }
        if (snapshot.decisionPrinciples.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "DECISION PRINCIPLES",
                    snapshot.decisionPrinciples.map { it.node.title },
                )
            }
        }
        if (snapshot.constraints.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "PERSONAL CONSTRAINTS",
                    snapshot.constraints.map { it.node.title },
                )
            }
        }
        if (snapshot.foundationalRules.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "FOUNDATIONAL LIFE RULES",
                    snapshot.foundationalRules.map { it.node.title },
                )
            }
        }
        if (snapshot.recoveryReminders.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "RECOVERY REMINDERS",
                    snapshot.recoveryReminders.map { it.node.title },
                )
            }
        }
        if (snapshot.distrustBrainNotes.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "DO NOT TRUST YOUR BRAIN WHEN...",
                    snapshot.distrustBrainNotes.map { it.node.title },
                )
            }
        }
        if (snapshot.whatWorksNotes.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "WHAT WORKS FOR ME",
                    snapshot.whatWorksNotes.map { it.node.title },
                )
            }
        }

        items(snapshot.vault, key = { it.node.id }) { rule ->
            val isPinned = snapshot.pinnedPrinciples.any { it.node.id == rule.node.id }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border =
                    BorderStroke(
                        1.dp,
                        if (isPinned) TactileTheme.Primary else TactileTheme.Border,
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        rule.node.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TactileTheme.Text,
                        fontWeight = FontWeight.Bold,
                    )
                    if (rule.node.content.isNotBlank()) {
                        Text(
                            rule.node.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = TactileTheme.Muted,
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        AssistChip(
                            onClick = { viewModel.pinOperatingPrinciple(rule.node, !isPinned) },
                            label = { Text(if (isPinned) "UNPIN FROM HOME" else "PIN TO HOME") },
                        )
                        AssistChip(onClick = { onEditNode(rule.node.id) }, label = { Text("OPEN") })
                    }
                    if (playbookSnapshot.playbooks.isNotEmpty()) {
                        Text(
                            "LINK TO PLAYBOOK",
                            style = MaterialTheme.typography.labelSmall,
                            color = TactileTheme.Muted,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                            verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        ) {
                            playbookSnapshot.playbooks.take(8).forEach { playbook ->
                                AssistChip(
                                    onClick = {
                                        viewModel.linkPrincipleToPlaybook(
                                            rule.node.id,
                                            playbook.node.node.id,
                                        )
                                    },
                                    label = {
                                        Text(
                                            playbook.node.node.title
                                                .uppercase(),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
