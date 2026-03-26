/*
 * Copyright (c) Grzegorz Kaczmarski (TajemnikTV) 2026. All rights reserved.
 */

package com.tajemniktv.tajsos.ui.screens.vaults

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import com.tajemniktv.tajsos.ui.VaultsSnapshot
import com.tajemniktv.tajsos.ui.screens.GroupedOpenLoopSection
import com.tajemniktv.tajsos.ui.theme.TactileTheme
import kotlin.time.Clock

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun VaultsScreen(
    viewModel: MainViewModel,
    onEditNode: (Long) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val surface =
            if (maxWidth > 900.dp) VaultsDashboardSurface.DESKTOP else VaultsDashboardSurface.MOBILE
        val plan = remember(surface) { buildVaultsDashboardPlan(surface) }
        val context =
            remember(viewModel, onEditNode) { VaultsDashboardContext(viewModel, onEditNode) }
        Column(modifier = Modifier.fillMaxSize()) {
            plan.primary.forEach { block ->
                VaultsDashboardBlockRegistry.resolve(block.id)?.invoke(context)
            }
        }
    }
}

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
