/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tajemniktv.tajsos.ui.MainViewModel
import com.tajemniktv.tajsos.ui.PersonalRulesSnapshot
import com.tajemniktv.tajsos.ui.PlaybookSnapshot
import com.tajemniktv.tajsos.ui.VaultsSnapshot
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlin.time.Clock

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun VaultsLayer(
    viewModel: MainViewModel,
    snapshot: VaultsSnapshot,
    onEditNode: (Long) -> Unit,
) {
    var entryTitle by remember { mutableStateOf("") }
    var entryContent by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("vault_document") }
    var entryType by remember { mutableStateOf("document") }

    val categories =
        listOf(
            "vault_document" to "DOCUMENT VAULT",
            "vault_links" to "IMPORTANT LINKS",
            "vault_medical" to "MEDICAL INFO",
            "vault_university" to "UNIVERSITY INFO",
            "vault_ids_forms" to "IDs & FORMS",
            "vault_application_status" to "APPLICATION STATUS",
            "vault_receipts_paperwork" to "RECEIPTS / PAPERWORK",
            "vault_account_reference" to "ACCOUNT / REFERENCE",
            "vault_official_deadline" to "OFFICIAL DEADLINE",
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
                "LIFE LOGISTICS & VAULTS",
                style = MaterialTheme.typography.labelSmall,
                color = TactileTheme.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Documents ${snapshot.documentVault.size} • Links ${snapshot.importantLinksVault.size} • Must-find-later ${snapshot.mustFindLater.size}",
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
                value = entryTitle,
                onValueChange = { entryTitle = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Entry title") },
            )
            OutlinedTextField(
                value = entryContent,
                onValueChange = { entryContent = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Details / reference") },
                minLines = 2,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                categories.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = { selectedCategory = key },
                        label = { Text(label) },
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                listOf("document", "resource", "note", "vault").forEach { type ->
                    FilterChip(
                        selected = entryType == type,
                        onClick = { entryType = type },
                        label = { Text(type.uppercase()) },
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
            ) {
                AssistChip(
                    onClick = {
                        viewModel.addVaultEntry(
                            categoryTag = selectedCategory,
                            title = entryTitle,
                            content = entryContent,
                            asType = entryType,
                        )
                        entryTitle = ""
                        entryContent = ""
                    },
                    label = { Text("SAVE ENTRY") },
                )
                AssistChip(
                    onClick = {
                        viewModel.createApplicationStatusEntry(
                            title = if (entryTitle.isBlank()) "Application status" else entryTitle,
                            status = if (entryContent.isBlank()) "pending" else entryContent,
                            dueAt =
                                Clock.System
                                    .now()
                                    .toEpochMilliseconds() + (14L * 24 * 60 * 60 * 1000),
                        )
                        entryTitle = ""
                        entryContent = ""
                    },
                    label = { Text("APP STATUS +14D") },
                )
            }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm)) {
        if (snapshot.documentVault.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "DOCUMENT VAULT",
                    snapshot.documentVault.map { it.node.title },
                )
            }
        }
        if (snapshot.importantLinksVault.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "IMPORTANT LINKS VAULT",
                    snapshot.importantLinksVault.map { it.node.title },
                )
            }
        }
        if (snapshot.medicalInfoVault.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "MEDICAL INFO VAULT",
                    snapshot.medicalInfoVault.map { it.node.title },
                )
            }
        }
        if (snapshot.universityInfoVault.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "UNIVERSITY INFO VAULT",
                    snapshot.universityInfoVault.map { it.node.title },
                )
            }
        }
        if (snapshot.idsAndFormsVault.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "IDs / FORMS VAULT",
                    snapshot.idsAndFormsVault.map { it.node.title },
                )
            }
        }
        if (snapshot.applicationStatusTracking.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "APPLICATION STATUS TRACKING",
                    snapshot.applicationStatusTracking.map { it.node.title },
                )
            }
        }
        if (snapshot.receiptsPaperwork.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "RECEIPTS / PAPERWORK",
                    snapshot.receiptsPaperwork.map { it.node.title },
                )
            }
        }
        if (snapshot.accountReferenceVault.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "ACCOUNT / REFERENCE VAULT",
                    snapshot.accountReferenceVault.map { it.node.title },
                )
            }
        }
        if (snapshot.officialDeadlineReminders.isNotEmpty()) {
            item {
                GroupedOpenLoopSection(
                    "OFFICIAL DEADLINE REMINDERS",
                    snapshot.officialDeadlineReminders.map { it.node.title },
                )
            }
        }

        items(snapshot.mustFindLater, key = { it.node.id }) { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = TactileTheme.Surface,
                shape = RoundedCornerShape(TactileTheme.RadiusMd),
                border = BorderStroke(1.dp, TactileTheme.Primary),
            ) {
                Column(
                    modifier = Modifier.padding(TactileTheme.SpacingMd),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        item.node.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TactileTheme.Text,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Type ${item.node.type.uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TactileTheme.Muted,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                        verticalArrangement = Arrangement.spacedBy(TactileTheme.SpacingSm),
                    ) {
                        AssistChip(
                            onClick = { viewModel.markMustFindLater(item.node, false) },
                            label = { Text("UNMARK MUST-FIND-LATER") },
                        )
                        AssistChip(onClick = { onEditNode(item.node.id) }, label = { Text("OPEN") })
                    }
                }
            }
        }
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
