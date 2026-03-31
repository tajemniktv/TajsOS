/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.rules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.screens.GroupedOpenLoopSection
import com.tajemniktv.tajsos.ui.theme.TajsOSTheme

object RulesDashboardBlockRegistry {
    private val renderers: Map<String, RulesDashboardBlockRenderer> =
        mapOf(
            "rules_header" to ::renderRulesHeader,
            "rules_stats" to ::renderRulesStats,
            "rules_input" to ::renderRulesInput,
            "rules_grouped_sections" to ::renderRulesGroupedSections,
            "rules_list" to ::renderRulesList,
        )

    fun resolve(id: String): RulesDashboardBlockRenderer? = renderers[id]
}

@Composable
private fun renderRulesHeader(context: RulesDashboardContext) {
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingMd)) {
        Text(
            text = "RULES",
            style = MaterialTheme.typography.displaySmall,
            color = TajsOSTheme.Text
        )
        Text(
            text = "Keep anti-goals, principles, constraints, and recovery reminders in one retrievable system.",
            style = MaterialTheme.typography.bodySmall,
            color = TajsOSTheme.Muted
        )
    }
}

@Composable
private fun renderRulesStats(context: RulesDashboardContext) {
    val snapshot = context.snapshot
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.Border)
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "PERSONAL RULES VAULT",
                style = MaterialTheme.typography.labelSmall,
                color = TajsOSTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Rules ${snapshot.vault.size} • Pinned ${snapshot.pinnedPrinciples.size} • Playbook links ${snapshot.playbookLinksCount}",
                style = MaterialTheme.typography.bodySmall,
                color = TajsOSTheme.Muted
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun renderRulesInput(context: RulesDashboardContext) {
    context.viewModel
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
        color = TajsOSTheme.Surface,
        shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
        border = BorderStroke(1.dp, TajsOSTheme.Border)
    ) {
        Column(
            modifier = Modifier.padding(TajsOSTheme.SpacingMd),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = context.ruleTitle,
                onValueChange = context.onRuleTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rule title") },
            )
            OutlinedTextField(
                value = context.ruleContent,
                onValueChange = context.onRuleContentChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rule details") },
                minLines = 2,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)
            ) {
                ruleCategories.forEach { (tag, label) ->
                    FilterChip(
                        selected = context.selectedRuleTag == tag,
                        onClick = { context.onRuleTagChange(tag) },
                        label = { Text(label) },
                    )
                }
            }
            AssistChip(
                onClick = context.onSaveRule,
                label = { Text("SAVE TO RULES VAULT") },
            )
        }
    }
}

@Composable
private fun renderRulesGroupedSections(context: RulesDashboardContext) {
    val snapshot = context.snapshot
    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        if (snapshot.antiGoals.isNotEmpty()) {
            GroupedOpenLoopSection(
                "ANTI-GOALS",
                snapshot.antiGoals.map { it.node.title },
            )
        }
        if (snapshot.redFlags.isNotEmpty()) {
            GroupedOpenLoopSection(
                "RED FLAGS",
                snapshot.redFlags.map { it.node.title },
            )
        }
        if (snapshot.greenFlags.isNotEmpty()) {
            GroupedOpenLoopSection(
                "GREEN FLAGS",
                snapshot.greenFlags.map { it.node.title },
            )
        }
        if (snapshot.priorities.isNotEmpty()) {
            GroupedOpenLoopSection(
                "PRIORITIES",
                snapshot.priorities.map { it.node.title },
            )
        }
        if (snapshot.tendToForget.isNotEmpty()) {
            GroupedOpenLoopSection(
                "WHAT I TEND TO FORGET",
                snapshot.tendToForget.map { it.node.title },
            )
        }
        if (snapshot.messesMeUp.isNotEmpty()) {
            GroupedOpenLoopSection(
                "WHAT RELIABLY MESSES ME UP",
                snapshot.messesMeUp.map { it.node.title },
            )
        }
        if (snapshot.helpsOffBalance.isNotEmpty()) {
            GroupedOpenLoopSection(
                "WHAT HELPS WHEN OFF-BALANCE",
                snapshot.helpsOffBalance.map { it.node.title },
            )
        }
        if (snapshot.decisionPrinciples.isNotEmpty()) {
            GroupedOpenLoopSection(
                "DECISION PRINCIPLES",
                snapshot.decisionPrinciples.map { it.node.title },
            )
        }
        if (snapshot.constraints.isNotEmpty()) {
            GroupedOpenLoopSection(
                "PERSONAL CONSTRAINTS",
                snapshot.constraints.map { it.node.title },
            )
        }
        if (snapshot.foundationalRules.isNotEmpty()) {
            GroupedOpenLoopSection(
                "FOUNDATIONAL LIFE RULES",
                snapshot.foundationalRules.map { it.node.title },
            )
        }
        if (snapshot.recoveryReminders.isNotEmpty()) {
            GroupedOpenLoopSection(
                "RECOVERY REMINDERS",
                snapshot.recoveryReminders.map { it.node.title },
            )
        }
        if (snapshot.distrustBrainNotes.isNotEmpty()) {
            GroupedOpenLoopSection(
                "DO NOT TRUST YOUR BRAIN WHEN...",
                snapshot.distrustBrainNotes.map { it.node.title },
            )
        }
        if (snapshot.whatWorksNotes.isNotEmpty()) {
            GroupedOpenLoopSection(
                "WHAT WORKS FOR ME",
                snapshot.whatWorksNotes.map { it.node.title },
            )
        }
    }
}

@Composable
private fun renderRulesList(context: RulesDashboardContext) {
    val snapshot = context.snapshot
    val playbookSnapshot = context.playbookSnapshot
    val viewModel = context.viewModel

    Column(verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)) {
        snapshot.vault.forEach { rule ->
            val isPinned = snapshot.pinnedPrinciples.any { it.node.id == rule.node.id }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TajsOSTheme.Surface,
                shape = RoundedCornerShape(TajsOSTheme.RadiusMd),
                border =
                    BorderStroke(
                        1.dp,
                        if (isPinned) TajsOSTheme.Primary else TajsOSTheme.Border
                    )
            ) {
                Column(
                    modifier = Modifier.padding(TajsOSTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        rule.node.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TajsOSTheme.Text,
                        fontWeight = FontWeight.Bold,
                    )
                    if (rule.node.content.isNotBlank()) {
                        Text(
                            rule.node.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = TajsOSTheme.Muted
                        )
                    }
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)
                    ) {
                        AssistChip(
                            onClick = { viewModel.pinOperatingPrinciple(rule.node, !isPinned) },
                            label = { Text(if (isPinned) "UNPIN FROM HOME" else "PIN TO HOME") },
                        )
                        AssistChip(
                            onClick = { context.onEditNode(rule.node.id) },
                            label = { Text("OPEN") },
                        )
                    }
                    if (playbookSnapshot.playbooks.isNotEmpty()) {
                        Text(
                            "LINK TO PLAYBOOK",
                            style = MaterialTheme.typography.labelSmall,
                            color = TajsOSTheme.Muted
                        )
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm),
                            verticalArrangement = Arrangement.spacedBy(TajsOSTheme.SpacingSm)
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
